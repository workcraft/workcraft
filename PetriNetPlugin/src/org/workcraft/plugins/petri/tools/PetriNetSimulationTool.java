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
import javax.swing.JSplitPane;
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
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.ContainerDecoration;
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
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.util.ColorGenerator;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;

public class PetriNetSimulationTool extends AbstractTool implements ClipboardOwner {
	protected VisualModel visualNet;
	protected PetriNetModel net;
	protected JPanel interfacePanel;
	protected JPanel controlPanel;
	protected JPanel infoPanel;
	protected JSplitPane splitPane;
	protected JScrollPane tracePane;
	protected JScrollPane statePane;
	protected JTable traceTable;

	private JSlider speedSlider;
	private JButton randomButton, playButton, stopButton, backwardButton, forwardButton;
	private JButton copyStateButton, pasteStateButton, mergeTraceButton;

	// cache of "excited" containers (the ones containing the excited simulation elements)
	protected HashMap<Container, Boolean> excitedContainers = new HashMap<Container, Boolean>();

	final double DEFAULT_SIMULATION_DELAY = 0.3;
	final double EDGE_SPEED_MULTIPLIER = 10;

	protected Map<Place, Integer> initialMarking;
	protected final Trace mainTrace = new Trace();
	protected final Trace branchTrace = new Trace();

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
		traceControl.add(mergeTraceButton);

		controlPanel = new JPanel();
		controlPanel.setLayout(new WrapLayout());
		controlPanel.add(simulationControl);
		controlPanel.add(speedControl);
		controlPanel.add(traceControl);

		traceTable = new JTable(new TraceTableModel());
		traceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		traceTable.setDefaultRenderer(Object.class,	new TraceTableCellRendererImplementation());

		tracePane = new JScrollPane();
		tracePane.setViewportView(traceTable);
		tracePane.setMinimumSize(new Dimension(1, 50));

		statePane = new JScrollPane();
		statePane.setMinimumSize(new Dimension(1, 50));

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tracePane, statePane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);
		splitPane.setResizeWeight(0.5);

		infoPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout());
		infoPanel.add(splitPane, BorderLayout.CENTER);

		interfacePanel = new JPanel();
		interfacePanel.setLayout(new BorderLayout());
		interfacePanel.add(controlPanel, BorderLayout.NORTH);
		interfacePanel.add(infoPanel, BorderLayout.CENTER);
		interfacePanel.setPreferredSize(new Dimension(0, 0));

		speedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(timer != null) {
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
					timer = new Timer(getAnimationDelay(), new ActionListener()	{
						@Override
						public void actionPerformed(ActionEvent e) {
							randomStep(editor);
						}
					});
					timer.start();
					random = true;
				} else if (random) {
					timer.stop();
					timer = null;
					random = false;
				} else {
					random = true;
				}
				updateState(editor);
			}
		});

		playButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (timer == null) {
					timer = new Timer(getAnimationDelay(), new ActionListener()	{
						@Override
						public void actionPerformed(ActionEvent e) {
							step(editor);
						}
					});
					timer.start();
					random = false;
				} else if (!random) {
					timer.stop();
					timer = null;
					random = false;
				} else {
					random = false;
				}
				updateState(editor);
			}
		});

		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearTraces(editor);
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
				if (column == 0) {
					if (row < mainTrace.size()) {
						boolean work = true;
						while (work && (branchTrace.getPosition() > 0)) {
							work = quietStepBack();
						}
						while (work && (mainTrace.getPosition() > row)) {
							work = quietStepBack();
						}
						while (work && (mainTrace.getPosition() < row)) {
							work = quietStep();
						}
					}
				} else {
					if ((row >= mainTrace.getPosition()) && (row < mainTrace.getPosition() + branchTrace.size())) {
						boolean work = true;
						while (work && (mainTrace.getPosition() + branchTrace.getPosition() > row)) {
							work = quietStepBack();
						}
						while (work && (mainTrace.getPosition() + branchTrace.getPosition() < row)) {
							work = quietStep();
						}
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
	}

	public void setStatePaneVisibility(boolean visible) {
		statePane.setVisible(visible);
		splitPane.setDividerSize(visible ? 10 : 0);
	}

	@Override
	public void activated(final GraphEditor editor) {
		editor.getWorkspaceEntry().captureMemento();
		editor.getWorkspaceEntry().setCanModify(false);
		visualNet = getUnderlyingModel(editor.getModel());
		net = (PetriNetModel)visualNet.getMathModel();
		initialMarking = readMarking();
		super.activated(editor);
		setStatePaneVisibility(true);
		resetTraces(editor);
	}

	@Override
	public void deactivated(final GraphEditor editor) {
		super.deactivated(editor);
		if (timer != null) {
			timer.stop();
			timer = null;
		}
		editor.getWorkspaceEntry().cancelMemento();
		this.visualNet = null;
		this.net = null;
	}

	private void applyMarking(Map<Place, Integer> marking) {
		for (Place p: marking.keySet()) {
			if (net.getPlaces().contains(p)) {
				p.setTokens(marking.get(p));
			} else {
				ExceptionDialog.show(null, new RuntimeException("Place "+p.toString()+" is not in the model"));
			}
		}
	}

	public void updateState(final GraphEditor editor) {
		if (timer == null) {
			playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"));
			randomButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-random_play.svg"));
		} else {
			if (random) {
				playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"));
				randomButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-random_pause.svg"));
				timer.setDelay(getAnimationDelay());
			} else if (branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress())) {
				playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-pause.svg"));
				randomButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-random_play.svg"));
				timer.setDelay(getAnimationDelay());
			} else {
				playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"));
				randomButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-random_play.svg"));
				timer.stop();
				timer = null;
			}
		}
		playButton.setEnabled(branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress()));
		stopButton.setEnabled(!mainTrace.isEmpty() || !branchTrace.isEmpty());
		backwardButton.setEnabled((mainTrace.getPosition() > 0) || (branchTrace.getPosition() > 0));
		forwardButton.setEnabled(branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress()));
		traceTable.tableChanged(new TableModelEvent(traceTable.getModel()));
		editor.requestFocus();
		editor.repaint();
	}

	private boolean quietStepBack() {
		excitedContainers.clear();

		boolean result = false;
		String transitionId = null;
		int mainDec = 0;
		int branchDec = 0;
		if (branchTrace.getPosition() > 0) {
			transitionId = branchTrace.get(branchTrace.getPosition()-1);
			branchDec = 1;
		} else if (mainTrace.getPosition() > 0) {
			transitionId = mainTrace.get(mainTrace.getPosition() - 1);
			mainDec = 1;
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
			mainTrace.decPosition(mainDec);
			branchTrace.decPosition(branchDec);
			if ((branchTrace.getPosition() == 0) && !mainTrace.isEmpty()) {
				branchTrace.clear();
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
		excitedContainers.clear();

		boolean result = false;
		String transitionId = null;
		int mainInc = 0;
		int branchInc = 0;
		if (branchTrace.canProgress()) {
			transitionId = branchTrace.getCurrent();
			branchInc = 1;
		} else if (mainTrace.canProgress()) {
			transitionId = mainTrace.getCurrent();
			mainInc = 1;
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
			mainTrace.incPosition(mainInc);
			branchTrace.incPosition(branchInc);
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

	private void resetTraces(final GraphEditor editor) {
		applyMarking(initialMarking);
		mainTrace.setPosition(0);
		branchTrace.clear();
		updateState(editor);
	}

	private void clearTraces(final GraphEditor editor) {
		applyMarking(initialMarking);
		mainTrace.clear();
		branchTrace.clear();
		if (timer != null) 	{
			timer.stop();
			timer = null;
		}
		updateState(editor);
	}

	private void copyState(final GraphEditor editor) {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(
				mainTrace.toString() + "\n" + branchTrace.toString() + "\n");
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

		applyMarking(initialMarking);
		mainTrace.clear();
		branchTrace.clear();
		boolean first = true;
		for (String s: str.split("\n")) {
			if (first) {
				mainTrace.fromString(s);
				int mainTracePosition = mainTrace.getPosition();
				mainTrace.setPosition(0);
				boolean work = true;
				while (work && (mainTrace.getPosition() < mainTracePosition)) {
					work = quietStep();
				}
			} else {
				branchTrace.fromString(s);
				int branchTracePosition = branchTrace.getPosition();
				branchTrace.setPosition(0);
				boolean work = true;
				while (work && (branchTrace.getPosition() < branchTracePosition)) {
					work = quietStep();
				}
				break;
			}
			first = false;
		}
		updateState(editor);
	}

	private void mergeTrace(final GraphEditor editor) {
		if (!branchTrace.isEmpty()) {
			while (mainTrace.getPosition() < mainTrace.size()) {
				mainTrace.removeCurrent();
			}
			mainTrace.addAll(branchTrace);
			mainTrace.incPosition(branchTrace.getPosition());
			branchTrace.clear();
		}
		updateState(editor);
	}

	private int getAnimationDelay() {
		return (int)(1000.0 * DEFAULT_SIMULATION_DELAY * Math.pow(EDGE_SPEED_MULTIPLIER, -speedSlider.getValue() / 1000.0));
	}

	@SuppressWarnings("serial")
	private final class TraceTableCellRendererImplementation implements TableCellRenderer {
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
				if (!mainTrace.isEmpty() && branchTrace.isEmpty()) {
					return row == mainTrace.getPosition();
				}
			} else {
				if (!branchTrace.isEmpty() && (row >= mainTrace.getPosition()) && (row < mainTrace.getPosition() + branchTrace.size())) {
					return (row == mainTrace.getPosition() + branchTrace.getPosition());
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
			if (column == 0) return "Trace";
			return "Branch";
		}

		@Override
		public int getRowCount() {
			return Math.max(mainTrace.size(), mainTrace.getPosition() + branchTrace.size());
		}

		@Override
		public Object getValueAt(int row, int column) {
			String ref = null;
			if (column == 0) {
				if (!mainTrace.isEmpty() && (row < mainTrace.size())) {
					ref = mainTrace.get(row);
				}
			} else {
				if (!branchTrace.isEmpty() && (row >= mainTrace.getPosition()) && (row < mainTrace.getPosition() + branchTrace.size())) {
					ref = branchTrace.get(row - mainTrace.getPosition());
				}
			}
			return getTraceLabelByReference(ref);
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

		String transitionId = null;
		// if clicked on the trace event, do the step forward
		if (branchTrace.isEmpty() && !mainTrace.isEmpty() && (mainTrace.getPosition() < mainTrace.size())) {
			transitionId = mainTrace.get(mainTrace.getPosition());
		}
		// otherwise form/use the branch trace
		if (!branchTrace.isEmpty() && (branchTrace.getPosition() < branchTrace.size())) {
			transitionId = branchTrace.get(branchTrace.getPosition());
		}
		if (transitionId != null) {
			Node transition = net.getNodeByReference(transitionId);
			if ((transition != null) && (transition == t)) {
				step(editor);
				return;
			}
		}
		while (branchTrace.getPosition() < branchTrace.size()) {
			branchTrace.removeCurrent();
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
		GUI.drawEditorMessage(editor, g, Color.BLACK, "Click on the highlighted transitions to fire them.");
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

	public void setTrace(Trace mainTrace, Trace branchTrace, GraphEditor editor) {
		this.mainTrace.clear();
		if (mainTrace != null) {
			this.mainTrace.addAll(mainTrace);
		}
		this.branchTrace.clear();
		if (branchTrace != null) {
			this.branchTrace.addAll(branchTrace);
		}
		updateState(editor);
	}


	protected boolean isContainerExcited(Container container) {
		if (excitedContainers.containsKey(container)) return excitedContainers.get(container);
		boolean ret = false;
		for (Node node: container.getChildren()) {
			if (node instanceof VisualTransition) {
				ret = ret || net.isEnabled(((VisualTransition)node).getReferencedTransition());
			}

			if (node instanceof Container) {
				ret = ret || isContainerExcited((Container)node);
			}
			if (ret) break;
		}
		excitedContainers.put(container, ret);
		return ret;
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
					if (branchTrace.canProgress()) {
						transitionId = branchTrace.get(branchTrace.getPosition());
						transition2 = net.getNodeByReference(transitionId);
					} else if (branchTrace.isEmpty() && mainTrace.canProgress()) {
						transitionId = mainTrace.get(mainTrace.getPosition());
						transition2 = net.getNodeByReference(transitionId);
					}

					if (transition == transition2) {
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return CommonSimulationSettings.getEnabledBackgroundColor();
							}
							@Override
							public Color getBackground() {
								return CommonSimulationSettings.getEnabledForegroundColor();
							}
						};
					}

					if (net.isEnabled(transition)) {
						return new Decoration(){
							@Override
							public Color getColorisation() {
								return CommonSimulationSettings.getEnabledForegroundColor();
							}
							@Override
							public Color getBackground() {
								return CommonSimulationSettings.getEnabledBackgroundColor();
							}
						};
					}
				}

				if (node instanceof VisualPage || node instanceof VisualGroup) {

					final boolean ret = isContainerExcited((Container)node);

					return new ContainerDecoration() {

						@Override
						public Color getColorisation() {
							return null;
						}

						@Override
						public Color getBackground() {
							return null;
						}

						@Override
						public boolean isContainerExcited() {
							return ret;
						}
					};

				}

				return null;
			}

		};
	}

	@Override
	public void lostOwnership(Clipboard clip, Transferable arg) {
	}

	public String getTraceLabelByReference(String ref) {
		return ref;
	}

}