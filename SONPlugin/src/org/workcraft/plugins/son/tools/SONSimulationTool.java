package org.workcraft.plugins.son.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ContainerDecoration;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.petri.tools.PetriNetSimulationTool;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.Step;
import org.workcraft.plugins.son.Trace;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.ErrorTracingAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.algorithm.SimulationAlg;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualTransitionNode;
import org.workcraft.plugins.son.exception.InvalidStructureException;
import org.workcraft.plugins.son.gui.ParallelSimDialog;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class SONSimulationTool extends PetriNetSimulationTool {

	private SON net;
	protected VisualSON visualNet;
	private GraphEditor editor;

	private RelationAlgorithm relationAlg;
	private BSONAlg bsonAlg;
	private SimulationAlg simuAlg;
	private ErrorTracingAlg	errAlg;

	private Collection<Path> sync = new ArrayList<Path>();
	private Map<Condition, Collection<Phase>> phases = new HashMap<Condition, Collection<Phase>>();
	protected Map<PlaceNode, Boolean>initialMarking = new HashMap<PlaceNode, Boolean>();

	protected JPanel interfacePanel;
	protected JPanel controlPanel;
	protected JScrollPane infoPanel;
	protected JPanel statusPanel;
	protected JTable traceTable;

	private JSlider speedSlider;
	private JButton playButton, stopButton, backwardButton, forwardButton, reverseButton, autoSimuButton;
	private JButton copyStateButton, pasteStateButton, mergeTraceButton;

	protected HashMap<Container, Boolean> excitedContainers = new HashMap<Container, Boolean>();

	final double DEFAULT_SIMULATION_DELAY = 0.3;
	final double EDGE_SPEED_MULTIPLIER = 10;

	protected final Trace mainTrace = new Trace();
	protected final Trace branchTrace = new Trace();

	protected boolean isRev;

	protected Timer timer = null;

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);

		playButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"), "Automatic trace playback");
		stopButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-stop.svg"), "Reset trace playback");
		backwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-backward.svg"), "Step backward");
		forwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-forward.svg"), "Step forward");
		reverseButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/son-forward-simulation.svg"), "Switch to reverse simulation");
		autoSimuButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/son-auto-simulation.svg"), "Automatic simulation (maximum parallelism)");

		speedSlider = new JSlider(-1000, 1000, 0);
		speedSlider.setToolTipText("Simulation playback speed");

		copyStateButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-trace-copy.svg"), "Copy trace to clipboard");
		pasteStateButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-trace-paste.svg"), "Paste trace from clipboard");
		mergeTraceButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-trace-merge.svg"), "Merge branch into trace");

		int buttonWidth = (int)Math.round(playButton.getPreferredSize().getWidth() + 5);
		int buttonHeight = (int)Math.round(playButton.getPreferredSize().getHeight() + 5);
		Dimension panelSize = new Dimension(buttonWidth * 6, buttonHeight);

		JPanel simulationControl = new JPanel();
		simulationControl.setLayout(new FlowLayout());
		simulationControl.setPreferredSize(panelSize);
		simulationControl.setMaximumSize(panelSize);
		simulationControl.add(playButton);
		simulationControl.add(stopButton);
		simulationControl.add(backwardButton);
		simulationControl.add(forwardButton);
		simulationControl.add(reverseButton);
		simulationControl.add(autoSimuButton);

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
				if(timer != null) {
					timer.stop();
					timer.setInitialDelay(getAnimationDelay());
					timer.setDelay(getAnimationDelay());
					timer.start();
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
				} else  {
					timer.stop();
					timer = null;
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

		reverseButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				Map<PlaceNode, Boolean> currentMarking = readSONMarking();
				setReverse(editor, !isRev);
				applyMarking(currentMarking);
				setDecoration(simuAlg.getEnabledNodes(sync, phases, isRev));
				excitedContainers.clear();
				updateState(editor);
			}
		});

		autoSimuButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Collection<Map<PlaceNode, Boolean>> history = new ArrayList<Map<PlaceNode, Boolean>>();
				try {
					autoSimulator(editor, readSONMarking(), history);
				} catch (InvalidStructureException e1) {
					errorMsg(e1.getMessage(), editor);
				}
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
							work = quietStepBack(editor);
						}
						while (work && (mainTrace.getPosition() > row)) {
							work = quietStepBack(editor);
						}
						while (work && (mainTrace.getPosition() < row)) {
							work = quietStep(editor);
						}
					}
				} else {
					if ((row >= mainTrace.getPosition()) && (row < mainTrace.getPosition() + branchTrace.size())) {
						boolean work = true;
						while (work && (mainTrace.getPosition() + branchTrace.getPosition() > row)) {
							work = quietStepBack(editor);
						}
						while (work && (mainTrace.getPosition() + branchTrace.getPosition() < row)) {
							work = quietStep(editor);
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
		traceTable.setDefaultRenderer(Object.class,	new TraceTableCellRendererImplementation());
	}

	@Override
	public void activated(final GraphEditor editor) {

		visualNet = (VisualSON)editor.getModel();
		net = (SON)visualNet.getMathModel();

		this.editor = editor;
		WorkspaceEntry we = editor.getWorkspaceEntry();

		relationAlg = new RelationAlgorithm(net);
		bsonAlg = new BSONAlg(net);
		simuAlg = new SimulationAlg(net);
		errAlg = new ErrorTracingAlg(net);

		we.setCanModify(false);
		visualNet.connectToBlocks(we);
		initialMarking=simuAlg.getInitialMarking();

		reset(editor);

		sync = getSyncCycles();
		phases = bsonAlg.getAllPhases();
		setDecoration(simuAlg.getEnabledNodes(sync, phases, isRev));

		if (ErrTracingDisable.showErrorTracing()) {
			net.resetConditionErrStates();
		}
		updateState(editor);
		editor.forceRedraw();
		editor.getModel().setTemplateNode(null);
	}

	private Collection<Path> getSyncCycles(){
		HashSet<Node> nodes = new HashSet<Node>();
		nodes.addAll(net.getTransitionNodes());
		nodes.addAll(net.getChannelPlaces());
		CSONCycleAlg cycleAlg = new CSONCycleAlg(net);

		return cycleAlg.syncEventCycleTask(nodes);
	}

	@Override
	public void deactivated(GraphEditor editor) {
		super.deactivated(editor);
		mainTrace.clear();
		branchTrace.clear();
		isRev = false;
	}

	@Override
	public void updateState(final GraphEditor editor) {
		if (timer == null) {
			playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"));
		} else {
			if (branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress())) {
				playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-pause.svg"));
				timer.setDelay(getAnimationDelay());
			} else {
				playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"));
				timer.stop();
				timer = null;
			}
		}
		playButton.setEnabled(branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress()));
		stopButton.setEnabled(!mainTrace.isEmpty() || !branchTrace.isEmpty());
		backwardButton.setEnabled((mainTrace.getPosition() > 0) || (branchTrace.getPosition() > 0));
		forwardButton.setEnabled(branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress()));
		traceTable.tableChanged(new TableModelEvent(traceTable.getModel()));
		if(!isRev){
			reverseButton.setIcon(GUI.createIconFromSVG("images/icons/svg/son-forward-simulation.svg"));
			reverseButton.setToolTipText("Switch to reverse simulation");
		}

		else{
			reverseButton.setIcon(GUI.createIconFromSVG("images/icons/svg/son-reverse-simulation.svg"));
			reverseButton.setToolTipText("Switch to forward simulation");
		}

		editor.requestFocus();
		editor.repaint();
	}

	private int getAnimationDelay() {
		return (int)(1000.0 * DEFAULT_SIMULATION_DELAY * Math.pow(EDGE_SPEED_MULTIPLIER, -speedSlider.getValue() / 1000.0));
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
			if (column == 0) {
				if (!mainTrace.isEmpty() && (row < mainTrace.size())) {
					return mainTrace.get(row);
				}
			} else {
				if (!branchTrace.isEmpty() && (row >= mainTrace.getPosition()) && (row < mainTrace.getPosition() + branchTrace.size())) {
					return branchTrace.get(row - mainTrace.getPosition());
					}
				}
			return "";
		}
	};


	private void errorMsg(String message, final GraphEditor editor){

		final Framework framework = Framework.getInstance();
		MainWindow mainWindow = framework.getMainWindow();

		JOptionPane.showMessageDialog(mainWindow,
				message, "Invalid structure", JOptionPane.WARNING_MESSAGE);
		reset(editor);
	}

	protected Map<PlaceNode, Boolean> readSONMarking() {
		HashMap<PlaceNode, Boolean> result = new HashMap<PlaceNode, Boolean>();
		for (PlaceNode c : net.getPlaceNodes())
			result.put(c, c.isMarked());

		return result;
	}

	private boolean quietStep(final GraphEditor editor) {
		excitedContainers.clear();
		boolean result = false;
		List<TransitionNode> fireList = null;
		int mainInc = 0;
		int branchInc = 0;
		if (branchTrace.canProgress()) {
			Step step = branchTrace.getCurrent();
			fireList=this.getFireList(step);
			setReverse(editor, step);
			branchInc = 1;
		} else if (mainTrace.canProgress()) {
			Step step = mainTrace.getCurrent();
			fireList=this.getFireList(step);
			setReverse(editor, step);
			mainInc = 1;
		}

		if (fireList != null) {
			try {
				simuAlg.setMarking(fireList, phases, isRev);
			} catch (InvalidStructureException e) {
				errorMsg(e.getMessage(), editor);
				return false;
			}
			setErrNum(fireList, isRev);
			mainTrace.incPosition(mainInc);
			branchTrace.incPosition(branchInc);
			setDecoration(simuAlg.getEnabledNodes(sync, phases, isRev));
			result = true;
		}

		return result;
	}

	private boolean step(final GraphEditor editor) {
		boolean ret = quietStep(editor);
		updateState(editor);
		return ret;
	}

	private boolean stepBack(final GraphEditor editor) {
		boolean ret = quietStepBack(editor);
		updateState(editor);
		return ret;
	}

	private boolean quietStepBack(final GraphEditor editor) {
		excitedContainers.clear();
		boolean result = false;
		List<TransitionNode> fireList = null;
		int mainDec = 0;
		int branchDec = 0;
		if (branchTrace.getPosition() > 0) {
			Step step = branchTrace.get(branchTrace.getPosition()-1);
			fireList=this.getFireList(step);
			setReverse(editor, step);
			branchDec = 1;
		} else if (mainTrace.getPosition() > 0) {
			Step step = mainTrace.get(mainTrace.getPosition() - 1);
			fireList=this.getFireList(step);
			setReverse(editor, step);
			mainDec = 1;
		}

		if (fireList != null) {
			try {
				simuAlg.setMarking(fireList, phases, !isRev);
			} catch (InvalidStructureException e) {
				errorMsg(e.getMessage(), editor);
				return false;
			}

			mainTrace.decPosition(mainDec);
			branchTrace.decPosition(branchDec);
			if ((branchTrace.getPosition() == 0) && !mainTrace.isEmpty()) {
				branchTrace.clear();
			}
			setDecoration(simuAlg.getEnabledNodes(sync, phases, isRev));
			result = true;
			this.setErrNum(fireList, !isRev);
		}

		return result;
	}


	private void reset(final GraphEditor editor) {
		applyMarking(initialMarking);
		isRev = false;
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
				while (work && (mainTrace.getPosition() < mainTracePosition) ) {
					work = quietStep(editor);
				}
			} else {
				branchTrace.fromString(s);
				int branchTracePosition = branchTrace.getPosition();
				branchTrace.setPosition(0);
				boolean work = true;
				while (work && (branchTrace.getPosition() < branchTracePosition)) {
					work = quietStep(editor);
				}
				break;
			}
			first = false;
		}
		updateState(editor);
	}

	public void mergeTrace(final GraphEditor editor) {
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

	private void setErrNum(List<TransitionNode> fireList, boolean isRev){
		if (ErrTracingDisable.showErrorTracing()){
			Collection<TransitionNode> upperEvents = new ArrayList<TransitionNode>();

			//get high level events
			for(TransitionNode node : fireList){
				if(bsonAlg.isUpperEvent(node))
					upperEvents.add(node);
			}
			//get low level events
			fireList.removeAll(upperEvents);

			if(!isRev){
				//set error number for upper events
				errAlg.setErrNum(upperEvents, sync, phases, false);
				//set error number for lower events
				errAlg.setErrNum(fireList, sync, phases, true);
			}
			else{
				errAlg.setRevErrNum(upperEvents, sync, phases, false);
				errAlg.setRevErrNum(fireList, sync, phases, true);
			}
		}
	}

	private void applyMarking(Map<PlaceNode, Boolean> marking){
		for (PlaceNode c: marking.keySet())
			c.setMarked(marking.get(c));
	}

	protected void autoSimulator(final GraphEditor editor, Map<PlaceNode, Boolean> marking, Collection<Map<PlaceNode, Boolean>> history) throws InvalidStructureException{
		List<TransitionNode> enabled = null;
		history.add(marking);

		enabled = simuAlg.getEnabledNodes(sync, phases, isRev);

		if(!enabled.isEmpty()){

			executeEvent(editor, enabled);

			Map<PlaceNode, Boolean> currentMarking = readSONMarking();

			for(Map<PlaceNode, Boolean> m : history){
				if(m.equals(currentMarking)){
					throw new InvalidStructureException("repeat markings");
				}
			}
			autoSimulator(editor, readSONMarking(), history);
		}
	}

	public Map<PlaceNode, Boolean> ReachabilitySimulator(final GraphEditor editor, Collection<String> causalPredecessorRefs, Collection<String> markingRefs){
		Collection<TransitionNode> causalPredecessors = new ArrayList<TransitionNode>();
		for(String ref : causalPredecessorRefs){
			Node node = net.getNodeByReference(ref);
			if(node instanceof TransitionNode)
				causalPredecessors.add((TransitionNode)net.getNodeByReference(ref));
		}
		return ReachabilitySimulatorTask(editor, causalPredecessors, markingRefs);
	}

	private Map<PlaceNode, Boolean> ReachabilitySimulatorTask(final GraphEditor editor, Collection<TransitionNode> causalPredecessors,  Collection<String> markingRefs){
		List<TransitionNode> enabled = null;
		List<TransitionNode> fireList = new ArrayList<TransitionNode>();

		enabled = simuAlg.getEnabledNodes(sync, phases, isRev);
		for(Node node : relationAlg.getCommonElements(enabled, causalPredecessors)){
			if(node instanceof TransitionNode)
				fireList.add((TransitionNode)node);
		}

		//causalPredecessors.removeAll(fireList);

		if(!fireList.isEmpty()){

			executeEvent(editor, fireList);

			ReachabilitySimulatorTask(editor, causalPredecessors, markingRefs);
		}

		for(String ref : markingRefs){
			Node node = net.getNodeByReference(ref);
			if(node instanceof PlaceNode){
				((PlaceNode)node).setForegroundColor(Color.BLUE);
				((PlaceNode)node).setTokenColor(Color.BLUE);
			}
		}

		return readSONMarking();
	}

	public void executeEvent(final GraphEditor editor, List<TransitionNode> fireList) {
		if (fireList.isEmpty()) return;
		List<TransitionNode> traceList = new ArrayList<TransitionNode>();
		// if clicked on the trace event, do the step forward
		if (branchTrace.isEmpty() && !mainTrace.isEmpty() && (mainTrace.getPosition() < mainTrace.size())) {
			Step step = mainTrace.get(mainTrace.getPosition());
			traceList=getFireList(step);
		}
		// otherwise form/use the branch trace
		if (!branchTrace.isEmpty() && (branchTrace.getPosition() < branchTrace.size())) {
			Step step = branchTrace.get(branchTrace.getPosition());
			traceList=getFireList(step);
		}
		if (!traceList.isEmpty() && traceList.containsAll(fireList) && fireList.containsAll(traceList)){
				step(editor);
				return;
		}
		while (branchTrace.getPosition() < branchTrace.size()) {
			branchTrace.removeCurrent();
		}

		Step newStep = new Step();
		if(!isRev)
			newStep.add(">");
		else
			newStep.add("<");
		for(TransitionNode e : fireList)
			newStep.add(net.getNodeReference(e));

		branchTrace.add(newStep);
		step(editor);
		return;
	}

	private ArrayList<TransitionNode> getFireList(Step step){
		ArrayList<TransitionNode> result = new ArrayList<TransitionNode>();
		for(int i =0; i<step.size(); i++){
			final Node node = net.getNodeByReference(step.get(i));
			if(node instanceof TransitionNode)
				result.add((TransitionNode)node);
		}
		return result;
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

			if (!(value instanceof Step)) return null;

			label.setText(((Step)value).toString());

			if (isActive(row, column)) {
				label.setBackground(Color.YELLOW);
			} else {
				label.setBackground(Color.WHITE);
			}

			return label;
		}
	}

	private void setDecoration(List<TransitionNode> enabled){
		net.refreshColor();

		for(TransitionNode e : enabled){
			//e.setFillColor(CommonSimulationSettings.getEnabledForegroundColor());
			e.setForegroundColor(CommonSimulationSettings.getEnabledForegroundColor());
		}

		Step step = null;
		if (branchTrace.canProgress()) {
			step = branchTrace.get(branchTrace.getPosition());
		}else if (branchTrace.isEmpty() && mainTrace.canProgress()) {
			step = mainTrace.get(mainTrace.getPosition());
		}

		if(step != null){
			if(step.isReverse() == isRev){
				for(String ref : step){
					Node n = net.getNodeByReference(ref);
					if(n != null){
						if(n instanceof TransitionNode){
							((TransitionNode) n).setFillColor(new Color(255, 228, 181));
						}
					}
				}
			}
		}
	}

	private boolean isEnabled(Node e, List<TransitionNode> fireList){
		if(fireList.contains(e))
			return true;
		return false;
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {

		Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(),
			new Func<Node, Boolean>() {
			@Override
			public Boolean eval(Node node) {
				if(node instanceof VisualTransitionNode){
					TransitionNode node1 = ((VisualTransitionNode)node).getMathTransitionNode();
					List<TransitionNode> enabled = null;

					enabled = simuAlg.getEnabledNodes(sync, phases, isRev);
					if(isEnabled(node1, enabled))
						return true;

				}
				return false;

			}
		});

		final Framework framework = Framework.getInstance();
		final MainWindow mainWindow = framework.getMainWindow();

		if (node instanceof VisualTransitionNode) {

			List<TransitionNode> enabled = null;
			TransitionNode selected = ((VisualTransitionNode)node).getMathTransitionNode();

			enabled = simuAlg.getEnabledNodes(sync, phases, isRev);

			List<TransitionNode> minFire = simuAlg.getMinFire(selected, sync, enabled, isRev);

			List<TransitionNode> possibleFires = new ArrayList<TransitionNode>();
			for(TransitionNode pe : enabled)
				if(!minFire.contains(pe))
					possibleFires.add(pe);

			minFire.remove(selected);

			List<TransitionNode> fireList = new ArrayList<TransitionNode>();

			if(possibleFires.isEmpty() && minFire.isEmpty()){
				fireList.add(selected);
				executeEvent(e.getEditor(),fireList);

			}else{
				e.getEditor().requestFocus();
				ParallelSimDialog dialog = new ParallelSimDialog(mainWindow,
						net, possibleFires, minFire, selected, isRev, sync);
				GUI.centerToParent(dialog, mainWindow);
				dialog.setVisible(true);

				fireList.addAll(minFire);
				fireList.add(selected);

				if (dialog.getRun() == 1){
					fireList.addAll(dialog.getSelectedEvent());
					executeEvent(e.getEditor(),fireList);
				}
				if(dialog.getRun()==2){
					setDecoration(enabled);
					return;
					}
				}
			//Error tracing
		//	setErrNum(runList, reverse);

		}
	}

	@Override
	protected boolean isContainerExcited(Container container) {
		if (excitedContainers.containsKey(container)) return excitedContainers.get(container);

		boolean ret = false;

		for (Node node: container.getChildren()) {
			try{
				List<TransitionNode> enabled = null;

				enabled = simuAlg.getEnabledNodes(sync, phases, isRev);

				if (node instanceof VisualTransitionNode) {
					TransitionNode event = ((VisualTransitionNode)node).getMathTransitionNode();
					ret=ret || isEnabled(event, enabled);
				}
			}catch(NullPointerException ex){

			}

			if (node instanceof Container) {
				ret = ret || isContainerExcited((Container)node);
			}

			if (ret) break;
		}

		excitedContainers.put(container, ret);
		return ret;
	}

	public void setReverse(final GraphEditor editor, boolean reverse){
		this.isRev = reverse;
		updateState(editor);
	}

	public void setReverse(final GraphEditor editor, Step step){

		if(step.contains(">"))
			this.setReverse(editor, false);
		else
			this.setReverse(editor, true);
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {

		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				if ((node instanceof VisualPage && !(node instanceof VisualBlock)) || node instanceof VisualGroup) {
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

	public GraphEditor getGraphEditor(){
		return editor;
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub
	}

}
