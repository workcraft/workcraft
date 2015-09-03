package org.workcraft.plugins.son.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.workcraft.dom.Node;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.son.BlockConnector;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.Scenario;
import org.workcraft.plugins.son.Step;
import org.workcraft.plugins.son.Trace;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.InvalidStructureException;
import org.workcraft.plugins.son.gui.SONGUI;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class ScenarioGenerator extends SONSimulationTool{

	protected JButton saveButton, loadButton, removeButton, resetButton;
	protected JToggleButton startButton;

	protected final Scenario scenario = new Scenario();
	private Color greyoutColor = Color.LIGHT_GRAY;

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);

		startButton = SONGUI.createIconToggleButton(GUI.createIconFromSVG("images/icons/svg/son-scenario-start.svg"), "Generate scenario");
		resetButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/son-scenario-reset.svg"), "Reset scenario");
		saveButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-marking-save.svg"), "Save scenario");
		loadButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-marking-load.svg"), "Load seleted scenario");
		removeButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/son-scenario-delete.svg"), "Remove seleted scenario");

		int buttonWidth = (int)Math.round(startButton.getPreferredSize().getWidth() + 5);
		int buttonHeight = (int)Math.round(startButton.getPreferredSize().getHeight() + 5);
		Dimension panelSize = new Dimension(buttonWidth * 6, buttonHeight);

		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		controlPanel.setPreferredSize(panelSize);
		controlPanel.add(new JSeparator());
		controlPanel.add(startButton);
		controlPanel.add(resetButton);
		controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		controlPanel.add(saveButton);
		controlPanel.add(loadButton);
		controlPanel.add(removeButton);

		traceTable = new JTable(new ScenarioTableModel());
		traceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		infoPanel = new JScrollPane(traceTable);
		infoPanel.setPreferredSize(new Dimension(1, 1));

		statusPanel = new JPanel();

		interfacePanel = new JPanel();
		BorderLayout layout = new BorderLayout();
		layout.setVgap(10);
		interfacePanel.setLayout(layout);

		interfacePanel.add(controlPanel, BorderLayout.PAGE_START);
		interfacePanel.add(infoPanel, BorderLayout.CENTER);
		interfacePanel.add(statusPanel, BorderLayout.PAGE_END);

		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(startButton.isSelected()){
					branchTrace.clear();
					net.clearMarking();
					net.refreshColor();
					scenarioGenerator(editor);
				}else{
					Step step = simuAlg.getEnabledNodes(sync, phases, isRev);
					setGrayout(step, greyoutColor);
					net.clearMarking();
				}
			}
		});

		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(getScenario().toString(net));
			}
		});

		traceTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = traceTable.getSelectedColumn();
				int row = traceTable.getSelectedRow();
				if (column == 1) {
					if (row < branchTrace.size()) {
						boolean work = true;
						while (work && (branchTrace.getPosition() > row)) {
							work = quietStepBack(editor);
						}
						while (work && (branchTrace.getPosition() < row)) {
							work = quietStep(editor);
						}
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
		traceTable.setDefaultRenderer(Object.class,	new TraceTableCellRendererImplementation());
	}

	@Override
	public void activated(final GraphEditor editor) {
		visualNet = (VisualSON)editor.getModel();
		net = (SON)visualNet.getMathModel();
		WorkspaceEntry we = editor.getWorkspaceEntry();
		BlockConnector.blockBoundingConnector(visualNet);
		we.setCanSelect(false);

		net.refreshColor();
		net.clearMarking();
		initialise();

		editor.forceRedraw();
		editor.getModel().setTemplateNode(null);
	}

	@Override
	public void deactivated(final GraphEditor editor) {
		BlockConnector.blockInternalConnector(visualNet);
	}

	protected void scenarioGenerator(final GraphEditor editor){
		applyMarking(initialMarking);
		scenario.add(getCurrentMarking());
		Step step = simuAlg.getEnabledNodes(sync, phases, false);
		setDecoration(step);
		try {
			autoSimulator(editor);
		} catch (InvalidStructureException e1) {
			errorMsg(e1.getMessage(), editor);
		}
	}

	@Override
	protected void autoSimulator(final GraphEditor editor) throws InvalidStructureException{

		if(!acyclicChecker()){
			startButton.setSelected(false);
			throw new InvalidStructureException("Cyclic structure error");
		}else{
			autoSimulationTask(editor);
		}

		Collection<Node> nodes = getScenario().getAllNodes();
		setGrayout(nodes, Color.BLACK);
	}

	@Override
	protected void autoSimulationTask(final GraphEditor editor){
		Step step = simuAlg.getEnabledNodes(sync, phases, false);

		if(step.isEmpty()){
			startButton.setSelected(false);
		}
		step = conflictfilter(step);
		if(!step.isEmpty()){
			scenario.add(step);
			step = simuAlg.getMinFire(step.iterator().next(), sync, step, false);
			executeEvents(editor, step);
			ArrayList<PlaceNode> marking = new ArrayList<PlaceNode>();
			marking.addAll(getCurrentMarking());
			marking.addAll(getSyncChannelPlaces(step));
			scenario.add(marking);
			autoSimulationTask(editor);
		}
	}

	private ArrayList<PlaceNode> getCurrentMarking(){
		ArrayList<PlaceNode> result = new ArrayList<PlaceNode>();
		for(PlaceNode c : readSONMarking().keySet()){
			if(c.isMarked())
				result.add(c);
		}
		return result;
	}

	//get channel places in a synchronous step.
	private Collection<ChannelPlace> getSyncChannelPlaces(Step step){
		HashSet<ChannelPlace> result = new HashSet<ChannelPlace>();
		for(TransitionNode e :step){
			for(SONConnection con : net.getSONConnections(e)){
				if(con.getSemantics() == Semantics.ASYNLINE || con.getSemantics() == Semantics.SYNCLINE){
					if(con.getFirst() == e)
						result.add((ChannelPlace)con.getSecond());
					else
						result.add((ChannelPlace)con.getFirst());
				}
			}
		}
		return result;
	}

	public void setGrayout(Collection<? extends Node> nodes, Color color){
		for(Node node : nodes){
			net.setForegroundColor(node, color);
			net.setTimeColor(node, color);
		}
	}

	private ArrayList<SONConnection> getPreConnections(ArrayList<PlaceNode> marking){
		ArrayList<SONConnection> result = new ArrayList<SONConnection>();
		Step step = getStep(branchTrace.get(branchTrace.getPosition()));
		for(PlaceNode c : marking){
			for(SONConnection con : net.getOutputSONConnections(c)){
				if(step.contains(con.getSecond()) && con.getSemantics() != Semantics.BHVLINE)
					result.add(con);
			}
		}

		return result;
	}

	private ArrayList<SONConnection> getPostConnections(ArrayList<PlaceNode> marking){
		ArrayList<SONConnection> result = new ArrayList<SONConnection>();
		Step step = getStep(branchTrace.get(branchTrace.getPosition()-1));
		for(PlaceNode c : marking){
			for(SONConnection con : net.getInputSONConnections(c)){
				if(step.contains(con.getFirst()) && con.getSemantics() != Semantics.BHVLINE)
					result.add(con);
			}
		}
		return result;
	}

	@Override
	protected void setDecoration(Step enabled){
		if(startButton.isSelected()){
			setGrayout(net.getNodes(), greyoutColor);
			for(TransitionNode e : enabled){
				e.setForegroundColor(CommonSimulationSettings.getEnabledForegroundColor());
			}
		}
	}

	@SuppressWarnings("serial")
	protected class ScenarioTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) return "Scenario";
			return "Trace";
		}

		@Override
		public int getRowCount() {
			return Math.max(scenario.size(), branchTrace.size());
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) {
				if (!scenario.isEmpty() && (row < scenario.size())) {
					return scenario.get(row);
				}
			} else {
				if (!branchTrace.isEmpty() && (row < branchTrace.size())) {
					return branchTrace.get(row);
					}
				}
			return "";
		}
	};

	public Scenario getScenario(){
		return scenario;
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	@Override
	public String getLabel() {
		return "Scenario Generator";
	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		GUI.drawEditorMessage(editor, g, Color.BLACK, "");
	}

	@Override
	public int getHotKeyCode() {
		return 0;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/son-scenario.svg");
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e){
		if(startButton.isSelected()){
			super.mousePressed(e);
		}
	}

	@Override
	public Decorator getDecorator(GraphEditor editor) {
		return new Decorator(){
			@Override
			public Decoration getDecoration(Node node) {
				return null;

			}
		};
	}
}
