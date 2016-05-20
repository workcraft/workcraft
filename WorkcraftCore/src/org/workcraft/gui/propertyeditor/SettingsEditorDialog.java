/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.PluginInfo;

public class SettingsEditorDialog extends JDialog {
    private static final String DIALOG_RESTORE_SETTINGS = "Restore settings";
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JScrollPane sectionPane;
    private JScrollPane propertiesPane;
    private JSplitPane splitPane;
    private JPanel buttonsPane;
    private JButton okButton;
    private JButton cancelButton;
    private JButton restoreButton;
    private DefaultMutableTreeNode sectionRoot;
    private JTree sectionTree;
    private final PropertyEditorTable propertiesTable;

    private Settings currentPage;
    private Config currentConfig;

    static class SettingsPageNode {
        private Settings page;

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

        propertiesTable = new PropertyEditorTable();
        propertiesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setModal(true);
        setTitle("Settings");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ok();
            }
        });

        Dimension minSize = new Dimension(500, 200);
        setMinimumSize(minSize);
        Dimension mySize = new Dimension(900, 600);
        setSize(mySize);

        Dimension parentSize = owner.getSize();
        owner.getLocationOnScreen();
        setLocation(((parentSize.width - mySize.width) / 2) + 0, ((parentSize.height - mySize.height) / 2) + 0);

        initComponents();
        loadSections();
    }

    public DefaultMutableTreeNode getSectionNode(DefaultMutableTreeNode node, String section) {
        int dotPos = section.indexOf('.');

        String thisLevel, nextLevel;

        if (dotPos < 0) {
            thisLevel = section;
            nextLevel = null;
        } else {
            thisLevel = section.substring(0, dotPos);
            nextLevel = section.substring(dotPos + 1);
        }

        DefaultMutableTreeNode thisLevelNode = null;

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (!(child.getUserObject() instanceof String)) {
                continue;
            }
            if (((String) child.getUserObject()).equals(thisLevel)) {
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
        ArrayList<Settings> settings = getSortedPluginSettings(pm.getPlugins(Settings.class));

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
        Collections.sort(settings, new Comparator<Settings>() {
            @Override
            public int compare(Settings o1, Settings o2) {
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
            }
        });
        return settings;
    }

    private void setObject(Settings page) {
        if (page == null) {
            currentConfig = null;
            restoreButton.setText("Restore defaults (all)");
        } else {
            currentConfig = new Config();
            page.save(currentConfig);
            restoreButton.setText("Restore defaults");
        }
        currentPage = page;
        propertiesTable.setObject(currentPage);
    }

    private void initComponents() {
        contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);
        sectionPane = new JScrollPane();
        sectionRoot = new DefaultMutableTreeNode("root");

        sectionTree = new JTree();
        sectionTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        sectionTree.setRootVisible(false);
        sectionTree.setShowsRootHandles(true);

        sectionTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                Object userObject = ((DefaultMutableTreeNode) e.getPath().getLastPathComponent()).getUserObject();
                if (userObject instanceof SettingsPageNode) {
                    Settings page = ((SettingsPageNode) userObject).getPage();
                    setObject(page);
                } else {
                    setObject(null);
                }
            }
        });

        sectionPane.setViewportView(sectionTree);
        sectionPane.setMinimumSize(new Dimension(50, 0));
        sectionPane.setPreferredSize(new Dimension(250, 0));
        sectionPane.setBorder(BorderFactory.createTitledBorder("Section"));

        propertiesPane = new JScrollPane();
        propertiesPane.setMinimumSize(new Dimension(250, 0));
        propertiesPane.setPreferredSize(new Dimension(450, 0));
        propertiesPane.setBorder(BorderFactory.createTitledBorder("Selection properties"));
        propertiesPane.setViewportView(propertiesTable);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sectionPane, propertiesPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.1);

        okButton = new JButton();
        okButton.setPreferredSize(new Dimension(100, 25));
        okButton.setText("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        cancelButton = new JButton();
        cancelButton.setPreferredSize(new Dimension(100, 25));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        restoreButton = new JButton();
        restoreButton.setPreferredSize(new Dimension(170, 25));
        restoreButton.setText("Restore defaults");
        restoreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restore();
            }
        });

        buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPane.add(okButton);
        buttonsPane.add(cancelButton);
        buttonsPane.add(restoreButton);
        contentPane.add(splitPane, BorderLayout.CENTER);
        contentPane.add(buttonsPane, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);

        getRootPane().registerKeyboardAction(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ok();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cancel();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void ok() {
        setObject(null);
        setVisible(false);
    }

    private void cancel() {
        if ((currentPage != null) && (currentConfig != null)) {
            currentPage.load(currentConfig);
        }
        setObject(null);
        setVisible(false);
    }

    private void restore() {
        if (currentPage != null) {
            currentPage.load(new Config());
            setObject(currentPage);
        } else {
            final Framework framework = Framework.getInstance();
            int answer = JOptionPane.showConfirmDialog(framework.getMainWindow(),
                    "This will reset all the settings to defaults.\n" + "Continue?",
                    DIALOG_RESTORE_SETTINGS, JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                framework.resetConfig();
            }
        }
    }

}
