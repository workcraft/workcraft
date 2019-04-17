package org.workcraft.gui.properties;

import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.PluginInfo;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SettingsEditorDialog extends JDialog {

    // Right arrow symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    private static final char RIGHT_ARROW_SYMBOL = 0x2192;
    private static final String SEPARATOR = " " + RIGHT_ARROW_SYMBOL + " ";

    private static final String DIALOG_RESTORE_SETTINGS = "Restore settings";
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
                actionOk();
            }
        });

        Dimension minSize = new Dimension(500, 200);
        setMinimumSize(minSize);
        GuiUtils.centerAndSizeToParent(this, owner);

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
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        ArrayList<Settings> settings = getSortedPluginSettings(pm.getSettingsPlugins());

        // Add settings to the tree
        for (Settings s: settings) {
            addItem(s.getSection(), s);
        }

        sectionTree.setModel(new DefaultTreeModel(sectionRoot));

        // Expand all tree branches
        for (int i = 0; i < sectionTree.getRowCount(); i++) {
            final TreePath treePath = sectionTree.getPathForRow(i);
            sectionTree.expandPath(treePath);
        }
        setObject(null);
    }

    private ArrayList<Settings> getSortedPluginSettings(Collection<PluginInfo<? extends Settings>> plugins) {
        ArrayList<Settings> settings = new ArrayList<>();
        for (PluginInfo<? extends Settings> info : plugins) {
            settings.add(info.getSingleton());
        }

        // Sort settings by (Sections + Name) strings
        Collections.sort(settings, (o1, o2) -> {
            if (o1 == o2) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;
            String s1 = o1.getSection();
            String s2 = o2.getSection();
            if (s1 == null) return -1;
            if (s2 == null) return 1;
            if (s1.equals(s2)) {
                String n1 = o1.getName();
                String n2 = o2.getName();
                if (n1 == null) return -1;
                if (n2 == null) return 1;
                return n1.compareTo(n2);
            }
            return s1.compareTo(s2);
        });
        return settings;
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
            if (userObject instanceof SettingsPageNode) {
                SettingsPageNode pageNode = (SettingsPageNode) userObject;
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
        sectionPanel.setBorder(SizeHelper.getEmptyBorder());
        sectionPanel.add(sectionScrollPane, BorderLayout.CENTER);

        JPanel propertiesPanel = new JPanel(new BorderLayout());
        propertiesPanel.setMinimumSize(new Dimension(250, 0));
        propertiesPanel.setBorder(SizeHelper.getEmptyBorder());
        propertiesPanel.add(sectionLabel, BorderLayout.NORTH);
        propertiesPanel.add(propertiesScrollPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sectionPanel, propertiesPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation((int) Math.round(0.3 * getWidth()));
        splitPane.setResizeWeight(0.1);

        JButton okButton = GuiUtils.createDialogButton("OK");
        okButton.addActionListener(event -> actionOk());

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> actionCancel());

        restoreButton = GuiUtils.createDialogButton("Restore defaults (all)");
        restoreButton.addActionListener(event -> actionRestore());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(restoreButton);

        JPanel contentPanel = new JPanel(new BorderLayout());
        setContentPane(contentPanel);
        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);

        getRootPane().registerKeyboardAction(event -> actionOk(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(event -> actionCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void actionOk() {
        modalResult = true;
        setObject(null);
        setVisible(false);
    }

    private void actionCancel() {
        modalResult = false;
        if ((currentPage != null) && (currentConfig != null)) {
            currentPage.load(currentConfig);
        }
        setObject(null);
        setVisible(false);
    }

    private void actionRestore() {
        if (currentPage != null) {
            currentPage.load(new Config());
            setObject(currentPage);
        } else {
            final Framework framework = Framework.getInstance();
            String msg = "This will reset all the settings to defaults.\n" + "Continue?";
            if (DialogUtils.showConfirmWarning(msg, DIALOG_RESTORE_SETTINGS, false)) {
                framework.resetConfig();
            }
        }
    }

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }

}
