package org.workcraft.plugins.son.gui;

import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.StructureVerifySettings;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class StructureVerifyDialog extends JDialog {

    protected WorkspaceEntry we;
    protected SON net;
    private static final long serialVersionUID = 1L;

    protected JPanel  selectionButtonPanel;
    protected JPanel groupItemPanel;
    protected JPanel groupSelectionPanel;
    protected JPanel typePanel;
    protected JPanel settingPanel;
    protected JPanel confirmButtonsPanel;
    protected JButton runButton;
    protected JButton cancelButton;
    protected JButton addAllButton;
    protected JButton removeAllButton;
    protected JComboBox typeCombo;
    protected JList groupList;
    protected JCheckBox highLight;
    protected JCheckBox outputBefore;

    protected ArrayList<ONGroup> selectedGroups;
    protected Font font = new Font("Arial", Font.PLAIN, 12);
    protected Dimension buttonSize = new Dimension(100, 25);
    protected Dimension listScrollerSize = new Dimension(350, 220);

    protected boolean modalResult;

    static class TypeMode {
        public int value;
        public String description;

        TypeMode(int value, String description) {
            this.value = value;
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public class ListItem {
        private final String label;
        private boolean selected = true;
        private final Object obj;

        ListItem(String label, Object obj) {
            this.label = label;
            this.obj = obj;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public String toString() {
            return label;
        }

        public Object getListItem() {
            return obj;
        }

        public void setItemColor(Color color) {
            if (obj instanceof MathNode) {
                net.setForegroundColor((MathNode) obj, color);
            }
        }
    }

    class ItemListRenderer extends JCheckBox implements ListCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean hasFocus) {

            setEnabled(list.isEnabled());
            setSelected(((ListItem) value).isSelected());
            setFont(list.getFont());

            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setText(value.toString());
            return this;
        }
    }

    protected void createSelectionPanel() {
        createGroupItemsPanel();
        createSelectionButtonsPanel();

        groupSelectionPanel = new JPanel(new FlowLayout());
        groupSelectionPanel.setBorder(createTitileBorder(groupPanelTitle()));
        groupSelectionPanel.add(groupItemPanel);
        groupSelectionPanel.add(selectionButtonPanel);

    }

    @SuppressWarnings("unchecked")
    protected void createTypePanel() {
        typeCombo = new JComboBox<>();
        typeCombo.addItem(new TypeMode(0, "Structured Occurrence Nets"));
        typeCombo.addItem(new TypeMode(1, "Occurrence Net (Group)"));
        typeCombo.addItem(new TypeMode(2, "Communication Structured Occurrence Nets"));
        typeCombo.addItem(new TypeMode(3, "Behavioural Structured Occurrence Nets"));
        typeCombo.addItem(new TypeMode(4, "Temporal Structured Occurrence Nets"));

        typePanel = new JPanel();
        typePanel.add(GuiUtils.createLabeledComponent(typeCombo, "Types:"));

    }

    @SuppressWarnings("unchecked")
    protected void createGroupItemsPanel() {
        selectedGroups = new ArrayList<>();

        DefaultListModel listModel = new DefaultListModel<>();

        for (ONGroup group : net.getGroups()) {
            group.setForegroundColor(Color.ORANGE);
            selectedGroups.add(group);
            if (group.getLabel().isEmpty()) {
                listModel.addElement(new ListItem("Group: " + net.getNodeReference(group), group));
            } else {
                listModel.addElement(new ListItem("Group: " + net.getNodeReference(group) + " (" + group.getLabel() + ")", group));
            }
        }

        groupList = new JList<>(listModel);
        groupList.setCellRenderer(new ItemListRenderer());
        groupList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        groupList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                JList list = (JList) event.getSource();

                int index = list.locationToIndex(event.getPoint());
                try {
                    ListItem item = (ListItem) list.getModel().getElementAt(index);
                    item.setSelected(!item.isSelected());

                    if (item.isSelected()) {
                        selectedGroups.add((ONGroup) item.getListItem());
                        item.setItemColor(Color.ORANGE);

                    }
                    if (!item.isSelected()) {
                        selectedGroups.remove(item.getListItem());
                        item.setItemColor(Color.BLACK);
                    }
                    list.repaint(list.getCellBounds(index, index));

                } catch (ArrayIndexOutOfBoundsException ignored) { }
            }
        });

        groupItemPanel = new JPanel();
        groupItemPanel.add(createJScrollPane(groupList));
    }

    protected JScrollPane createJScrollPane(JList list) {
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(getListScrollerSize());
        listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        listScroller.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        listScroller.getHorizontalScrollBar().setPreferredSize(new Dimension(12, 0));

        return listScroller;
    }

    protected void createSelectionButtonsPanel() {
        addAllButton = new JButton("Select All");
        addAllButton.setMaximumSize(buttonSize);
        addAllButton.setFont(this.getFont());
        addAllButton.addActionListener(event -> addAllAction());

        removeAllButton = new JButton("Remove All");
        removeAllButton.setMaximumSize(buttonSize);
        removeAllButton.setFont(this.getFont());
        removeAllButton.addActionListener(event -> removeAllAction());

        selectionButtonPanel = new JPanel();
        selectionButtonPanel.setLayout(new BoxLayout(selectionButtonPanel, BoxLayout.Y_AXIS));
        selectionButtonPanel.add(addAllButton);
        selectionButtonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        selectionButtonPanel.add(removeAllButton);
    }

    private void addAllAction() {
        selectedGroups.clear();
        for (int i = 0; i < getList().getModel().getSize(); i++) {
            ((ListItem) getList().getModel().getElementAt(i)).setSelected(true);
            Object obj = ((ListItem) getList().getModel().getElementAt(i)).getListItem();
            if (obj instanceof ONGroup) {
                selectedGroups.add((ONGroup) obj);
            }
            ((ListItem) getList().getModel().getElementAt(i)).setItemColor(Color.ORANGE);
        }
        getList().repaint();
    }

    private void removeAllAction() {
        for (int i = 0; i < getList().getModel().getSize(); i++) {
            ((ListItem) getList().getModel().getElementAt(i)).setSelected(false);
            ((ListItem) getList().getModel().getElementAt(i)).setItemColor(Color.BLACK);
        }
        getList().repaint();
        selectedGroups.clear();
    }

    protected void createSettingPanel() {
        settingPanel = new JPanel(new BorderLayout());
        settingPanel.setBorder(createTitileBorder("Setting"));

        highLight = new JCheckBox("Highlight erroneous nodes");
        highLight.setFont(font);
        highLight.setSelected(true);

        outputBefore = new JCheckBox("Output causal dependencies in BSON");
        outputBefore.setFont(font);
        outputBefore.setSelected(false);

        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.add(highLight);
        leftColumn.add(outputBefore);

        settingPanel.add(leftColumn, BorderLayout.WEST);
    }

    protected void createButtonsPanel() {
        runButton = new JButton("Run");
        runButton.setPreferredSize(buttonSize);
        runButton.addActionListener(event -> {
            modalResult = true;
            setVisible(false);
        });

        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(buttonSize);
        cancelButton.addActionListener(event -> {
            modalResult = false;
            setVisible(false);
        });

        confirmButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        confirmButtonsPanel.add(cancelButton);
        confirmButtonsPanel.add(runButton);
    }

    public StructureVerifyDialog(Window owner, String title, ModalityType modalityType, WorkspaceEntry we) {
        super(owner, title, modalityType);
        this.we = we;
        net = WorkspaceUtils.getAs(we, SON.class);
        createInterface(owner);
    }

    protected void createInterface(Window owner) {
        createTypePanel();
        createSelectionPanel();
        createButtonsPanel();
        createSettingPanel();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(Box.createRigidArea(new Dimension(0, 5)));
        content.add(typePanel);
        content.add(groupSelectionPanel);
        content.add(settingPanel);
        content.add(confirmButtonsPanel);

        setSize(new Dimension(500, 600));
        add(content);
        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    protected TitledBorder createTitileBorder(String title) {
        TitledBorder titledBorder = new TitledBorder(title);
        titledBorder.setTitleColor(Color.BLUE.darker());
        return titledBorder;
    }

    public StructureVerifyDialog(Window owner, WorkspaceEntry we) {
        this(owner, "Structure Verification Setting", ModalityType.APPLICATION_MODAL, we);
    }

    protected String groupPanelTitle() {
        return "Group selection";
    }

    public ArrayList<ONGroup> getSelectedGroups() {
        return selectedGroups;
    }

    public JList getList() {
        return this.groupList;
    }

    public StructureVerifySettings getSettings() {
        return new StructureVerifySettings(highLight.isSelected(), outputBefore.isSelected(),
                getSelectedGroups(), typeCombo.getSelectedIndex());
    }

    public Dimension getListScrollerSize() {
        return listScrollerSize;
    }

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }

}
