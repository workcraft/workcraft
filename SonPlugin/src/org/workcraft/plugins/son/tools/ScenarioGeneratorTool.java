package org.workcraft.plugins.son.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;

import org.workcraft.dom.Node;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.son.BlockConnector;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.InvalidStructureException;
import org.workcraft.plugins.son.gui.SONGUI;
import org.workcraft.plugins.son.gui.ScenarioTable;
import org.workcraft.plugins.son.util.MarkingRef;
import org.workcraft.plugins.son.util.Scenario;
import org.workcraft.plugins.son.util.ScenarioRef;
import org.workcraft.plugins.son.util.ScenarioSaveList;
import org.workcraft.plugins.son.util.Step;
import org.workcraft.plugins.son.util.StepRef;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class ScenarioGeneratorTool extends SONSimulationTool {

    protected JButton saveButton, removeButton, resetButton, importButton, exportButton;
    protected JToggleButton startButton;
    protected ScenarioTable scenarioTable;

    protected ScenarioRef scenarioRef;
    protected ScenarioSaveList saveList;
    private static final Color greyoutColor = Color.LIGHT_GRAY;

    public class SaveList extends ArrayList<ScenarioRef> {
        private static final long serialVersionUID = 1L;
        private int position = 0;

        public int getPosition() {
            return position;
        }

        public void setPosition(int value) {
            position = Math.min(Math.max(0, value), size());
        }

        public void incPosition(int value) {
            setPosition(position + value);
        }

        public void decPosition(int value) {
            setPosition(position - value);
        }
    }

    @Override
    public void createInterfacePanel(final GraphEditor editor) {
        super.createInterfacePanel(editor);

        //workcraft invoke this method before activate method
        visualNet = (VisualSON) editor.getModel();
        net = (SON) visualNet.getMathModel();
        net.refreshAllColor();

        startButton = SONGUI.createIconToggleButton(GUI.createIconFromSVG("images/scenario-start.svg"), "Generate");
        resetButton = GUI.createIconButton(GUI.createIconFromSVG("images/scenario-reset.svg"), "Reset");
        saveButton = GUI.createIconButton(GUI.createIconFromSVG("images/scenario-save.svg"), "Save");
        removeButton = GUI.createIconButton(GUI.createIconFromSVG("images/scenario-delete.svg"), "Remove");
        importButton = GUI.createIconButton(GUI.createIconFromSVG("images/scenario-import.svg"), "Import scenarios");
        exportButton = GUI.createIconButton(GUI.createIconFromSVG("images/scenario-export.svg"), "Export scenarios");

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
        //controlPanel.add(importButton);
        //controlPanel.add(exportButton);
        //controlPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlPanel.add(removeButton);

        saveList = net.importScenarios(editor.getMainWindow());
        scenarioTable = new ScenarioTable(saveList, editor);

        tabelPanel = new JScrollPane(scenarioTable);
        tabelPanel.setPreferredSize(new Dimension(1, 1));

        statusPanel = new JPanel();

        interfacePanel = new JPanel();
        BorderLayout layout = new BorderLayout();
        layout.setVgap(10);
        interfacePanel.setLayout(layout);
        interfacePanel.add(controlPanel, BorderLayout.PAGE_START);
        interfacePanel.add(tabelPanel, BorderLayout.CENTER);
        interfacePanel.add(statusPanel, BorderLayout.PAGE_END);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startButton.isSelected()) {
                    start();
                } else {
                    Step step = simuAlg.getEnabledNodes(sync, phases, isRev);
                    setColors(step, greyoutColor);
                    net.clearMarking();
                }
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButton.setSelected(true);
                start();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!saveList.isEmpty()) {
                    scenarioTable.setIsCellColor(true);
                    int currentPosition = saveList.getPosition();
                    saveList.remove(currentPosition);
                    scenarioRef.clear();
                    if (saveList.getPosition() > saveList.size() - 1) {
                        saveList.decPosition(1);
                    }
                    if (!saveList.isEmpty()) {
                        scenarioRef.addAll(saveList.get(saveList.getPosition()).getNodeRefs(net));
                    }
                    scenarioTable.runtimeUpdateColor();
                    updateState(editor);
                }
            }
        });

        scenarioTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = scenarioTable.getSelectedColumn();
                int row = scenarioTable.getSelectedRow();

                if (column == 0 && row < saveList.size()) {
                    saveList.setPosition(row);
                    Object obj = scenarioTable.getValueAt(row, column);
                    if (obj instanceof ScenarioRef) {
                        startButton.setSelected(false);
                        scenarioTable.setIsCellColor(true);
                        scenarioRef.clear();
                        scenarioRef.addAll(((ScenarioRef) obj).getNodeRefs(net));
                        updateState(editor);
                        scenarioTable.runtimeUpdateColor();
                    }
                }
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

    private void start() {
        if (!acyclicChecker()) {
            startButton.setSelected(false);
            startButton.repaint();
            try {
                throw new InvalidStructureException("Cyclic structure error");
            } catch (InvalidStructureException e1) {
                errorMsg(e1.getMessage(), editor);
            }
        } else {
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
        this.editor = editor;
        WorkspaceEntry we = editor.getWorkspaceEntry();
        BlockConnector.blockBoundingConnector(visualNet);
        we.setCanSelect(false);

        net.clearMarking();
        initialise();

        editor.forceRedraw();
        editor.getModel().setTemplateNode(null);
    }

    @Override
    protected void initialise() {
        super.initialise();
        saveList = scenarioTable.getSaveList();
        scenarioRef = scenarioTable.getScenarioRef();
        updateState(editor);
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        BlockConnector.blockInternalConnector(visualNet);
        exportScenarios();
        scenarioRef.clear();
        net.refreshAllColor();
        net.clearMarking();
    }

    private void exportScenarios() {
        saveList.setPosition(0);
        for (Scenario scenario: net.getScenarios()) {
            net.remove(scenario);
        }
        int i = 1;
        for (ScenarioRef s: saveList) {
            net.createScenario("Scenario" + i++, s);
        }
    }

    protected void scenarioGenerator(final GraphEditor editor) {
        writeModelState(initialMarking);

        MarkingRef markingRef = new MarkingRef();
        markingRef.addAll(net.getNodeRefs(getCurrentMarking()));
        scenarioRef.addAll(markingRef);
        updateState(editor);

        Step step = simuAlg.getEnabledNodes(sync, phases, false);
        setDecoration(step);
        autoSimulator(editor);
    }

    @Override
    protected void autoSimulator(final GraphEditor editor) {
        autoSimulationTask(editor);
        Collection<Node> nodes = new ArrayList<>();
        nodes.addAll(scenarioRef.getNodes(net));
        nodes.addAll(scenarioRef.getRuntimeConnections(net));
        setColors(nodes, Color.BLACK);
    }

    @Override
    protected void autoSimulationTask(final GraphEditor editor) {
        Step step = simuAlg.getEnabledNodes(sync, phases, false);

        if (step.isEmpty()) {
            startButton.setSelected(false);
        }
        step = conflictfilter(step);
        if (!step.isEmpty()) {
            step = simuAlg.getMinFire(step.iterator().next(), sync, step, false);
            executeEvents(editor, step);
            autoSimulationTask(editor);
        }
    }

    @Override
    public void updateState(final GraphEditor editor) {
        scenarioTable.updateTable(editor);
    }

    @Override
    public void executeEvents(final GraphEditor editor, Step step) {
        ArrayList<PlaceNode> oldMarking = new ArrayList<>();
        oldMarking.addAll(getCurrentMarking());
        scenarioTable.setIsCellColor(false);
        super.executeEvents(editor, step);

        //add step references
        StepRef stepRef = new StepRef();
        stepRef.addAll(net.getNodeRefs(step));
        scenarioRef.addAll(stepRef);
        //add marking references
        MarkingRef markingRef = new MarkingRef();
        ArrayList<PlaceNode> marking = new ArrayList<>();
        marking.addAll(getCurrentMarking());
        marking.addAll(getSyncChannelPlaces(step));
        markingRef.addAll(net.getNodeRefs(marking));
        for (String str : markingRef) {
            if (!scenarioRef.contains(str)) {
                scenarioRef.add(str);
            }
        }
    }

    private ArrayList<PlaceNode> getCurrentMarking() {
        ArrayList<PlaceNode> result = new ArrayList<>();
        for (PlaceNode c : readSONMarking().keySet()) {
            if (c.isMarked()) {
                result.add(c);
            }
        }
        return result;
    }

    //get channel places in a synchronous step.
    private Collection<ChannelPlace> getSyncChannelPlaces(Step step) {
        HashSet<ChannelPlace> result = new HashSet<>();
        for (TransitionNode e :step) {
            for (SONConnection con : net.getSONConnections(e)) {
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

    private void setColors(Collection<? extends Node> nodes, Color color) {
        for (Node node : nodes) {
            net.setForegroundColor(node, color);
        }
    }

    @Override
    protected void setDecoration(Step enabled) {
        if (startButton.isSelected()) {
            setColors(net.getNodes(), greyoutColor);
            for (TransitionNode e : enabled) {
                e.setForegroundColor(CommonSimulationSettings.getEnabledForegroundColor());
            }
        }
    }

    @Override
    public JPanel getInterfacePanel() {
        return interfacePanel;
    }

    @Override
    public String getLabel() {
        return "Scenario Generator";
    }

    @Override
    public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
        GUI.drawEditorMessage(editor, g, Color.BLACK, "Click on the highlight node to choose a scenario");
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_G;
    }

    @Override
    public Icon getIcon() {
        return GUI.createIconFromSVG("images/tool-scenario.svg");
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (startButton.isSelected()) {
            super.mousePressed(e);
            autoSimulator(editor);
        }
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
