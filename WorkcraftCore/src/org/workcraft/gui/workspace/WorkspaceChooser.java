package org.workcraft.gui.workspace;

import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.WorkspaceTreeDecorator;
import org.workcraft.gui.trees.FilteredTreeSource;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.trees.TreeWindow.CheckBoxMode;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;
import org.workcraft.workspace.Workspace;

import info.clearthought.layout.TableLayout;

@SuppressWarnings("serial")
public class WorkspaceChooser extends JPanel {
    private final Func<Path<String>, Boolean> filter;
    private final TreeWindow<Path<String>> tree;
    private final JTextField nameFilter;
    private final FilteredTreeSource<Path<String>> filteredSource;

    public WorkspaceChooser(Workspace workspace, Func<Path<String>, Boolean> filter) {
        super();
        this.filter = filter;

        double[][] sizes = {
            {TableLayout.FILL},
            {TableLayout.PREFERRED, TableLayout.FILL},
        };

        final TableLayout layout = new TableLayout(sizes);
        layout.setHGap(SizeHelper.getLayoutHGap());
        layout.setVGap(SizeHelper.getLayoutVGap());

        this.setBorder(SizeHelper.getEmptyBorder());
        this.setLayout(layout);

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

        this.add(GUI.createWideLabeledComponent(nameFilter, "Search: "), "0 0");

        filteredSource = new FilteredTreeSource<>(workspace.getTree(), filter);

        tree = TreeWindow.create(filteredSource, new WorkspaceTreeDecorator(workspace), null);

        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(tree);

        expand(filteredSource.getRoot());

        this.add(scroll, "0 1");

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
