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

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.TimeEstimatorSettings;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.TimeAlg;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualChannelPlace;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.plugins.son.elements.VisualPlaceNode;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.granularity.HourMins;
import org.workcraft.plugins.son.gui.GranularityPanel;
import org.workcraft.plugins.son.gui.TimeInputFilter;
import org.workcraft.plugins.son.gui.TimeEstimatorDialog;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeValueSetterTool extends AbstractTool {

    protected SON net;
    protected GraphEditor editor;
    protected VisualSON visualNet;

    private JPanel interfacePanel, timeInputPanel, timePropertyPanel, timeSetterPanel, buttonPanel;
    private GranularityPanel granularityPanel;
    private JButton estimatorButton, clearButton;

    private int labelheight = 20;
    private int labelwidth = 35;
    protected Dimension buttonSize = new Dimension(100, 25);
    private TimeEstimatorSettings settings;

    private Node selection = null;
    private Node visualSelection = null;
    private boolean visibility;
    private Color selectedColor = Color.ORANGE;
    private Font font = new Font("Arial", Font.PLAIN, 12);
    private String startLabel = "Start time interval: ";
    private String endLabel = "End time interval: ";
    private String durationLabel = "Duration interval: ";
    private String timeLabel = "Time interval: ";

    @Override
    public void createInterfacePanel(final GraphEditor editor) {
        super.createInterfacePanel(editor);

        //workcraft invoke this method before activate method
        visualNet = (VisualSON) editor.getModel();
        net = (SON) visualNet.getMathModel();
        this.editor = editor;

        createTimeSetterPanel();

        interfacePanel = new JPanel();
        interfacePanel.setLayout(new BorderLayout());
        interfacePanel.add(timeSetterPanel);
    }

    private void createTimeSetterPanel() {
        granularityPanel = new GranularityPanel(BorderFactory.createTitledBorder("Time Granularity"));

        timePropertyPanel = new JPanel();
        timePropertyPanel.setBorder(BorderFactory.createTitledBorder("Time value"));
        timePropertyPanel.setLayout(new WrapLayout());
        timePropertyPanel.setPreferredSize(new Dimension(1, labelheight * 6));

        createButtonPanel();

        timeSetterPanel = new JPanel();
        timeSetterPanel.setLayout(new BorderLayout());
        timeSetterPanel.add(granularityPanel, BorderLayout.NORTH);
        timeSetterPanel.add(timePropertyPanel, BorderLayout.CENTER);
        timeSetterPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createButtonPanel() {
        estimatorButton = new JButton("Estimate...");
        estimatorButton.setPreferredSize(buttonSize);
        estimatorButton.setEnabled(false);

        clearButton = new JButton("Clear");
        clearButton.setPreferredSize(buttonSize);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(estimatorButton);
        buttonPanel.add(clearButton);

        estimatorButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editor.requestFocus();
                Granularity g = granularityPanel.getSelection();
                TimeEstimatorDialog estimator = new TimeEstimatorDialog(editor, settings, selection, g);
                visualNet.setForegroundColor(selection, selectedColor);
                GUI.centerToParent(estimator, editor.getMainWindow());
                estimator.setVisible(true);
                if (estimator.getRun() == 1) {
                    updateTimePanel(editor, visualSelection);
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Interval interval = new Interval();
                if (visualSelection != null) {
                    if (visualSelection instanceof VisualComponent) {
                        if ((selection instanceof Time) && !(selection instanceof Event)) {
                            Time time = (Time) selection;
                            time.setDuration(interval);
                            time.setStartTime(interval);
                            time.setEndTime(interval);
                        }
                    } else if (visualSelection instanceof VisualSONConnection) {
                        ((SONConnection) selection).setTime(interval);
                    }
                    updateTimePanel(editor, visualSelection);
                }
            }
        });
    }

    private JPanel createTimeInputPanel(final String title, final Interval value, final Node node) {

        timeInputPanel = new JPanel();
        timeInputPanel.setLayout(new FlowLayout());

        JLabel label = new JLabel();
        label.setText(title);
        label.setFont(font);
        label.setPreferredSize(new Dimension(labelwidth * 3, labelheight));

        final JTextField min = new JTextField();
        min.setPreferredSize(new Dimension(labelwidth, labelheight));
        min.setText(value.minToString());
        ((AbstractDocument) min.getDocument()).setDocumentFilter(new TimeInputFilter());

        JLabel dash = new JLabel();
        dash.setText("-");

        final JTextField max = new JTextField();
        max.setText(value.maxToString());
        max.setPreferredSize(new Dimension(labelwidth, labelheight));
        ((AbstractDocument) max.getDocument()).setDocumentFilter(new TimeInputFilter());

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
                min.selectAll();
            }
        });

        min.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
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
                max.selectAll();
            }
        });

        max.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timeInputPanel.requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

        });
        return timeInputPanel;
    }

    private void setValue(Node node, String title, JTextField field, boolean isMin) {

        autoComplete(field);

        if (title.equals(timeLabel)) {
            setTimeLabelValue(node, field, isMin);
        } else if (title.equals(startLabel)) {
            setStartLabel(node, field, isMin);
        } else if (title.equals(durationLabel)) {
            setDurationLabel(node, field, isMin);
        } else if (title.equals(endLabel)) {
            setEndLabel(node, field, isMin);
        }
    }

    private void setTimeLabelValue(Node node, JTextField field, boolean isMin) {
        VisualSONConnection vcon = (VisualSONConnection) node;
        SONConnection con = vcon.getReferencedSONConnection();

        Interval value = con.getTime();
        if (isMin) {
            int min = Interval.getInteger(field.getText());
            //24 hour clock granularity checking
            if (granularityPanel.getHourMinsButton().isSelected()) {
                try {
                    HourMins.validValue(min);
                } catch (TimeOutOfBoundsException e) {
                    con.setTime(value);
                    field.setText(value.minToString());
                    return;
                }
            }
            Interval input = new Interval(min, value.getMax());
            if (isValid(input)) {
                con.setTime(input);
            } else {
                con.setTime(value);
                field.setText(value.minToString());
            }
        } else {
            int max = Interval.getInteger(field.getText());
            if (granularityPanel.getHourMinsButton().isSelected()) {
                try {
                    HourMins.validValue(max);
                } catch (TimeOutOfBoundsException e) {
                    con.setTime(value);
                    field.setText(value.maxToString());
                    return;
                }
            }
            Interval input = new Interval(value.getMin(), max);
            if (isValid(input)) {
                con.setTime(input);
            } else {
                con.setTime(value);
                field.setText(value.maxToString());
            }
        }
    }

    private void setStartLabel(Node node, JTextField field, boolean isMin) {
        VisualCondition vc = (VisualCondition) node;
        Condition c = (Condition) vc.getReferencedComponent();

        Interval value = c.getStartTime();
        if (isMin) {
            int min = Interval.getInteger(field.getText());
            //24 hour clock granularity checking
            if (granularityPanel.getHourMinsButton().isSelected()) {
                try {
                    HourMins.validValue(min);
                } catch (TimeOutOfBoundsException e) {
                    c.setStartTime(value);
                    field.setText(value.minToString());
                    return;
                }
            }
            Interval input = new Interval(min, value.getMax());
            if (isValid(input)) {
                c.setStartTime(input);
            } else {
                c.setStartTime(value);
                field.setText(value.minToString());
            }
        } else {
            int max = Interval.getInteger(field.getText());
            if (granularityPanel.getHourMinsButton().isSelected()) {
                try {
                    HourMins.validValue(max);
                } catch (TimeOutOfBoundsException e) {
                    c.setStartTime(value);
                    field.setText(value.maxToString());
                    return;
                }
            }
            Interval input = new Interval(value.getMin(), max);
            if (isValid(input)) {
                c.setStartTime(input);
            } else {
                c.setStartTime(value);
                field.setText(value.maxToString());
            }
        }

    }

    private void setEndLabel(Node node, JTextField field, boolean isMin) {
        VisualCondition vc = (VisualCondition) node;
        Condition c = (Condition) vc.getReferencedComponent();

        Interval value = c.getEndTime();
        if (isMin) {
            int min = Interval.getInteger(field.getText());
            //24 hour clock granularity checking
            if (granularityPanel.getHourMinsButton().isSelected()) {
                try {
                    HourMins.validValue(min);
                } catch (TimeOutOfBoundsException e) {
                    c.setEndTime(value);
                    field.setText(value.minToString());
                    return;
                }
            }
            Interval input = new Interval(min, value.getMax());
            if (isValid(input)) {
                c.setEndTime(input);
            } else {
                c.setEndTime(value);
                field.setText(value.minToString());
            }
        } else {
            int max = Interval.getInteger(field.getText());
            if (granularityPanel.getHourMinsButton().isSelected()) {
                try {
                    HourMins.validValue(max);
                } catch (TimeOutOfBoundsException e) {
                    c.setEndTime(value);
                    field.setText(value.maxToString());
                    return;
                }
            }
            Interval input = new Interval(value.getMin(), max);
            if (isValid(input)) {
                c.setEndTime(input);
            } else {
                c.setEndTime(value);
                field.setText(value.maxToString());
            }
        }

    }

    private void setDurationLabel(Node node, JTextField field, boolean isMin) {

        Interval value;
        if (node instanceof VisualPlaceNode) {
            VisualPlaceNode vc = (VisualPlaceNode) node;
            PlaceNode c = (PlaceNode) vc.getReferencedComponent();

            value = c.getDuration();
            if (isMin) {
                Interval input = new Interval(Interval.getInteger(field.getText()), value.getMax());
                if (isValid(input)) {
                    c.setDuration(input);
                } else {
                    c.setDuration(value);
                    field.setText(value.minToString());
                }
            } else {
                Interval input = new Interval(value.getMin(), Interval.getInteger(field.getText()));
                if (isValid(input)) {
                    c.setDuration(input);
                } else {
                    c.setDuration(value);
                    field.setText(value.maxToString());
                }
            }
        } else if (node instanceof VisualBlock) {
            VisualBlock vb = (VisualBlock) node;
            Block b = vb.getReferencedComponent();
            value = b.getDuration();

            if (isMin) {
                Interval input = new Interval(Interval.getInteger(field.getText()), value.getMax());
                if (isValid(input)) {
                    b.setDuration(input);
                } else {
                    b.setDuration(value);
                    field.setText(value.minToString());
                }
            } else {
                Interval input = new Interval(value.getMin(), Interval.getInteger(field.getText()));
                if (isValid(input)) {
                    b.setDuration(input);
                } else {
                    b.setDuration(value);
                    field.setText(value.maxToString());
                }
            }
        }
    }

    private void autoComplete(JTextField field) {
        String text = field.getText();
        int length = text.length();

        if (length < 4) {
            while (length < 4) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(text);
                text = sb.toString();
                field.setText(text);
                length = text.length();
            }
        }
    }

    private boolean isValid(Interval value) {
        int start = value.getMin();
        int end = value.getMax();

        if (start <= end) {
            return true;
        }
        return false;
    }

    private void updateTimePanel(final GraphEditor editor, Node node) {
        timePropertyPanel.removeAll();
        timePropertyPanel.revalidate();
        timePropertyPanel.repaint();

        Interval value;
        if (node instanceof VisualSONConnection) {
            VisualSONConnection vcon = (VisualSONConnection) node;
            SONConnection con = vcon.getReferencedSONConnection();

            if (con.getSemantics() == Semantics.PNLINE || con.getSemantics() == Semantics.ASYNLINE) {
                value = con.getTime();
                timePropertyPanel.add(createTimeInputPanel(timeLabel, value, node));
            }
        } else if (node instanceof VisualPlaceNode) {

            if (node instanceof VisualCondition) {
                VisualCondition vc2 = (VisualCondition) node;
                Condition c2 = (Condition) vc2.getReferencedComponent();

                if (c2.isInitial()) {
                    value = c2.getStartTime();
                    timePropertyPanel.add(createTimeInputPanel(startLabel, value, node));
                }
                if (c2.isFinal()) {
                    value = c2.getEndTime();
                    timePropertyPanel.add(createTimeInputPanel(endLabel, value, node));
                }
            }

            VisualPlaceNode vc = (VisualPlaceNode) node;
            PlaceNode c = (PlaceNode) vc.getReferencedComponent();

            value = c.getDuration();
            timePropertyPanel.add(createTimeInputPanel(durationLabel, value, node));
        } else if (node instanceof VisualBlock) {
            VisualBlock vb = (VisualBlock) node;
            Block b = vb.getReferencedComponent();

            value = b.getDuration();
            timePropertyPanel.add(createTimeInputPanel(durationLabel, value, node));
        }

        timePropertyPanel.revalidate();
        editor.requestFocus();
        editor.repaint();
    }

    @Override
    public void activated(final GraphEditor editor) {
        visualNet = (VisualSON) editor.getModel();
        net = (SON) visualNet.getMathModel();
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanSelect(false);
        settings = new TimeEstimatorSettings();

        net.refreshAllColor();
        net.clearMarking();

        //set property states for initial and final states
        TimeAlg.removeProperties(net);
        TimeAlg.setProperties(net);
        //save visibility state
        visibility = SONSettings.getTimeVisibility();
        //set visibility to true
        SONSettings.setTimeVisibility(true);

        editor.forceRedraw();
        editor.getModel().setTemplateNode(null);
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        if (!visibility) {
        	 TimeAlg.removeProperties(net);
        }
        SONSettings.setTimeVisibility(visibility);
        net.refreshAllColor();
        net.clearMarking();
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        net.refreshNodeColor();

        Node node = HitMan.hitTestForConnection(e.getPosition(), e.getModel().getRoot());
        if (node instanceof VisualSONConnection) {
            estimatorButton.setEnabled(false);
            VisualSONConnection con = (VisualSONConnection) node;
            selection = con.getReferencedConnection();
            visualSelection = node;
            if (con.getSemantics() == Semantics.PNLINE) {
                ((VisualSONConnection) node).setColor(selectedColor);
                updateTimePanel(e.getEditor(), node);
                net.setTimeColor(selection, Color.BLACK);
                return;
            }
        }

        Node node2 = HitMan.hitFirstNodeOfType(e.getPosition(), e.getModel().getRoot(), VisualBlock.class);
        if (node2 != null) {
            selection = ((VisualBlock) node2).getReferencedComponent();
            visualSelection = node2;
            if (((VisualBlock) node2).getIsCollapsed()) {
                estimatorButton.setEnabled(true);
                ((VisualBlock) node2).setForegroundColor(selectedColor);
                updateTimePanel(e.getEditor(), node2);
                net.setTimeColor(selection, Color.BLACK);
                return;
            }
        }

        Node node3 = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(),
                new Func<Node, Boolean>() {
                    @Override
                    public Boolean eval(Node node) {
                        return (node instanceof VisualPlaceNode) || (node instanceof VisualEvent);
                    }
                });
        if (node3 instanceof VisualPlaceNode || node3 instanceof VisualEvent) {
            if (!(node3 instanceof VisualChannelPlace)) {
                estimatorButton.setEnabled(true);
            }

            selection = ((VisualComponent) node3).getReferencedComponent();
            visualSelection = node3;
            ((VisualComponent) node3).setForegroundColor(selectedColor);
            updateTimePanel(e.getEditor(), node3);
            net.setTimeColor(selection, Color.BLACK);
        }
    }

    @Override
    public JPanel getInterfacePanel() {
        return interfacePanel;
    }

    @Override
    public String getLabel() {
        return "Time value setter";
    }

    @Override
    public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
        GUI.drawEditorMessage(editor, g, Color.BLACK, "Click on the node to set time value in tool controls panel.");
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
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                return null;

            }
        };
    }
}
