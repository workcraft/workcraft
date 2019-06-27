package org.workcraft.gui.workspace;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.trees.FilteredTreeSource;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.trees.TreeWindow.CheckBoxMode;
import org.workcraft.types.Func;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.Workspace;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

        setLayout(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL},
                new double[]{TableLayout.PREFERRED, TableLayout.FILL}));

        setBorder(SizeHelper.getEmptyBorder());

        nameFilter = new JTextField();

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

        add(GuiUtils.createLabeledComponent(nameFilter, "Search: "), new TableLayoutConstraints(0, 0));

        filteredSource = new FilteredTreeSource<>(workspace.getTree(), filter);

        tree = TreeWindow.create(filteredSource, new WorkspaceTreeDecorator(workspace), null);

        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(tree);

        expand(filteredSource.getRoot());

        add(scroll, new TableLayoutConstraints(0, 1));

        filteredSource.setFilter(filter);
    }

    private void expand(Path<String> node) {
        if (filteredSource.isLeaf(node)) {
            if (filter.eval(node)) {
                tree.makeVisible(filteredSource.getPath(node));
            }
        } else {
            for (Path<String> n : filteredSource.getChildren(node)) {
                expand(n);
            }
        }
    }

    private void updateFilter() {
        filteredSource.setFilter(arg -> filter(arg));
        expand(filteredSource.getRoot());
    }

    private boolean filter(Path<String> arg) {
        return (filter.eval(arg) && arg.getNode().contains(nameFilter.getText())) || getCheckedNodes().contains(arg);
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

}
