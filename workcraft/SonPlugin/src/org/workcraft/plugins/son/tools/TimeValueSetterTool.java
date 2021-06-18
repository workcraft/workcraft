package org.workcraft.plugins.son.tools;

import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.gui.tools.AbstractGraphEditorTool;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.TimeEstimatorSettings;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.TimeAlg;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.Event;
import org.workcraft.plugins.son.elements.*;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.granularity.HourMins;
import org.workcraft.plugins.son.gui.GranularityPanel;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.gui.TimeEstimatorDialog;
import org.workcraft.plugins.son.gui.TimeInputFilter;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;

public class TimeValueSetterTool extends AbstractGraphEditorTool {

    private JPanel timeInputPanel;
    private JPanel timePropertyPanel;
    private GranularityPanel granularityPanel;
    private JButton estimatorButton;
    private JPanel panel;

    private static final int labelheight = 20;
    private static final int labelwidth = 40;
    protected Dimension buttonSize = new Dimension(110, 25);
    private TimeEstimatorSettings settings;

    private Node selection = null;
    private Node visualSelection = null;
    private boolean visibility;
    private static final Color selectedColor = Color.ORANGE;
    private static final Font font = new Font("Arial", Font.PLAIN, 12);
    private static final String startLabel = "Start time interval: ";
    private static final String endLabel = "Finish time interval: ";
    private static final String durationLabel = "Duration interval: ";
    private static final String timeLabel = "Time interval: ";

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel != null) {
            return panel;
        }

        granularityPanel = new GranularityPanel(GuiUtils.getTitledBorder("Time Granularity"));

        timePropertyPanel = new JPanel(new WrapLayout());
        timePropertyPanel.setBorder(GuiUtils.getTitledBorder("Time value"));

        estimatorButton = new JButton("Estimate...");
        estimatorButton.setPreferredSize(buttonSize);
        estimatorButton.setEnabled(false);

        JButton clearButton = new JButton("Clear");
        clearButton.setPreferredSize(buttonSize);

        JPanel buttonPanel = new JPanel(new WrapLayout());
        buttonPanel.add(estimatorButton);
        buttonPanel.add(clearButton);

        estimatorButton.addActionListener(event -> {
            editor.requestFocus();
            editor.getWorkspaceEntry().saveMemento();
            Granularity g = granularityPanel.getSelection();
            final VisualSON visualNet = (VisualSON) editor.getModel();
            visualNet.setForegroundColor(selection, selectedColor);
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            TimeEstimatorDialog estimator = new TimeEstimatorDialog(mainWindow, editor, settings, selection, g);
            if (estimator.reveal()) {
                updateTimePanel(editor, visualSelection);
            }
        });

        clearButton.addActionListener(event -> {
            editor.getWorkspaceEntry().saveMemento();
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
        });

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(granularityPanel, BorderLayout.NORTH);
        panel.add(timePropertyPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    private void setValue(Node node, String title, JTextField field, boolean isMin) {
        autoComplete(field);
        if (timeLabel.equals(title)) {
            setTimeLabelValue(node, field, isMin);
        } else if (startLabel.equals(title)) {
            setStartLabel(node, field, isMin);
        } else if (durationLabel.equals(title)) {
            setDurationLabel(node, field, isMin);
        } else if (endLabel.equals(title)) {
            setEndLabel(node, field, isMin);
        }
    }

    private void setTimeLabelValue(Node node, JTextField field, boolean isMin) {
        VisualSONConnection vcon = (VisualSONConnection) node;
        SONConnection con = vcon.getReferencedSONConnection();

        Interval value = con.getTime();
        if (isMin) {
            int min = Interval.getInteger(field.getText());
            // 24 hour clock granularity checking
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
            // 24 hour clock granularity checking
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
            // 24 hour clock granularity checking
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
            PlaceNode c = vc.getReferencedComponent();

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
                timePropertyPanel.add(createTimeInputPanel(editor, timeLabel, value, node));
            }
        } else if (node instanceof VisualPlaceNode) {

            if (node instanceof VisualCondition) {
                VisualCondition vc2 = (VisualCondition) node;
                Condition c2 = (Condition) vc2.getReferencedComponent();

                if (c2.isInitial()) {
                    value = c2.getStartTime();
                    timePropertyPanel.add(createTimeInputPanel(editor, startLabel, value, node));
                }
                if (c2.isFinal()) {
                    value = c2.getEndTime();
                    timePropertyPanel.add(createTimeInputPanel(editor, endLabel, value, node));
                }
            }

            VisualPlaceNode vc = (VisualPlaceNode) node;
            PlaceNode c = (PlaceNode) vc.getReferencedComponent();

            value = c.getDuration();
            timePropertyPanel.add(createTimeInputPanel(editor, durationLabel, value, node));
        } else if (node instanceof VisualBlock) {
            VisualBlock vb = (VisualBlock) node;
            Block b = vb.getReferencedComponent();

            value = b.getDuration();
            timePropertyPanel.add(createTimeInputPanel(editor, durationLabel, value, node));
        }

        timePropertyPanel.revalidate();
        editor.requestFocus();
        editor.repaint();
    }

    private JPanel createTimeInputPanel(final GraphEditor editor, final String title,
            final Interval value, final Node node) {
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
                editor.getWorkspaceEntry().saveMemento();
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
                editor.getWorkspaceEntry().saveMemento();
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

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();
        settings = new TimeEstimatorSettings();
        net.refreshAllColor();
        net.clearMarking();

        // Set property states for initial and final states
        TimeAlg.removeProperties(net);
        TimeAlg.setProperties(net);
        // Save visibility state
        visibility = SONSettings.getTimeVisibility();
        // set visibility to true
        SONSettings.setTimeVisibility(true);
        editor.forceRedraw();
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();
        if (!visibility) {
            TimeAlg.removeProperties(net);
        }
        SONSettings.setTimeVisibility(visibility);
        net.refreshAllColor();
        net.clearMarking();
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(false);
        we.setCanSelect(false);
        we.setCanCopy(true);
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        final VisualSON visualNet = (VisualSON) e.getEditor().getModel();
        final SON net = visualNet.getMathModel();
        net.refreshNodeColor();
        Point2D position = e.getPosition();
        Container root = e.getModel().getRoot();

        Node node0 = HitMan.hitDeepest(position, root,
                node -> (node instanceof VisualPlaceNode) || (node instanceof VisualEvent));

        if ((node0 instanceof VisualPlaceNode) || (node0 instanceof VisualEvent)) {
            if (!(node0 instanceof VisualChannelPlace)) {
                estimatorButton.setEnabled(true);
            }

            selection = ((VisualComponent) node0).getReferencedComponent();
            visualSelection = node0;
            ((VisualComponent) node0).setForegroundColor(selectedColor);
            updateTimePanel(e.getEditor(), node0);
            net.setTimeColor(selection, Color.BLACK);
            return;
        }

        Node node1 = HitMan.hitDeepest(position, root, VisualSONConnection.class);
        if (node1 instanceof VisualSONConnection) {
            estimatorButton.setEnabled(false);
            VisualSONConnection con = (VisualSONConnection) node1;
            selection = con.getReferencedConnection();
            visualSelection = node1;
            if (con.getSemantics() == Semantics.PNLINE) {
                ((VisualSONConnection) node1).setColor(selectedColor);
                updateTimePanel(e.getEditor(), node1);
                net.setTimeColor(selection, Color.BLACK);
                return;
            }
        }

        Node node2 = HitMan.hitFirstChild(position, root, VisualBlock.class);
        if (node2 instanceof VisualBlock) {
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
    }

    @Override
    public String getLabel() {
        return "Time value setter";
    }

    @Override
    public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
        GuiUtils.drawEditorMessage(editor, g, Color.BLACK, "Click on the node to set time value in tool controls panel.");
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_T;
    }

    @Override
    public Icon getIcon() {
        return GuiUtils.createIconFromSVG("images/son-tool-time.svg");
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
