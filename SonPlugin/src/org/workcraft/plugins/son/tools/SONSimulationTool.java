package org.workcraft.plugins.son.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractGraphEditorTool;
import org.workcraft.gui.graph.tools.ContainerDecoration;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.son.BlockConnector;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.ErrorTracingAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.algorithm.SONCycleAlg;
import org.workcraft.plugins.son.algorithm.SimulationAlg;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualTransitionNode;
import org.workcraft.plugins.son.exception.InvalidStructureException;
import org.workcraft.plugins.son.exception.UnboundedException;
import org.workcraft.plugins.son.gui.ParallelSimDialog;
import org.workcraft.plugins.son.gui.SONGUI;
import org.workcraft.plugins.son.util.Phase;
import org.workcraft.plugins.son.util.Step;
import org.workcraft.plugins.son.util.StepRef;
import org.workcraft.plugins.son.util.Trace;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class SONSimulationTool extends AbstractGraphEditorTool implements ClipboardOwner {

    protected SON net;
    protected VisualSON visualNet;
    protected GraphEditor editor;

    protected RelationAlgorithm relationAlg;
    protected BSONAlg bsonAlg;
    protected SimulationAlg simuAlg;
    private ErrorTracingAlg    errAlg;

    protected Collection<Path> sync;
    protected Map<Condition, Collection<Phase>> phases;
    protected Map<PlaceNode, Boolean> initialMarking;

    protected JPanel panel;
    protected JPanel controlPanel;
    protected JScrollPane tabelPanel;
    protected JTable traceTable;

    protected JSlider speedSlider;
    protected JButton playButton, stopButton, backwardButton, forwardButton, reverseButton, errorButton;
    protected JButton copyStateButton, pasteStateButton, mergeTraceButton;
    protected JToggleButton autoSimuButton;

    protected HashMap<Container, Boolean> excitedContainers = new HashMap<>();

    static final double DEFAULT_SIMULATION_DELAY = 0.3;
    static final double EDGE_SPEED_MULTIPLIER = 10;

    protected final Trace mainTrace = new Trace();
    protected final Trace branchTrace = new Trace();

    protected boolean isRev;

    protected Timer timer = null;

    @Override
    public String getHintText(final GraphEditor editor) {
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
        return GUI.createIconFromSVG("images/son-tool-simulation.svg");
    }

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel != null) {
            return panel;
        }

        playButton = GUI.createIconButton(GUI.createIconFromSVG("images/son-simulation-play.svg"),
                "Automatic trace playback");
        stopButton = GUI.createIconButton(GUI.createIconFromSVG("images/son-simulation-stop.svg"),
                "Reset trace playback");
        backwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/son-simulation-backward.svg"),
                "Step backward");
        forwardButton = GUI.createIconButton(GUI.createIconFromSVG("images/son-simulation-forward.svg"),
                "Step forward");
        reverseButton = GUI.createIconButton(GUI.createIconFromSVG("images/son-simulation-progress.svg"),
                "Switch to reverse simulation");
        autoSimuButton = SONGUI.createIconToggleButton(GUI.createIconFromSVG("images/son-simulation-auto.svg"),
                "Automatic simulation");
        errorButton = GUI.createIconButton(GUI.createIconFromSVG("images/son-simulation-trace-error.svg"),
                "Enable/Disable error tracing");

        speedSlider = new JSlider(-1000, 1000, 0);
        speedSlider.setToolTipText("Simulation playback speed");

        copyStateButton = GUI.createIconButton(GUI.createIconFromSVG("images/son-simulation-trace-copy.svg"),
                "Copy trace to clipboard");
        pasteStateButton = GUI.createIconButton(GUI.createIconFromSVG("images/son-simulation-trace-paste.svg"),
                "Paste trace from clipboard");
        mergeTraceButton = GUI.createIconButton(GUI.createIconFromSVG("images/son-simulation-trace-merge.svg"),
                "Merge branch into trace");

        int buttonWidth = (int) Math.round(playButton.getPreferredSize().getWidth() + 5);
        int buttonHeight = (int) Math.round(playButton.getPreferredSize().getHeight() + 5);
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
        simulationControl.add(autoSimuButton);
        simulationControl.add(errorButton);

        JPanel speedControl = new JPanel();
        speedControl.setLayout(new BorderLayout());
        speedControl.setPreferredSize(panelSize);
        speedControl.setMaximumSize(panelSize);
        speedControl.add(speedSlider, BorderLayout.CENTER);

        JPanel traceControl = new JPanel();
        traceControl.setLayout(new FlowLayout());
        traceControl.setPreferredSize(panelSize);
        traceControl.add(new JSeparator());
        traceControl.add(copyStateButton);
        traceControl.add(pasteStateButton);
        traceControl.add(mergeTraceButton);

        controlPanel = new JPanel();
        controlPanel.setLayout(new WrapLayout());
        controlPanel.add(simulationControl);
        controlPanel.add(speedControl);
        controlPanel.add(traceControl);

        traceTable = new JTable(new TraceTableModel());
        traceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabelPanel = new JScrollPane(traceTable);
        tabelPanel.setPreferredSize(new Dimension(1, 1));

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
            }
        });

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer == null) {
                    timer = new Timer(getAnimationDelay(), new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            step(editor);
                        }
                    });
                    timer.start();
                } else {
                    timer.stop();
                    timer = null;
                }
                updateState(editor);
                editor.requestFocus();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset(editor);
                setDecoration(simuAlg.getEnabledNodes(sync, phases, isRev));
            }
        });

        backwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepBack(editor);
            }
        });

        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step(editor);
            }
        });

        reverseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Map<PlaceNode, Boolean> currentMarking = readSONMarking();
                setReverse(editor, !isRev);
                writeModelState(currentMarking);
                setDecoration(simuAlg.getEnabledNodes(sync, phases, isRev));
                excitedContainers.clear();
                updateState(editor);
                editor.requestFocus();
            }
        });

        autoSimuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (autoSimuButton.isSelected()) {
                    if (!acyclicChecker()) {
                        autoSimuButton.setSelected(false);
                        try {
                            throw new InvalidStructureException("Cyclic structure error");
                        } catch (InvalidStructureException e1) {
                            errorMsg(e1.getMessage(), editor);
                        }
                    } else {
                        autoSimulator(editor);
                    }
                }
            }
        });

        errorButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SONSettings.setErrorTracing(!SONSettings.isErrorTracing());
                editor.repaint();
            }
        });

        copyStateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyState(editor);
            }
        });

        pasteStateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteState(editor);
            }
        });

        mergeTraceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mergeTrace(editor);
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
                            work = quietStepBack(editor);
                        }
                        while (work && (mainTrace.getPosition() > row)) {
                            work = quietStepBack(editor);
                        }
                        while (work && (mainTrace.getPosition() < row)) {
                            work = quietStep(editor);
                        }
                    }
                } else {
                    if ((row >= mainTrace.getPosition()) && (row < mainTrace.getPosition() + branchTrace.size())) {
                        boolean work = true;
                        while (work && (mainTrace.getPosition() + branchTrace.getPosition() > row)) {
                            work = quietStepBack(editor);
                        }
                        while (work && (mainTrace.getPosition() + branchTrace.getPosition() < row)) {
                            work = quietStep(editor);
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
        traceTable.setDefaultRenderer(Object.class, new TraceTableCellRendererImplementation());

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(tabelPanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        this.editor = editor;
        visualNet = (VisualSON) editor.getModel();
        net = (SON) visualNet.getMathModel();
        editor.getWorkspaceEntry().captureMemento();

        BlockConnector.blockBoundingConnector(visualNet);
        errAlg = new ErrorTracingAlg(net);
        initialise();
        reset(editor);
        setDecoration(simuAlg.getEnabledNodes(sync, phases, isRev));

        if (SONSettings.isErrorTracing()) {
            net.resetConditionErrStates();
        }
        updateState(editor);
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
        mainTrace.clear();
        branchTrace.clear();
        isRev = false;
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(false);
        we.setCanSelect(true);
        we.setCanCopy(true);
    }

    protected void initialise() {
        relationAlg = new RelationAlgorithm(net);
        bsonAlg = new BSONAlg(net);
        simuAlg = new SimulationAlg(net);
        initialMarking = simuAlg.getInitialMarking();
        sync = getSyncEventCycles();
        phases = bsonAlg.getAllPhases();
    }

    protected Collection<Path> getSyncEventCycles() {
        HashSet<Node> nodes = new HashSet<>();
        nodes.addAll(net.getTransitionNodes());
        nodes.addAll(net.getChannelPlaces());
        CSONCycleAlg cycleAlg = new CSONCycleAlg(net);

        return cycleAlg.syncEventCycleTask(nodes);
    }

    public void updateState(final GraphEditor editor) {
        if (timer == null) {
            playButton.setIcon(GUI.createIconFromSVG("images/son-simulation-play.svg"));
        } else {
            if (branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress())) {
                playButton.setIcon(GUI.createIconFromSVG("images/son-simulation-pause.svg"));
                timer.setDelay(getAnimationDelay());
            } else {
                playButton.setIcon(GUI.createIconFromSVG("images/son-simulation-play.svg"));
                timer.stop();
                timer = null;
            }
        }
        playButton.setEnabled(branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress()));
        stopButton.setEnabled(!mainTrace.isEmpty() || !branchTrace.isEmpty());
        backwardButton.setEnabled((mainTrace.getPosition() > 0) || (branchTrace.getPosition() > 0));
        forwardButton.setEnabled(branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress()));
        traceTable.tableChanged(new TableModelEvent(traceTable.getModel()));
        if (!isRev) {
            reverseButton.setIcon(GUI.createIconFromSVG("images/son-simulation-progress.svg"));
            reverseButton.setToolTipText("Switch to reverse simulation");
        } else {
            reverseButton.setIcon(GUI.createIconFromSVG("images/son-simulation-reverse.svg"));
            reverseButton.setToolTipText("Switch to forward simulation");
        }
        editor.repaint();
    }

    private int getAnimationDelay() {
        return (int) (1000.0 * DEFAULT_SIMULATION_DELAY * Math.pow(EDGE_SPEED_MULTIPLIER, -speedSlider.getValue() / 1000.0));
    }

    @SuppressWarnings("serial")
    protected class TraceTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) return "Trace";
            return "Branch";
        }

        @Override
        public int getRowCount() {
            return Math.max(mainTrace.size(), mainTrace.getPosition() + branchTrace.size());
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (column == 0) {
                if (!mainTrace.isEmpty() && (row < mainTrace.size())) {
                    return mainTrace.get(row);
                }
            } else {
                if (!branchTrace.isEmpty() && (row >= mainTrace.getPosition()) && (row < mainTrace.getPosition() + branchTrace.size())) {
                    return branchTrace.get(row - mainTrace.getPosition());
                }
            }
            return "";
        }
    }

    protected void errorMsg(String message, final GraphEditor editor) {

        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();

        JOptionPane.showMessageDialog(mainWindow,
                message, "Invalid structure", JOptionPane.WARNING_MESSAGE);
        reset(editor);
    }

    protected Map<PlaceNode, Boolean> readSONMarking() {
        HashMap<PlaceNode, Boolean> result = new HashMap<>();
        for (PlaceNode c : net.getPlaceNodes()) {
            result.put(c, c.isMarked());
        }

        return result;
    }

    protected boolean quietStep(final GraphEditor editor) {
        excitedContainers.clear();
        boolean result = false;
        Step step = null;
        int mainInc = 0;
        int branchInc = 0;
        if (branchTrace.canProgress()) {
            StepRef stepRef = branchTrace.getCurrent();
            step = this.getStep(stepRef);
            setReverse(editor, stepRef);
            branchInc = 1;
        } else if (mainTrace.canProgress()) {
            StepRef stepRef = mainTrace.getCurrent();
            step = this.getStep(stepRef);
            setReverse(editor, stepRef);
            mainInc = 1;
        }

        if (step != null) {
            try {
                simuAlg.setMarking(step, phases, isRev);
            } catch (UnboundedException e) {
            }
            setErrNum(step, isRev);
            mainTrace.incPosition(mainInc);
            branchTrace.incPosition(branchInc);
            setDecoration(simuAlg.getEnabledNodes(sync, phases, isRev));
            result = true;
        }

        return result;
    }

    protected boolean step(final GraphEditor editor) {
        boolean ret = quietStep(editor);
        updateState(editor);
        return ret;
    }

    private boolean stepBack(final GraphEditor editor) {
        boolean ret = quietStepBack(editor);
        updateState(editor);
        return ret;
    }

    protected boolean quietStepBack(final GraphEditor editor) {
        excitedContainers.clear();
        boolean result = false;
        Step step = null;
        int mainDec = 0;
        int branchDec = 0;
        if (branchTrace.getPosition() > 0) {
            StepRef stepRef = branchTrace.get(branchTrace.getPosition() - 1);
            step = getStep(stepRef);
            setReverse(editor, stepRef);
            branchDec = 1;
        } else if (mainTrace.getPosition() > 0) {
            StepRef stepRef = mainTrace.get(mainTrace.getPosition() - 1);
            step = getStep(stepRef);
            setReverse(editor, stepRef);
            mainDec = 1;
        }

        if (step != null) {
            try {
                simuAlg.setMarking(step, phases, !isRev);
            } catch (UnboundedException e) {
                e.printStackTrace();
            }
            mainTrace.decPosition(mainDec);
            branchTrace.decPosition(branchDec);
            if ((branchTrace.getPosition() == 0) && !mainTrace.isEmpty()) {
                branchTrace.clear();
            }
            setDecoration(simuAlg.getEnabledNodes(sync, phases, isRev));
            result = true;
            this.setErrNum(step, !isRev);
        }

        return result;
    }

    private void reset(final GraphEditor editor) {
        writeModelState(initialMarking);
        isRev = false;
        mainTrace.clear();
        branchTrace.clear();

        if (timer != null) {
            timer.stop();
            timer = null;
        }
        updateState(editor);
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

        writeModelState(initialMarking);
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
                    work = quietStep(editor);
                }
            } else {
                branchTrace.fromString(s);
                int branchTracePosition = branchTrace.getPosition();
                branchTrace.setPosition(0);
                boolean work = true;
                while (work && (branchTrace.getPosition() < branchTracePosition)) {
                    work = quietStep(editor);
                }
                break;
            }
            first = false;
        }
        updateState(editor);
    }

    public void mergeTrace(final GraphEditor editor) {
        if (!branchTrace.isEmpty()) {
            while (mainTrace.getPosition() < mainTrace.size()) {
                mainTrace.removeCurrent();
            }
            mainTrace.addAll(branchTrace);
            mainTrace.incPosition(branchTrace.getPosition());
            branchTrace.clear();
        }
        updateState(editor);
    }

    private void setErrNum(Step step, boolean isRev) {
        if (SONSettings.isErrorTracing()) {
            Collection<TransitionNode> upperEvents = new ArrayList<>();

            //get high level events
            for (TransitionNode node : step) {
                if (bsonAlg.isUpperEvent(node)) {
                    upperEvents.add(node);
                }
            }
            //get low level events
            step.removeAll(upperEvents);

            if (!isRev) {
                //set error number for upper events
                errAlg.setErrNum(upperEvents, sync, phases, false);
                //set error number for lower events
                errAlg.setErrNum(step, sync, phases, true);
            } else {
                errAlg.setRevErrNum(upperEvents, sync, phases, false);
                errAlg.setRevErrNum(step, sync, phases, true);
            }
        }
    }

    protected void writeModelState(Map<PlaceNode, Boolean> marking) {
        for (PlaceNode c: marking.keySet()) {
            c.setMarked(marking.get(c));
        }
    }

    protected void autoSimulator(final GraphEditor editor) {
        if (!acyclicChecker()) {
            autoSimuButton.setSelected(false);
        } else {
            autoSimulationTask(editor);
        }
    }

    protected boolean acyclicChecker() {
        SONCycleAlg cycle = new SONCycleAlg(net, phases);
        if (!cycle.cycleTask(net.getComponents()).isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    protected void autoSimulationTask(final GraphEditor editor) {
        Step step = simuAlg.getEnabledNodes(sync, phases, isRev);

        if (step.isEmpty()) {
            autoSimuButton.setSelected(false);
            return;
        }
        step = conflictfilter(step);
        if (!step.isEmpty()) {
            step = simuAlg.getMinFire(step.iterator().next(), sync, step, isRev);
            executeEvents(editor, step);
            autoSimulationTask(editor);
        }
    }

    protected Step conflictfilter(Step step) {
        Step result = new Step();
        result.addAll(step);

        for (PlaceNode c : readSONMarking().keySet()) {
            Collection<TransitionNode> conflict = new ArrayList<>();
            if (c.isMarked()) {
                if (!isRev) {
                    conflict.addAll(relationAlg.getPostConflictEvents(c));
                } else {
                    conflict.addAll(relationAlg.getPreConflictEvents(c));
                }
            }

            if (!conflict.isEmpty()) {
                for (TransitionNode e : conflict) {
                    result.removeAll(simuAlg.getMinFire(e, sync, step, isRev));
                    result.removeAll(simuAlg.getMinFire(e, sync, step, !isRev));
                }
            }
        }
        return result;
    }

    public Map<PlaceNode, Boolean> reachabilitySimulator(final GraphEditor editor, Collection<String> causalPredecessorRefs, Collection<String> markingRefs) {
        Collection<TransitionNode> causalPredecessors = new ArrayList<>();
        for (String ref : causalPredecessorRefs) {
            Node node = net.getNodeByReference(ref);
            if (node instanceof TransitionNode) {
                causalPredecessors.add((TransitionNode) net.getNodeByReference(ref));
            }
        }
        return reachabilitySimulationTask(editor, causalPredecessors, markingRefs);
    }

    private Map<PlaceNode, Boolean> reachabilitySimulationTask(final GraphEditor editor,
            Collection<TransitionNode> causalPredecessors, Collection<String> markingRefs) {

        Step enabled = null;
        enabled = simuAlg.getEnabledNodes(sync, phases, isRev);

        Step step = new Step();
        for (Node node : relationAlg.getCommonElements(enabled, causalPredecessors)) {
            if (node instanceof TransitionNode) {
                step.add((TransitionNode) node);
            }
        }

        //causalPredecessors.removeAll(fireList);

        if (!step.isEmpty()) {
            executeEvents(editor, step);
            reachabilitySimulationTask(editor, causalPredecessors, markingRefs);
        }

        for (String ref : markingRefs) {
            Node node = net.getNodeByReference(ref);
            if (node instanceof PlaceNode) {
                ((PlaceNode) node).setForegroundColor(Color.BLUE);
                ((PlaceNode) node).setTokenColor(Color.BLUE);
            }
        }

        return readSONMarking();
    }

    public void executeEvents(final GraphEditor editor, Step step) {
        if (step.isEmpty()) return;
        Step traceList = new Step();
        // if clicked on the trace event, do the step forward
        if (branchTrace.isEmpty() && !mainTrace.isEmpty() && (mainTrace.getPosition() < mainTrace.size())) {
            StepRef stepRef = mainTrace.get(mainTrace.getPosition());
            traceList = getStep(stepRef);
        }
        // otherwise form/use the branch trace
        if (!branchTrace.isEmpty() && (branchTrace.getPosition() < branchTrace.size())) {
            StepRef stepRef = branchTrace.get(branchTrace.getPosition());
            traceList = getStep(stepRef);
        }
        if (!traceList.isEmpty() && traceList.containsAll(step) && step.containsAll(traceList)) {
            step(editor);
            return;
        }
        while (branchTrace.getPosition() < branchTrace.size()) {
            branchTrace.removeCurrent();
        }

        StepRef newStep = new StepRef();
        if (!isRev) {
            newStep.add(">");
        } else {
            newStep.add("<");
        }
        for (TransitionNode e : step) {
            newStep.add(net.getNodeReference(e));
        }

        branchTrace.add(newStep);
        step(editor);
    }

    protected Step getStep(StepRef stepRef) {
        Step result = new Step();
        for (int i = 0; i < stepRef.size(); i++) {
            final Node node = net.getNodeByReference(stepRef.get(i));
            if (node instanceof TransitionNode) {
                result.add((TransitionNode) node);
            }
        }
        return result;
    }

    @SuppressWarnings("serial")
    protected final class TraceTableCellRendererImplementation implements TableCellRenderer {
        JLabel label = new JLabel() {
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
                if (!branchTrace.isEmpty() && (row >= mainTrace.getPosition()) && (row < mainTrace.getPosition() + branchTrace.size())) {
                    return row == mainTrace.getPosition() + branchTrace.getPosition();
                }
            }
            return false;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            if (!(value instanceof StepRef)) return null;

            label.setText(((StepRef) value).toString());

            if (isActive(row, column)) {
                label.setBackground(Color.YELLOW);
            } else {
                label.setBackground(Color.WHITE);
            }

            return label;
        }
    }

    protected void setDecoration(Step enabled) {
        net.refreshAllColor();

        for (TransitionNode e : enabled) {
            //e.setFillColor(CommonSimulationSettings.getEnabledForegroundColor());
            e.setForegroundColor(CommonSimulationSettings.getExcitedComponentColor());
        }

        StepRef refStep = null;
        if (branchTrace.canProgress()) {
            refStep = branchTrace.get(branchTrace.getPosition());
        } else if (branchTrace.isEmpty() && mainTrace.canProgress()) {
            refStep = mainTrace.get(mainTrace.getPosition());
        }

        if (refStep != null) {
            if (refStep.isReverse() == isRev) {
                for (String ref : refStep) {
                    Node n = net.getNodeByReference(ref);
                    if (n != null) {
                        if (n instanceof TransitionNode) {
                            ((TransitionNode) n).setFillColor(new Color(255, 228, 181));
                        }
                    }
                }
            }
        }
    }

    private boolean isEnabled(Node e, Step step) {
        if (step.contains(e)) {
            return true;
        }
        return false;
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {

        Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(),
                new Func<Node, Boolean>() {
                    @Override
                    public Boolean eval(Node node) {
                        if (node instanceof VisualTransitionNode) {
                            TransitionNode node1 = ((VisualTransitionNode) node).getMathTransitionNode();
                            Step enabled = null;

                            enabled = simuAlg.getEnabledNodes(sync, phases, isRev);
                            if (isEnabled(node1, enabled)) {
                                return true;
                            }
                        }
                        return false;

                    }
                });

        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();

        if (node instanceof VisualTransitionNode) {

            Step enabled = null;
            TransitionNode select = ((VisualTransitionNode) node).getMathTransitionNode();

            enabled = simuAlg.getEnabledNodes(sync, phases, isRev);

            Step minFire = simuAlg.getMinFire(select, sync, enabled, isRev);
            Step possibleFire = simuAlg.getMinFire(select, sync, enabled, !isRev);
            //remove select node and directed sync cycle
            possibleFire.removeAll(minFire);
            possibleFire.remove(select);

            Step step = new Step();
            step.addAll(minFire);

            minFire.remove(select);

            if (possibleFire.isEmpty()) {
                executeEvents(e.getEditor(), step);
            } else {
                e.getEditor().requestFocus();
                ParallelSimDialog dialog = new ParallelSimDialog(mainWindow,
                        net, possibleFire, minFire, select, isRev, sync);
                GUI.centerToParent(dialog, mainWindow);
                dialog.setVisible(true);

                if (dialog.getRun() == 1) {
                    step.addAll(dialog.getSelectedEvent());
                    executeEvents(e.getEditor(), step);
                }
                if (dialog.getRun() == 2) {
                    setDecoration(enabled);
                    return;
                }
            }

            if (autoSimuButton.isSelected()) {
                autoSimulator(editor);
            }
        }
    }

    protected boolean isContainerExcited(Container container) {
        if (excitedContainers.containsKey(container)) return excitedContainers.get(container);

        boolean ret = false;

        for (Node node: container.getChildren()) {
            try {
                Step enabled = null;

                enabled = simuAlg.getEnabledNodes(sync, phases, isRev);

                if (node instanceof VisualTransitionNode) {
                    TransitionNode event = ((VisualTransitionNode) node).getMathTransitionNode();
                    ret = ret || isEnabled(event, enabled);
                }
            } catch (NullPointerException ex) {

            }

            if (node instanceof Container) {
                ret = ret || isContainerExcited((Container) node);
            }

            if (ret) break;
        }

        excitedContainers.put(container, ret);
        return ret;
    }

    public void setReverse(final GraphEditor editor, boolean rev) {
        this.isRev = rev;
        updateState(editor);
    }

    public void setReverse(final GraphEditor editor, StepRef stepRef) {

        if (stepRef.contains(">")) {
            this.setReverse(editor, false);
        } else {
            this.setReverse(editor, true);
        }
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {

        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                if ((node instanceof VisualPage && !(node instanceof VisualBlock)) || node instanceof VisualGroup) {
                    final boolean ret = isContainerExcited((Container) node);

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

                return null;
            }
        };
    }

    public GraphEditor getGraphEditor() {
        return editor;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

}
