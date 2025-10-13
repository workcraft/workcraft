package org.workcraft.gui.properties;

import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.PluginManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SettingsEditorDialog extends JDialog {

    private static final String RIGHT_ARROW_SYMBOL = Character.toString((char) 0x2192);
    private static final String SEPARATOR = ' ' + RIGHT_ARROW_SYMBOL + ' ';

    private static final long serialVersionUID = 1L;

    private final JLabel sectionLabel  = new JLabel();
    private final PropertyEditorTable propertiesTable = new PropertyEditorTable();

    private Settings currentPage;
    private Config currentConfig;

    private JButton restoreButton;
    private DefaultMutableTreeNode sectionRoot;
    private JTree sectionTree;

    private boolean modalResult;

    static class SettingsPageNode {
        private final Settings page;

        SettingsPageNode(Settings page) {
            this.page = page;
        }

        @Override
        public String toString() {
            return page.getName();
        }

        public Settings getPage() {
            return page;
        }
    }

    public SettingsEditorDialog(MainWindow owner) {
        super(owner);

        sectionLabel.setFont(sectionLabel.getFont().deriveFont(Font.BOLD));
        propertiesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setModal(true);
        setResizable(true);
        setTitle("Preferences");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                okAction();
            }
        });

        GuiUtils.sizeToScreen(this, 0.5f, 0.5f);
        setLocationRelativeTo(owner);

        initComponents();
        loadSections();
    }

    public DefaultMutableTreeNode getSectionNode(DefaultMutableTreeNode node, String section) {
        if (section == null) {
            return node;
        }
        int dotPos = section.indexOf('.');
        String thisLevel = (dotPos < 0) ? section : section.substring(0, dotPos);
        String nextLevel = (dotPos < 0) ? null : section.substring(dotPos + 1);
        DefaultMutableTreeNode thisLevelNode = null;

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (!(child.getUserObject() instanceof String)) {
                continue;
            }
            if (child.getUserObject().equals(thisLevel)) {
                thisLevelNode = child;
                break;
            }
        }

        if (thisLevelNode == null) {
            thisLevelNode = new DefaultMutableTreeNode(thisLevel);
        }
        node.add(thisLevelNode);

        if (nextLevel == null) {
            return thisLevelNode;
        } else {
            return getSectionNode(thisLevelNode, nextLevel);
        }
    }

    private void addItem(String section, Settings item) {
        DefaultMutableTreeNode sectionNode = getSectionNode(sectionRoot, section);
        sectionNode.add(new DefaultMutableTreeNode(new SettingsPageNode(item)));
    }

    private void loadSections() {
        // Add settings to the tree
        PluginManager pm = Framework.getInstance().getPluginManager();
        for (Settings settings : pm.getSortedSettings()) {
            String section = settings.getSection();
            if (section != null) {
                addItem(section, settings);
            }
        }

        sectionTree.setModel(new DefaultTreeModel(sectionRoot));

        // Expand all tree branches
        for (int i = 0; i < sectionTree.getRowCount(); i++) {
            final TreePath treePath = sectionTree.getPathForRow(i);
            sectionTree.expandPath(treePath);
        }
        setObject(null);
    }

    private void setObject(Settings page) {
        if (page == null) {
            currentConfig = null;
            restoreButton.setText("Restore defaults (all)");
            sectionLabel.setText("");
        } else {
            currentConfig = new Config();
            page.save(currentConfig);
            restoreButton.setText("Restore defaults");
            sectionLabel.setText(page.getSection() + SEPARATOR + page.getName());
        }
        currentPage = page;
        propertiesTable.assign(currentPage);
    }

    private void initComponents() {
        JScrollPane sectionScrollPane = new JScrollPane();
        sectionRoot = new DefaultMutableTreeNode("root");

        sectionTree = new JTree();
        sectionTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        sectionTree.setRootVisible(false);
        sectionTree.setShowsRootHandles(true);

        sectionTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode pathComponent = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            Object userObject = pathComponent.getUserObject();
            if (userObject instanceof SettingsPageNode pageNode) {
                Settings page = pageNode.getPage();
                setObject(page);
            } else {
                setObject(null);
            }
        });

        sectionScrollPane.setViewportView(sectionTree);

        JScrollPane propertiesScrollPane = new JScrollPane();
        propertiesScrollPane.setViewportView(propertiesTable);

        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setMinimumSize(new Dimension(100, 0));
        sectionPanel.setBorder(GuiUtils.getEmptyBorder());
        sectionPanel.add(sectionScrollPane, BorderLayout.CENTER);

        JPanel propertiesPanel = new JPanel(new BorderLayout());
        propertiesPanel.setMinimumSize(new Dimension(250, 0));
        propertiesPanel.setBorder(GuiUtils.getEmptyBorder());
        propertiesPanel.add(sectionLabel, BorderLayout.NORTH);
        propertiesPanel.add(propertiesScrollPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sectionPanel, propertiesPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation((int) Math.round(0.3 * getWidth()));
        splitPane.setResizeWeight(0.1);

        JButton okButton = GuiUtils.createDialogButton("OK");
        okButton.addActionListener(event -> okAction());

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> cancelAction());

        restoreButton = GuiUtils.createDialogButton("Restore defaults (all)");
        restoreButton.addActionListener(event -> restoreAction());

        JPanel buttonsPanel = GuiUtils.createDialogButtonsPanel();
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(restoreButton);

        JPanel contentPanel = new JPanel(new BorderLayout());
        setContentPane(contentPanel);
        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);

        getRootPane().registerKeyboardAction(event -> okAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(event -> cancelAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void okAction() {
        modalResult = true;
        setObject(null);
        setVisible(false);
    }

    private void cancelAction() {
        modalResult = false;
        if ((currentPage != null) && (currentConfig != null)) {
            currentPage.load(currentConfig);
        }
        setObject(null);
        setVisible(false);
    }

    private void restoreAction() {
        if (currentPage != null) {
            currentPage.load(new Config());
            setObject(currentPage);
        } else {
            final Framework framework = Framework.getInstance();
            if (DialogUtils.showConfirm("This will reset all the settings to defaults", ".\nContinue?",
                    "Restore settings", false, JOptionPane.WARNING_MESSAGE, false)) {
                framework.resetConfig();
            }
        }
    }

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }

}
