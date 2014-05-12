package org.workcraft.plugins.son.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import org.workcraft.Trace;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.ErrorTracingAlg;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.algorithm.SimulationAlg;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.plugins.son.gui.ParallelSimDialog;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;

public class SONSimulationTool extends AbstractTool implements ClipboardOwner {

	protected VisualModel visualNet;
	private Framework framework;
	private RelationAlgorithm relationAlg;
	private BSONAlg bsonAlg;
	private SimulationAlg simuAlg;
	private ErrorTracingAlg	errAlg;
	private SONModel net;

	private Collection<ArrayList<Node>> syncSet;
	private Map<Condition, Collection<Condition>> phases;
	private Collection<ONGroup> abstractGroups;

	protected JPanel interfacePanel;
	protected JPanel controlPanel;
	protected JScrollPane infoPanel;
	protected JPanel statusPanel;
	protected JPanel simuControalPanel;
	protected JTable traceTable;

	protected JSlider speedSlider;
	protected JButton playButton, stopButton, backwardButton, forwardButton, reverseButton;
	protected JButton saveMarkingButton, loadMarkingButton;
	protected JComboBox typeCombo;

	protected Map<Node, Boolean>initialMarking = null;
	protected Map<Node, Boolean> savedMarking = null;
	int savedStep = 0;
	protected List<Trace> savedBranchTrace;
	protected int savedBranchStep = 0;

	protected List<Trace> branchTrace;
	protected int branchStep = 0;
	protected Trace trace;
	protected int traceStep = 0;
	protected boolean reverse = false;

	final double DEFAULT_SIMULATION_DELAY = 0.3;
	final double EDGE_SPEED_MULTIPLIER = 10;

	protected Timer timer = null;

	class typeMode {
		public int value;
		public String description;

		public typeMode(int value, String description) {
			this.value = value;
			this.description = description;
		}

		public String toString() {
			return description;
		}
	}

	private void applyMarking(Map<Node, Boolean> marking)
	{
		for (Node c: marking.keySet()) {
			if(c instanceof Condition)
				if (net.getConditions().contains(c)) {
					((Condition)c).setMarked(marking.get((Condition)c));
				} else {
					//ExceptionDialog.show(null, new RuntimeException("Place "+p.toString()+" is not in the model"));
				}
			if(c instanceof ChannelPlace)
				if (net.getChannelPlace().contains(c)){
					((ChannelPlace)c).setToken(marking.get((ChannelPlace)c));
				}
		}
	}

	protected void updateState()
	{
		if (timer == null) {
			playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"));
		} else {
			if (branchTrace == null || branchStep == branchTrace.size()) {
				playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"));
				timer.stop();
				timer = null;
			} else {
				playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-pause.svg"));
				timer.setDelay(getAnimationDelay());
			}
		}

		playButton.setEnabled(branchTrace != null && branchStep < branchTrace.size());
		stopButton.setEnabled(trace != null || branchTrace != null);
		backwardButton.setEnabled(traceStep > 0 || branchStep > 0);
		forwardButton.setEnabled(branchTrace==null && trace != null && traceStep < trace.size() || branchTrace != null && branchStep < branchTrace.size());
		traceTable.tableChanged(new TableModelEvent(traceTable.getModel()));
		if(!reverse){
			reverseButton.setIcon(GUI.createIconFromSVG("images/icons/svg/son-reverse-simulation.svg"));
			reverseButton.setToolTipText("Reverse simulation");
		}
		else{
			reverseButton.setIcon(GUI.createIconFromSVG("images/icons/svg/son-forward-simulation.svg"));
			reverseButton.setToolTipText("Forward simulation");
		}
	}

	protected int getAnimationDelay()
	{
		return (int)(1000.0 * DEFAULT_SIMULATION_DELAY * Math.pow(EDGE_SPEED_MULTIPLIER, -speedSlider.getValue() / 1000.0));
	}

	private boolean quietStep() {
		if(branchTrace!=null && branchStep < branchTrace.size()){
			List<Event> runList = new ArrayList<Event>();

			Trace step = branchTrace.get(branchStep);
			for(int i =0; i<step.size(); i++){
				final Node event = net.getNodeByReference(step.get(i));
				if(event instanceof Event)
					runList.add((Event)event);
			}
			if(!reverse){
				simuAlg.fire(runList);
				this.setErrNum(runList, reverse);
			}
			else{
				simuAlg.unFire(runList);
				this.setErrNum(runList, reverse);
			}
			branchStep++;
			return true;
		}

		if (trace==null) return false;
		if (traceStep>=trace.size()) return false;

		List<Event> runList = new ArrayList<Event>();

		Trace step = branchTrace.get(branchStep);
		for(int i =0; i<step.size(); i++){
			final Node event = net.getNodeByReference(step.get(i));
			if(event instanceof Event)
				runList.add((Event)event);
		}

		if (runList.isEmpty()) return false;

		if(!reverse){
			simuAlg.fire(runList);
			this.setErrNum(runList, reverse);
		}
		else{
			simuAlg.unFire(runList);
			this.setErrNum(runList, reverse);
		}

		traceStep++;
		return true;
	}


	private boolean step() {
		boolean ret = quietStep();
		updateState();
		return ret;
	}

	private boolean quietStepBack() {
		if (branchTrace!=null&&branchStep>0) {
			List<Event> runList = new ArrayList<Event>();
			Trace step = branchTrace.get(branchStep-1);

			for(int i =0; i<step.size(); i++){
				final Node event = net.getNodeByReference(step.get(i));
				if(event instanceof Event)
					runList.add((Event)event);
			}

			if (runList.isEmpty()) return false;
			branchStep--;

			if(!reverse){
				simuAlg.unFire(runList);
				this.setErrNum(runList, !reverse);
			}
			else{
				simuAlg.fire(runList);
				this.setErrNum(runList, !reverse);
			}

			if (branchStep==0&&trace!=null) branchTrace=null;
			return true;
		}

		if (trace==null) return false;
		if (traceStep==0) return false;

		List<Event> runList = new ArrayList<Event>();
		Trace step = branchTrace.get(branchStep-1);

		for(int i =0; i<step.size(); i++){
			final Node event = net.getNodeByReference(step.get(i));
			if(event instanceof Event)
				runList.add((Event)event);
		}

		if (runList.isEmpty()) return false;
		branchStep--;

		if(!reverse){
			simuAlg.unFire(runList);
			this.setErrNum(runList, !reverse);
		}
		else{
			simuAlg.fire(runList);
			this.setErrNum(runList, !reverse);
		}
		return true;
	}

	private boolean stepBack() {
		boolean ret = quietStepBack();
		updateState();
		return ret;
	}

	private void reset() {
		if (traceStep==0&&branchTrace==null) {
			applyMarking(initialMarking);
			trace = null;
			traceStep = 0;
		} else {
			applyMarking(initialMarking);
			traceStep = 0;
			branchStep=0;
			branchTrace=null;
		}

		if(timer!=null)
		{
			timer.stop();
			timer = null;
		}
		updateState();
	}

	protected Map<Node, Boolean> readMarking() {
		HashMap<Node, Boolean> result = new HashMap<Node, Boolean>();
		for (Condition c : net.getConditions()) {
			result.put(c, c.isMarked());
		}
		for(ChannelPlace cp : net.getChannelPlace()){
			result.put(cp, cp.hasToken());
		}
		return result;
	}

	//auto set initial marking
	protected Map<Node, Boolean> autoInitalMarking(){
		HashMap<Node, Boolean> result = new HashMap<Node, Boolean>();

		for (Condition c : net.getConditions()) {
			c.setMarked(false);
			result.put(c, false);
		}
		for(ChannelPlace cp : net.getChannelPlace()){
			cp.setToken(false);
			result.put(cp, false);
		}
		//initial marking for abstract groups and behavioral groups
		for(ONGroup abstractGroup : bsonAlg.getAbstractGroups(net.getGroups())){
			for(Node c : relationAlg.getInitial(abstractGroup.getComponents())){
				if(c instanceof Condition){
					result.put(c, true);
					((Condition) c).setMarked(true);
					Collection<ONGroup> bhvGroup = bsonAlg.getBhvGroups((Condition) c);
					if(bhvGroup.size() != 1)
						JOptionPane.showMessageDialog(null, "Incorrect BSON structure (disjoint phase/empty phase), run structure verification.", "error", JOptionPane.WARNING_MESSAGE);
					else
						for(ONGroup group : bhvGroup){
							//can optimize
							Collection<Node> initial = relationAlg.getInitial(group.getComponents());
							if(bsonAlg.getPhase((Condition)c).containsAll(initial))
								for(Node c1 : relationAlg.getInitial(group.getComponents())){
									result.put(c1, true);
									((Condition) c1).setMarked(true);}
							else
								JOptionPane.showMessageDialog(null, "Incorrect BSON structure (minimal phase), run structure verification.", "error", JOptionPane.WARNING_MESSAGE);
						}
				}
			}
		}
		//initial marking for channel places
		for(Node c : relationAlg.getInitial(net.getComponents())){
			if(c instanceof ChannelPlace){
				result.put(c, true);
				((ChannelPlace)c).setToken(true);}
		}

		//initial marking for other groups.
		for(ONGroup group : net.getGroups()){
			boolean hasBhvLine = false;
			for(Condition c : group.getConditions())
				if(net.getSONConnectionTypes(c).contains("BHVLINE"))
					hasBhvLine = true;
			if(!hasBhvLine){
				for(Node c : relationAlg.getInitial(group.getComponents())){
					if(c instanceof Condition){
						result.put(c, true);
						((Condition)c).setMarked(true);}
				}
			}
		}
		return result;
	}
	/*
	protected Map<Node, Boolean> autoInitalReverseMarking(){
		HashMap<Node, Boolean> result = new HashMap<Node, Boolean>();

		for (Condition c : net.getConditions()) {
			c.setMarked(false);
			result.put(c, false);
		}
		for(ChannelPlace cp : net.getChannelPlace()){
			cp.setToken(false);
			result.put(cp, false);
		}
		//initial marking for abstract groups and behavioral groups
		for(ONGroup abstractGroup : relationAlg.getAbstractGroups(net.getGroups())){
			for(Node c : relationAlg.getFinal(abstractGroup.getComponents())){
				if(c instanceof Condition){
					result.put(c, true);
					((Condition) c).setMarked(true);
					Collection<ONGroup> bhvGroup = relationAlg.getBhvGroups((Condition) c);
					if(bhvGroup.size() != 1)
						JOptionPane.showMessageDialog(null, "Incorrect BSON structure (disjoint phase/empty phase), run structure verification.", "error", JOptionPane.WARNING_MESSAGE);
					else
						for(ONGroup group : bhvGroup){
							//can optimize
							Collection<Node> fin = relationAlg.getFinal(group.getComponents());
							if(relationAlg.getPhase((Condition)c).containsAll(fin))
								for(Node c1 : relationAlg.getFinal(group.getComponents())){
									result.put(c1, true);
									((Condition) c1).setMarked(true);}
							else
								JOptionPane.showMessageDialog(null, "Incorrect BSON structure (minimal phase), run structure verification.", "error", JOptionPane.WARNING_MESSAGE);
						}
				}
			}
		}
		//initial marking for channel places
		for(Node c : relationAlg.getFinal(net.getComponents())){
			if(c instanceof ChannelPlace){
				result.put(c, true);
				((ChannelPlace)c).setToken(true);}
		}

		//initial marking for other groups.
		for(ONGroup group : net.getGroups()){
			boolean hasBhvLine = false;
			for(Condition c : group.getConditions())
				if(net.getSONConnectionTypes(c).contains("BHVLINE"))
					hasBhvLine = true;
			if(!hasBhvLine){
				for(Node c : relationAlg.getFinal(group.getComponents())){
					if(c instanceof Condition){
						result.put(c, true);
						((Condition)c).setMarked(true);}
				}
			}
		}

		return result;
	}
*/
	private final class TraceTableMouseListenerImplementation implements MouseListener {
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

					updateState();
				}
			} else {
				if (branchTrace!=null&&row>=traceStep&&row<traceStep+branchTrace.size()) {

					boolean work=true;
					while (traceStep+branchStep>row&&work) work=quietStepBack();
					while (traceStep+branchStep<row&&work) work=quietStep();
					updateState();
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
	}

	private final class TraceTableCellRendererImplementation implements TableCellRenderer {

		@SuppressWarnings("serial")
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
				if (trace!=null&&branchTrace==null){
					return row==traceStep;
				}
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

			if (!(value instanceof Trace)) return null;

			label.setText(((Trace)value).toString());


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
			return "Trace";
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

	private void createSimuControalPanel(){
		simuControalPanel = new JPanel();

		typeCombo = new JComboBox();
		typeCombo.addItem(new typeMode(0, "Forward"));
		typeCombo.addItem(new typeMode(1, "Backward"));

		simuControalPanel.add(typeCombo);

	}

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);

		playButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"), "Automatic trace playback");
		stopButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-stop.svg"), "Reset trace playback");
		backwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-backward.svg"), "Step backward");
		forwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-forward.svg"), "Step forward");
		reverseButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/son-reverse-simulation.svg"), "Reverse simulation");
		speedSlider = new JSlider(-1000, 1000, 0);
		speedSlider.setToolTipText("Simulation playback speed");
		loadMarkingButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-marking-load.svg"), "Load marking from memory");
		saveMarkingButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-marking-save.svg"), "Save marking to memory");
		//createSimuControalPanel();

		int buttonWidth = (int)Math.round(playButton.getPreferredSize().getWidth() + 5);
		int buttonHeight = (int)Math.round(playButton.getPreferredSize().getHeight() + 5);
		Dimension panelSize = new Dimension(buttonWidth * 7, buttonHeight);

		JPanel simulationControl = new JPanel();
		simulationControl.setLayout(new FlowLayout());
		simulationControl.setPreferredSize(panelSize);
		simulationControl.setMaximumSize(panelSize);
		simulationControl.add(playButton);
		simulationControl.add(stopButton);
		simulationControl.add(backwardButton);
		simulationControl.add(forwardButton);
		simulationControl.add(reverseButton);

		JPanel speedControl = new JPanel();
		speedControl.setLayout(new BorderLayout());
		speedControl.setPreferredSize(panelSize);
		speedControl.setMaximumSize(panelSize);
		speedControl.add(speedSlider, BorderLayout.CENTER);
		speedControl.add(simulationControl, BorderLayout.SOUTH);

		JPanel traceControl = new JPanel();
		traceControl.setLayout(new FlowLayout());
		traceControl.setPreferredSize(panelSize);
		traceControl.add(new JSeparator());
		traceControl.add(loadMarkingButton);
		traceControl.add(saveMarkingButton);


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
				updateState();
			}
		});


		playButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (timer == null) {
					timer = new Timer(getAnimationDelay(), new ActionListener()	{
						@Override
						public void actionPerformed(ActionEvent e) {
							step();
						}
					});
					timer.start();
				} else {
					timer.stop();
					timer = null;
				}
				updateState();
			}
		});

		stopButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});

		forwardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				step();
			}
		});

		backwardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stepBack();
			}
		});

		reverseButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				 Map<Node, Boolean> currentMarking = readMarking();
					setReverse(!reverse);
					if(savedBranchTrace!=null)
						savedBranchTrace.clear();
					savedBranchStep = 0;
					savedStep = 0;
					if(!reverse)
						initialMarking = currentMarking;
					else
						initialMarking = currentMarking;
					reset();
					updateState();
			}
		});

		loadMarkingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyMarking(savedMarking);
				traceStep = savedStep;
				if (savedBranchTrace != null) {
					branchTrace = new ArrayList<Trace>();
					branchStep = savedBranchStep;
					for(Trace step : savedBranchTrace){
						branchTrace.add(step);
					}
				} else {
					branchStep = 0;
					branchTrace = null;
				}
				updateState();
			}
		});

		saveMarkingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				savedMarking = readMarking();
				savedStep = traceStep;
				savedBranchStep = 0;
				savedBranchTrace = new ArrayList<Trace>();
				if (branchTrace!=null) {
					for(Trace step : branchTrace){
						savedBranchTrace.add(step);
					}
					savedBranchStep = branchStep;
				}
				updateState();
			}
		});
		traceTable.getColumn("Trace").setMaxWidth(0);
		traceTable.addMouseListener(new TraceTableMouseListenerImplementation());
		traceTable.setDefaultRenderer(Object.class,	new TraceTableCellRendererImplementation());
	}


	private Collection<ArrayList<Node>> getSyncCycles(){

		HashSet<Node> nodes = new HashSet<Node>();
		nodes.addAll(net.getConditions());
		nodes.addAll(net.getEvents());

		return simuAlg.getSyncCycles(nodes);
	}

	@Override
	public void activated(GraphEditor editor) {
		editor.getWorkspaceEntry().setCanModify(false);
		editor.getWorkspaceEntry().captureMemento();
		visualNet = editor.getModel();
		this.setFramework(editor.getFramework());
		net = (SONModel)visualNet.getMathModel();
		relationAlg = new RelationAlgorithm(net);
		bsonAlg = new BSONAlg(net);
		initialMarking = autoInitalMarking();
		simuAlg = new SimulationAlg(net);

		reverse=false;
		traceStep = 0;
		branchTrace = null;
		branchStep = 0;

		syncSet = getSyncCycles();
		errAlg = new ErrorTracingAlg(net);
		abstractGroups = bsonAlg.getAbstractGroups(net.getGroups());
		phases = new HashMap<Condition, Collection<Condition>>();
		for(ONGroup group : abstractGroups){
			for(Condition c : group.getConditions())
				phases.put(c, bsonAlg.getPhase(c));
		}
		if (ErrTracingDisable.showErrorTracing()) {
			net.resetConditionErrStates();
		}
		updateState();
	}

	@Override
	public void deactivated(GraphEditor editor)	{
		editor.getWorkspaceEntry().cancelMemento();
		if (traceStep==0&&branchTrace==null) {
			applyMarking(readMarking());
			trace = null;
			traceStep = 0;
		} else {
			applyMarking(readMarking());
			traceStep = 0;
			branchStep=0;
			branchTrace=null;
		}

		if(timer!=null)
		{
			timer.stop();
			timer = null;
		}
		updateState();
	}


	public void executeEvent(List<Event> syncList) {
		// otherwise form/use the branch trace
		if (branchTrace!=null&&branchStep<branchTrace.size()) {
			List<Event> runList = new ArrayList<Event>();

			Trace step = branchTrace.get(branchStep);
			for(int i =0; i<step.size(); i++){
				final Node event = net.getNodeByReference(step.get(i));
				if(event instanceof Event)
					runList.add((Event)event);
			}

			if (!runList.isEmpty()&&syncList.containsAll(runList)) {
				step();
				return;
			}
		}

		if(branchTrace==null) branchTrace = new ArrayList<Trace>();

		Trace step = new Trace();
		for(Event e : syncList)
			step.add(net.getName(e));

		while (branchStep<branchTrace.size())
			branchTrace.remove(branchStep);

		branchTrace.add(step);

		step();
		updateState();
		return;
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET) stepBack();
		if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET) step();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {

		Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(), new Func<Node, Boolean>() {
				@Override
				public Boolean eval(Node node) {

					if(node instanceof VisualEvent && simuAlg.isEnabled(((VisualEvent)node).getReferencedEvent(), syncSet, phases) && !reverse){
						return true;
					}
					if(node instanceof VisualEvent && simuAlg.isUnfireEnabled(((VisualEvent)node).getReferencedEvent(), syncSet, phases) && reverse){
						return true;
					}
					return false;

				}
			});

		if (node instanceof VisualEvent){
			Collection<Event> enabledEvents = new ArrayList<Event>();
			Event event = ((VisualEvent)node).getReferencedEvent();

			if(reverse){
				for(Event enable : net.getEvents())
					if(simuAlg.isUnfireEnabled(enable, syncSet, phases))
						enabledEvents.add(enable);
				}
			else{
				for(Event enable : net.getEvents())
					if(simuAlg.isEnabled(enable, syncSet, phases))
						enabledEvents.add(enable);
				}

			List<Event> minimalEvents = simuAlg.getMinimalExeResult(event, syncSet, enabledEvents);
			List<Event> minimalReverseEvents = simuAlg.getMinimalReverseExeResult(event, syncSet, enabledEvents);

			if(!reverse){
				List<Event> possibleEvents = new ArrayList<Event>();
				for(Event psbE : enabledEvents)
					if(!minimalEvents.contains(psbE))
						possibleEvents.add(psbE);

				minimalEvents.remove(event);

				List<Event> runList = new ArrayList<Event>();

				if(possibleEvents.isEmpty() && minimalEvents.isEmpty()){
					runList.add(event);
					executeEvent(runList);

				}else{
					e.getEditor().requestFocus();
					ParallelSimDialog dialog = new ParallelSimDialog(this.getFramework().getMainWindow(), net, possibleEvents, minimalEvents, event, syncSet, enabledEvents, reverse);
					GUI.centerToParent(dialog, this.getFramework().getMainWindow());
					dialog.setVisible(true);

					runList.addAll(minimalEvents);
					runList.add(event);

					if (dialog.getRun() == 1){
						runList.addAll(dialog.getSelectedEvent());
						executeEvent(runList);
					}
					if(dialog.getRun()==2){
						simuAlg.clearAll();
						return;
						}
					}
				//Error tracing
			//	setErrNum(runList, reverse);
				simuAlg.clearAll();

			}else{
				//reverse simulation

				List<Event> possibleEvents = new ArrayList<Event>();
				for(Event psbE : enabledEvents)
					if(!minimalReverseEvents.contains(psbE))
						possibleEvents.add(psbE);

						minimalReverseEvents.remove(event);

				List<Event> runList = new ArrayList<Event>();

				if(possibleEvents.isEmpty() && minimalReverseEvents.isEmpty()){
					runList.add(event);
					executeEvent(runList);
					simuAlg.clearAll();
				} else {
					e.getEditor().requestFocus();
					ParallelSimDialog dialog = new ParallelSimDialog(this.getFramework().getMainWindow(), net, possibleEvents, minimalReverseEvents, event, syncSet, enabledEvents, reverse);

					GUI.centerToParent(dialog, this.getFramework().getMainWindow());
					dialog.setVisible(true);

					runList.addAll(minimalReverseEvents);
					runList.add(event);

					if (dialog.getRun() == 1){
						runList.addAll(dialog.getSelectedEvent());
						executeEvent(runList);
					}
					if(dialog.getRun()==2){
						simuAlg.clearAll();
						return;
					}
				}
				//Reverse error tracing
				//setErrNum(runList, reverse);
				simuAlg.clearAll();
			}
		}
	}

	private void setErrNum(List<Event> runList, boolean reverse){
		if (ErrTracingDisable.showErrorTracing()){
			Collection<Event> abstractEvents = new ArrayList<Event>();
			//get high level events
			for(Event absEvent : runList){
				for(ONGroup group : abstractGroups){
					if(group.getEvents().contains(absEvent))
						abstractEvents.add(absEvent);
				}
			}
			//get low level events
			runList.removeAll(abstractEvents);
			if(!reverse){
				errAlg.setErrNum(abstractEvents, syncSet, false);
				errAlg.setErrNum(runList, syncSet, true);
			}
			else{
				errAlg.setReverseErrNum(abstractEvents, syncSet, false);
				errAlg.setReverseErrNum(runList, syncSet, true);
			}

		}

	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		GUI.drawEditorMessage(editor, g, Color.BLACK, "Simulation: click on the highlighted transitions to fire them");
	}

	@Override
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

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				if(node instanceof VisualEvent) {
					Event event = ((VisualEvent)node).getReferencedEvent();
					String eventId = null;
					Node event2 = null;
					if (branchTrace!=null&&branchStep<branchTrace.size()) {
						Trace step = branchTrace.get(branchStep);
							if (step.contains(net.getName(event)))
								event2 = net.getNodeByReference(net.getName(event));

					} else if (branchTrace==null&&trace!=null&&traceStep<trace.size()) {
						eventId = trace.get(traceStep);
						event2 = net.getNodeByReference(eventId);
					}

					if (event==event2) {
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

					if (simuAlg.isEnabled(event, syncSet, phases)&& !reverse)
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
					if (simuAlg.isUnfireEnabled(event, syncSet, phases)&& reverse)
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
				return null;
			}
		};
	}

	public void setTrace(Trace t) {
		this.trace = t;
		this.traceStep = 0;
		this.branchTrace = null;
		this.branchStep = 0;
	}

	public Framework getFramework(){
		return this.framework;
	}

	public void setFramework(Framework framework){
		this.framework =framework;
	}

	public boolean isReverse(){
		return reverse;
	}

	public void setReverse(boolean reverse){
		this.reverse = reverse;
		updateState();
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

}
