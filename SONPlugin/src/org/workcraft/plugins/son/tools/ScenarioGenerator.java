package org.workcraft.plugins.son.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.workcraft.dom.Node;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.son.BlockConnector;
import org.workcraft.plugins.son.MarkingRef;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.Scenario;
import org.workcraft.plugins.son.StepExecution;
import org.workcraft.plugins.son.Step;
import org.workcraft.plugins.son.StepRef;
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

	protected JButton saveButton, removeButton, resetButton, importButton, exportButton;
	protected JToggleButton startButton;
	protected JTable scenarioTable;

	protected final StepExecution stepExecution = new StepExecution();
	protected Scenario scenario = new Scenario();
	protected SaveList saveList = new SaveList();
	private Color greyoutColor = Color.LIGHT_GRAY;
	private boolean setCellColor = true;

	public class SaveList extends ArrayList<Scenario>{
		private static final long serialVersionUID = 1L;
		private int position = 0;

		public int getPosition() {
			return position;
		}

		public void setPosition(int value) {
			position = Math.min(Math.max(0, value), size());
		}

		public void incPosition(int value) {
			setPosition(position + value);
		}

		public void decPosition(int value) {
			setPosition(position - value);
		}
	}

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);

		startButton = SONGUI.createIconToggleButton(GUI.createIconFromSVG("images/icons/svg/son-scenario-start.svg"), "Generate scenario");
		resetButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/son-scenario-reset.svg"), "Reset scenario");
		saveButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/simulation-marking-save.svg"), "Save scenario");
		removeButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/son-scenario-delete.svg"), "Remove scenario");
		importButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/son-scenario-import.svg"), "Import scenarios");
		exportButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/son-scenario-export.svg"), "Export scenarios");

		int buttonWidth = (int)Math.round(startButton.getPreferredSize().getWidth() + 5);
		int buttonHeight = (int)Math.round(startButton.getPreferredSize().getHeight() + 5);
		Dimension panelSize = new Dimension(buttonWidth * 6, buttonHeight);

		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		controlPanel.setPreferredSize(panelSize);
		controlPanel.add(new JSeparator());
		controlPanel.add(startButton);
		controlPanel.add(resetButton);
		controlPanel.add(saveButton);
		controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		controlPanel.add(importButton);
		controlPanel.add(exportButton);
		controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		controlPanel.add(removeButton);

		scenarioTable = new JTable(new ScenarioTableModel());
		scenarioTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


		infoPanel = new JScrollPane(scenarioTable);
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
					start();
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
				startButton.setSelected(true);
				start();
			}
		});

		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!scenario.isEmpty()){
					setCellColor = true;
					Scenario cache = new Scenario();
					cache.addAll(scenario);
					saveList.add(cache);
					saveList.setPosition(saveList.size()-1);
					updateState(editor);
				}
			}
		});

		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!saveList.isEmpty()){
					setCellColor = true;
					saveList.remove(saveList.getPosition());
					scenario.clear();
					if(saveList.getPosition() > saveList.size()-1)
						saveList.decPosition(1);
					if(!saveList.isEmpty())
						scenario.addAll(saveList.get(saveList.getPosition()));
					updateColor();
					updateState(editor);
				}
			}
		});

		scenarioTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = scenarioTable.getSelectedColumn();
				int row = scenarioTable.getSelectedRow();

				if (column == 0 && row < saveList.size()) {
					saveList.setPosition(row);
					Object obj = scenarioTable.getValueAt(row, column);
					if(obj instanceof Scenario){
						startButton.setSelected(false);
						setCellColor = true;
						scenario.clear();
						scenario.addAll((Scenario)obj);
						updateState(editor);
						updateColor();
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
		scenarioTable.setDefaultRenderer(Object.class,	new ScenarioTableCellRendererImplementation());
	}

	private void start(){
		if(!acyclicChecker()){
			startButton.setSelected(false);
			startButton.repaint();
			try {
				throw new InvalidStructureException("Cyclic structure error");
			} catch (InvalidStructureException e1) {
				errorMsg(e1.getMessage(), editor);
			}
		}else{
			scenario.clear();
			net.clearMarking();
			net.refreshColor();
			setCellColor = false;
			saveList.setPosition(0);
			scenarioGenerator(editor);
		}

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
		scenario.clear();
		net.clearMarking();
	}

	protected void scenarioGenerator(final GraphEditor editor){
		applyMarking(initialMarking);

		MarkingRef markingRef = new MarkingRef();
		markingRef.addAll(net.getNodeRefs(getCurrentMarking()));
		stepExecution.add(markingRef);
		scenario.addAll(markingRef);
		updateState(editor);

		Step step = simuAlg.getEnabledNodes(sync, phases, false);
		setDecoration(step);
		autoSimulator(editor);
	}

	@Override
	protected void autoSimulator(final GraphEditor editor){
		autoSimulationTask(editor);
		Collection<Node> nodes = new ArrayList<Node>();
		nodes.addAll(getScenario().getNodes(net));
		nodes.addAll(getScenario().getConnections(net));
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
			step = simuAlg.getMinFire(step.iterator().next(), sync, step, false);
			executeEvents(editor, step);
			autoSimulationTask(editor);
		}
	}

	@Override
	public void updateState(final GraphEditor editor) {
		scenarioTable.tableChanged(new TableModelEvent(scenarioTable.getModel()));
	}

	public void updateColor(){
		net.clearMarking();
		setGrayout(net.getNodes(), greyoutColor);
		Collection<Node> nodes = new ArrayList<Node>();
		nodes.addAll(getScenario().getNodes(net));
		nodes.addAll(getScenario().getConnections(net));
		setGrayout(nodes, Color.BLACK);
	}

	@SuppressWarnings("serial")
	protected class ScenarioTableCellRendererImplementation implements TableCellRenderer {

		JLabel label = new JLabel () {
			@Override
			public void paint( Graphics g ) {
				g.setColor( getBackground() );
				g.fillRect( 0, 0, getWidth() - 1, getHeight() - 1 );
				super.paint( g );
			}
		};

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus,int row, int column) {
			if(value instanceof String)
				label.setText(((String)value));
			else if(value instanceof Scenario){
				label.setText("Senario "+(row+1));
			}
			else
				return null;

			if (row == saveList.getPosition() && column == 0 && !saveList.isEmpty() && setCellColor) {
				label.setBackground(Color.PINK);
			} else {
				label.setBackground(Color.WHITE);
			}

			return label;
		}

	}

	@Override
	public void executeEvents(final GraphEditor editor, Step step) {
		ArrayList<PlaceNode> oldMarking = new ArrayList<PlaceNode>();
		oldMarking.addAll(getCurrentMarking());
		setCellColor = false;
		super.executeEvents(editor, step);

		//add step references
		StepRef stepRef = new StepRef();
		stepRef.addAll(net.getNodeRefs(step));
		stepExecution.add(stepRef);
		scenario.addAll(stepRef);

		//add marking references
		MarkingRef markingRef = new MarkingRef();
		ArrayList<PlaceNode> marking = new ArrayList<PlaceNode>();
		marking.addAll(getCurrentMarking());
		marking.addAll(getSyncChannelPlaces(step));
		markingRef.addAll(net.getNodeRefs(marking));
		stepExecution.add(markingRef);
		for(String str : markingRef){
			if(!scenario.contains(str))
				scenario.add(str);
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
		}
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
			if (column == 0) return "Save List";
			return "Scenario";
		}

		@Override
		public int getRowCount() {
			return Math.max(saveList.size(), scenario.size());
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) {
				if (!saveList.isEmpty() && (row < saveList.size())) {
					return saveList.get(row);
				}
			} else {
				if (!scenario.isEmpty() && (row < scenario.size())) {
					return scenario.get(row);
					}
				}
			return "";
		}
	};

	public StepExecution getStepExecution(){
		return stepExecution;
	}

	public Scenario getScenario(){
		return scenario;
	}

	public ArrayList<Scenario> getSaveList(){
		return saveList;
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
			autoSimulator(editor);
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
