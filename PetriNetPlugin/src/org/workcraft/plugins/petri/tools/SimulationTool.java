/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.petri.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.workcraft.Trace;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetSettings;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;


public class SimulationTool extends AbstractTool {
	private VisualModel visualNet;

	protected PetriNetModel net;
	protected JPanel interfacePanel;

	private JButton resetButton, autoPlayButton, stopButton, backButton, stepButton, loadTraceButton, saveMarkingButton, loadMarkingButton;
	private JSlider speedSlider;

	protected JTable traceTable;

	final double DEFAULT_SIMULATION_DELAY = 0.3;
	final double EDGE_SPEED_MULTIPLIER = 10;

	Map<Place, Integer> initialMarking;
	Map<Place, Integer> savedMarking = null;
	int savedStep = 0;
	private Trace savedBranchTrace;
	private int savedBranchStep = 0;

	public SimulationTool() {
		super();
		createInterface();
	}

	protected Trace branchTrace;
	protected int branchStep = 0;
	protected Trace trace;
	protected int traceStep = 0;

	private Timer timer = null;

	private void applyMarking(Map<Place, Integer> marking)
	{
		for(Place p: marking.keySet()) {
			if (net.getPlaces().contains(p)) {
				p.setTokens(marking.get(p));
			} else {
				//ExceptionDialog.show(null, new RuntimeException("Place "+p.toString()+" is not in the model"));
			}
		}
	}

	private void update()
	{
		if(timer != null && (trace == null || traceStep == trace.size()))
		{
			timer.stop();
			timer = null;
		}

		if(timer != null)
			timer.setDelay(getAnimationDelay());

		resetButton.setEnabled(trace != null || branchTrace != null);
		autoPlayButton.setEnabled(trace != null && traceStep < trace.size());
		stopButton.setEnabled(timer!=null);

		backButton.setEnabled(traceStep > 0 || branchStep > 0);


		stepButton.setEnabled(branchTrace==null && trace != null && traceStep < trace.size() || branchTrace != null && branchStep < branchTrace.size());

		loadTraceButton.setEnabled(true);

		saveMarkingButton.setEnabled(true);
		loadMarkingButton.setEnabled(savedMarking != null);

		traceTable.tableChanged(new TableModelEvent(traceTable.getModel()));

		// debug
/*		if (trace!=null)
			System.out.println("Trace:" + traceToString(trace, traceStep));
		if (branchTrace!=null)
			System.out.println("Branch:" + traceToString(branchTrace, branchStep));*/
	}

	private boolean quietStepBack() {
		if (branchTrace!=null&&branchStep>0) {
			String transitionId = branchTrace.get(branchStep-1);

			final Node transition = net.getNodeByReference(transitionId);
			if (transition==null||!(transition instanceof Transition)) return false;
			if (!net.isUnfireEnabled((Transition)transition)) return false;
			branchStep--;

			net.unFire((Transition)transition);
			if (branchStep==0&&trace!=null) branchTrace=null;
			return true;
		}

		if (trace==null) return false;
		if (traceStep==0) return false;

		String transitionId = trace.get(traceStep-1);

		final Node transition = net.getNodeByReference(transitionId);
		if (transition==null||!(transition instanceof Transition)) return false;
		if (!net.isUnfireEnabled((Transition)transition)) return false;
		traceStep--;

		net.unFire((Transition)transition);
		return true;
	}

	private boolean stepBack() {
		boolean ret = quietStepBack();
		update();
		return ret;
	}

	private boolean quietStep() {
		if (branchTrace!=null&&branchStep<branchTrace.size()) {
			String transitionId = branchTrace.get(branchStep);
			final Node transition = net.getNodeByReference(transitionId);

			if (transition==null||!(transition instanceof Transition)) return false;
			if (!net.isEnabled((Transition)transition)) return false;

			net.fire((Transition)transition);
			branchStep++;

			return true;
		}

		if (trace==null) return false;
		if (traceStep>=trace.size()) return false;

		String transitionId = trace.get(traceStep);
		final Node transition = net.getNodeByReference(transitionId);
		if (transition==null||!(transition instanceof Transition)) return false;
		if (!net.isEnabled((Transition)transition)) return false;

		net.fire((Transition)transition);
		traceStep++;
		return true;
	}

	private boolean step() {
		boolean ret = quietStep();
		update();
		return ret;
	}

	private void reset() {
		applyMarking(initialMarking);
		traceStep = 0;
		branchStep=0;
		branchTrace=null;

		if(timer!=null)
		{
			timer.stop();
			timer = null;
		}
		update();
	}

	private int getAnimationDelay()
	{
		return (int)(1000.0 * DEFAULT_SIMULATION_DELAY * Math.pow(EDGE_SPEED_MULTIPLIER, -speedSlider.getValue() / 1000.0));
	}

	@SuppressWarnings("serial")
	private void createInterface() {
		interfacePanel = new JPanel(new SimpleFlowLayout(5,5));

		traceTable = new JTable(new AbstractTableModel() {

			@Override
			public int getColumnCount() {
				return 2;
			}

			@Override
			public String getColumnName(int column) {
				if (column==0) return "Trace";
				return "Branch";
			}

			@Override
			public int getRowCount() {
				int tnum = 0;
				int bnum = 0;
				if (trace!=null) tnum=trace.size();
				if (branchTrace!=null) bnum=branchTrace.size();

				return Math.max(tnum, bnum+traceStep);
			}



			@Override
			public Object getValueAt(int row, int col) {
				if (col==0) {
					if (trace!=null&&row<trace.size())
						return trace.get(row);
				} else {
					if (branchTrace!=null&&row>=traceStep&&row<traceStep+branchTrace.size()) {
						return branchTrace.get(row-traceStep);
					}
				}
				return "";
			}

		});


		traceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		traceTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int column = traceTable.getSelectedColumn();
				int row = traceTable.getSelectedRow();

				if (column==0) {
					if (trace!=null&&row<trace.size()) {

						boolean work=true;

						while (branchStep>0&&work) work=quietStepBack();
						while (traceStep>row&&work) work=quietStepBack();
						while (traceStep<row&&work) work=quietStep();

						update();
					}
				} else {
					if (branchTrace!=null&&row>=traceStep&&row<traceStep+branchTrace.size()) {

						boolean work=true;
						while (traceStep+branchStep>row&&work) work=quietStepBack();
						while (traceStep+branchStep<row&&work) work=quietStep();
						update();
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

		});


		traceTable.setDefaultRenderer(Object.class,
			new TableCellRenderer() {
				JLabel label = new JLabel() {
					@Override
					public void paint( Graphics g ) {
						g.setColor( getBackground() );
						g.fillRect( 0, 0, getWidth() - 1, getHeight() - 1 );
						super.paint( g );
					}
				};

				boolean isActive(int row, int column) {
					if (column==0) {
						if (trace!=null&&branchTrace==null)
							return row==traceStep;
					} else {
						if (branchTrace!=null&&row>=traceStep&&row<traceStep+branchTrace.size()) {
							return (row-traceStep)==branchStep;
						}
					}

					return false;
				}

				@Override
				public Component getTableCellRendererComponent(JTable table,
						Object value, boolean isSelected, boolean hasFocus,
						int row, int column) {

					if (!(value instanceof String)) return null;

					label.setText((String)value);


					if (isActive(row, column)) {
						label.setBackground(Color.YELLOW);
					} else {
						label.setBackground(Color.WHITE);
					}

					return label;
				}

		});


		resetButton = new JButton ("Reset");
		speedSlider = new JSlider(-1000, 1000, 0);
		autoPlayButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/start.svg"), "Automatic simulation");
		stopButton = new JButton ("Stop");
		backButton = new JButton ("Step <");
		stepButton = new JButton ("Step >");
		loadTraceButton = new JButton ("Load trace");
		saveMarkingButton = new JButton ("Save marking");
		loadMarkingButton = new JButton ("Load marking");

		speedSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if(timer != null)
				{
					timer.stop();
					timer.setInitialDelay(getAnimationDelay());
					timer.setDelay(getAnimationDelay());
					timer.start();
				}
				update();
			}
		});

		resetButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});

		autoPlayButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				timer = new Timer(getAnimationDelay(), new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e) {
						step();
					}
				});
				timer.start();
				update();
			}
		});

		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timer.stop();
				timer = null;
				update();
			}
		});

		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stepBack();
			}
		});

		stepButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				step();
			}
		});

		saveMarkingButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				savedMarking = readMarking();
				savedStep = traceStep;

				savedBranchStep = 0;
				savedBranchTrace = null;

				if (branchTrace!=null) {
					savedBranchTrace = (Trace)branchTrace.clone();
					savedBranchStep = branchStep;
				}

				update();
			}
		});

		loadMarkingButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				applyMarking(savedMarking);
				traceStep = savedStep;
				if (savedBranchTrace!=null) {
					branchStep = savedBranchStep;
					branchTrace = (Trace)savedBranchTrace.clone();
				} else {
					branchStep = 0;
					branchTrace = null;
				}
				update();
			}
		});

		interfacePanel.add(resetButton);
		interfacePanel.add(speedSlider);
		interfacePanel.add(autoPlayButton);
		interfacePanel.add(stopButton);
		interfacePanel.add(backButton);
		interfacePanel.add(stepButton);
		interfacePanel.add(loadTraceButton);
		interfacePanel.add(saveMarkingButton);
		interfacePanel.add(loadMarkingButton);
		interfacePanel.add(traceTable);
	}

	@Override
	public void deactivated(GraphEditor editor)
	{
		reset();
	}

	@Override
	public void activated(GraphEditor editor)
	{
		visualNet = editor.getModel();
		net = (PetriNetModel)visualNet.getMathModel();

		initialMarking = readMarking();
		branchTrace = null;
		branchStep = 0;

		update();
	}

	private Map<Place, Integer> readMarking() {
		HashMap<Place, Integer> result = new HashMap<Place, Integer>();
		for (Place p : net.getPlaces()) {
			result.put(p, p.getTokens());
		}
		return result;
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET) stepBack();
		if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET) step();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(), new Func<Node, Boolean>()
				{
					@Override
					public Boolean eval(Node node) {
						return
							   node instanceof VisualTransition
							&& net.isEnabled(((VisualTransition)node).getReferencedTransition());
					}
				});

		if (node instanceof VisualTransition) {
			VisualTransition vt = (VisualTransition)node;

			// if clicked on the trace event, do the step forward
			if (branchTrace==null&&trace!=null&&traceStep<trace.size()) {
				String transitionId = trace.get(traceStep);
				Node transition = net.getNodeByReference(transitionId);
				if (transition!=null&&transition==vt.getReferencedTransition()) {
					step();
					return;
				}
			}

			// otherwise form/use the branch trace
			if (branchTrace!=null&&branchStep<branchTrace.size()) {
				String transitionId = branchTrace.get(branchStep);
				Node transition = net.getNodeByReference(transitionId);
				if (transition!=null&&transition==vt.getReferencedTransition()) {
					step();
					return;
				}

			}

			if (branchTrace==null) branchTrace = new Trace();

			while (branchStep<branchTrace.size())
				branchTrace.remove(branchStep);

			branchTrace.add(net.getNodeReference(vt.getReferencedTransition()));
			step();
			update();
			return;
		}
	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		GUI.drawEditorMessage(editor, g, Color.BLACK, "Simulation: click on the highlighted transitions to fire them");
	}

	public String getLabel() {
		return "Simulation";
	}

	public int getHotKeyCode() {
		return KeyEvent.VK_M;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/start-green.svg");
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	public void setTrace(Trace t) {
		this.trace = t;
		this.traceStep = 0;
		this.branchTrace = null;
		this.branchStep = 0;

	}

	@Override
	public Decorator getDecorator() {
		return new Decorator() {

			@Override
			public Decoration getDecoration(Node node) {
				if(node instanceof VisualTransition) {
					Transition transition = ((VisualTransition)node).getReferencedTransition();

					if (branchTrace!=null&&branchStep<branchTrace.size()) {
						String transitionId = branchTrace.get(branchStep);
						Node transition2 = net.getNodeByReference(transitionId);

						if (transition==transition2) {
							return new Decoration(){

								@Override
								public Color getColorisation() {
									return PetriNetSettings.getEnabledBackgroundColor();
								}

								@Override
								public Color getBackground() {
									return PetriNetSettings.getEnabledForegroundColor();
								}
							};

						}
					}

					if (branchTrace==null&&trace!=null&&traceStep<trace.size()) {
						String transitionId = trace.get(traceStep);
						Node transition2 = net.getNodeByReference(transitionId);

						if (transition==transition2) {
							return new Decoration(){

								@Override
								public Color getColorisation() {
									return PetriNetSettings.getEnabledBackgroundColor();
								}

								@Override
								public Color getBackground() {
									return PetriNetSettings.getEnabledForegroundColor();
								}
							};
						}
					}

					if (net.isEnabled(transition))
						return new Decoration(){

							@Override
							public Color getColorisation() {
								return PetriNetSettings.getEnabledForegroundColor();
							}

							@Override
							public Color getBackground() {
								return PetriNetSettings.getEnabledBackgroundColor();
							}
						};
				}
				return null;
			}

		};
	}
}