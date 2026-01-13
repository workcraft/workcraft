package org.workcraft.gui.workspace;

import org.workcraft.gui.trees.FilteredTreeSource;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.trees.TreeWindow.CheckBoxMode;
import org.workcraft.types.Func;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.Workspace;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class WorkspaceChooser extends JPanel {
    private final Func<Path<String>, Boolean> filter;
    private final TreeWindow<Path<String>> tree;
    private final FilteredTreeSource<Path<String>> filteredSource;
    private final JTextField nameFilter = new JTextField();
    private final JPanel searchPanel = GuiUtils.createLabeledComponent(nameFilter, "Search: ");

    public WorkspaceChooser(Workspace workspace, Func<Path<String>, Boolean> filter) {
        super();
        this.filter = filter;
        setLayout(GuiUtils.createBorderLayout());
        setBorder(GuiUtils.getEmptyBorder());

        String tipText = "Hide unselected items that do not contain the given substring";
        searchPanel.setToolTipText(tipText);
        nameFilter.setToolTipText(tipText);
        nameFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFilter();
            }
        });

        filteredSource = new FilteredTreeSource<>(workspace.getTree(), filter);
        filteredSource.setFilter(filter);
        tree = new TreeWindow<>(filteredSource, new WorkspaceTreeDecorator(workspace), null, null);
        tree.setAutoExpand(true);
        expand(filteredSource.getRoot());

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(tree);

        JButton toggleButton = new JButton("Toggle selection");
        toggleButton.addActionListener(e -> {
            Set<Path<String>> checkedNodes = new HashSet<>(getCheckedNodes());
            for (Path<String> node : getLeaves()) {
                if (isFiltered(node)) {
                    tree.setChecked(node, !checkedNodes.contains(node));
                }
            }
        });

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(toggleButton, BorderLayout.SOUTH);
    }

    public void setCheckBoxMode(CheckBoxMode mode) {
        tree.setCheckBoxMode(mode);
    }

    public void setFileterVisibility(boolean value) {
        searchPanel.setVisible(value);
    }

    private void expand(Path<String> node) {
        if (filteredSource.isLeaf(node)) {
            if (filter.eval(node)) {
                tree.makeVisible(filteredSource.getPath(node));
            }
        } else {
            for (Path<String> childNode : filteredSource.getChildren(node)) {
                expand(childNode);
            }
        }
    }

    private void updateFilter() {
        filteredSource.setFilter(this::isFiltered);
        expand(filteredSource.getRoot());
    }

    private boolean isFiltered(Path<String> arg) {
        return (filter.eval(arg) && arg.getNode().contains(nameFilter.getText()))
                || getCheckedNodes().contains(arg);
    }

    public void setChecked(Path<String> node, boolean value) {
        if (filteredSource.isLeaf(node)) {
            if (filter.eval(node)) {
                tree.setChecked(node, value);
            }
        }
    }

    public Set<Path<String>> getCheckedNodes() {
        return tree.getCheckedNodes();
    }


    private Set<Path<String>> getLeaves() {
        return getLeaves(filteredSource.getRoot());
    }

    private Set<Path<String>> getLeaves(Path<String> node) {
        Set<Path<String>> result = new HashSet<>();
        for (Path<String> childNode : filteredSource.getChildren(node)) {
            if (filteredSource.isLeaf(childNode)) {
                result.add(childNode);
            } else {
                result.addAll(getLeaves(childNode));
            }
        }
        return result;
    }

}
