package org.workcraft.gui.workspace;

import java.util.Set;

import info.clearthought.layout.TableLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.workcraft.gui.WorkspaceTreeDecorator;
import org.workcraft.gui.trees.FilteredTreeSource;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.trees.TreeWindow.CheckBoxMode;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;
import org.workcraft.workspace.Workspace;

@SuppressWarnings("serial")
public class WorkspaceChooser extends JPanel {
    private final Func<Path<String>, Boolean> filter;
    private TreeWindow<Path<String>> tree;
    private JTextField nameFilter;
    private FilteredTreeSource<Path<String>> filteredSource;

    public WorkspaceChooser(Workspace workspace, Func<Path<String>, Boolean> filter) {
        super();

        this.filter = filter;

        double[][] sizes =
        {
                {TableLayout.FILL },
                {TableLayout.PREFERRED, TableLayout.FILL },
        };

        final TableLayout mgr = new TableLayout(sizes);
        mgr.setHGap(4);
        mgr.setVGap(4);
        this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        this.setLayout(mgr);

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

        this.add(GUI.createWideLabeledComponent(nameFilter, "Search:"), "0 0");

        filteredSource = new FilteredTreeSource<Path<String>>(workspace.getTree(), filter);

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
        filteredSource.setFilter(new Func<Path<String>, Boolean>() {
            @Override
            public Boolean eval(Path<String> arg) {
                return (filter.eval(arg) && arg.getNode().contains(nameFilter.getText())) || getCheckedNodes().contains(arg);
            }
        });

        expand(filteredSource.getRoot());
    }

    public void clearCheckBoxes() {
        tree.clearCheckBoxes();
    }

    public void setCheckBoxMode(CheckBoxMode mode) {
        tree.setCheckBoxMode(mode);
    }

    public Set<Path<String>> getCheckedNodes() {
        return tree.getCheckedNodes();
    }
}
