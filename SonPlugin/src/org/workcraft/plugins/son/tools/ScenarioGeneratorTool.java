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
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class ScenarioGeneratorTool extends SONSimulationTool {

    private static final Color GREYOUT_COLOR = Color.LIGHT_GRAY;

    protected JButton saveButton;
    protected JButton removeButton;
    protected JButton resetButton;
    protected JToggleButton startButton;
    protected ScenarioTable scenarioTable;

    protected ScenarioRef scenarioRef;
    protected ScenarioSaveList saveList;

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        // FIXME: Currently the panel needs to be re-created on every activation as saveList and scenarioTable are updated from the net.

        startButton = GuiUtils.createIconToggleButton(GuiUtils.createIconFromSVG("images/son-scenario-start.svg"), "Generate");
        resetButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-scenario-reset.svg"), "Reset");
        saveButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-scenario-save.svg"), "Save");
        removeButton = GuiUtils.createIconButton(GuiUtils.createIconFromSVG("images/son-scenario-delete.svg"), "Remove");

        int buttonWidth = (int) Math.round(startButton.getPreferredSize().getWidth() + 5);
        int buttonHeight = (int) Math.round(startButton.getPreferredSize().getHeight() + 5);
        Dimension panelSize = new Dimension(buttonWidth * 6, buttonHeight);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.setPreferredSize(panelSize);
        controlPanel.add(new JSeparator());
        controlPanel.add(startButton);
        controlPanel.add(resetButton);
        controlPanel.add(saveButton);
        controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlPanel.add(removeButton);

        final SON net = (SON) editor.getModel().getMathModel();
        saveList = net.importScenarios();
        scenarioTable = new ScenarioTable(saveList, editor);

        tablePanel = new JScrollPane(scenarioTable);
        tablePanel.setPreferredSize(new Dimension(1, 1));

        startButton.addActionListener(event -> {
            if (startButton.isSelected()) {
                start(editor);
            } else {
                Step step = simuAlg.getEnabledNodes(sync, phases, isRev);
                setColors(net, step, GREYOUT_COLOR);
                net.clearMarking();
            }
        });

        resetButton.addActionListener(event -> {
            startButton.setSelected(true);
            start(editor);
        });

        saveButton.addActionListener(event -> {
            if (!scenarioRef.isEmpty()) {
                scenarioTable.setIsCellColor(true);
                ScenarioRef cache = new ScenarioRef();
                //add scenario nodes
                cache.addAll(scenarioRef);
                //add scenario connections
                for (SONConnection con : scenarioRef.getRuntimeConnections(net)) {
                    cache.add(net.getNodeReference(con));
                }
                saveList.add(cache);
                saveList.setPosition(saveList.size() - 1);
                updateState(editor);
            }
        });

        removeButton.addActionListener(event -> {
            if (!saveList.isEmpty()) {
                scenarioTable.setIsCellColor(true);
                int currentPosition = saveList.getPosition();
                saveList.remove(currentPosition);
                scenarioRef.clear();
                if (saveList.getPosition() > saveList.size() - 1) {
                    saveList.decPosition(1);
                }
                if (!saveList.isEmpty()) {
                    ScenarioRef savedScenarioRef = saveList.get(saveList.getPosition());
                    scenarioRef.addAll(savedScenarioRef.getNodeRefs(net));
                }
                scenarioTable.runtimeUpdateColor();
                updateState(editor);
            }
        });

        scenarioTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = scenarioTable.getSelectedColumn();
                int row = scenarioTable.getSelectedRow();
                if ((column == 0) && (row < saveList.size())) {
                    saveList.setPosition(row);
                    Object obj = scenarioTable.getValueAt(row, column);
                    if (obj instanceof ScenarioRef) {
                        startButton.setSelected(false);
                        scenarioRef = (ScenarioRef) obj;
                        scenarioTable.setScenarioRef(scenarioRef);
                        scenarioTable.setIsCellColor(true);
                        scenarioTable.runtimeUpdateColor();
                        updateState(editor);
                    }
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

    private void start(final GraphEditor editor) {
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
            scenarioRef.clear();
            net.clearMarking();
            net.refreshAllColor();
            scenarioTable.setIsCellColor(false);
            saveList.setPosition(0);
            scenarioGenerator(editor);
        }

    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        final VisualSON visualNet = (VisualSON) editor.getModel();
        BlockConnector.blockBoundingConnector(visualNet);
        final SON net = visualNet.getMathModel();
        net.refreshAllColor();
        net.clearMarking();
        initialise(editor);
        editor.forceRedraw();
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
        updateState(editor);
    }

    private void exportScenarios(final SON net) {
        for (Scenario scenario: net.getScenarios()) {
            net.remove(scenario);
        }
        int i = 1;
        for (ScenarioRef s: saveList) {
            net.createScenario("Scenario" + i++, s);
        }
    }

    protected void scenarioGenerator(final GraphEditor editor) {
        final SON net = (SON) editor.getModel().getMathModel();
        writeModelState(initialMarking);
        MarkingRef markingRef = new MarkingRef();
        ArrayList<PlaceNode> currentMarking = getCurrentMarking(net);
        markingRef.addAll(net.getNodeRefs(currentMarking));
        scenarioRef.addAll(markingRef);
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
        scenarioTable.updateTable(editor);
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

        //add step references
        StepRef stepRef = new StepRef();
        stepRef.addAll(net.getNodeRefs(step));
        scenarioRef.addAll(stepRef);
        //add marking references
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
