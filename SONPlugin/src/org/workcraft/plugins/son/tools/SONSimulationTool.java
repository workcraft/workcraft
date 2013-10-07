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
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.algorithm.RelationAlg;
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
	private RelationAlg relationAlg;
	protected SimulationAlg alg;

	protected SONModel net;
	protected Collection<ArrayList<Node>> sync;
	protected JPanel interfacePanel;
	protected JPanel controlPanel;
	protected JScrollPane infoPanel;
	protected JPanel statusPanel;
	protected JPanel simuControalPanel;
	protected JTable traceTable;

	protected JSlider speedSlider;
	protected JButton playButton, stopButton, backwardButton, forwardButton, reverseButton;
	protected JButton saveMarkingButton, loadMarkingButton;
	protected JComboBox<typeMode> typeCombo;

	protected Map<Node, Boolean>initialMarking = null;
	Map<Node, Boolean> savedMarking = null;
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

	public SONSimulationTool(){
		super();
		createInterface();
	}

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
					((Condition)c).setToken(marking.get((Condition)c));
				} else {
					//ExceptionDialog.show(null, new RuntimeException("Place "+p.toString()+" is not in the model"));
				}
			if(c instanceof ChannelPlace)
				if (net.getChannelPlace().contains(c)){
					((ChannelPlace)c).setToken(marking.get((ChannelPlace)c));
				}
		}
	}

	protected void update()
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
			List<Event> syncList = new ArrayList<Event>();

			Trace step = branchTrace.get(branchStep);
			for(int i =0; i<step.size(); i++){
				final Node event = net.getNodeByReference(step.get(i));
				if(event instanceof Event)
					syncList.add((Event)event);
			}
			if(!reverse)
				alg.fire(syncList);
			else
				alg.unFire(syncList);
			branchStep++;
			return true;
		}

		if (trace==null) return false;
		if (traceStep>=trace.size()) return false;

		List<Event> syncList = new ArrayList<Event>();

		Trace step = branchTrace.get(branchStep);
		for(int i =0; i<step.size(); i++){
			final Node event = net.getNodeByReference(step.get(i));
			if(event instanceof Event)
				syncList.add((Event)event);
		}

		if (syncList.isEmpty()) return false;

		if(!reverse)
			alg.fire(syncList);
		else
			alg.unFire(syncList);
		traceStep++;
		return true;
	}


	private boolean step() {
		boolean ret = quietStep();
		update();
		return ret;
	}

	private boolean quietStepBack() {
		if (branchTrace!=null&&branchStep>0) {
			List<Event> syncList = new ArrayList<Event>();
			Trace step = branchTrace.get(branchStep-1);

			for(int i =0; i<step.size(); i++){
				final Node event = net.getNodeByReference(step.get(i));
				if(event instanceof Event)
					syncList.add((Event)event);
			}

			if (syncList.isEmpty()) return false;
			branchStep--;
			if(!reverse)
				alg.unFire(syncList);
			else
				alg.fire(syncList);
			if (branchStep==0&&trace!=null) branchTrace=null;
			return true;
		}

		if (trace==null) return false;
		if (traceStep==0) return false;

		List<Event> syncList = new ArrayList<Event>();
		Trace step = branchTrace.get(branchStep-1);

		for(int i =0; i<step.size(); i++){
			final Node event = net.getNodeByReference(step.get(i));
			if(event instanceof Event)
				syncList.add((Event)event);
		}

		if (syncList.isEmpty()) return false;
		branchStep--;

		if(!reverse)
			alg.unFire(syncList);
		else
			alg.fire(syncList);
		return true;
	}

	private boolean stepBack() {
		boolean ret = quietStepBack();
		update();
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
		update();
	}

	protected Map<Node, Boolean> readMarking() {
		HashMap<Node, Boolean> result = new HashMap<Node, Boolean>();
		for (Condition c : net.getConditions()) {
			result.put(c, c.hasToken());
		}
		for(ChannelPlace cp : net.getChannelPlace()){
			result.put(cp, cp.hasToken());
		}
		return result;
	}

	protected Map<Node, Boolean> autoInitalMarking(){
		HashMap<Node, Boolean> result = new HashMap<Node, Boolean>();

		for (Condition c : net.getConditions()) {
			c.setToken(false);
			result.put(c, false);
		}
		for(ChannelPlace cp : net.getChannelPlace()){
			cp.setToken(false);
			result.put(cp, false);
		}

		for(Node c : relationAlg.getInitial(net.getComponents())){
			if(c instanceof Condition){
				result.put(c, true);
				((Condition) c).setToken(true);}
			if(c instanceof ChannelPlace){
				result.put(c, true);
				((ChannelPlace)c).setToken(true);}
		}
		return result;
	}

	protected Map<Node, Boolean> autoReverseInitalMarking(){
		HashMap<Node, Boolean> result = new HashMap<Node, Boolean>();

		for (Condition c : net.getConditions()) {
			c.setToken(false);
			result.put(c, false);
		}
		for(ChannelPlace cp : net.getChannelPlace()){
			cp.setToken(false);
			result.put(cp, false);
		}

		for(Node c : relationAlg.getFinal(net.getComponents())){
			if(c instanceof Condition){
				result.put(c, true);
				((Condition) c).setToken(true);}
			if(c instanceof ChannelPlace){
				result.put(c, true);
				((ChannelPlace)c).setToken(true);}
		}
		return result;
	}

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

	private void createSimuControalPanel(){
		simuControalPanel = new JPanel();

		typeCombo = new JComboBox<typeMode>();
		typeCombo.addItem(new typeMode(0, "Forward"));
		typeCombo.addItem(new typeMode(1, "Backward"));

		simuControalPanel.add(typeCombo);

	}

	private void createInterface(){
		playButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"), "Automatic trace playback");
		stopButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-stop.svg"), "Reset trace playback");
		backwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-backward.svg"), "Step backward");
		forwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-forward.svg"), "Step forward");
		reverseButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/son-reverse-simulation.svg"), "Reverse simulation");
		speedSlider = new JSlider(-1000, 1000, 0);
		speedSlider.setToolTipText("Simulation playback speed");
		loadMarkingButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-marking-load.svg"), "Load marking from memory");
		saveMarkingButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-marking-save.svg"), "Save marking to memory");
		createSimuControalPanel();

		int buttonWidth = (int)Math.round(playButton.getPreferredSize().getWidth() + 5);
		int buttonHeight = (int)Math.round(playButton.getPreferredSize().getHeight() + 5);
		Dimension panelSize = new Dimension(buttonWidth * 7, buttonHeight);

		JPanel simulationControl = new JPanel();
		simulationControl.setLayout(new FlowLayout());
		simulationControl.setPreferredSize(panelSize);
		simulationControl.setMaximumSize(panelSize);
	//	simulationControl.add(Box.createRigidArea(new Dimension(10, 0)));
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
				update();
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
				update();
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
					setReverse(!reverse);
					if(savedBranchTrace!=null)
						savedBranchTrace.clear();
					savedBranchStep = 0;
					savedStep = 0;
					reset();
					if(!reverse)
						initialMarking = autoInitalMarking();
					else
						initialMarking = autoReverseInitalMarking();
					update();
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
				update();
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
				update();
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

		return alg.getSyncCycles(nodes);
	}

	@Override
	public void activated(GraphEditor editor)
	{
		editor.getWorkspaceEntry().setCanModify(false);
		editor.getWorkspaceEntry().captureMemento();
		visualNet = editor.getModel();
		this.setFramework(editor.getFramework());
		net = (SONModel)visualNet.getMathModel();
		alg = new SimulationAlg(net);
		relationAlg = new RelationAlg(net);;
		initialMarking = autoInitalMarking();

		reverse=false;
		traceStep = 0;
		branchTrace = null;
		branchStep = 0;
		sync = getSyncCycles();
		update();
	}

	@Override
	public void deactivated(GraphEditor editor)
	{
		editor.getWorkspaceEntry().cancelMemento();
	}

	public void executeEvent(List<Event> syncList) {
		// otherwise form/use the branch trace
		if (branchTrace!=null&&branchStep<branchTrace.size()) {
			List<Event> list = new ArrayList<Event>();

			Trace step = branchTrace.get(branchStep);
			for(int i =0; i<step.size(); i++){
				final Node event = net.getNodeByReference(step.get(i));
				if(event instanceof Event)
					list.add((Event)event);
			}

			if (!list.isEmpty()&&syncList.containsAll(list)) {
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
		update();
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
					if(node instanceof VisualEvent && alg.isEnabled(((VisualEvent)node).getReferencedEvent(), sync) && !reverse){
						return true;
					}
					if(node instanceof VisualEvent && alg.isUnfireEnabled(((VisualEvent)node).getReferencedEvent(), sync) && reverse){
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
					if(alg.isUnfireEnabled(enable, sync))
						enabledEvents.add(enable);
				}
			else{
				for(Event enable : net.getEvents())
					if(alg.isEnabled(enable, sync))
						enabledEvents.add(enable);
				}

			List<Event> minimalEvents = alg.getMinimalExeResult(event, sync, enabledEvents);
			List<Event> minimalReverseEvents = alg.getMinimalReverseExeResult(event, sync, enabledEvents);

			if(!reverse){
				List<Event> possibleEvents = new ArrayList<Event>();
				for(Event psbE : enabledEvents)
					if(!minimalEvents.contains(psbE))
						possibleEvents.add(psbE);

				minimalEvents.remove(event);

				List<Event> syncList = new ArrayList<Event>();

				if(possibleEvents.isEmpty() && minimalEvents.isEmpty()){
					syncList.add(event);
					executeEvent(syncList);
					alg.clearEventSet();
					} else{
					ParallelSimDialog dialog = new ParallelSimDialog(this.getFramework().getMainWindow(), net, possibleEvents, minimalEvents, event, sync, enabledEvents, reverse);

					GUI.centerToParent(dialog, this.getFramework().getMainWindow());
					dialog.setVisible(true);

					syncList.addAll(minimalEvents);
					syncList.add(event);

					if (dialog.getRun() == 1){
						syncList.addAll(dialog.getSelectedEvent());
						executeEvent(syncList);
						alg.clearEventSet();
					}
					if(dialog.getRun()==2){
						alg.clearEventSet();
						return;
						}
					}
			}else{
				List<Event> possibleEvents = new ArrayList<Event>();
				for(Event psbE : enabledEvents)
					if(!minimalReverseEvents.contains(psbE))
						possibleEvents.add(psbE);

						minimalReverseEvents.remove(event);

				List<Event> syncList = new ArrayList<Event>();

				if(possibleEvents.isEmpty() && minimalReverseEvents.isEmpty()){
					syncList.add(event);
					executeEvent(syncList);
					alg.clearEventSet();
					} else{
					ParallelSimDialog dialog = new ParallelSimDialog(this.getFramework().getMainWindow(), net, possibleEvents, minimalReverseEvents, event, sync, enabledEvents, reverse);

					GUI.centerToParent(dialog, this.getFramework().getMainWindow());
					dialog.setVisible(true);

					syncList.addAll(minimalReverseEvents);
					syncList.add(event);

					if (dialog.getRun() == 1){
						syncList.addAll(dialog.getSelectedEvent());
						executeEvent(syncList);
						alg.clearEventSet();
					}
					if(dialog.getRun()==2){
						alg.clearEventSet();
						return;
					}
				}
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
		return GUI.createIconFromSVG("images/icons/svg/start-green.svg");
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	@Override
	public Decorator getDecorator() {
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

					if (alg.isEnabled(event, sync)&& !reverse)
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
					if (alg.isUnfireEnabled(event, sync)&& reverse)
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
		update();
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

}
