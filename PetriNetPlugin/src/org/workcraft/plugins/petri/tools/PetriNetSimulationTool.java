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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.ColorGenerator;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;

public class PetriNetSimulationTool extends AbstractTool implements ClipboardOwner {
	protected VisualModel visualNet;
	protected PetriNetModel net;
	protected JPanel interfacePanel;
	protected JPanel controlPanel;
	protected JScrollPane infoPanel;
	protected JPanel statusPanel;
	protected JTable traceTable;

	private JSlider speedSlider;
	private JButton randomButton, playButton, stopButton, backwardButton, forwardButton;
	private JButton copyStateButton, pasteStateButton, saveMarkingButton, loadMarkingButton, mergeTraceButton;

	final double DEFAULT_SIMULATION_DELAY = 0.3;
	final double EDGE_SPEED_MULTIPLIER = 10;

	protected Map<Place, Integer> initialMarking;
	Map<Place, Integer> savedMarking = null;
	int savedStep = 0;
	private Trace savedBranchTrace;
	private int savedBranchStep = 0;

	protected Trace branchTrace;
	protected int branchStep = 0;
	protected Trace trace;
	protected int traceStep = 0;

	private Timer timer = null;
	private boolean random = false;

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);

		playButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"), "Automatic trace playback");
		stopButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-stop.svg"), "Reset trace playback");
		backwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-backward.svg"), "Step backward");
		forwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-forward.svg"), "Step forward");
		randomButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-random_play.svg"), "Random playback");

		speedSlider = new JSlider(-1000, 1000, 0);
		speedSlider.setToolTipText("Simulation playback speed");

		copyStateButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-trace-copy.svg"), "Copy trace to clipboard");
		pasteStateButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-trace-paste.svg"), "Paste trace from clipboard");
		saveMarkingButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-marking-save.svg"), "Save marking to memory");
		loadMarkingButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-marking-load.svg"), "Load marking from memory");
		mergeTraceButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-trace-merge.svg"), "Merge branch into trace");

		int buttonWidth = (int)Math.round(playButton.getPreferredSize().getWidth() + 5);
		int buttonHeight = (int)Math.round(playButton.getPreferredSize().getHeight() + 5);
		Dimension panelSize = new Dimension(buttonWidth * 5, buttonHeight);

		JPanel simulationControl = new JPanel();
		simulationControl.setLayout(new FlowLayout());
		simulationControl.setPreferredSize(panelSize);
		simulationControl.setMaximumSize(panelSize);
		simulationControl.add(playButton);
		simulationControl.add(stopButton);
		simulationControl.add(backwardButton);
		simulationControl.add(forwardButton);
		simulationControl.add(randomButton);

		JPanel speedControl = new JPanel();
		speedControl.setLayout(new BorderLayout());
		speedControl.setPreferredSize(panelSize);
		speedControl.setMaximumSize(panelSize);
		speedControl.add(speedSlider, BorderLayout.CENTER);

		JPanel traceControl = new JPanel();
		traceControl.setLayout(new FlowLayout());
		traceControl.setPreferredSize(panelSize);
		traceControl.add(new JSeparator());
		traceControl.add(copyStateButton);
		traceControl.add(pasteStateButton);
		traceControl.add(saveMarkingButton);
		traceControl.add(loadMarkingButton);
		traceControl.add(mergeTraceButton);

		controlPanel = new JPanel();
		controlPanel.setLayout(new WrapLayout());
		controlPanel.add(simulationControl);
		controlPanel.add(speedControl);
		controlPanel.add(traceControl);

		traceTable = new JTable(new TraceTableModel());
		traceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		infoPanel = new JScrollPane(traceTable);
		infoPanel.setPreferredSize(new Dimension(1, 1));

		statusPanel = new JPanel();
		interfacePanel = new JPanel();
		interfacePanel.setLayout(new BorderLayout());
		interfacePanel.add(controlPanel, BorderLayout.PAGE_START);
		interfacePanel.add(infoPanel, BorderLayout.CENTER);
		interfacePanel.add(statusPanel, BorderLayout.PAGE_END);

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
				updateState(editor);
			}
		});

		randomButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (timer == null) {
					random = true;
					timer = new Timer(getAnimationDelay(), new ActionListener()	{
						@Override
						public void actionPerformed(ActionEvent e) {
							randomStep(editor);
						}
					});
					timer.start();
				} else {
					timer.stop();
					timer = null;
					random = false;
				}
				updateState(editor);
			}
		});

		playButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (timer == null) {
					random = false;
					timer = new Timer(getAnimationDelay(), new ActionListener()	{
						@Override
						public void actionPerformed(ActionEvent e) {
							step(editor);
						}
					});
					timer.start();
				} else {
					timer.stop();
					timer = null;
					random = false;
				}
				updateState(editor);
			}
		});

		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reset(editor);
			}
		});

		backwardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stepBack(editor);
			}
		});

		forwardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				step(editor);
			}
		});

		copyStateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyState(editor);
			}
		});

		pasteStateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pasteState(editor);
			}
		});

		saveMarkingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveMarking(editor);
			}
		});

		loadMarkingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadMarking(editor);
			}
		});

		mergeTraceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mergeTrace(editor);
			}
		});

		traceTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = traceTable.getSelectedColumn();
				int row = traceTable.getSelectedRow();
				if (column==0) {
					if ((trace != null) && (row < trace.size())) {
						boolean work=true;
						while (branchStep>0&&work) work=quietStepBack();
						while (traceStep>row&&work) work=quietStepBack();
						while (traceStep<row&&work) work=quietStep();
					}
				} else {
					if ((branchTrace != null) && (row >= traceStep) && (row < traceStep+branchTrace.size())) {
						boolean work=true;
						while (traceStep+branchStep>row&&work) work=quietStepBack();
						while (traceStep+branchStep<row&&work) work=quietStep();
					}
				}
				updateState(editor);
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
		traceTable.setDefaultRenderer(Object.class,	new TraceTableCellRendererImplementation());
	}

	@Override
	public void activated(final GraphEditor editor) {
		super.activated(editor);
		visualNet = getUnderlyingModel(editor.getModel());
		net = (PetriNetModel)visualNet.getMathModel();
		initialMarking = readMarking();
		traceStep = 0;
		branchTrace = null;
		branchStep = 0;
		if (visualNet == editor.getModel()) {
			editor.getWorkspaceEntry().captureMemento();
		}
		editor.getWorkspaceEntry().setCanModify(false);
		updateState(editor);
	}

	@Override
	public void deactivated(final GraphEditor editor) {
		super.deactivated(editor);
		if (timer != null) {
			timer.stop();
			timer = null;
		}
		if (visualNet == editor.getModel()) {
			editor.getWorkspaceEntry().cancelMemento();
		}
		this.visualNet = null;
		this.net = null;
	}

	private void applyMarking(Map<Place, Integer> marking) {
		for (Place p: marking.keySet()) {
			if (net.getPlaces().contains(p)) {
				p.setTokens(marking.get(p));
			} else {
				//ExceptionDialog.show(null, new RuntimeException("Place "+p.toString()+" is not in the model"));
			}
		}
	}

	public void updateState(final GraphEditor editor) {
		if (timer == null) {
			playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"));
			randomButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-random_play.svg"));
		} else {
			if (!random && (trace == null || traceStep == trace.size())) {
				playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"));
				timer.stop();
				timer = null;
			} else {
				playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-pause.svg"));
				randomButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-random_pause.svg"));
				timer.setDelay(getAnimationDelay());
			}
		}
		playButton.setEnabled(trace != null && traceStep < trace.size());
		stopButton.setEnabled(trace != null || branchTrace != null);
		backwardButton.setEnabled(traceStep > 0 || branchStep > 0);
		forwardButton.setEnabled(branchTrace==null && trace != null && traceStep < trace.size() || branchTrace != null && branchStep < branchTrace.size());
		saveMarkingButton.setEnabled(true);
		loadMarkingButton.setEnabled(savedMarking != null);
		traceTable.tableChanged(new TableModelEvent(traceTable.getModel()));
		editor.requestFocus();
		editor.repaint();
	}

	private boolean quietStepBack() {
		boolean result = false;
		String transitionId = null;
		int traceDec = 0;
		int branchDec = 0;
		if (branchTrace != null && branchStep > 0) {
			transitionId = branchTrace.get(branchStep-1);
			branchDec = 1;
		} else if (trace != null && traceStep > 0) {
			transitionId = trace.get(traceStep-1);
			traceDec = 1;
		}
		Transition transition = null;
		if (transitionId != null) {
			final Node node = net.getNodeByReference(transitionId);
			if (node != null && (node instanceof Transition)) {
				transition = (Transition)node;
			}
		}
		if (transition != null && net.isUnfireEnabled(transition)) {
			net.unFire(transition);
			traceStep -= traceDec;
			branchStep -= branchDec;
			if (branchStep == 0 && trace != null) {
				branchTrace=null;
			}
			result = true;
		}
		return result;
	}

	private boolean stepBack(final GraphEditor editor) {
		boolean ret = quietStepBack();
		updateState(editor);
		return ret;
	}

	private boolean quietStep() {
		boolean result = false;
		String transitionId = null;
		int traceInc = 0;
		int branchInc = 0;
		if (branchTrace != null && branchStep < branchTrace.size()) {
			transitionId = branchTrace.get(branchStep);
			branchInc = 1;
		} else if (trace != null && traceStep < trace.size()) {
			transitionId = trace.get(traceStep);
			traceInc = 1;
		}
		Transition transition = null;
		if (transitionId != null) {
			final Node node = net.getNodeByReference(transitionId);
			if (node != null && (node instanceof Transition)) {
				transition = (Transition)node;
			}
		}
		if (transition != null && net.isEnabled(transition)) {
			net.fire(transition);
			coloriseTokens(transition);
			traceStep += traceInc;
			branchStep += branchInc;
			result = true;
		}
		return result;
	}

	protected void coloriseTokens(Transition transition) {
		VisualTransition vt = ((VisualPetriNet)visualNet).getVisualTransition(transition);
		if (vt == null) return;
		Color tokenColor = Color.black;
		ColorGenerator tokenColorGenerator = vt.getTokenColorGenerator();
		if (tokenColorGenerator != null) {
			// generate token colour
			tokenColor = tokenColorGenerator.updateColor();
		} else {
			// combine preset token colours
			for (Connection c: visualNet.getConnections(vt)) {
				if ((c.getSecond() == vt) && (c instanceof VisualConnection)) {
					VisualConnection vc = (VisualConnection)c;
					if (vc.isTokenColorPropagator() && (vc.getFirst() instanceof VisualPlace)) {
						VisualPlace vp = (VisualPlace)vc.getFirst();
						tokenColor = Coloriser.colorise(tokenColor, vp.getTokenColor());
					}
				}
			}
		}
		// propagate the colour to postset tokens
		for (Connection c: visualNet.getConnections(vt)) {
			if ((c.getFirst() == vt) && (c instanceof VisualConnection)) {
				VisualConnection vc = (VisualConnection)c;
				if (vc.isTokenColorPropagator() && (vc.getSecond() instanceof VisualPlace)) {
					VisualPlace vp = (VisualPlace)vc.getFirst();
					vp.setTokenColor(tokenColor);
				}
			}
		}
	}

	private boolean step(final GraphEditor editor) {
		boolean ret = quietStep();
		updateState(editor);
		return ret;
	}

	private boolean randomStep(final GraphEditor editor) {
		ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();
		for (Transition transition: net.getTransitions()) {
			if (net.isEnabled(transition)) {
				enabledTransitions.add(transition);
			}
		}
		if (enabledTransitions.size() == 0) {
			return false;
		}
		int randomIndex = (int)(Math.random() * enabledTransitions.size());
		Transition transition = enabledTransitions.get(randomIndex);
		executeTransition(editor, transition);
		return true;
	}

	private void reset(final GraphEditor editor) {
		if (traceStep == 0 && branchTrace == null) {
			trace = null;
			traceStep = 0;
			branchStep=0;
		} else {
			applyMarking(initialMarking);
			traceStep = 0;
			branchStep=0;
			branchTrace=null;
		}
		if (timer != null) 	{
			timer.stop();
			timer = null;
		}
		updateState(editor);
	}

	private void copyState(final GraphEditor editor) {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		String st = ((trace!=null)?trace.toString():"")+"\n"+traceStep+"\n";
		String st2 = (branchTrace!=null)?branchTrace.toString()+"\n"+branchStep:"";
		StringSelection stringSelection = new StringSelection(st+st2);
		clip.setContents(stringSelection, this);
		updateState(editor);
	}

	private void pasteState(final GraphEditor editor) {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clip.getContents(null);
		boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		String str="";
		if (hasTransferableText) {
			try {
				str = (String)contents.getTransferData(DataFlavor.stringFlavor);
			}
			catch (UnsupportedFlavorException ex){
				System.out.println(ex);
				ex.printStackTrace();
			}
			catch (IOException ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}

		int i=0;
		trace = new Trace();
		branchTrace = null;
		traceStep = 0;
		branchStep = 0;
		for (String s: str.split("\n")) {
			if (i==0) {
				trace.fromString(s);
			} else if (i==1) {
				traceStep = Integer.valueOf(s);
			} else if (i==2) {
				branchTrace = new Trace();
				branchTrace.fromString(s);
			} else if (i==3) {
				branchStep = Integer.valueOf(s);
			}
			i++;
			if (i>3) break;
		}
		updateState(editor);
	}

	private void saveMarking(final GraphEditor editor) {
		savedMarking = readMarking();
		savedStep = traceStep;
		savedBranchStep = 0;
		savedBranchTrace = null;
		if (branchTrace!=null) {
			savedBranchTrace = (Trace)branchTrace.clone();
			savedBranchStep = branchStep;
		}
		updateState(editor);
	}

	private void loadMarking(final GraphEditor editor) {
		applyMarking(savedMarking);
		traceStep = savedStep;
		if (savedBranchTrace != null) {
			branchStep = savedBranchStep;
			branchTrace = (Trace)savedBranchTrace.clone();
		} else {
			branchStep = 0;
			branchTrace = null;
		}
		updateState(editor);
	}

	private void mergeTrace(final GraphEditor editor) {
		if (branchTrace != null) {
			if (trace == null) {
				trace = new Trace();
			}
			for (String s: branchTrace) {
				if (s.length() > 0) {
					trace.add(s);
				}
			}
			branchTrace = null;
			if (branchStep > 0) {
				traceStep = branchStep;
			}
			branchStep = 0;
		}
		updateState(editor);
	}

	private int getAnimationDelay() {
		return (int)(1000.0 * DEFAULT_SIMULATION_DELAY * Math.pow(EDGE_SPEED_MULTIPLIER, -speedSlider.getValue() / 1000.0));
	}

	@SuppressWarnings("serial")
	private final class TraceTableCellRendererImplementation implements
			TableCellRenderer {
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
				if ((trace != null) && (branchTrace==null)) {
					return row==traceStep;
				}
			} else {
				if ((branchTrace != null) && (row >= traceStep) && (row < traceStep+branchTrace.size())) {
					return (row-traceStep)==branchStep;
				}
			}
			return false;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus,	int row, int column) {
			if (!(value instanceof String)) return null;
			label.setText((String)value);
			if (isActive(row, column)) {
				label.setBackground(Color.YELLOW);
			} else {
				label.setBackground(Color.WHITE);
			}
			return label;
		}
	}

	@SuppressWarnings("serial")
	private class TraceTableModel extends AbstractTableModel {
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
	};

	protected Map<Place, Integer> readMarking() {
		HashMap<Place, Integer> result = new HashMap<Place, Integer>();
		for (Place p : net.getPlaces()) {
			result.put(p, p.getTokens());
		}
		return result;
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET) {
			stepBack(e.getEditor());
		}
		if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET) {
			step(e.getEditor());
		}
	}

	public void executeTransition(final GraphEditor editor, Transition t) {
		if (t == null) return;

		// if clicked on the trace event, do the step forward
		if ((branchTrace == null) && (trace != null) && (traceStep < trace.size())) {
			String transitionId = trace.get(traceStep);
			Node transition = net.getNodeByReference(transitionId);
			if ((transition != null) && (transition == t)) {
				step(editor);
				return;
			}
		}
		// otherwise form/use the branch trace
		if ((branchTrace != null) && (branchStep < branchTrace.size())) {
			String transitionId = branchTrace.get(branchStep);
			Node transition = net.getNodeByReference(transitionId);
			if ((transition != null) && (transition == t)) {
				step(editor);
				return;
			}
		}

		if (branchTrace==null) {
			branchTrace = new Trace();
		}
		while (branchStep < branchTrace.size()) {
			branchTrace.remove(branchStep);
		}
		branchTrace.add(net.getNodeReference(t));
		step(editor);
		return;
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(),
			new Func<Node, Boolean>() {
				@Override
				public Boolean eval(Node node) {
					return node instanceof VisualTransition
						&& net.isEnabled(((VisualTransition)node).getReferencedTransition());
				}
			});

		if (node instanceof VisualTransition) {
			executeTransition(e.getEditor(), ((VisualTransition)node).getReferencedTransition());
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
		return GUI.createIconFromSVG("images/icons/svg/tool-simulation.svg");
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
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				if(node instanceof VisualTransition) {
					Transition transition = ((VisualTransition)node).getReferencedTransition();
					String transitionId = null;
					Node transition2 = null;
					if ((branchTrace != null) && (branchStep < branchTrace.size())) {
						transitionId = branchTrace.get(branchStep);
						transition2 = net.getNodeByReference(transitionId);
					} else if ((branchTrace == null) && (trace != null) && (traceStep < trace.size())) {
						transitionId = trace.get(traceStep);
						transition2 = net.getNodeByReference(transitionId);
					}

					if (transition == transition2) {
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return CommonVisualSettings.getEnabledBackgroundColor();
							}
							@Override
							public Color getBackground() {
								return CommonVisualSettings.getEnabledForegroundColor();
							}
						};
					}

					if (net.isEnabled(transition)) {
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return CommonVisualSettings.getEnabledForegroundColor();
							}
							@Override
							public Color getBackground() {
								return CommonVisualSettings.getEnabledBackgroundColor();
							}
						};
					}
				}
				return null;
			}

		};
	}

	@Override
	public void lostOwnership(Clipboard clip, Transferable arg) {
	}

}