package org.workcraft.gui.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.controls.FlatHeaderRenderer;
import org.workcraft.gui.controls.SpeedSlider;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Func;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.TraceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SimulationTool extends AbstractGraphEditorTool implements ClipboardOwner {

    private static final ImageIcon PLAY_ICON = GuiUtils.createIconFromSVG("images/simulation-play.svg");
    private static final ImageIcon PAUSE_ICON = GuiUtils.createIconFromSVG("images/simulation-pause.svg");
    private static final ImageIcon BACKWARD_ICON = GuiUtils.createIconFromSVG("images/simulation-backward.svg");
    private static final ImageIcon FORWARD_ICON = GuiUtils.createIconFromSVG("images/simulation-forward.svg");
    private static final ImageIcon RECORD_ICON = GuiUtils.createIconFromSVG("images/simulation-record.svg");
    private static final ImageIcon STOP_ICON = GuiUtils.createIconFromSVG("images/simulation-stop.svg");
    private static final ImageIcon EJECT_ICON = GuiUtils.createIconFromSVG("images/simulation-eject.svg");
    private static final ImageIcon TIMING_DIAGRAM_ICON = GuiUtils.createIconFromSVG("images/simulation-trace-graph.svg");
    private static final ImageIcon COPY_STATE_ICON = GuiUtils.createIconFromSVG("images/simulation-trace-copy.svg");
    private static final ImageIcon PASTE_STATE_ICON = GuiUtils.createIconFromSVG("images/simulation-trace-paste.svg");
    private static final ImageIcon MERGE_TRACE_ICON = GuiUtils.createIconFromSVG("images/simulation-trace-merge.svg");
    private static final ImageIcon SAVE_INITIAL_STATE_ICON = GuiUtils.createIconFromSVG("images/simulation-marking-save.svg");

    private static final String PLAY_HINT = "Play through the trace";
    private static final String PAUSE_HINT = "Pause trace playback";
    private static final String BACKWARD_HINT = "Step backward ([)";
    private static final String FORWARD_HINT = "Step forward (])";
    private static final String RECORD_HINT = "Generate a random trace";
    private static final String STOP_HINT = "Stop trace generation";
    private static final String EJECT_HINT = "Reset the trace";
    private static final String TIMING_DIAGRAM_HINT = "Generate trace timing diagram";
    private static final String COPY_STATE_HINT = "Copy trace to clipboard";
    private static final String PASTE_STATE_HINT = "Paste trace from clipboard";
    private static final String MERGE_TRACE_HINT = "Merge branch into trace";
    private static final String SAVE_INITIAL_STATE_HINT = "Save current state as initial";

    private MathModel underlyingModel;

    protected JPanel controlPanel;
    protected JPanel infoPanel;
    protected JSplitPane splitPane;
    protected JScrollPane tracePane;
    protected JScrollPane statePane;
    protected JTable traceTable;

    private SpeedSlider speedSlider;
    private JButton playButton;
    private JButton backwardButton;
    private JButton forwardButton;
    private JButton recordButton;
    private JButton ejectButton;
    private JPanel panel;

    // cache of "excited" containers (the ones containing the excited simulation elements)
    protected HashMap<Container, Boolean> excitedContainers = new HashMap<>();

    protected Map<? extends MathNode, Integer> initialState;
    public HashMap<? extends MathNode, Integer> savedState;
    protected final Trace mainTrace = new Trace();
    protected final Trace branchTrace = new Trace();
    private  int loopPosition = -1;

    private Timer timer = null;
    private boolean random = false;

    private final boolean enableTraceGraph;

    public SimulationTool(boolean enableTraceGraph) {
        super();
        this.enableTraceGraph = enableTraceGraph;
    }

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel != null) {
            return panel;
        }

        playButton = GuiUtils.createIconButton(PLAY_ICON, PLAY_HINT);
        backwardButton = GuiUtils.createIconButton(BACKWARD_ICON, BACKWARD_HINT);
        forwardButton = GuiUtils.createIconButton(FORWARD_ICON, FORWARD_HINT);
        recordButton = GuiUtils.createIconButton(RECORD_ICON, RECORD_HINT);
        ejectButton = GuiUtils.createIconButton(EJECT_ICON, EJECT_HINT);

        speedSlider = new SpeedSlider();

        JButton generateGraphButton = GuiUtils.createIconButton(TIMING_DIAGRAM_ICON, TIMING_DIAGRAM_HINT);
        JButton copyStateButton = GuiUtils.createIconButton(COPY_STATE_ICON, COPY_STATE_HINT);
        JButton pasteStateButton = GuiUtils.createIconButton(PASTE_STATE_ICON, PASTE_STATE_HINT);
        JButton mergeTraceButton = GuiUtils.createIconButton(MERGE_TRACE_ICON, MERGE_TRACE_HINT);
        JButton saveInitStateButton = GuiUtils.createIconButton(SAVE_INITIAL_STATE_ICON, SAVE_INITIAL_STATE_HINT);

        JPanel simulationControl = new JPanel();
        simulationControl.add(playButton);
        simulationControl.add(backwardButton);
        simulationControl.add(forwardButton);
        simulationControl.add(recordButton);
        simulationControl.add(ejectButton);
        GuiUtils.setButtonPanelLayout(simulationControl, playButton.getPreferredSize());

        JPanel speedControl = new JPanel();
        speedControl.add(speedSlider);
        GuiUtils.setButtonPanelLayout(speedControl, speedSlider.getPreferredSize());

        JPanel traceControl = new JPanel();
        if (enableTraceGraph) {
            traceControl.add(generateGraphButton);
        }
        traceControl.add(copyStateButton);
        traceControl.add(pasteStateButton);
        traceControl.add(mergeTraceButton);
        traceControl.add(saveInitStateButton);
        GuiUtils.setButtonPanelLayout(simulationControl, copyStateButton.getPreferredSize());

        controlPanel = new JPanel();
        controlPanel.setLayout(new WrapLayout());
        controlPanel.add(simulationControl);
        controlPanel.add(speedControl);
        controlPanel.add(traceControl);

        traceTable = new JTable(new TraceTableModel());
        traceTable.getTableHeader().setDefaultRenderer(new FlatHeaderRenderer());
        traceTable.getTableHeader().setReorderingAllowed(false);
        traceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        traceTable.setRowHeight(SizeHelper.getComponentHeightFromFont(traceTable.getFont()));
        traceTable.setDefaultRenderer(Object.class, new TraceTableCellRenderer());

        tracePane = new JScrollPane();
        tracePane.setViewportView(traceTable);
        tracePane.setMinimumSize(new Dimension(1, 50));

        statePane = new JScrollPane();
        statePane.setMinimumSize(new Dimension(1, 50));

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tracePane, statePane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.5);

        infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.add(splitPane, BorderLayout.CENTER);
        speedSlider.addChangeListener(e -> {
            if (timer != null) {
                timer.stop();
                int delay = speedSlider.getDelay();
                timer.setInitialDelay(delay);
                timer.setDelay(delay);
                timer.start();
            }
            updateState(editor);
            editor.requestFocus();
        });

        recordButton.addActionListener(event -> {
            if (timer == null) {
                timer = new Timer(speedSlider.getDelay(), event1 -> stepRandom(editor));
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
        });

        playButton.addActionListener(event -> {
            if (timer == null) {
                timer = new Timer(speedSlider.getDelay(), event1 -> stepForward(editor));
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
        });

        ejectButton.addActionListener(event -> {
            clearTraces(editor);
            editor.requestFocus();
        });

        backwardButton.addActionListener(event -> {
            stepBackward(editor);
            editor.requestFocus();
        });

        forwardButton.addActionListener(event -> {
            stepForward(editor);
            editor.requestFocus();
        });

        generateGraphButton.addActionListener(event -> {
            generateTraceGraph(editor);
            editor.requestFocus();
        });

        copyStateButton.addActionListener(event -> {
            copyState(editor);
            editor.requestFocus();
        });

        pasteStateButton.addActionListener(event -> {
            pasteState(editor);
            editor.requestFocus();
        });

        mergeTraceButton.addActionListener(event -> {
            mergeTrace(editor);
            editor.requestFocus();
        });

        saveInitStateButton.addActionListener(event -> {
            savedState = readUnderlyingModelState();
            editor.requestFocus();
        });

        traceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = traceTable.getSelectedColumn();
                int row = traceTable.getSelectedRow();
                if (col == 0) {
                    if (row < mainTrace.size()) {
                        boolean hasProgress = true;
                        while (hasProgress && (branchTrace.getPosition() > 0)) {
                            hasProgress = quietStepBackward();
                        }
                        while (hasProgress && (mainTrace.getPosition() > row)) {
                            hasProgress = quietStepBackward();
                        }
                        while (hasProgress && (mainTrace.getPosition() < row)) {
                            hasProgress = quietStepForward();
                        }
                    }
                } else {
                    if ((row >= mainTrace.getPosition()) && (row < mainTrace.getPosition() + branchTrace.size())) {
                        boolean hasProgress = true;
                        while (hasProgress && (mainTrace.getPosition() + branchTrace.getPosition() > row)) {
                            hasProgress = quietStepBackward();
                        }
                        while (hasProgress && (mainTrace.getPosition() + branchTrace.getPosition() < row)) {
                            hasProgress = quietStepForward();
                        }
                    }
                }
                updateState(editor);
                editor.requestFocus();
            }
        });

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    public void setStatePaneVisibility(boolean visible) {
        statePane.setVisible(visible);
        splitPane.setDividerSize(visible ? 10 : 0);
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        WorkspaceEntry we = editor.getWorkspaceEntry();
        generateUnderlyingModel(we);
        we.captureMemento();
        initialState = readUnderlyingModelState();
        setStatePaneVisibility(false);
        resetTraces(editor);
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
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(false);
        we.setCanSelect(false);
        we.setCanCopy(false);
    }

    public void generateUnderlyingModel(WorkspaceEntry we) {
        underlyingModel = we.getModelEntry().getMathModel();
    }

    public MathModel getUnderlyingModel() {
        return underlyingModel;
    }

    public MathNode getUnderlyingNode(String ref) {
        MathModel underlyingModel = getUnderlyingModel();
        return (ref == null) || (underlyingModel == null) ? null : underlyingModel.getNodeByReference(ref);
    }

    public boolean isActivated() {
        return getUnderlyingModel() != null;
    }

    public void updateState(final GraphEditor editor) {
        if (timer == null) {
            playButton.setIcon(PLAY_ICON);
            playButton.setToolTipText(PLAY_HINT);
            recordButton.setIcon(RECORD_ICON);
            recordButton.setToolTipText(RECORD_HINT);
        } else {
            if (random) {
                playButton.setIcon(PLAY_ICON);
                playButton.setToolTipText(PLAY_HINT);
                recordButton.setIcon(STOP_ICON);
                recordButton.setToolTipText(STOP_HINT);
                timer.setDelay(speedSlider.getDelay());
            } else if (branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress())) {
                playButton.setIcon(PAUSE_ICON);
                playButton.setToolTipText(PAUSE_HINT);
                recordButton.setIcon(RECORD_ICON);
                timer.setDelay(speedSlider.getDelay());
            } else {
                playButton.setIcon(PLAY_ICON);
                playButton.setToolTipText(PLAY_HINT);
                recordButton.setIcon(RECORD_ICON);
                recordButton.setToolTipText(RECORD_HINT);
                timer.stop();
                timer = null;
            }
        }
        playButton.setEnabled(branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress()));
        ejectButton.setEnabled(!mainTrace.isEmpty() || !branchTrace.isEmpty());
        backwardButton.setEnabled((mainTrace.getPosition() > 0) || (branchTrace.getPosition() > 0));
        forwardButton.setEnabled(branchTrace.canProgress() || (branchTrace.isEmpty() && mainTrace.canProgress()));
        traceTable.tableChanged(new TableModelEvent(traceTable.getModel()));
        editor.repaint();
    }

    public void scrollTraceToBottom() {
        JScrollBar verticalScrollBar = tracePane.getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());
    }

    private boolean quietStepBackward() {
        excitedContainers.clear();

        boolean result = false;
        String ref = null;
        boolean decMain = false;
        boolean decBranch = false;
        if (branchTrace.getPosition() > 0) {
            ref = branchTrace.get(branchTrace.getPosition() - 1);
            decBranch = true;
        } else if (mainTrace.getPosition() > 0) {
            ref = mainTrace.get(mainTrace.getPosition() - 1);
            decMain = true;
        }
        if (unfire(ref)) {
            if (decMain) {
                mainTrace.decPosition();
            }
            if (decBranch) {
                branchTrace.decPosition();
            }
            if ((branchTrace.getPosition() == 0) && !mainTrace.isEmpty()) {
                branchTrace.clear();
            }
            result = true;
        }
        return result;
    }

    private void stepBackward(final GraphEditor editor) {
        quietStepBackward();
        updateState(editor);
    }

    private boolean quietStepForward() {
        excitedContainers.clear();

        boolean result = false;
        String ref = null;
        boolean incMain = false;
        boolean incBranch = false;
        if (branchTrace.canProgress()) {
            ref = branchTrace.getCurrent();
            incBranch = true;
        } else if (mainTrace.canProgress()) {
            ref = mainTrace.getCurrent();
            incMain = true;
        }
        if (fire(ref)) {
            if (incMain) {
                mainTrace.incPosition();
            }
            if (incBranch) {
                branchTrace.incPosition();
            }
            result = true;
            if (!branchTrace.canProgress() && !mainTrace.canProgress() && (loopPosition >= 0)) {
                mainTrace.setPosition(loopPosition);
            }
        }
        return result;
    }

    private void stepForward(final GraphEditor editor) {
        quietStepForward();
        updateState(editor);
    }

    private void stepRandom(final GraphEditor editor) {
        ArrayList<? extends MathNode> enabledUnderlyingNodes = getEnabledUnderlyingNodes();
        if (!enabledUnderlyingNodes.isEmpty()) {
            int randomIndex = (int) (Math.random() * enabledUnderlyingNodes.size());
            MathNode underlyingNode = enabledUnderlyingNodes.get(randomIndex);
            executeUnderlyingNode(editor, underlyingNode);
        }
    }

    private void resetTraces(final GraphEditor editor) {
        writeUnderlyingModelState(initialState);
        mainTrace.setPosition(0);
        branchTrace.clear();
        updateState(editor);
    }

    private void clearTraces(final GraphEditor editor) {
        writeUnderlyingModelState(initialState);
        mainTrace.clear();
        branchTrace.clear();
        loopPosition = -1;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        updateState(editor);
    }

    public void generateTraceGraph(final GraphEditor editor) {
    }

    private void copyState(final GraphEditor editor) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Solution solution = new Solution(mainTrace, branchTrace);
        solution.setLoopPosition(loopPosition);
        StringSelection stringSelection = new StringSelection(TraceUtils.serialiseSolution(solution));
        clip.setContents(stringSelection, this);
        updateState(editor);
    }

    private void pasteState(final GraphEditor editor) {
        String str = getClipboardText();
        writeUnderlyingModelState(initialState);
        Solution solution = TraceUtils.deserialiseSolution(str);
        mainTrace.clear();
        if (solution.getMainTrace() != null) {
            mainTrace.addAll(solution.getMainTrace());
            while (mainTrace.getPosition() < solution.getMainTrace().getPosition()) {
                if (!quietStepForward()) break;
            }
        }
        branchTrace.clear();
        if (solution.getBranchTrace() != null) {
            branchTrace.addAll(solution.getBranchTrace());
            while (branchTrace.getPosition() < solution.getBranchTrace().getPosition()) {
                if (!quietStepForward()) break;
            }
        }
        loopPosition = solution.getLoopPosition();
        updateState(editor);
    }

    private String getClipboardText() {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clip.getContents(null);
        boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        String result = "";
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                System.out.println(e);
            }
        }
        return result;
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
            loopPosition = -1;
            mainTrace.addAll(combinedTrace);
            mainTrace.setPosition(combinedTrace.getPosition());
        }
        updateState(editor);
    }

    private final class TraceTableCellRenderer implements TableCellRenderer {
        private final JLabel label = new JLabel() {
            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paint(g);
            }
        };

        private boolean isActive(int row, int column) {
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
                boolean isSelected, boolean hasFocus, int row, int col) {

            JLabel result = null;
            label.setBorder(GuiUtils.getTableCellBorder());
            if (isActivated() && (value instanceof String)) {
                label.setText(value.toString());
                if (isActive(row, col)) {
                    label.setForeground(table.getSelectionForeground());
                    label.setBackground(table.getSelectionBackground());
                } else {
                    label.setForeground(table.getForeground());
                    label.setBackground(table.getBackground());
                }
                boolean fits = GuiUtils.getLabelTextWidth(label) < GuiUtils.getTableColumnTextWidth(table, col);
                label.setToolTipText(fits ? null : label.getText());
                result = label;
            }
            return result;
        }
    }

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

            String result = getTraceLabelByReference(ref);
            if ((result != null) && (loopPosition >= 0) && (column == 0) && (row >= loopPosition)) {
                result = TraceUtils.addLoopDecoration(result, row == loopPosition, row == mainTrace.size() - 1);
            }
            return result;
        }
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET) {
            stepBackward(e.getEditor());
            return true;
        }
        if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET) {
            stepForward(e.getEditor());
            return true;
        }
        return super.keyPressed(e);
    }

    public void executeUnderlyingNode(final GraphEditor editor, MathNode candidateNode) {
        if (candidateNode == null) return;

        String ref = null;
        // If clicked on the trace event, do the step forward.
        if (branchTrace.isEmpty() && !mainTrace.isEmpty() && (mainTrace.getPosition() < mainTrace.size())) {
            ref = mainTrace.get(mainTrace.getPosition());
        }
        // Otherwise form/use the branch trace.
        if (!branchTrace.isEmpty() && (branchTrace.getPosition() < branchTrace.size())) {
            ref = branchTrace.get(branchTrace.getPosition());
        }
        Node node = getUnderlyingNode(ref);
        if (node == candidateNode) {
            stepForward(editor);
            return;
        }
        while (branchTrace.getPosition() < branchTrace.size()) {
            branchTrace.removeCurrent();
        }
        if (getUnderlyingModel() != null) {
            String candidateRef = getUnderlyingModel().getNodeReference(candidateNode);
            if (candidateRef != null) {
                branchTrace.add(candidateRef);
            }
        }
        stepForward(editor);
        scrollTraceToBottom();
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            VisualModel model = e.getModel();
            Func<Node, Boolean> filter = node -> {
                if (node instanceof VisualComponent) {
                    String ref = model.getMathReference(node);
                    MathNode underlyingNode = getUnderlyingNode(ref);
                    return isEnabledUnderlyingNode(underlyingNode);
                }
                return false;
            };
            Node deepestNode = HitMan.hitDeepest(e.getPosition(), model.getRoot(), filter);
            if (deepestNode instanceof VisualComponent) {
                String ref = model.getMathReference(deepestNode);
                MathNode underlyingNode = getUnderlyingNode(ref);
                executeUnderlyingNode(e.getEditor(), underlyingNode);
            }
        }
    }

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
        return GuiUtils.createIconFromSVG("images/tool-simulation.svg");
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    public void setTraces(Trace mainTrace, Trace branchTrace, int loopPosition, GraphEditor editor) {
        this.mainTrace.clear();
        if (mainTrace != null) {
            this.mainTrace.addAll(mainTrace);
        }
        this.branchTrace.clear();
        if (branchTrace != null) {
            this.branchTrace.addAll(branchTrace);
        }
        this.loopPosition = loopPosition;
        updateState(editor);
    }

    public MathNode getCurrentUnderlyingNode() {
        String ref = null;
        if (branchTrace.canProgress()) {
            ref = branchTrace.getCurrent();
        } else if (branchTrace.isEmpty() && mainTrace.canProgress()) {
            ref = mainTrace.getCurrent();
        }
        return getUnderlyingNode(ref);
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {
            VisualModel model = editor.getModel();
            if ((node instanceof VisualPage) || (node instanceof VisualGroup)) {
                return getContainerDecoration(model, (Container) node);
            }
            if (node instanceof VisualComponent) {
                return getComponentDecoration(model, (VisualComponent) node);
            }
            if (node instanceof VisualConnection) {
                return getConnectionDecoration(model, (VisualConnection) node);
            }
            return null;
        };
    }

    public Decoration getComponentDecoration(VisualModel model, VisualComponent component) {
        String ref = model.getMathReference(component);
        MathNode node = getUnderlyingNode(ref);
        final boolean isExcited = isEnabledUnderlyingNode(node);
        MathNode currentNode = getCurrentUnderlyingNode();
        final boolean isSuggested = isExcited && (node == currentNode);
        return new Decoration() {
            @Override
            public Color getColorisation() {
                return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
            }
            @Override
            public Color getBackground() {
                return isSuggested ? SimulationDecorationSettings.getSuggestedComponentColor() : null;
            }
        };
    }

    public Decoration getConnectionDecoration(VisualModel model, VisualConnection connection) {
        final boolean isExcited = isConnectionExcited(model, connection);
        return new Decoration() {
            @Override
            public Color getColorisation() {
                return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
            }
            @Override
            public Color getBackground() {
                return null;
            }
        };
    }

    public abstract boolean isConnectionExcited(VisualModel model, VisualConnection connection);

    public Decoration getContainerDecoration(VisualModel model, Container container) {
        final boolean isExcited = isContainerExcited(model, container);
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
                return isExcited;
            }
        };
    }

    public boolean isContainerExcited(VisualModel model, Container container) {
        if (excitedContainers.containsKey(container)) {
            return excitedContainers.get(container);
        }
        boolean result = false;
        for (Node node : container.getChildren()) {
            if (node instanceof VisualComponent) {
                String ref = model.getMathReference(node);
                result = isEnabledUnderlyingNode(getUnderlyingNode(ref));
            } else if (node instanceof Container) {
                result = isContainerExcited(model, (Container) node);
            }
            if (result) break;
        }
        excitedContainers.put(container, result);
        return result;
    }

    @Override
    public void lostOwnership(Clipboard clip, Transferable arg) {
    }

    public String getTraceLabelByReference(String ref) {
        return ref;
    }

    public abstract HashMap<? extends MathNode, Integer> readUnderlyingModelState();

    public abstract void writeUnderlyingModelState(Map<? extends MathNode, Integer> state);

    public abstract void applySavedState(GraphEditor editor);

    public abstract ArrayList<? extends MathNode> getEnabledUnderlyingNodes();

    public abstract boolean isEnabledUnderlyingNode(MathNode node);

    public abstract boolean fire(String ref);

    public abstract boolean unfire(String ref);

}
