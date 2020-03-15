package org.workcraft.plugins.son.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.plugins.son.BlockConnector;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.InvalidStructureException;
import org.workcraft.plugins.son.gui.ScenarioTable;
import org.workcraft.plugins.son.util.*;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class ScenarioGeneratorTool extends SONSimulationTool {

    @SuppressWarnings("serial")
    private class ScenarioTableModel extends AbstractTableModel {
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
            return Math.max(scenarioTable.getSaveList().size(), scenarioTable.getScenarioNodeRef().size());
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (column == 0) {
                ScenarioSaveList saveList = scenarioTable.getSaveList();
                if (!saveList.isEmpty() && (row < saveList.size())) {
                    return saveList.get(row);
                }
            } else {
                ArrayList<String> nodes = scenarioTable.getScenarioNodeRef();
                if (!nodes.isEmpty() && (row < nodes.size())) {
                    return nodes.get(row);
                }
            }
            return "";
        }
    }

    private static final Color GREYOUT_COLOR = Color.LIGHT_GRAY;

    private JToggleButton startButton;
    private final ScenarioTable scenarioTable = new ScenarioTable();

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel != null) {
            return panel;
        }

        startButton = GuiUtils.createIconToggleButton(GuiUtils.createIconFromSVG("images/son-scenario-start.svg"), "Generate");
        JButton resetButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-scenario-reset.svg"), "Reset");
        JButton saveButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-scenario-save.svg"), "Save");
        JButton removeButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-scenario-delete.svg"), "Remove");

        int buttonWidth = (int) Math.round(startButton.getPreferredSize().getWidth() + 5);
        int buttonHeight = (int) Math.round(startButton.getPreferredSize().getHeight() + 5);
        Dimension panelSize = new Dimension(buttonWidth * 6, buttonHeight);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.setPreferredSize(panelSize);
        controlPanel.add(new JSeparator());
        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(saveButton);
        controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlPanel.add(removeButton);

        JScrollPane tablePanel = new JScrollPane(scenarioTable);
        tablePanel.setPreferredSize(new Dimension(1, 1));

        startButton.addActionListener(event -> {
            if (startButton.isSelected()) {
                startScenario(editor);
            } else {
                SON net = scenarioTable.getNet();
                Step step = simuAlg.getEnabledNodes(sync, phases, isRev);
                setColors(net, step, GREYOUT_COLOR);
                net.clearMarking();
            }
        });

        resetButton.addActionListener(event -> {
            startButton.setSelected(true);
            startScenario(editor);
        });

        saveButton.addActionListener(event -> {
            ScenarioRef currentScenario = scenarioTable.getScenarioRef();
            if (currentScenario != null) {
                scenarioTable.setIsCellColor(true);
                ScenarioSaveList saveList = scenarioTable.getSaveList();
                if (saveList != null) {
                    ScenarioRef newScenario = new ScenarioRef();
                    // Add scenario nodes
                    newScenario.addAll(currentScenario);
                    // Add scenario connections
                    SON net = scenarioTable.getNet();
                    for (SONConnection con : currentScenario.getRuntimeConnections(net)) {
                        newScenario.add(net.getNodeReference(con));
                    }
                    saveList.add(newScenario);
                    saveList.setPosition(saveList.size() - 1);
                }
                updateState(editor);
            }
        });

        removeButton.addActionListener(event -> {
            ScenarioSaveList saveList = scenarioTable.getSaveList();
            if ((saveList != null) && !saveList.isEmpty()) {
                scenarioTable.setIsCellColor(true);
                saveList.remove(saveList.getPosition());
                ScenarioRef currentScenario = scenarioTable.getScenarioRef();
                currentScenario.clear();
                if (saveList.getPosition() > saveList.size() - 1) {
                    saveList.decPosition(1);
                }
                if (!saveList.isEmpty()) {
                    ScenarioRef savedScenarioRef = saveList.get(saveList.getPosition());
                    SON net = scenarioTable.getNet();
                    currentScenario.addAll(savedScenarioRef.getNodeRefs(net));
                }
                scenarioTable.runtimeUpdateColor();
                updateState(editor);
            }
        });

        scenarioTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = scenarioTable.getSelectedColumn();
                if (col == 0) {
                    selectScenario(editor, scenarioTable.getSelectedRow());
                }
            }
        });

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    private void startScenario(final GraphEditor editor) {
        if (!acyclicChecker(editor)) {
            startButton.setSelected(false);
            startButton.repaint();
            try {
                throw new InvalidStructureException("Cyclic structure error");
            } catch (InvalidStructureException e1) {
                errorMsg(e1.getMessage(), editor);
            }
        } else {
            final SON net = (SON) editor.getModel().getMathModel();
            net.clearMarking();
            net.refreshAllColor();
            scenarioTable.setIsCellColor(false);
            scenarioTable.getScenarioRef().clear();
            scenarioTable.getSaveList().setPosition(0);
            scenarioGenerator(editor);
        }

    }

    private void selectScenario(GraphEditor editor, int row) {
        ScenarioSaveList saveList = scenarioTable.getSaveList();
        if (row < saveList.size()) {
            saveList.setPosition(row);
            Object value = scenarioTable.getValueAt(row, 0);
            if (value instanceof ScenarioRef) {
                startButton.setSelected(false);
                scenarioTable.setScenarioRef((ScenarioRef) value);
                scenarioTable.setIsCellColor(true);
                scenarioTable.runtimeUpdateColor();
                updateState(editor);
            }
        }
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        final VisualSON visualNet = (VisualSON) editor.getModel();
        BlockConnector.blockInternalConnector(visualNet);
        final SON net = visualNet.getMathModel();
        exportScenarios(net);
        net.refreshAllColor();
        net.clearMarking();
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(false);
        we.setCanSelect(false);
        we.setCanCopy(false);
    }

    @Override
    protected void initialise(final GraphEditor editor) {
        super.initialise(editor);
        final VisualSON visualNet = (VisualSON) editor.getModel();
        BlockConnector.blockBoundingConnector(visualNet);
        final SON net = visualNet.getMathModel();
        net.refreshAllColor();
        net.clearMarking();
        scenarioTable.setModel(new ScenarioTableModel());
        scenarioTable.setNet(net);
        ScenarioSaveList saveList = net.importScenarios();
        scenarioTable.setSaveList(saveList);
        selectScenario(editor, 0);
        editor.forceRedraw();
    }

    private void exportScenarios(final SON net) {
        for (Scenario scenario: net.getScenarios()) {
            net.remove(scenario);
        }
        int i = 1;
        for (ScenarioRef scenario : scenarioTable.getSaveList()) {
            net.createScenario("Scenario" + i++, scenario);
        }
    }

    protected void scenarioGenerator(final GraphEditor editor) {
        final SON net = (SON) editor.getModel().getMathModel();
        writeModelState(initialMarking);
        MarkingRef markingRef = new MarkingRef();
        ArrayList<PlaceNode> currentMarking = getCurrentMarking(net);
        markingRef.addAll(net.getNodeRefs(currentMarking));
        scenarioTable.getScenarioRef().addAll(markingRef);
        updateState(editor);

        Step step = simuAlg.getEnabledNodes(sync, phases, false);
        setDecoration(editor, step);
        autoSimulator(editor);
    }

    @Override
    protected void autoSimulator(final GraphEditor editor) {
        final SON net = (SON) editor.getModel().getMathModel();
        autoSimulationTask(editor);
        Collection<Node> nodes = new ArrayList<>();
        ScenarioRef scenarioRef = scenarioTable.getScenarioRef();
        nodes.addAll(scenarioRef.getNodes(net));
        nodes.addAll(scenarioRef.getRuntimeConnections(net));
        setColors(net, nodes, Color.BLACK);
    }

    @Override
    protected void autoSimulationTask(final GraphEditor editor) {
        Step step = simuAlg.getEnabledNodes(sync, phases, false);

        if (step.isEmpty()) {
            startButton.setSelected(false);
        }
        final SON net = (SON) editor.getModel().getMathModel();
        step = conflictFilter(net, step);
        if (!step.isEmpty()) {
            step = simuAlg.getMinFire(step.iterator().next(), sync, step, false);
            executeEvents(editor, step);
            autoSimulationTask(editor);
        }
    }

    @Override
    public void updateState(final GraphEditor editor) {
        scenarioTable.updateTable();
        final SON net = (SON) editor.getModel().getMathModel();
        exportScenarios(net);
        editor.requestFocus();
    }

    @Override
    public void executeEvents(final GraphEditor editor, Step step) {
        final SON net = (SON) editor.getModel().getMathModel();
        ArrayList<PlaceNode> oldMarking = new ArrayList<>();
        oldMarking.addAll(getCurrentMarking(net));
        scenarioTable.setIsCellColor(false);
        super.executeEvents(editor, step);

        ScenarioRef scenarioRef = scenarioTable.getScenarioRef();
        // Add step references
        StepRef stepRef = new StepRef();
        stepRef.addAll(net.getNodeRefs(step));
        scenarioRef.addAll(stepRef);
        // Add marking references
        MarkingRef markingRef = new MarkingRef();
        ArrayList<PlaceNode> marking = new ArrayList<>();
        marking.addAll(getCurrentMarking(net));
        marking.addAll(getSyncChannelPlaces(net, step));
        markingRef.addAll(net.getNodeRefs(marking));
        for (String str : markingRef) {
            if (!scenarioRef.contains(str)) {
                scenarioRef.add(str);
            }
        }
    }

    private ArrayList<PlaceNode> getCurrentMarking(final SON net) {
        ArrayList<PlaceNode> result = new ArrayList<>();
        for (PlaceNode c : readSONMarking(net).keySet()) {
            if (c.isMarked()) {
                result.add(c);
            }
        }
        return result;
    }

    //get channel places in a synchronous step.
    private Collection<ChannelPlace> getSyncChannelPlaces(final SON net, Step step) {
        HashSet<ChannelPlace> result = new HashSet<>();
        for (TransitionNode e : step) {
            for (SONConnection con : net.getSONConnections((MathNode) e)) {
                if (con.getSemantics() == Semantics.ASYNLINE || con.getSemantics() == Semantics.SYNCLINE) {
                    if (con.getFirst() == e) {
                        result.add((ChannelPlace) con.getSecond());
                    } else {
                        result.add((ChannelPlace) con.getFirst());
                    }
                }
            }
        }
        return result;
    }

    private void setColors(final SON net, Collection<? extends Node> nodes, Color color) {
        for (Node node : nodes) {
            net.setForegroundColor(node, color);
        }
    }

    @Override
    protected void setDecoration(final GraphEditor editor, Step enabled) {
        if (startButton.isSelected()) {
            final SON net = (SON) editor.getModel().getMathModel();
            setColors(net, net.getNodes(), GREYOUT_COLOR);
            for (TransitionNode e : enabled) {
                e.setForegroundColor(SimulationDecorationSettings.getExcitedComponentColor());
            }
        }
    }

    @Override
    public String getLabel() {
        return "Scenario Generator";
    }

    @Override
    public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
        GuiUtils.drawEditorMessage(editor, g, Color.BLACK, "Click on the highlight node to choose a scenario.");
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_G;
    }

    @Override
    public Icon getIcon() {
        return GuiUtils.createIconFromSVG("images/son-tool-scenario.svg");
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (startButton.isSelected()) {
            super.mousePressed(e);
            autoSimulator(e.getEditor());
        }
    }

    @Override
    public Decorator getDecorator(GraphEditor editor) {
        return node -> null;
    }

}
