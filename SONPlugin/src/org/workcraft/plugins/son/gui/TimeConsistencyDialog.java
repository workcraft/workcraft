package org.workcraft.plugins.son.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.TimeConsistencySettings;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeConsistencyDialog extends StructureVerifyDialog {

    private static final long serialVersionUID = 1L;

    protected VisualSON vNet;

    protected JPanel scenarioItemPanel, nodeItemPanel, selectionPanel, causalConsistencyPanel;
    protected DefaultDurationPanel defaultDurationPanel;
    protected GranularityPanel granularityPanel;
    protected JPanel leftPanel, rightPanel;
    protected JTabbedPane selectionTabbedPane;
    protected JList<ListItem> scenarioList, nodeList;
    protected JCheckBox inconsistencyHighLight, unspecifyHighlight, causalHighlight, causalConsistency;

    private Color greyoutColor = Color.LIGHT_GRAY;
    protected ScenarioRef selectedScenario = null;
    protected ArrayList<Node> selectedNodes;

    public enum Granularity {
        YEAR_YEAR,
        HOUR_MINS;
    }

    @SuppressWarnings("rawtypes")
    class ScenarioListRenderer extends JRadioButton implements ListCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean hasFocus) {

            setSelected(isSelected);
            setFont(list.getFont());

            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setText(value.toString());
            return this;
        }
    }

    @SuppressWarnings("unchecked")
    protected void createScenarioItemPanel() {
    	
        scenarioItemPanel = new JPanel();
        ArrayList<ScenarioRef> scenarioSavelist = net.importScenarios(owner);
        DefaultListModel<ListItem> listModel = new DefaultListModel<>();

        for (int i = 0; i < scenarioSavelist.size(); i++) {
            listModel.addElement(new ListItem("Scenario " + (i + 1), scenarioSavelist.get(i)));
        }

        scenarioList = new JList<ListItem>(listModel);
        scenarioList.setCellRenderer(new ScenarioListRenderer());
        scenarioList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scenarioList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                JList<ListItem> list = (JList<ListItem>) event.getSource();

                int index = list.locationToIndex(event.getPoint());
                try {
                    for (int i = 0; i < list.getModel().getSize(); i++) {
                        ListItem item;
                        item = list.getModel().getElementAt(i);
                        if (item != null)item.setSelected(false);
                    }
                    setScenarioFromList(index);
                    updateScenarioColor();
                } catch (ArrayIndexOutOfBoundsException e) { }
            }
        });

        scenarioItemPanel.add(createJScrollPane(scenarioList));
    }

    private void setScenarioFromList(int index) {
        Object obj = null;
        ListItem item = scenarioList.getModel().getElementAt(index);
        if (item != null) {
            item.setSelected(true);
            obj = item.getListItem();
            if (obj instanceof ScenarioRef) {
                selectedScenario = (ScenarioRef) obj;
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void createNodeItemPanel() {
        nodeItemPanel = new JPanel();
        vNet = (VisualSON) we.getModelEntry().getVisualModel();
        selectedNodes = new ArrayList<Node>();

        DefaultListModel<ListItem> listModel = new DefaultListModel<>();

        for (Node vn : vNet.getSelection()) {
            if (vn instanceof VisualComponent) {
                Node node = ((VisualComponent) vn).getReferencedComponent();
                if (node instanceof Time) {
                    selectedNodes.add(node);
                    listModel.addElement(new ListItem(net.getNodeReference(node), node));
                }
            }
        }

        nodeList = new JList<ListItem>(listModel);
        nodeList.setCellRenderer(new ItemListRenderer());
        nodeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        nodeList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                JList<ListItem> list = (JList<ListItem>) event.getSource();

                int index = list.locationToIndex(event.getPoint());
                try {
                    ListItem item = list.getModel().getElementAt(index);
                    item.setSelected(!item.isSelected());

                    if (item.isSelected()) {
                        selectedNodes.add((Node) item.getListItem());
                        item.setItemColor(Color.ORANGE);
                    }
                    if (!item.isSelected()) {
                        selectedNodes.remove((Node) item.getListItem());
                        item.setItemColor(Color.BLACK);
                    }
                    list.repaint(list.getCellBounds(index, index));

                } catch (ArrayIndexOutOfBoundsException e) { }
            }
        });
        nodeItemPanel.add(createJScrollPane(nodeList));
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected JScrollPane createJScrollPane(JList list) {
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(280, 220));
        listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        listScroller.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        listScroller.getHorizontalScrollBar().setPreferredSize(new Dimension(12, 0));

        return listScroller;
    }

    protected void createGroupItemsPanel() {
        super.createGroupItemsPanel();
    }

    protected void createSelectionPane() {
        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(1, 1, 1, 1));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", false);

        createGroupItemsPanel();
        createScenarioItemPanel();
        createNodeItemPanel();

        selectionTabbedPane = new JTabbedPane();
        //selectionTabbedPane.addTab("Group", groupItemPanel);
        selectionTabbedPane.addTab("Scenario", scenarioItemPanel);
        selectionTabbedPane.addTab("Node", nodeItemPanel);

        selectionTabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                net.refreshAllColor();
                addAllButton.setEnabled(true);
                removeAllButton.setEnabled(true);
                int index = getTabIndex();

//                if (index == 0) {
//                    updateCausalConsistencyPanel(false);
//                    for (int i = 0; i < groupList.getModel().getSize(); i++) {
//                        ListItem item = (ListItem) groupList.getModel().getElementAt(i);
//                        item.setItemColor(Color.ORANGE);
//                    }
//
//                } else 
                if (index == 0) {
                    addAllButton.setEnabled(false);
                    removeAllButton.setEnabled(false);
                    updateCausalConsistencyPanel(true);
                    updateScenarioColor();

                } else if (index == 1) {
                    updateCausalConsistencyPanel(false);
                    for (int i = 0; i < nodeList.getModel().getSize(); i++) {
                        ListItem item = nodeList.getModel().getElementAt(i);
                        item.setItemColor(Color.ORANGE);
                    }
                    vNet.selectNone();
                }
            }
        });
    }

    @Override
    protected void createSelectionButtonsPanel() {

        super.createSelectionButtonsPanel();
        
        addAllButton.setEnabled(false);
        removeAllButton.setEnabled(false);

        addAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getTabIndex() == 0) {
                    selectedGroups.clear();
                } else if (getTabIndex() == 2) {
                    selectedNodes.clear();
                }

                for (int i = 0; i < getList().getModel().getSize(); i++) {
                    getList().getModel().getElementAt(i).setSelected(true);
                    Object obj = getList().getModel().getElementAt(i).getListItem();
                    if (obj instanceof ONGroup) {
                        selectedGroups.add((ONGroup) obj);
                    } else if (obj instanceof Time) {
                        selectedNodes.add((Time) obj);
                    }
                    getList().getModel().getElementAt(i).setItemColor(Color.ORANGE);
                }
                getList().repaint();
            }
        });

        removeAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < getList().getModel().getSize(); i++) {
                    getList().getModel().getElementAt(i).setSelected(false);
                    getList().getModel().getElementAt(i).setItemColor(Color.BLACK);
                }
                getList().repaint();
                if (getTabIndex() == 0) {
                    selectedGroups.clear();
                } else if (getTabIndex() == 2) {
                    selectedNodes.clear();
                }
            }
        });
    }

    @Override
    protected void createSelectionPanel() {
        createSelectionButtonsPanel();
        createSelectionPane();

        selectionPanel = new JPanel(new FlowLayout());
        selectionPanel.setBorder(createTitileBorder("Selection"));
        selectionPanel.add(selectionTabbedPane);
        selectionPanel.add(selectionButtonPanel);
    }

    protected void createCausalConsistencyPanel() {
        causalConsistency = new JCheckBox("Check for causal consistency");
        causalConsistency.setSelected(false);
        causalConsistency.setAlignmentX(Component.LEFT_ALIGNMENT);

        defaultDurationPanel = new DefaultDurationPanel();
        defaultDurationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        causalConsistencyPanel = new JPanel();
        causalConsistencyPanel.setLayout(new BoxLayout(causalConsistencyPanel, BoxLayout.Y_AXIS));
        causalConsistencyPanel.setPreferredSize(new Dimension(0, 200));
        causalConsistencyPanel.setBorder(createTitileBorder("Causal Consistency (Scenario)"));
        causalConsistencyPanel.add(causalConsistency);
        causalConsistencyPanel.add(defaultDurationPanel);

        causalConsistency.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                boolean b = causalConsistency.isSelected();
                defaultDurationPanel.getMin().setEnabled(b);
                defaultDurationPanel.getMax().setEnabled(b);
                defaultDurationPanel.getDurationLabel().setEnabled(b);
            }
        });

    }

    private void updateCausalConsistencyPanel(boolean b) {
        causalConsistency.setEnabled(b);
        defaultDurationPanel.getMin().setEnabled(b);
        defaultDurationPanel.getMax().setEnabled(b);
        defaultDurationPanel.getDurationLabel().setEnabled(b);
    }

    private Interval getDefaultDuration() {
        int minValue = Interval.getInteger(defaultDurationPanel.getMin().getText());
        int maxValue = Interval.getInteger(defaultDurationPanel.getMax().getText());
        return new Interval(minValue, maxValue);
    }

    @Override
    protected void createSettingPanel() {
        settingPanel = new JPanel(new BorderLayout());
        settingPanel.setPreferredSize(new Dimension(300, 130));
        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));

        settingPanel.setBorder(createTitileBorder("Setting"));

        inconsistencyHighLight = new JCheckBox("Highlight inconsistency nodes");
        inconsistencyHighLight.setFont(font);
        inconsistencyHighLight.setSelected(true);

        unspecifyHighlight = new JCheckBox("Highlight nodes with unspecified time values");
        unspecifyHighlight.setFont(font);
        unspecifyHighlight.setSelected(false);

        causalHighlight = new JCheckBox("Highlight causally inconsistency nodes");
        causalHighlight.setFont(font);
        causalHighlight.setSelected(true);

        leftColumn.add(inconsistencyHighLight);
        leftColumn.add(unspecifyHighlight);
        //leftColumn.add(causalHighlight);

        settingPanel.add(leftColumn, BorderLayout.WEST);
    }

    @Override
    protected void createButtonsPanel() {
        runButton = new JButton("Run");
        runButton.setPreferredSize(buttonSize);
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (defaultDurationPanel.isValidDuration()) {
                    run = 1;
                    setVisible(false);
                } else {
                    defaultDurationPanel.getMin().setForeground(Color.RED);
                    defaultDurationPanel.getMax().setForeground(Color.RED);
                }
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(buttonSize);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run = 2;
                setVisible(false);
            }
        });

        confirmButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        confirmButtonsPanel.add(cancelButton);
        confirmButtonsPanel.add(runButton);

    }

    @Override
    protected void createInterface() {
        createSelectionPanel();
        createSettingPanel();
        createButtonsPanel();
        createCausalConsistencyPanel();
        granularityPanel = new GranularityPanel(BorderFactory.createTitledBorder("Time Granularity"));

        leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(granularityPanel, BorderLayout.PAGE_START);
        leftPanel.add(selectionPanel, BorderLayout.PAGE_END);

        rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(causalConsistencyPanel, BorderLayout.PAGE_START);
        rightPanel.add(settingPanel, BorderLayout.CENTER);
        rightPanel.add(confirmButtonsPanel, BorderLayout.PAGE_END);

        JPanel content = new JPanel();
        content.setLayout(new FlowLayout());
        content.add(leftPanel);
        content.add(rightPanel);

        updateCausalConsistencyPanel(true);

        this.add(content);
        this.setResizable(false);
        this.pack();
    }

    public TimeConsistencyDialog(Window owner, WorkspaceEntry we) {
        super(owner, "Time Anayalsis Setting",  ModalityType.APPLICATION_MODAL, we);
    }

    protected void updateScenarioColor() {
        net.clearMarking();
        setGrayout(net.getNodes(), greyoutColor);
        Collection<Node> nodes = new ArrayList<>();
        nodes.addAll(selectedScenario.getNodes(net));
        nodes.addAll(selectedScenario.getConnections(net));
        setGrayout(nodes, Color.BLACK);
    }

    protected void setGrayout(Collection<? extends Node> nodes, Color color) {
        for (Node node : nodes) {
            net.setForegroundColor(node, color);
        }
    }

    @Override
    protected String groupPanelTitle() {
        return "";
    }

    @SuppressWarnings("unchecked")
    @Override
    public JList<ListItem> getList() {
        if (getTabIndex() == 0) {
            return groupList;
        } else if (getTabIndex() == 2) {
            return nodeList;
        } else {
            return null;
        }
    }

    public ArrayList<Node> getSelectedNodes() {
        return selectedNodes;
    }

    public ArrayList<ONGroup> getSelectedGroups() {
        return selectedGroups;
    }

    public ScenarioRef getSelectedScenario() {
        return selectedScenario;
    }

    public int getTabIndex() {
        return selectionTabbedPane.getSelectedIndex();
    }

    public TimeConsistencySettings getTimeConsistencySettings() {
        return new TimeConsistencySettings(inconsistencyHighLight.isSelected(), unspecifyHighlight.isSelected(),
                getSelectedGroups(), getSelectedScenario(), getSelectedNodes(), getTabIndex(), granularityPanel.getSelection(),
                causalConsistency.isSelected(), getDefaultDuration(), causalHighlight.isSelected());
    }
}
