/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.graph.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.workcraft.Trace;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.gui.propertyeditor.PropertyEditorTable;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;

public abstract class SimulationTool extends AbstractTool implements ClipboardOwner {
    private VisualModel underlyingModel;

    protected JPanel interfacePanel;
    protected JPanel controlPanel;
    protected JPanel infoPanel;
    protected JSplitPane splitPane;
    protected JScrollPane tracePane;
    protected JScrollPane statePane;
    protected JTable traceTable;

    private JSlider speedSlider;
    private JButton randomButton, playButton, stopButton, backwardButton, forwardButton;

    // cache of "excited" containers (the ones containing the excited simulation elements)
    protected HashMap<Container, Boolean> excitedContainers = new HashMap<>();

    static final double DEFAULT_SIMULATION_DELAY = 0.3;
    static final double EDGE_SPEED_MULTIPLIER = 10;

    protected Map<Node, Integer> initialState;
    public HashMap<Node, Integer> savedState;
    protected final Trace mainTrace = new Trace();
    protected final Trace branchTrace = new Trace();

    private Timer timer = null;
    private boolean random = false;


    private final boolean enableTraceGraph;

    public SimulationTool(boolean enableTraceGraph) {
        super();
        this.enableTraceGraph = enableTraceGraph;
    }

    @Override
    public void createInterfacePanel(final GraphEditor editor) {
        super.createInterfacePanel(editor);

        playButton = GUI.createIconButton(GUI.createIconFromSVG("images/simulation-play.svg"), "Automatic trace playback");
        stopButton = GUI.createIconButton(GUI.createIconFromSVG("images/simulation-stop.svg"), "Reset trace playback");
        backwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/simulation-backward.svg"), "Step backward");
        forwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/simulation-forward.svg"), "Step forward");
        randomButton = GUI.createIconButton(GUI.createIconFromSVG("images/simulation-random_play.svg"), "Random playback");

        speedSlider = new JSlider(-1000, 1000, 0);
        speedSlider.setToolTipText("Simulation playback speed");

        JButton generateGraphButton = GUI.createIconButton(GUI.createIconFromSVG("images/simulation-trace-graph.svg"), "Generate trace digram");
        JButton copyStateButton = GUI.createIconButton(GUI.createIconFromSVG("images/simulation-trace-copy.svg"), "Copy trace to clipboard");
        JButton pasteStateButton = GUI.createIconButton(GUI.createIconFromSVG("images/simulation-trace-paste.svg"), "Paste trace from clipboard");
        JButton mergeTraceButton = GUI.createIconButton(GUI.createIconFromSVG("images/simulation-trace-merge.svg"), "Merge branch into trace");
        JButton saveInitStateButton = GUI.createIconButton(GUI.createIconFromSVG("images/simulation-marking-save.svg"), "Save current state as initial");

        FlowLayout flowLayout = new FlowLayout();
        int buttonWidth = (int) Math.round(playButton.getPreferredSize().getWidth() + flowLayout.getHgap());
        int buttonHeight = (int) Math.round(playButton.getPreferredSize().getHeight() + flowLayout.getVgap());
        Dimension panelSize = new Dimension(buttonWidth * 5 + flowLayout.getHgap(), buttonHeight + flowLayout.getVgap());

        JPanel simulationControl = new JPanel();
        simulationControl.setLayout(flowLayout);
        simulationControl.setPreferredSize(panelSize);
        simulationControl.setMaximumSize(panelSize);
        simulationControl.add(playButton);
        simulationControl.add(stopButton);
        simulationControl.add(backwardButton);
        simulationControl.add(forwardButton);
        simulationControl.add(randomButton);

        JPanel speedControl = new JPanel();
        speedControl.setLayout(new BorderLayout());
        speedControl.setPreferredSize(panelSize);
        speedControl.setMaximumSize(panelSize);
        speedControl.add(speedSlider, BorderLayout.CENTER);

        JPanel traceControl = new JPanel();
        traceControl.setLayout(flowLayout);
        traceControl.setPreferredSize(panelSize);
        traceControl.setMaximumSize(panelSize);
        if (enableTraceGraph) {
            traceControl.add(generateGraphButton);
        }
        traceControl.add(copyStateButton);
        traceControl.add(pasteStateButton);
        traceControl.add(mergeTraceButton);
        traceControl.add(saveInitStateButton);

        controlPanel = new JPanel();
        controlPanel.setLayout(new WrapLayout());
        controlPanel.add(simulationControl);
        controlPanel.add(speedControl);
        controlPanel.add(traceControl);

        traceTable = new JTable(new TraceTableModel());
        traceTable.getTableHeader().setReorderingAllowed(false);
        traceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        traceTable.setRowHeight(SizeHelper.getComponentHeightFromFont(traceTable.getFont()));
        traceTable.setDefaultRenderer(Object.class, new TraceTableCellRendererImplementation());

        tracePane = new JScrollPane();
        tracePane.setViewportView(traceTable);
        tracePane.setMinimumSize(new Dimension(1, 50));

        statePane = new JScrollPane();
        statePane.setMinimumSize(new Dimension(1, 50));

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tracePane, statePane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);
        splitPane.setResizeWeight(0.5);

        infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(splitPane, BorderLayout.CENTER);

        interfacePanel = new JPanel();
        interfacePanel.setLayout(new BorderLayout());
        interfacePanel.add(controlPanel, BorderLayout.NORTH);
        interfacePanel.add(infoPanel, BorderLayout.CENTER);
        interfacePanel.setPreferredSize(new Dimension(0, 0));

        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (timer != null) {
                    timer.stop();
                    timer.setInitialDelay(getAnimationDelay());
                    timer.setDelay(getAnimationDelay());
                    timer.start();
                }
                updateState(editor);
                editor.requestFocus();
            }
        });

        randomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer == null) {
                    timer = new Timer(getAnimationDelay(), new ActionListener()    {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            randomStep(editor);
                        }
                    });
                    timer.start();
                    random = true;
                } else if (random) {
                    timer.stop();
                    timer = null;
                    random = false;
                } else {
                    random = true;
                }
                updateState(editor);
                editor.requestFocus();
            }
        });

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer == null) {
                    timer = new Timer(getAnimationDelay(), new ActionListener()    {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            step(editor);
                        }
                    });
                    timer.start();
                    random = false;
                } else if (!random) {
                    timer.stop();
                    timer = null;
                    random = false;
                } else {
                    random = false;
                }
                updateState(editor);
                editor.requestFocus();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearTraces(editor);
                editor.requestFocus();
            }
        });

        backwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepBack(editor);
                editor.requestFocus();
            }
        });

        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step(editor);
                editor.requestFocus();
            }
        });

        generateGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateTraceGraph(editor);
                editor.requestFocus();
            }
        });

        copyStateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyState(editor);
                editor.requestFocus();
            }
        });

        pasteStateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteState(editor);
                editor.requestFocus();
            }
        });

        mergeTraceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mergeTrace(editor);
                editor.requestFocus();
            }
        });

        saveInitStateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savedState = readModelState();
                editor.requestFocus();
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
                            work = quietStepBack();
                        }
                        while (work && (mainTrace.getPosition() > row)) {
                            work = quietStepBack();
                        }
                        while (work && (mainTrace.getPosition() < row)) {
                            work = quietStep();
                        }
                    }
                } else {
                    if ((row >= mainTrace.getPosition()) && (row < mainTrace.getPosition() + branchTrace.size())) {
                        boolean work = true;
                        while (work && (mainTrace.getPosition() + branchTrace.getPosition() > row)) {
                            work = quietStepBack();
                        }
                        while (work && (mainTrace.getPosition() + branchTrace.getPosition() < row)) {
                            work = quietStep();
                        }
                    }
                }
                updateState(editor);
                editor.requestFocus();
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
    }

    public void setStatePaneVisibility(boolean visible) {
        statePane.setVisible(visible);
        splitPane.setDividerSize(visible ? 10 : 0);
    }

    @Override
    public void activated(final GraphEditor editor) {
        editor.getWorkspaceEntry().captureMemento();
        generateUnderlyingModel(editor.getModel());
        initialState = readModelState();
        super.activated(editor);
        setStatePaneVisibility(false);
        resetTraces(editor);
        editor.forceRedraw();
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        editor.getWorkspaceEntry().cancelMemento();
        applySavedState(editor);
        savedState = null;
        underlyingModel = null;
    }

    @Override
    public void setup(final GraphEditor editor) {
        super.setup(editor);
        editor.getWorkspaceEntry().setCanModify(false);
        editor.getWorkspaceEntry().setCanSelect(false);
        editor.getWorkspaceEntry().setCanCopy(false);
    }

    public void generateUnderlyingModel(VisualModel model) {
        setUnderlyingModel(model);
    }

    public void setUnderlyingModel(VisualModel model) {
        this.underlyingModel = model;
    }

    public VisualModel getUnderlyingModel() {
        return underlyingModel;
    }

    public boolean isActivated() {
        return underlyingModel != null;
    }

    public void updateState(final GraphEditor editor) {
        if (timer == null) {
            playButton.setIcon(GUI.createIconFromSVG("images/simulation-play.svg"));
            randomButton.setIcon(GUI.createIconFromSVG("images/simulation-random_play.svg"));
        } else {
            if (random) {
                playButton.setIcon(GUI.createIconFromSVG("images/simulation-play.svg"));
                randomButton.setIcon(GUI.createIconFromSVG("images/simulation-random_pause.svg"));
                timer.setDelay(getAnimationDelay());
            } else if (branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress())) {
                playButton.setIcon(GUI.createIconFromSVG("images/simulation-pause.svg"));
                randomButton.setIcon(GUI.createIconFromSVG("images/simulation-random_play.svg"));
                timer.setDelay(getAnimationDelay());
            } else {
                playButton.setIcon(GUI.createIconFromSVG("images/simulation-play.svg"));
                randomButton.setIcon(GUI.createIconFromSVG("images/simulation-random_play.svg"));
                timer.stop();
                timer = null;
            }
        }
        playButton.setEnabled(branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress()));
        stopButton.setEnabled(!mainTrace.isEmpty() || !branchTrace.isEmpty());
        backwardButton.setEnabled((mainTrace.getPosition() > 0) || (branchTrace.getPosition() > 0));
        forwardButton.setEnabled(branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress()));
        traceTable.tableChanged(new TableModelEvent(traceTable.getModel()));
        editor.repaint();
    }

    public void scrollTraceToBottom() {
        JScrollBar verticalScrollBar = tracePane.getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());
    }

    private boolean quietStepBack() {
        excitedContainers.clear();

        boolean result = false;
        String ref = null;
        int mainDec = 0;
        int branchDec = 0;
        if (branchTrace.getPosition() > 0) {
            ref = branchTrace.get(branchTrace.getPosition() - 1);
            branchDec = 1;
        } else if (mainTrace.getPosition() > 0) {
            ref = mainTrace.get(mainTrace.getPosition() - 1);
            mainDec = 1;
        }
        if (unfire(ref)) {
            mainTrace.decPosition(mainDec);
            branchTrace.decPosition(branchDec);
            if ((branchTrace.getPosition() == 0) && !mainTrace.isEmpty()) {
                branchTrace.clear();
            }
            result = true;
        }
        return result;
    }

    private boolean stepBack(final GraphEditor editor) {
        boolean ret = quietStepBack();
        updateState(editor);
        return ret;
    }

    private boolean quietStep() {
        excitedContainers.clear();

        boolean result = false;
        String ref = null;
        int mainInc = 0;
        int branchInc = 0;
        if (branchTrace.canProgress()) {
            ref = branchTrace.getCurrent();
            branchInc = 1;
        } else if (mainTrace.canProgress()) {
            ref = mainTrace.getCurrent();
            mainInc = 1;
        }
        if (fire(ref)) {
            mainTrace.incPosition(mainInc);
            branchTrace.incPosition(branchInc);
            result = true;
        }
        return result;
    }

    private boolean step(final GraphEditor editor) {
        boolean ret = quietStep();
        updateState(editor);
        return ret;
    }

    private boolean randomStep(final GraphEditor editor) {
        ArrayList<Node> enabledTransitions = getEnabledNodes();
        if (enabledTransitions.size() == 0) {
            return false;
        }
        int randomIndex = (int) (Math.random() * enabledTransitions.size());
        Node transition = enabledTransitions.get(randomIndex);
        executeTransition(editor, transition);
        return true;
    }

    private void resetTraces(final GraphEditor editor) {
        writeModelState(initialState);
        mainTrace.setPosition(0);
        branchTrace.clear();
        updateState(editor);
    }

    private void clearTraces(final GraphEditor editor) {
        writeModelState(initialState);
        mainTrace.clear();
        branchTrace.clear();
        if (timer != null)     {
            timer.stop();
            timer = null;
        }
        updateState(editor);
    }

    public void generateTraceGraph(final GraphEditor editor) {
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
        String str = "";
        if (hasTransferableText) {
            try {
                str = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ex) {
                System.out.println(ex);
                ex.printStackTrace();
            } catch (IOException ex) {
                System.out.println(ex);
                ex.printStackTrace();
            }
        }

        writeModelState(initialState);
        mainTrace.clear();
        branchTrace.clear();
        boolean first = true;
        for (String s: str.split("\n")) {
            if (first) {
                mainTrace.fromString(s);
                int mainTracePosition = mainTrace.getPosition();
                mainTrace.setPosition(0);
                boolean work = true;
                while (work && (mainTrace.getPosition() < mainTracePosition)) {
                    work = quietStep();
                }
            } else {
                branchTrace.fromString(s);
                int branchTracePosition = branchTrace.getPosition();
                branchTrace.setPosition(0);
                boolean work = true;
                while (work && (branchTrace.getPosition() < branchTracePosition)) {
                    work = quietStep();
                }
                break;
            }
            first = false;
        }
        updateState(editor);
    }

    public Trace getCombinedTrace() {
        Trace result = new Trace();
        if (branchTrace.isEmpty()) {
            result.addAll(mainTrace);
            result.setPosition(mainTrace.getPosition());
        } else {
            List<String> commonTrace = mainTrace.subList(0, mainTrace.getPosition());
            result.addAll(commonTrace);
            result.addAll(branchTrace);
            result.setPosition(mainTrace.getPosition() + branchTrace.getPosition());
        }
        return result;
    }

    private void mergeTrace(final GraphEditor editor) {
        if (!branchTrace.isEmpty()) {
            Trace combinedTrace = getCombinedTrace();
            mainTrace.clear();
            branchTrace.clear();
            mainTrace.addAll(combinedTrace);
            mainTrace.setPosition(combinedTrace.getPosition());
        }
        updateState(editor);
    }

    private int getAnimationDelay() {
        return (int) (1000.0 * DEFAULT_SIMULATION_DELAY * Math.pow(EDGE_SPEED_MULTIPLIER, -speedSlider.getValue() / 1000.0));
    }

    @SuppressWarnings("serial")
    private final class TraceTableCellRendererImplementation implements TableCellRenderer {
        private final JLabel label = new JLabel() {
            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                super.paint(g);
            }
        };

        boolean isActive(int row, int column) {
            if (column == 0) {
                if (!mainTrace.isEmpty() && branchTrace.isEmpty()) {
                    return row == mainTrace.getPosition();
                }
            } else {
                if (!branchTrace.isEmpty() && (row >= mainTrace.getPosition())
                        && (row < mainTrace.getPosition() + branchTrace.size())) {
                    return row == mainTrace.getPosition() + branchTrace.getPosition();
                }
            }
            return false;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel result = null;
            label.setBorder(PropertyEditorTable.BORDER_RENDER);
            if (isActivated() && (value instanceof String)) {
                label.setText((String) value);
                if (isActive(row, column)) {
                    label.setForeground(table.getSelectionForeground());
                    label.setBackground(table.getSelectionBackground());
                } else {
                    label.setForeground(table.getForeground());
                    label.setBackground(table.getBackground());
                }
                result = label;
            }
            return result;
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
            return (column == 0) ? "Trace" : "Branch";
        }

        @Override
        public int getRowCount() {
            return Math.max(mainTrace.size(), mainTrace.getPosition() + branchTrace.size());
        }

        @Override
        public Object getValueAt(int row, int column) {
            String ref = null;
            if (column == 0) {
                if (!mainTrace.isEmpty() && (row < mainTrace.size())) {
                    ref = mainTrace.get(row);
                }
            } else {
                if (!branchTrace.isEmpty() && (row >= mainTrace.getPosition()) && (row < mainTrace.getPosition() + branchTrace.size())) {
                    ref = branchTrace.get(row - mainTrace.getPosition());
                }
            }
            return getTraceLabelByReference(ref);
        }
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET) {
            stepBack(e.getEditor());
            return true;
        }
        if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET) {
            step(e.getEditor());
            return true;
        }
        return super.keyPressed(e);
    }

    public void executeTransition(final GraphEditor editor, Node candidate) {
        if (candidate == null) return;

        String ref = null;
        // If clicked on the trace event, do the step forward.
        if (branchTrace.isEmpty() && !mainTrace.isEmpty() && (mainTrace.getPosition() < mainTrace.size())) {
            ref = mainTrace.get(mainTrace.getPosition());
        }
        // Otherwise form/use the branch trace.
        if (!branchTrace.isEmpty() && (branchTrace.getPosition() < branchTrace.size())) {
            ref = branchTrace.get(branchTrace.getPosition());
        }
        MathModel mathModel = getUnderlyingModel().getMathModel();
        if ((mathModel != null) && (ref != null)) {
            Node node = mathModel.getNodeByReference(ref);
            if (node == candidate) {
                step(editor);
                return;
            }
        }
        while (branchTrace.getPosition() < branchTrace.size()) {
            branchTrace.removeCurrent();
        }
        branchTrace.add(mathModel.getNodeReference(candidate));
        step(editor);
        scrollTraceToBottom();
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(),
                new Func<Node, Boolean>() {
                    @Override
                    public Boolean eval(Node node) {
                        boolean result = false;
                        if (node instanceof VisualComponent) {
                            VisualComponent component = (VisualComponent) node;
                            result = isEnabledNode(component.getReferencedComponent());
                        }
                        return result;
                    }
                });

        if (node instanceof VisualComponent) {
            VisualComponent component = (VisualComponent) node;
            executeTransition(e.getEditor(), component.getReferencedComponent());
        }
    }

    @Override
    public String getHintMessage() {
        return "Click on a highlighted node to fire it.";
    }

    public String getLabel() {
        return "Simulation";
    }

    public int getHotKeyCode() {
        return KeyEvent.VK_M;
    }

    @Override
    public Icon getIcon() {
        return GUI.createIconFromSVG("images/tool-simulation.svg");
    }

    @Override
    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    @Override
    public JPanel getInterfacePanel() {
        return interfacePanel;
    }

    public void setTrace(Trace mainTrace, Trace branchTrace, GraphEditor editor) {
        this.mainTrace.clear();
        if (mainTrace != null) {
            this.mainTrace.addAll(mainTrace);
        }
        this.branchTrace.clear();
        if (branchTrace != null) {
            this.branchTrace.addAll(branchTrace);
        }
        updateState(editor);
    }

    public Node getTraceCurrentNode() {
        String ref = null;
        if (branchTrace.canProgress()) {
            ref = branchTrace.getCurrent();
        } else if (branchTrace.isEmpty() && mainTrace.canProgress()) {
            ref = mainTrace.getCurrent();
        }
        Node result = null;
        MathModel mathModel = getUnderlyingModel().getMathModel();
        if ((mathModel != null) && (ref != null)) {
            result = mathModel.getNodeByReference(ref);
        }
        return result;
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                if ((node instanceof VisualPage) || (node instanceof VisualGroup)) {
                    return getContainerDecoration((Container) node);
                } else if (node instanceof VisualConnection) {
                    return getConnectionDecoration((VisualConnection) node);
                } else if (node instanceof VisualComponent) {
                    return getComponentDecoration((VisualComponent) node);
                }
                return null;
            }
        };
    }

    public Decoration getContainerDecoration(Container container) {
        final boolean ret = isContainerExcited(container);
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

    public Decoration getConnectionDecoration(VisualConnection connection) {
        if (isConnectionExcited(connection)) {
            return new Decoration() {

                @Override
                public Color getColorisation() {
                    return CommonSimulationSettings.getEnabledForegroundColor();
                }

                @Override
                public Color getBackground() {
                    return CommonSimulationSettings.getEnabledBackgroundColor();
                }
            };
        }
        return null;
    }

    public Decoration getComponentDecoration(VisualComponent component) {
        Node node = component.getReferencedComponent();
        Node currentTraceNode = getTraceCurrentNode();
        if (node == currentTraceNode) {
            return new Decoration() {
                @Override
                public Color getColorisation() {
                    return CommonSimulationSettings.getEnabledBackgroundColor();
                }
                @Override
                public Color getBackground() {
                    return CommonSimulationSettings.getEnabledForegroundColor();
                }
            };
        }
        if (isEnabledNode(node)) {
            return new Decoration() {
                @Override
                public Color getColorisation() {
                    return CommonSimulationSettings.getEnabledForegroundColor();
                }
                @Override
                public Color getBackground() {
                    return CommonSimulationSettings.getEnabledBackgroundColor();
                }
            };
        }

        return null;
    }

    public boolean isContainerExcited(Container container) {
        if (excitedContainers.containsKey(container)) return excitedContainers.get(container);
        boolean ret = false;
        for (Node node: container.getChildren()) {
            if (node instanceof VisualComponent) {
                VisualComponent component = (VisualComponent) node;
                ret = ret || isEnabledNode(component.getReferencedComponent());
            }

            if (node instanceof Container) {
                ret = ret || isContainerExcited((Container) node);
            }
            if (ret) break;
        }
        excitedContainers.put(container, ret);
        return ret;
    }

    public boolean isConnectionExcited(VisualConnection connection) {
        return false;
    }

    @Override
    public void lostOwnership(Clipboard clip, Transferable arg) {
    }

    public String getTraceLabelByReference(String ref) {
        return ref;
    }

    public abstract HashMap<Node, Integer> readModelState();

    public abstract void writeModelState(Map<Node, Integer> state);

    public abstract void applySavedState(final GraphEditor editor);

    public abstract ArrayList<Node> getEnabledNodes();

    public abstract boolean isEnabledNode(Node node);

    public abstract boolean fire(String ref);

    public abstract boolean unfire(String ref);

}
