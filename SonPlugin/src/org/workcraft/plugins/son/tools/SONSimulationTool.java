package org.workcraft.plugins.son.tools;

import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.plugins.son.BlockConnector;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.*;
import org.workcraft.plugins.son.elements.*;
import org.workcraft.plugins.son.exception.InvalidStructureException;
import org.workcraft.plugins.son.exception.UnboundedException;
import org.workcraft.plugins.son.gui.ParallelSimDialog;
import org.workcraft.plugins.son.util.Phase;
import org.workcraft.plugins.son.util.Step;
import org.workcraft.plugins.son.util.StepRef;
import org.workcraft.plugins.son.util.Trace;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.*;

public class SONSimulationTool extends AbstractGraphEditorTool implements ClipboardOwner {

    protected RelationAlgorithm relationAlg;
    protected BSONAlg bsonAlg;
    protected SimulationAlg simuAlg;
    private ErrorTracingAlg    errAlg;

    protected Collection<Path> sync;
    protected Map<Condition, Collection<Phase>> phases;
    protected Map<PlaceNode, Boolean> initialMarking;

    protected JPanel panel;
    protected JPanel controlPanel;
    protected JScrollPane tablePanel;
    protected JTable traceTable;

    protected JSlider speedSlider;
    protected JButton playButton;
    protected JButton stopButton;
    protected JButton backwardButton;
    protected JButton forwardButton;
    protected JButton reverseButton;
    protected JButton errorButton;
    protected JButton copyStateButton;
    protected JButton pasteStateButton;
    protected JButton mergeTraceButton;
    protected JToggleButton autoSimuButton;

    protected HashMap<Container, Boolean> excitedContainers = new HashMap<>();

    private static final double DEFAULT_SIMULATION_DELAY = 0.3;
    private static final double EDGE_SPEED_MULTIPLIER = 10;

    protected final Trace mainTrace = new Trace();
    protected final Trace branchTrace = new Trace();

    protected boolean isRev;

    protected Timer timer = null;

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted node to fire it.";
    }

    @Override
    public String getLabel() {
        return "Simulation";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_M;
    }

    @Override
    public Icon getIcon() {
        return GuiUtils.createIconFromSVG("images/son-tool-simulation.svg");
    }

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel != null) {
            return panel;
        }
        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();

        playButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-simulation-play.svg"),
                "Automatic trace playback");
        stopButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-simulation-stop.svg"),
                "Reset trace playback");
        backwardButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-simulation-backward.svg"),
                "Step backward");
        forwardButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-simulation-forward.svg"),
                "Step forward");
        reverseButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-simulation-progress.svg"),
                "Switch to reverse simulation");
        autoSimuButton = GuiUtils.createIconToggleButton(GuiUtils.createIconFromSVG("images/son-simulation-auto.svg"),
                "Automatic simulation");
        errorButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-simulation-trace-error.svg"),
                "Enable/Disable error tracing");

        speedSlider = new JSlider(-1000, 1000, 0);
        speedSlider.setToolTipText("Simulation playback speed");

        copyStateButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-simulation-trace-copy.svg"),
                "Copy trace to clipboard");
        pasteStateButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-simulation-trace-paste.svg"),
                "Paste trace from clipboard");
        mergeTraceButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-simulation-trace-merge.svg"),
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

        tablePanel = new JScrollPane(traceTable);
        tablePanel.setPreferredSize(new Dimension(1, 1));

        speedSlider.addChangeListener(e -> {
            if (timer != null) {
                timer.stop();
                timer.setInitialDelay(getAnimationDelay());
                timer.setDelay(getAnimationDelay());
                timer.start();
            }
            updateState(editor);
        });

        playButton.addActionListener(event -> {
            if (timer == null) {
                timer = new Timer(getAnimationDelay(), event1 -> step(editor));
                timer.start();
            } else {
                timer.stop();
                timer = null;
            }
            updateState(editor);
            editor.requestFocus();
        });

        stopButton.addActionListener(event -> {
            reset(editor);
            setDecoration(editor, simuAlg.getEnabledNodes(sync, phases, isRev));
        });

        backwardButton.addActionListener(event -> stepBack(editor));

        forwardButton.addActionListener(event -> step(editor));

        reverseButton.addActionListener(event -> {
            Map<PlaceNode, Boolean> currentMarking = readSONMarking(net);
            setReverse(editor, !isRev);
            writeModelState(currentMarking);
            setDecoration(editor, simuAlg.getEnabledNodes(sync, phases, isRev));
            excitedContainers.clear();
            updateState(editor);
            editor.requestFocus();
        });

        autoSimuButton.addActionListener(event -> {
            if (autoSimuButton.isSelected()) {
                if (!acyclicChecker(editor)) {
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
        });

        errorButton.addActionListener(event -> {
            SONSettings.setErrorTracing(!SONSettings.isErrorTracing());
            editor.repaint();
        });

        copyStateButton.addActionListener(event -> copyState(editor));

        pasteStateButton.addActionListener(event -> pasteState(editor));

        mergeTraceButton.addActionListener(event -> mergeTrace(editor));

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
        panel.add(tablePanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        editor.getWorkspaceEntry().captureMemento();

        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();
        BlockConnector.blockBoundingConnector(visualNet);

        errAlg = new ErrorTracingAlg(net);
        initialise(editor);
        reset(editor);
        setDecoration(editor, simuAlg.getEnabledNodes(sync, phases, isRev));

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

    protected void initialise(final GraphEditor editor) {
        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();
        relationAlg = new RelationAlgorithm(net);
        bsonAlg = new BSONAlg(net);
        simuAlg = new SimulationAlg(net);
        initialMarking = simuAlg.getInitialMarking();
        sync = getSyncEventCycles(net);
        phases = bsonAlg.getAllPhases();
    }

    protected Collection<Path> getSyncEventCycles(final SON net) {
        HashSet<Node> nodes = new HashSet<>();
        nodes.addAll(net.getTransitionNodes());
        nodes.addAll(net.getChannelPlaces());
        CSONCycleAlg cycleAlg = new CSONCycleAlg(net);

        return cycleAlg.syncEventCycleTask(nodes);
    }

    public void updateState(final GraphEditor editor) {
        if (timer == null) {
            playButton.setIcon(GuiUtils.createIconFromSVG("images/son-simulation-play.svg"));
        } else {
            if (branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress())) {
                playButton.setIcon(GuiUtils.createIconFromSVG("images/son-simulation-pause.svg"));
                timer.setDelay(getAnimationDelay());
            } else {
                playButton.setIcon(GuiUtils.createIconFromSVG("images/son-simulation-play.svg"));
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
            reverseButton.setIcon(GuiUtils.createIconFromSVG("images/son-simulation-progress.svg"));
            reverseButton.setToolTipText("Switch to reverse simulation");
        } else {
            reverseButton.setIcon(GuiUtils.createIconFromSVG("images/son-simulation-reverse.svg"));
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
        JOptionPane.showMessageDialog(mainWindow, message,
                "Invalid structure", JOptionPane.WARNING_MESSAGE);

        reset(editor);
    }

    protected Map<PlaceNode, Boolean> readSONMarking(final SON net) {
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
            step = getStep(editor, stepRef);
            setReverse(editor, stepRef);
            branchInc = 1;
        } else if (mainTrace.canProgress()) {
            StepRef stepRef = mainTrace.getCurrent();
            step = getStep(editor, stepRef);
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
            setDecoration(editor, simuAlg.getEnabledNodes(sync, phases, isRev));
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
            step = getStep(editor, stepRef);
            setReverse(editor, stepRef);
            branchDec = 1;
        } else if (mainTrace.getPosition() > 0) {
            StepRef stepRef = mainTrace.get(mainTrace.getPosition() - 1);
            step = getStep(editor, stepRef);
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
            setDecoration(editor, simuAlg.getEnabledNodes(sync, phases, isRev));
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
            } catch (UnsupportedFlavorException | IOException ex) {
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

            // Get high level events
            for (TransitionNode node : step) {
                if (bsonAlg.isUpperEvent(node)) {
                    upperEvents.add(node);
                }
            }
            // Get low level events
            step.removeAll(upperEvents);

            if (!isRev) {
                // Set error number for upper events
                errAlg.setErrNum(upperEvents, sync, phases, false);
                // Set error number for lower events
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
        if (!acyclicChecker(editor)) {
            autoSimuButton.setSelected(false);
        } else {
            autoSimulationTask(editor);
        }
    }

    protected boolean acyclicChecker(final GraphEditor editor) {
        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();
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
        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();
        step = conflictFilter(net, step);
        if (!step.isEmpty()) {
            step = simuAlg.getMinFire(step.iterator().next(), sync, step, isRev);
            executeEvents(editor, step);
            autoSimulationTask(editor);
        }
    }

    protected Step conflictFilter(final SON net, Step step) {
        Step result = new Step();
        result.addAll(step);

        for (PlaceNode c : readSONMarking(net).keySet()) {
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

    public Map<PlaceNode, Boolean> reachabilitySimulator(final GraphEditor editor,
            Collection<String> causalPredecessorRefs, Collection<String> markingRefs) {

        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();
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

        Step enabled = simuAlg.getEnabledNodes(sync, phases, isRev);

        Step step = new Step();
        for (Node node : relationAlg.getCommonElements(enabled, causalPredecessors)) {
            if (node instanceof TransitionNode) {
                step.add((TransitionNode) node);
            }
        }

        if (!step.isEmpty()) {
            executeEvents(editor, step);
            reachabilitySimulationTask(editor, causalPredecessors, markingRefs);
        }

        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();
        for (String ref : markingRefs) {
            Node node = net.getNodeByReference(ref);
            if (node instanceof PlaceNode) {
                ((PlaceNode) node).setForegroundColor(Color.BLUE);
                ((PlaceNode) node).setTokenColor(Color.BLUE);
            }
        }

        return readSONMarking(net);
    }

    public void executeEvents(final GraphEditor editor, Step step) {
        if (step.isEmpty()) return;
        Step traceList = new Step();
        // If clicked on the trace event, do the step forward
        if (branchTrace.isEmpty() && !mainTrace.isEmpty() && (mainTrace.getPosition() < mainTrace.size())) {
            StepRef stepRef = mainTrace.get(mainTrace.getPosition());
            traceList = getStep(editor, stepRef);
        }
        // Otherwise form/use the branch trace
        if (!branchTrace.isEmpty() && (branchTrace.getPosition() < branchTrace.size())) {
            StepRef stepRef = branchTrace.get(branchTrace.getPosition());
            traceList = getStep(editor, stepRef);
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

        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();
        for (TransitionNode e : step) {
            newStep.add(net.getNodeReference(e));
        }

        branchTrace.add(newStep);
        step(editor);
    }

    protected Step getStep(final GraphEditor editor, StepRef stepRef) {
        Step result = new Step();
        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();
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
        private final JLabel label = new JLabel() {
            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                super.paint(g);
            }
        };

        private boolean isActive(int row, int column) {
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

            label.setText(value.toString());

            if (isActive(row, column)) {
                label.setBackground(Color.YELLOW);
            } else {
                label.setBackground(Color.WHITE);
            }

            return label;
        }
    }

    protected void setDecoration(final GraphEditor editor, Step enabled) {
        final VisualSON visualNet = (VisualSON) editor.getModel();
        final SON net = visualNet.getMathModel();
        net.refreshAllColor();

        for (TransitionNode e : enabled) {
            e.setForegroundColor(SimulationDecorationSettings.getExcitedComponentColor());
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

        Node deepestNode = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(),
                node -> {
                    if (node instanceof VisualTransitionNode) {
                        TransitionNode transitionNode = ((VisualTransitionNode) node).getMathTransitionNode();
                        Step enabled = simuAlg.getEnabledNodes(sync, phases, isRev);
                        return isEnabled(transitionNode, enabled);
                    }
                    return false;
                });

        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();

        if (deepestNode instanceof VisualTransitionNode) {

            Step enabled = null;
            TransitionNode select = ((VisualTransitionNode) deepestNode).getMathTransitionNode();

            enabled = simuAlg.getEnabledNodes(sync, phases, isRev);

            Step minFire = simuAlg.getMinFire(select, sync, enabled, isRev);
            Step possibleFire = simuAlg.getMinFire(select, sync, enabled, !isRev);
            //remove select node and directed sync cycle
            possibleFire.removeAll(minFire);
            possibleFire.remove(select);

            Step step = new Step();
            step.addAll(minFire);

            minFire.remove(select);

            GraphEditor editor = e.getEditor();
            if (possibleFire.isEmpty()) {
                executeEvents(editor, step);
            } else {
                editor.requestFocus();
                final VisualSON visualNet = (VisualSON) editor.getModel();
                final SON net = visualNet.getMathModel();
                ParallelSimDialog dialog = new ParallelSimDialog(mainWindow,
                        net, possibleFire, minFire, select, isRev, sync);
                dialog.setVisible(true);
                if (dialog.getModalResult() == 1) {
                    step.addAll(dialog.getSelectedEvent());
                    executeEvents(editor, step);
                }
                if (dialog.getModalResult() == 2) {
                    setDecoration(editor, enabled);
                    return;
                }
            }

            if ((autoSimuButton != null) && autoSimuButton.isSelected()) {
                autoSimulator(editor);
            }
        }
    }

    protected boolean isContainerExcited(Container container) {
        if (excitedContainers.containsKey(container)) return excitedContainers.get(container);

        boolean ret = false;

        for (Node node: container.getChildren()) {
            Step enabled = null;
            enabled = simuAlg.getEnabledNodes(sync, phases, isRev);

            if (node instanceof VisualTransitionNode) {
                TransitionNode event = ((VisualTransitionNode) node).getMathTransitionNode();
                ret = ret || isEnabled(event, enabled);
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

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

}
