package org.workcraft.plugins.son.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.TimeAlg;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualPlaceNode;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeAnalysisTool extends AbstractTool{

	private SON net;
	protected VisualSON visualNet;

	private TimeAlg timeAlg;

	private JPanel interfacePanel, timePropertyPanel, timeInputPanel;


	private int labelheight = 20;
	private int labelwidth = 35;
	private Font font = new Font("Arial", Font.PLAIN, 12);
	private String startLabel = "Start time interval: ";
	private String endLabel = "End time interval: ";
	private String durationLabel = "Duration interval: ";
	private String timeLabel = "Time interval: ";

	protected Map<PlaceNode, Boolean> initialMarking = new HashMap<PlaceNode, Boolean>();
	protected Map<PlaceNode, Boolean> finalMarking = new HashMap<PlaceNode, Boolean>();

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
		interfacePanel = new JPanel();
		timePropertyPanel = new JPanel();
		timePropertyPanel.setBorder(BorderFactory.createTitledBorder("Time property setting"));
		timePropertyPanel.setLayout(new WrapLayout());
		timePropertyPanel.setPreferredSize(new Dimension(300, 150));
		interfacePanel.add(timePropertyPanel);
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
		dash.setText("йд");

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
				con.setTime(input);
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
				c.setStartTime(input);
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
			VisualCondition c = (VisualCondition)node;
			String value = c.getDuration();
			if(isMin){
				String input = field.getText() + value.substring(4,9);
				c.setDuration(input);
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
		else if(title.equals(endLabel)){
			VisualCondition c = (VisualCondition)node;
			String value = c.getEndTime();
			if(isMin){
				String input = field.getText() + value.substring(4,9);
				c.setEndTime(input);
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
		String start = value.substring(0, 4);
		String end = value.substring(5, 9);

		try {
			 int s = Integer.parseInt(start);
			 int e = Integer.parseInt(end);
			 if (s <= e)
				 return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

	private void updatePanel(final GraphEditor editor, Node node){
		timePropertyPanel.removeAll();
		timePropertyPanel.revalidate();
		timePropertyPanel.repaint();

		String value = "";
		if(node instanceof VisualSONConnection){
			value = ((VisualSONConnection)node).getTime();
			timePropertyPanel.add(createTimeInputPanel(timeLabel, value, node));
		}
		else if(node instanceof VisualPlaceNode){
			VisualPlaceNode c = (VisualPlaceNode)node;
			value =c.getDuration();
			timePropertyPanel.add(createTimeInputPanel(durationLabel, value, node));

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
		}

		timePropertyPanel.revalidate();
		editor.requestFocus();
		editor.repaint();
	}

	@Override
	public void activated(final GraphEditor editor) {
		visualNet = (VisualSON)editor.getModel();
		net = (SON)visualNet.getMathModel();
		timeAlg = new TimeAlg(net);
		WorkspaceEntry we = editor.getWorkspaceEntry();

		SONSettings.setTimeVisibility(true);
		we.setCanSelect(false);

		//set property states for initial and final states
		removeProperties();
		initialMarking = timeAlg.getInitialMarking();
		finalMarking = timeAlg.getFinalMarking();
		setProperties();

		super.activated(editor);
	}

	@Override
	public void deactivated(final GraphEditor editor) {
		super.deactivated(editor);

		removeProperties();
		SONSettings.setTimeVisibility(false);
		this.visualNet = null;
		this.net = null;
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

	@Override
	public void mousePressed(GraphEditorMouseEvent e){
		Node node = HitMan.hitTestForConnection(e.getPosition(), e.getModel().getRoot());
		if( node instanceof VisualSONConnection){
			updatePanel(e.getEditor(), node);
		}

		Node node2 = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(),
				new Func<Node, Boolean>() {
					@Override
					public Boolean eval(Node node) {
						return node instanceof VisualPlaceNode;
					}
				});

			if (node2 instanceof VisualPlaceNode) {
				updatePanel(e.getEditor(), node);
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
