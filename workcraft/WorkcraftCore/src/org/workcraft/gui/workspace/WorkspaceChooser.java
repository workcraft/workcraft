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

@SuppressWarnings("serial")
public class WorkspaceChooser extends JPanel {
    private final Func<Path<String>, Boolean> filter;
    private final TreeWindow<Path<String>> tree;
    private final JTextField nameFilter;
    private final FilteredTreeSource<Path<String>> filteredSource;

    public WorkspaceChooser(Workspace workspace, Func<Path<String>, Boolean> filter) {
        super();
        this.filter = filter;
        setLayout(GuiUtils.createBorderLayout());
        setBorder(GuiUtils.getEmptyBorder());

        nameFilter = new JTextField();
        JPanel searchPanel = GuiUtils.createLabeledComponent(nameFilter, "Search: ");
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
        tree = TreeWindow.create(filteredSource, new WorkspaceTreeDecorator(workspace), null);
        expand();

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

    private void expand() {
        expand(filteredSource.getRoot());
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
        expand();
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

    public void setCheckBoxMode(CheckBoxMode mode) {
        tree.setCheckBoxMode(mode);
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
