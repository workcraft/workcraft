package org.workcraft.gui.trees;

import info.clearthought.layout.TableLayout;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.workspace.Path;
import org.workcraft.workspace.Workspace;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

public class TreeWindow<Node> extends JPanel {

    public static final String TRICKY_PREFIX = "!";

    private JTree tree;
    private final TreePopupProvider<Node> popupProvider;
    private final Set<Node> checkedNodes = new HashSet<>();
    private CheckBoxMode checkBoxMode = CheckBoxMode.NONE;
    private JCheckBox checkBox;
    private boolean externalExpanded = false;

    public enum CheckBoxMode { NONE, LEAF, ALL }

    private final class DecorationTreeSourceAdapter extends TreeSourceAdapter<Node> {
        private final TreeDecorator<Node> decorator;

        private DecorationTreeSourceAdapter(TreeSource<Node> source, TreeDecorator<Node> decorator) {
            super(source);
            this.decorator = decorator;
        }

        @Override
        public TreeListener<Node> getListener(final TreeListener<Node> chain) {
            return new TreeListenerAdapter<Node>(chain) {
                @Override
                public void restructured(Path<Node> path) {
                    List<TreePath> expanded = new ArrayList<>();
                    for (int i = 0; i < tree.getRowCount(); i++) {
                        final TreePath treePath = tree.getPathForRow(i);
                        if (tree.isExpanded(i)) {
                            expanded.add(treePath);
                        }
                    }

                    if (!externalExpanded) {
                        for (Node n : getChildren(getRoot())) {
                            if (Workspace.EXTERNAL_PATH.equals(decorator.getName(n))) {
                                expanded.add(new TreePath(Path.getPath(getPath(n)).toArray()));
                                externalExpanded = true;
                            }
                        }
                    }
                    super.restructured(path);
                    for (TreePath p : expanded) {
                        tree.expandPath(p);
                    }
                }
            };
        }
    }

    private final class PopupMouseAdapter extends MouseAdapter {
        private final TreeSource<Node> source;

        private PopupMouseAdapter(TreeSource<Node> source) {
            this.source = source;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                final int x = e.getX();
                final int y = e.getY();
                tree.setSelectionPath(tree.getClosestPathForLocation(x, y));
                final Rectangle rowBounds = tree.getRowBounds(tree.getSelectionRows()[0]);
                if (y < rowBounds.getMinY() || y > rowBounds.getMaxY()) {
                    tree.setSelectionPath(null);
                    popupProvider.getPopup(source.getRoot()).show(tree, x, y);
                } else {
                    popupProvider.getPopup(selected()).show(tree, x, y);
                }
            }
        }
    }

    private final class ClickMouseAdapter extends MouseAdapter {
        private final TreeSource<Node> source;

        private ClickMouseAdapter(TreeSource<Node> source) {
            this.source = source;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            final int x = e.getX();
            final int y = e.getY();
            int row = tree.getClosestRowForLocation(x, y);
            if (row != -1) {
                final Rectangle rowBounds = tree.getRowBounds(row);
                if (rowBounds.contains(x, y)) {
                    @SuppressWarnings("unchecked")
                    Node node = (Node) tree.getPathForRow(row).getLastPathComponent();
                    if ((checkBoxMode == CheckBoxMode.ALL)
                            || ((checkBoxMode == CheckBoxMode.LEAF) && source.isLeaf(node))) {
                        if (checkedNodes.contains(node)) {
                            checkedNodes.remove(node);
                        } else {
                            checkedNodes.add(node);
                        }
                        tree.repaint(rowBounds);
                    }
                }
            }
        }
    }

    private final class TreeCellRenderer extends DefaultTreeCellRenderer {
        private final TreeSource<Node> source;
        private final TreeDecorator<Node> decorator;
        private static final long serialVersionUID = 1L;

        private final double[][] size = new double[][] {
            {TableLayout.PREFERRED, TableLayout.PREFERRED },
            {TableLayout.PREFERRED},
        };

        private final TableLayout layout = new TableLayout(size);
        private final JPanel cellRenderer = new JPanel(layout);

        private TreeCellRenderer(TreeSource<Node> source, TreeDecorator<Node> decorator) {
            this.source = source;
            this.decorator = decorator;
            layout.setHGap(SizeHelper.getLayoutHGap());
            layout.setVGap(SizeHelper.getLayoutVGap());
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                Object value, boolean sel, boolean expanded, boolean leaf,
                int row, boolean hasFocus) {

            Node node = (Node) value;
            String name = decorator.getName(node);
            boolean tricky = name.startsWith(TRICKY_PREFIX);
            if (tricky) {
                name = name.substring(TRICKY_PREFIX.length());
            }

            Component result = super.getTreeCellRendererComponent(tree, name, sel, expanded, leaf, row, hasFocus);

            if (tricky) {
                result.setFont(result.getFont().deriveFont(Font.ITALIC));
            }

            Icon icon = decorator.getIcon(node);
            if (icon != null) {
                setIcon(icon);
            }

            cellRenderer.removeAll();
            if ((checkBoxMode == CheckBoxMode.ALL) || ((checkBoxMode == CheckBoxMode.LEAF) && source.isLeaf(node))) {
                cellRenderer.add(checkBox, "0 0");
                cellRenderer.add(result, "1 0");
                checkBox.setSelected(checkedNodes.contains(node));
                result = cellRenderer;
            }
            return result;
        }
    }

    private final class RefreshKeyAdapter extends KeyAdapter {
        private final TreeSource<Node> source;

        private RefreshKeyAdapter(TreeSource<Node> source) {
            this.source = source;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_F5) {
                Path<Node> root = Path.root(source.getRoot());
                sourceWithRestructuredTrapped.getListener().restructured(root);
            }
        }
    }

    public TreeWindow(TreeSource<Node> source, TreeDecorator<Node> decorator, TreePopupProvider<Node> popupProvider) {
        this.popupProvider = popupProvider;
        startup(source, decorator);
    }

    private static final long serialVersionUID = 1L;
    private TreeSourceAdapter<Node> sourceWithRestructuredTrapped;

    @SuppressWarnings("unchecked")
    public Node selected() {
        TreePath path = tree.getSelectionPath();
        return (Node) path.getLastPathComponent();
    }

    public void setCheckBoxMode(CheckBoxMode mode) {
        this.checkBoxMode = mode;
        checkedNodes.clear();
        Path<Node> root = Path.root(sourceWithRestructuredTrapped.getRoot());
        sourceWithRestructuredTrapped.getListener().restructured(root);
    }

    public void clearCheckBoxes() {
        checkedNodes.clear();
        tree.repaint();
    }

    public Set<Node> getCheckedNodes() {
        return Collections.unmodifiableSet(checkedNodes);
    }

    public void startup(final TreeSource<Node> source, final TreeDecorator<Node> decorator) {
        tree = new JTree();
        tree.setFocusable(true);
        tree.setBorder(SizeHelper.getEmptyBorder());

        checkBox = new JCheckBox();
        checkBox.setBackground(tree.getBackground());
        checkBox.setMargin(SizeHelper.getTreeCheckboxMargin());

        sourceWithRestructuredTrapped = new DecorationTreeSourceAdapter(source, decorator);

        tree.setModel(new TreeModelWrapper<>(sourceWithRestructuredTrapped));
        if (popupProvider != null) {
            tree.addMouseListener(new PopupMouseAdapter(source));
        }
        tree.addMouseListener(new ClickMouseAdapter(source));
        tree.setCellRenderer(new TreeCellRenderer(source, decorator));
        tree.addKeyListener(new RefreshKeyAdapter(source));
        setLayout(new BorderLayout());
        add(tree, BorderLayout.CENTER);
    }

    public void makeVisible(Path<Node> node) {
        tree.makeVisible(new TreePath(Path.getPath(node).toArray()));
    }

    public static <Node> TreeWindow<Node> create(TreeSource<Node> source, TreeDecorator<Node> decorator,
            TreePopupProvider<Node> popupProvider) {
        return new TreeWindow<>(source, decorator, popupProvider);
    }

    public void setChecked(Node node, boolean value) {
        boolean needsRepaint = false;
        if (value) {
            needsRepaint = checkedNodes.add(node);
        } else {
            needsRepaint = checkedNodes.remove(node);
        }
        if (needsRepaint) {
            tree.repaint();
        }
    }

}
