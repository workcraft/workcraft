package org.workcraft.plugins.son.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.BlockConnector;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.Scenario;
import org.workcraft.plugins.son.Step;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.TimeAlg;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualPlaceNode;
import org.workcraft.plugins.son.exception.InvalidStructureException;
import org.workcraft.plugins.son.gui.SONGUI;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeAnalysisTool extends SONSimulationTool{

	private JPanel interfacePanel, timePropertyPanel, timeInputPanel, buttonPanel;
	private JPanel scenarioPanel;
	private JButton clearButton;
	private JTabbedPane modeTabs;
	private JScrollPane tablePanel;

	private int labelheight = 20;
	private int labelwidth = 35;
	private Color selectedColor = new Color(255, 228, 181);
	private Font font = new Font("Arial", Font.PLAIN, 12);
	private String startLabel = "Start time interval: ";
	private String endLabel = "End time interval: ";
	private String durationLabel = "Duration interval: ";
	private String timeLabel = "Time interval: ";

	private Color greyoutColor = Color.LIGHT_GRAY;
	private Scenario scenario = new Scenario();
	private ArrayList<Node> syncSet = new ArrayList<Node>();

	protected Map<PlaceNode, Boolean> finalMarking;;

	//Set limit integers to JTextField
	class InputFilter extends DocumentFilter {

        private int maxLength;

        public InputFilter() {
            maxLength = 4; // The number of characters allowed
        }

        private boolean isInteger(String text) {
            try {
               Integer.parseInt(text);
               return true;
            } catch (NumberFormatException e) {
               return false;
            }
         }

        @Override
        public void insertString(FilterBypass fb, int offset, String string,
                AttributeSet attr) throws BadLocationException {

            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.insert(offset, string);

            if (doc.getLength() + string.length() <= maxLength
                    	&& isInteger(string)) {
                fb.insertString(offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length,
                String text, AttributeSet attrs) throws BadLocationException {

            Document doc = fb.getDocument();
            StringBuilder sb = new StringBuilder();
            sb.append(doc.getText(0, doc.getLength()));
            sb.replace(offset, offset + length, text);

            if (isInteger(sb.toString())
            		&& (doc.getLength() + text.length() - length) <= maxLength) {
                super.replace(fb, offset, length, text, attrs);
             }
        }
    }

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);

		this.editor = editor;
		timePropertyPanel = new JPanel();
		timePropertyPanel.setBorder(BorderFactory.createTitledBorder("Time value"));
		timePropertyPanel.setLayout(new WrapLayout());
		timePropertyPanel.setPreferredSize(new Dimension(0, 250));

		autoSimuButton = SONGUI.createIconToggleButton(GUI.createIconFromSVG("images/icons/svg/son-time-scenario.svg"), "Generate a scenario");

		int buttonWidth = (int)Math.round(autoSimuButton.getPreferredSize().getWidth() + 5);
		int buttonHeight = (int)Math.round(autoSimuButton.getPreferredSize().getHeight() + 5);
		Dimension panelSize = new Dimension(buttonWidth * 6, buttonHeight);

		traceTable = new JTable(new TraceTableModel());
		traceTable.setDefaultRenderer(Object.class,	new TraceTableCellRendererImplementation());

		tablePanel = new JScrollPane(traceTable);

		JPanel scenarioControl = new JPanel();
		scenarioControl.setLayout(new FlowLayout());
		scenarioControl.setPreferredSize(panelSize);
		scenarioControl.setMaximumSize(panelSize);
		scenarioControl.add(autoSimuButton);

		scenarioPanel = new JPanel();
		scenarioPanel.setLayout(new WrapLayout());
		scenarioPanel.setPreferredSize(new Dimension(0, 250));
		scenarioPanel.add(scenarioControl);
		scenarioPanel.add(tablePanel);

		modeTabs = new JTabbedPane();
		modeTabs.addTab("Setting", timePropertyPanel);
		modeTabs.addTab("Scenario", scenarioPanel);

		clearButton = new JButton("Output");
		clearButton.setPreferredSize(new Dimension(75,25));

		buttonPanel = new JPanel();
		buttonPanel.add(clearButton);

		interfacePanel = new JPanel();
		interfacePanel.setLayout(new BorderLayout());
		interfacePanel.add(modeTabs, BorderLayout.PAGE_START);
		interfacePanel.add(buttonPanel, BorderLayout.PAGE_END);

		clearButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(getScenario().toString(net));

			}
		});

		autoSimuButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(autoSimuButton.isSelected()){
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

		modeTabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				autoSimuButton.setSelected(false);
				Step step = simuAlg.getEnabledNodes(sync, phases, isRev);
				setGrayout(step, greyoutColor);
				net.clearMarking();
			}
		});
	}

	private JPanel createTimeInputPanel(final String title, final String value, final Node node){
		String start = value.substring(0, 4);
		String end = value.substring(5, 9);

		timeInputPanel = new JPanel();
		timeInputPanel.setLayout(new FlowLayout());

		JLabel label = new JLabel();
		label.setText(title);
		label.setFont(font);
		label.setPreferredSize(new Dimension(labelwidth * 3, labelheight));

		final JTextField min = new JTextField();
		min.setPreferredSize(new Dimension(labelwidth, labelheight));
		min.setText(start);
		((AbstractDocument) min.getDocument()).setDocumentFilter(new InputFilter());

		JLabel dash = new JLabel();
		dash.setText("-");

		final JTextField max = new JTextField();
		max.setText(end);
		max.setPreferredSize(new Dimension(labelwidth, labelheight));
		((AbstractDocument) max.getDocument()).setDocumentFilter(new InputFilter());


		timeInputPanel.add(label);
		timeInputPanel.add(min);
		timeInputPanel.add(dash);
		timeInputPanel.add(max);

		min.addFocusListener(new FocusListener() {
			@Override
	        public void focusLost(FocusEvent e) {
				setValue(node, title, min, true);
	        }

			@Override
			public void focusGained(FocusEvent e) {
			}
	      });

		min.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER){
					timeInputPanel.requestFocus();
			    }
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

		});

		max.addFocusListener(new FocusListener() {
			@Override
	        public void focusLost(FocusEvent e) {
				setValue(node, title, max, false);
	        }

			@Override
			public void focusGained(FocusEvent e) {
			}
	      });

		max.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER){
					timeInputPanel.requestFocus();
			    }
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

		});
		return timeInputPanel;
	}

	private void setValue(Node node, String title, JTextField field, boolean isMin){

		autocomplete(field);

		if(title.equals(timeLabel)){
			VisualSONConnection con = (VisualSONConnection)node;
			String value = con.getTime();
			if(isMin){
				String input = field.getText() + value.substring(4,9);
				if(compare(input)){
					con.setTime(input);
				}else{
					con.setTime(value);
					field.setText(value.substring(0,4));
				}
			}else{
				String input = value.substring(0,5) + field.getText();
				if(compare(input)){
					con.setTime(input);
				}else{
					con.setTime(value);
					field.setText(value.substring(5,9));
				}
			}
		}
		else if(title.equals(startLabel)){
			VisualCondition c = (VisualCondition)node;
			String value = c.getStartTime();
			if(isMin){
				String input = field.getText() + value.substring(4,9);
				if(compare(input)){
					c.setStartTime(input);
				}else{
					c.setStartTime(value);
					field.setText(value.substring(0,4));
				}
			}else{
				String input = value.substring(0,5) + field.getText();
				if(compare(input)){
					c.setStartTime(input);
				}else{
					c.setStartTime(value);
					field.setText(value.substring(5,9));
				}
			}
		}
		else if(title.equals(durationLabel)){
			String value = "";
			if(node instanceof VisualPlaceNode){
				VisualPlaceNode c = (VisualPlaceNode)node;
				value = c.getDuration();
				if(isMin){
					String input = field.getText() + value.substring(4,9);
					if(compare(input)){
						c.setDuration(input);
					}else{
						c.setDuration(value);
						field.setText(value.substring(0,4));
					}
				}else{
					String input = value.substring(0,5) + field.getText();
					if(compare(input)){
						c.setDuration(input);
					}else{
						c.setDuration(value);
						field.setText(value.substring(5,9));
					}
				}
			}
			else if(node instanceof VisualBlock){
				VisualBlock b = (VisualBlock)node;
				value = b.getDuration();

				if(isMin){
					String input = field.getText() + value.substring(4,9);
					if(compare(input)){
						b.setDuration(input);
					}else{
						b.setDuration(value);
						field.setText(value.substring(0,4));
					}
				}else{
					String input = value.substring(0,5) + field.getText();
					if(compare(input)){
						b.setDuration(input);
					}else{
						b.setDuration(value);
						field.setText(value.substring(5,9));
					}
				}
			}
		}

		else if(title.equals(endLabel)){
			VisualCondition c = (VisualCondition)node;
			String value = c.getEndTime();
			if(isMin){
				String input = field.getText() + value.substring(4,9);
				if(compare(input)){
					c.setEndTime(input);
				}else{
					c.setEndTime(value);
					field.setText(value.substring(0,4));
				}
			}else{
				String input = value.substring(0,5) + field.getText();
				if(compare(input)){
					c.setEndTime(input);
				}else{
					c.setEndTime(value);
					field.setText(value.substring(5,9));
				}
			}
		}
	}

	private void autocomplete(JTextField field){
		String text = field.getText();
		int length = text.length();

		if(length < 4){
		   while (length < 4) {
		    StringBuffer sb = new StringBuffer();
		    sb.append("0").append(text);
		    text = sb.toString();
		    field.setText(text);
		    length = text.length();
		   }
		}
	}

	private boolean compare(String value){
		int start = TimeAlg.getMinTime(value);
		int end = TimeAlg.getMaxTime(value);

		if(start <= end){
			return true;
		}
		return false;
	}

	private void updateTimePanel(final GraphEditor editor, Node node){
		timePropertyPanel.removeAll();
		timePropertyPanel.revalidate();
		timePropertyPanel.repaint();

		String value = "";
		if(node instanceof VisualSONConnection){
			VisualSONConnection con = (VisualSONConnection)node;
			if(con.getSemantics()==Semantics.PNLINE || con.getSemantics() == Semantics.ASYNLINE){
				value = ((VisualSONConnection)node).getTime();
				timePropertyPanel.add(createTimeInputPanel(timeLabel, value, node));
			}
		}
		else if(node instanceof VisualPlaceNode){

			if(node instanceof VisualCondition){
				VisualCondition c2 = (VisualCondition)node;

				if(c2.isInitial()){
					value = c2.getStartTime();
					timePropertyPanel.add(createTimeInputPanel(startLabel, value, node));
				}
				if(c2.isFinal()){
					value = c2.getEndTime();
					timePropertyPanel.add(createTimeInputPanel(endLabel, value, node));
				}
			}

			VisualPlaceNode c = (VisualPlaceNode)node;
			value =c.getDuration();
			timePropertyPanel.add(createTimeInputPanel(durationLabel, value, node));
		}
		else if(node instanceof VisualBlock){
			VisualBlock b = (VisualBlock)node;
			value =b.getDuration();
			timePropertyPanel.add(createTimeInputPanel(durationLabel, value, node));
		}

		timePropertyPanel.revalidate();
		editor.requestFocus();
		editor.repaint();
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
		SONSettings.setTimeVisibility(true);

		//set property states for initial and final states
		removeProperties();
		finalMarking = simuAlg.getFinalMarking();
		setProperties();

		editor.forceRedraw();
		editor.getModel().setTemplateNode(null);
	}

	@Override
	public void deactivated(final GraphEditor editor) {
		removeProperties();
		SONSettings.setTimeVisibility(false);
		BlockConnector.blockInternalConnector(visualNet);
		net.refreshColor();
		net.clearMarking();
		scenario.clear();
		syncSet.clear();
		mainTrace.clear();
		branchTrace.clear();
	}

	private void setProperties(){
		for(PlaceNode c : initialMarking.keySet()){
			if((c instanceof Condition) && initialMarking.get(c))
				((Condition)c).setInitial(true);
		}
		for(PlaceNode c : finalMarking.keySet()){
			if((c instanceof Condition) && finalMarking.get(c))
				((Condition)c).setFinal(true);
		}
	}

	private void removeProperties(){
		for(PlaceNode c : net.getPlaceNodes()){
			if(c instanceof Condition){
				((Condition)c).setInitial(false);
				((Condition)c).setFinal(false);
			}
		}
	}

	protected void scenarioGenerator(final GraphEditor editor){
		applyMarking(initialMarking);
		Step step = simuAlg.getEnabledNodes(sync, phases, isRev);
		setDecoration(step);

		try {
			autoSimulator(editor);
		} catch (InvalidStructureException e1) {
			errorMsg(e1.getMessage(), editor);
		}
	}

	protected void autoSimulator(final GraphEditor editor) throws InvalidStructureException{
		super.autoSimulator(editor);
		Collection<Node> nodes = getScenario().getAllNodes();
		nodes.addAll(syncSet);
		setGrayout(nodes, Color.BLACK);
	}

	@Override
	protected void autoSimulationTask(final GraphEditor editor){
		Step step = simuAlg.getEnabledNodes(sync, phases, isRev);
		if(step.isEmpty()){
			autoSimuButton.setSelected(false);
		}
		step = conflictfilter(step);
		if(!step.isEmpty()){
			step = simuAlg.getMinFire(step.iterator().next(), sync, step, isRev);
			executeEvents(editor, step);
			autoSimulationTask(editor);
		}
	}

	@Override
	protected boolean step(final GraphEditor editor) {
		boolean ret = quietStep(editor);
		return ret;
	}

	private ArrayList<PlaceNode> getCurrentMarking(){
		ArrayList<PlaceNode> result = new ArrayList<PlaceNode>();
		for(PlaceNode c : readSONMarking().keySet()){
			if(c.isMarked())
				result.add(c);
		}
		return result;
	}

	public Scenario getScenario(){
		scenario.clear();
		syncSet.clear();
		net.clearMarking();
		if(branchTrace.isEmpty())
			return scenario;
		branchTrace.setPosition(0);
		applyMarking(initialMarking);

		boolean work = true;
		while (work && branchTrace.canProgress()) {
			ArrayList<PlaceNode> marking = getCurrentMarking();
			Step step = getStep(branchTrace.get(branchTrace.getPosition()));
			scenario.add(marking);
			scenario.add(getPreConnections(marking));
			scenario.add(step);
			if(step.size() > 1)
				syncSet.addAll(getSyncSet(step));
			work = quietStep(editor);
			marking = getCurrentMarking();
			scenario.add(getPostConnections(marking));
		}
		scenario.add(getCurrentMarking());
		if(!autoSimuButton.isSelected()){
			net.clearMarking();
		}
		return scenario;
	}

	//get related communication connections and channel places among a synchronous step.
	private ArrayList<Node> getSyncSet(Step step){
		ArrayList<Node> result = new ArrayList<Node>();
		for(TransitionNode e :step){
			Collection<SONConnection> outputs = net.getSONConnections(e);
			for(SONConnection con : outputs){
				if(con.getSemantics() == Semantics.ASYNLINE || con.getSemantics() == Semantics.SYNCLINE){
					ChannelPlace cp = null;
					if(con.getFirst() instanceof ChannelPlace){
						cp = (ChannelPlace)con.getFirst();
					}else{
						cp = (ChannelPlace)con.getSecond();
					}
					Collection<SONConnection> outputs2 = net.getSONConnections(cp);
					for(SONConnection con2 : outputs2){
						Node first = con2.getFirst();
						Node second = con2.getSecond();
						if((step.contains(first) && first !=e) || (step.contains(second) && second != e)){
							result.add(con);
							result.add(cp);
							result.add(con2);
						}
					}
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

	@Override
	protected void setDecoration(Step enabled){
		if(autoSimuButton.isSelected()){
			setGrayout(net.getNodes(), greyoutColor);
			for(TransitionNode e : enabled){
				e.setForegroundColor(CommonSimulationSettings.getEnabledForegroundColor());
			}
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
	public void mousePressed(GraphEditorMouseEvent e){
		if(modeTabs.getSelectedIndex() == 0){
			for(Node node : net.getComponents()){
				net.setFillColor(node, CommonVisualSettings.getFillColor());
			}
			Node node = HitMan.hitTestForConnection(e.getPosition(), e.getModel().getRoot());
			if( node instanceof VisualSONConnection){
				VisualSONConnection con = (VisualSONConnection)node;
				if(con.getSemantics()==Semantics.PNLINE || con.getSemantics() == Semantics.ASYNLINE){
					//((VisualSONConnection) node).setColor(selectedColor);
					updateTimePanel(e.getEditor(), node);
					return;
				}
			}

			Node node2 = HitMan.hitFirstNodeOfType(e.getPosition(), e.getModel().getRoot(), VisualBlock.class);
			if(node2 != null){
				if(((VisualBlock)node2).getIsCollapsed()){
					((VisualBlock) node2).setFillColor(selectedColor);
					updateTimePanel(e.getEditor(), node2);
					return;
				}
			}

			Node node3 = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(),
					new Func<Node, Boolean>() {
						@Override
						public Boolean eval(Node node) {
							return node instanceof VisualPlaceNode;
						}
					});
				if (node3 instanceof VisualPlaceNode) {
					((VisualPlaceNode) node).setFillColor(selectedColor);
					updateTimePanel(e.getEditor(), node3);
				}
		}else{
			if(autoSimuButton.isSelected()){
				super.mousePressed(e);
			}
		}
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	@Override
	public String getLabel() {
		return "Time analysis";
	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		GUI.drawEditorMessage(editor, g, Color.BLACK, "Click on the condition or connection to set time value.");
	}

	@Override
	public int getHotKeyCode() {
		return KeyEvent.VK_T;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/son-time.svg");
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
