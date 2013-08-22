package org.workcraft.plugins.son.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.workcraft.Framework;
import org.workcraft.Trace;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.SONModel;
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

	protected SONModel net;
	protected SimulationAlg alg;
	protected JPanel interfacePanel;
	protected JPanel controlPanel;
	protected JScrollPane infoPanel;
	protected JPanel statusPanel;
	protected JTable traceTable;

	private JSlider speedSlider;
	private JButton playButton, stopButton, backwardButton, forwardButton;
	private JButton copyTraceButton, pasteTracedButton;

	protected Map<Node, Integer> initialMarking;

	protected Trace branchTrace;
	protected int branchStep = 0;
	protected Trace trace;
	protected int traceStep = 0;

	final double DEFAULT_SIMULATION_DELAY = 0.3;
	final double EDGE_SPEED_MULTIPLIER = 10;

	private Timer timer = null;

	public SONSimulationTool(){
		super();
		createInterface();
	}

	private void applyMarking(Map<Node, Integer> marking)
	{
		for (Node c: marking.keySet()) {
			if(c instanceof Condition)
				if (net.getConditions().contains(c)) {
					((Condition)c).setTokens(marking.get((Condition)c));
				} else {
					//ExceptionDialog.show(null, new RuntimeException("Place "+p.toString()+" is not in the model"));
				}
			if(c instanceof ChannelPlace)
				if (net.getChannelPlace().contains(c)){
					((ChannelPlace)c).setTokens(marking.get((ChannelPlace)c));
				}
		}
	}

	protected void update()
	{
		if (timer == null) {
			playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"));
		} else {
			if (trace == null || traceStep == trace.size()) {
				playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"));
				timer.stop();
				timer = null;
			} else {
				playButton.setIcon(GUI.createIconFromSVG("images/icons/svg/simulation-pause.svg"));
				timer.setDelay(getAnimationDelay());
			}
		}

		playButton.setEnabled(trace != null && traceStep < trace.size());
		stopButton.setEnabled(trace != null || branchTrace != null);
		backwardButton.setEnabled(traceStep > 0 || branchStep > 0);
		forwardButton.setEnabled(branchTrace==null && trace != null && traceStep < trace.size() || branchTrace != null && branchStep < branchTrace.size());
		traceTable.tableChanged(new TableModelEvent(traceTable.getModel()));
	}

	private int getAnimationDelay()
	{
		return (int)(1000.0 * DEFAULT_SIMULATION_DELAY * Math.pow(EDGE_SPEED_MULTIPLIER, -speedSlider.getValue() / 1000.0));
	}

	private boolean quietStep(List<Event> syncList) {
		alg.fire(syncList, "s");
		branchStep++;
		return true;
		/*
		if (branchTrace!=null && branchStep < branchTrace.size()) {
			String eventId = branchTrace.get(branchStep);
			final Node event = net.getNodeByReference(eventId);

			if (event==null||!(event instanceof Event)) return false;
			if (!alg.isEnabled((Event)event)) return false;

			alg.fire(postList, "s");

			branchStep++;

			return true;
		}

		if (trace==null) return false;
		if (traceStep>=trace.size()) return false;

		String eventId = trace.get(traceStep);
		final Node event = net.getNodeByReference(eventId);
		if (event==null||!(event instanceof Event)) return false;
		if (!alg.isEnabled((Event)event)) return false;

		alg.fire(postList, "s");

		traceStep++;
		return true;
		*/
	}


	private boolean step(List<Event> postList) {
		boolean ret = quietStep(postList);
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

	protected Map<Node, Integer> readMarking() {
		HashMap<Node, Integer> result = new HashMap<Node, Integer>();
		for (Condition c : net.getConditions()) {
			result.put(c, c.getTokens());
		}
		for(ChannelPlace cp : net.getChannelPlace()){
			result.put(cp, cp.getTokens());
		}
		return result;
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

	private void createInterface(){
		playButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-play.svg"), "Automatic trace playback");
		stopButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-stop.svg"), "Reset trace playback");
		backwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-backward.svg"), "Step backward");
		forwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-forward.svg"), "Step forward");
		speedSlider = new JSlider(-1000, 1000, 0);
		speedSlider.setToolTipText("Simulation playback speed");
		copyTraceButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-trace-copy.svg"), "Copy trace to clipboard");
		pasteTracedButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-trace-paste.svg"), "Paste trace from clipboard");

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

		JPanel speedControl = new JPanel();
		speedControl.setLayout(new BorderLayout());
		speedControl.setPreferredSize(panelSize);
		speedControl.setMaximumSize(panelSize);
		speedControl.add(speedSlider, BorderLayout.CENTER);

		JPanel traceControl = new JPanel();
		traceControl.setLayout(new FlowLayout());
		traceControl.setPreferredSize(panelSize);
		traceControl.add(new JSeparator());
		traceControl.add(copyTraceButton);
		traceControl.add(pasteTracedButton);

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

		stopButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});

	}

	@Override
	public void activated(GraphEditor editor)
	{
		editor.getWorkspaceEntry().setCanUndoAndRedo(false);
		editor.getWorkspaceEntry().captureMemento();
		visualNet = editor.getModel();
		this.setFramework(editor.getFramework());
		net = (SONModel)visualNet.getMathModel();
		initialMarking = readMarking();
		traceStep = 0;
		branchTrace = null;
		branchStep = 0;
		update();
	}

	@Override
	public void deactivated(GraphEditor editor)
	{
		//editor.getWorkspaceEntry().cancelMemento();
		 reset();
	}
	/*
	public void executeEvent(Event e) {
		// if clicked on the trace event, do the step forward
		if (branchTrace==null&&trace!=null&&traceStep<trace.size()) {
			String eventId = trace.get(traceStep);
			Node event = net.getNodeByReference(eventId);
			if (event!=null&&event==e) {
				step();
				return;
			}
		}
		// otherwise form/use the branch trace
		if (branchTrace!=null&&branchStep<branchTrace.size()) {
			String eventId = branchTrace.get(branchStep);
			Node event = net.getNodeByReference(eventId);
			if (event!=null&&event==e) {
				step();
				return;
			}
		}

		if (branchTrace==null) branchTrace = new Trace();

		while (branchStep<branchTrace.size())
			branchTrace.remove(branchStep);

		branchTrace.add(net.getNodeReference(e));
		step();
		update();
		return;
	}
	*/
	public void executeEvent(List<Event> list) {
		step(list);
		update();
		return;
	}


	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		alg = new SimulationAlg(net);
		Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(), new Func<Node, Boolean>() {
				@Override
				public Boolean eval(Node node) {
					if(node instanceof VisualEvent && alg.isEnabled(((VisualEvent)node).getReferencedEvent())){
						return true;
					}
					return false;

				}
			});

		if (node instanceof VisualEvent){
			Event event = ((VisualEvent)node).getReferencedEvent();
			List<Event> possibleEvents = alg.getPossibleExeEvents(event);
			List<Event> minimalEvents = alg.getPreRelate(event);
			List<Event> syncList = new ArrayList<Event>();

			if(possibleEvents.isEmpty() && minimalEvents.isEmpty()){
				syncList.addAll(alg.getPreRelate((Event)event));
				syncList.add(event);
				executeEvent(syncList);
				alg.clearEventSet();
				}
			else{
				ParallelSimDialog dialog = new ParallelSimDialog(this.getFramework().getMainWindow(), net, possibleEvents, minimalEvents, event);

				GUI.centerToParent(dialog, this.getFramework().getMainWindow());
				dialog.setVisible(true);

				syncList.addAll(alg.getPreRelate((Event)event));
				syncList.add(event);

				if (dialog.getRun() == 1){
					syncList.addAll(dialog.getSelectedEvent());
					executeEvent(syncList);
					alg.clearEventSet();
				}
				if(dialog.getRun()==2){
					return;
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
		alg = new SimulationAlg(net);
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				if(node instanceof VisualEvent) {
					Event event = ((VisualEvent)node).getReferencedEvent();
					String eventId = null;
					Node event2 = null;
					if (branchTrace!=null&&branchStep<branchTrace.size()) {
						eventId = branchTrace.get(branchStep);
						event2 = net.getNodeByReference(eventId);
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

					if (alg.isEnabled(event))
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

	public Framework getFramework(){
		return this.framework;
	}

	public void setFramework(Framework framework){
		this.framework =framework;
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

}
