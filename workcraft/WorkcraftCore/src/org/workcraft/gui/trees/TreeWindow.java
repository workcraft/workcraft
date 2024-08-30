package org.workcraft.gui.trees;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.workspace.Path;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.Workspace;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class TreeWindow<T> extends JPanel {

    public enum CheckBoxMode { NONE, LEAF, ALL }

    @Serial
    private static final long serialVersionUID = 1L;
    public static final String TRICKY_PREFIX = "!";

    private JTree tree;
    private final TreePopupProvider<T> popupProvider;
    private final Consumer<T> doubleClickAction;

    private final Set<T> checkedNodes = new HashSet<>();
    private CheckBoxMode checkBoxMode = CheckBoxMode.NONE;
    private JCheckBox checkBox;
    private DecorationTreeSourceAdapter sourceWithRestructuredTrapped;
    private boolean autoExpand = true;

    private final class ExpandTreeListenerAdapter extends TreeListenerAdapter<T> {
        ExpandTreeListenerAdapter(TreeListener<T> chain) {
            super(chain);
        }

        @Override
        public void restructured(Path<T> path) {
            if (!autoExpand) {
                super.restructured(path);
            } else {
                List<TreePath> treePaths = calcExpandPaths();
                super.restructured(path);
                expandPaths(treePaths);
            }
        }
    }

    private final class DecorationTreeSourceAdapter extends TreeSourceAdapter<T> {
        private final TreeDecorator<T> decorator;

        private DecorationTreeSourceAdapter(TreeSource<T> source, TreeDecorator<T> decorator) {
            super(source);
            this.decorator = decorator;
        }

        @Override
        public TreeListener<T> getListener(final TreeListener<T> chain) {
            return new ExpandTreeListenerAdapter(chain);
        }

        public TreeDecorator<T> getDecorator() {
            return decorator;
        }
    }

    private final class PopupMouseAdapter extends MouseAdapter {
        private final TreeSource<T> source;

        private PopupMouseAdapter(TreeSource<T> source) {
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
                int[] selectionRows = tree.getSelectionRows();
                if ((selectionRows != null) && (selectionRows.length > 0)) {
                    final Rectangle rowBounds = tree.getRowBounds(selectionRows[0]);
                    if ((y < rowBounds.getMinY()) || (y > rowBounds.getMaxY())) {
                        tree.setSelectionPath(null);
                        popupProvider.getPopup(source.getRoot()).show(tree, x, y);
                    } else {
                        popupProvider.getPopup(selected()).show(tree, x, y);
                    }
                }
            }
        }
    }

    private final class ClickMouseAdapter extends MouseAdapter {
        private final TreeSource<T> source;

        private ClickMouseAdapter(TreeSource<T> source) {
            this.source = source;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                final int x = e.getX();
                final int y = e.getY();
                int row = tree.getClosestRowForLocation(x, y);
                if (row != -1) {
                    final Rectangle rowBounds = tree.getRowBounds(row);
                    if (rowBounds.contains(x, y)) {
                        T node = cast(tree.getPathForRow(row).getLastPathComponent());
                        if (e.getClickCount() > 1) {
                            if (doubleClickAction != null) {
                                doubleClickAction.accept(node);
                            }
                        } else if ((checkBoxMode == CheckBoxMode.ALL)
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
    }

    private final class TreeCellRenderer extends DefaultTreeCellRenderer {
        private final TreeSource<T> source;
        private final TreeDecorator<T> decorator;
        @Serial
        private static final long serialVersionUID = 1L;

        private final TableLayout layout = GuiUtils.createTableLayout(
                new double[]{TableLayout.PREFERRED, TableLayout.PREFERRED},
                new double[]{TableLayout.PREFERRED});

        private final JPanel cellRenderer = new JPanel(layout);

        private TreeCellRenderer(TreeSource<T> source, TreeDecorator<T> decorator) {
            this.source = source;
            this.decorator = decorator;
            layout.setHGap(SizeHelper.getLayoutHGap());
            layout.setVGap(SizeHelper.getLayoutVGap());
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                Object value, boolean sel, boolean expanded, boolean leaf,
                int row, boolean hasFocus) {

            T node = cast(value);
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
                cellRenderer.add(checkBox, new TableLayoutConstraints(0, 0));
                cellRenderer.add(result, new TableLayoutConstraints(1, 0));
                checkBox.setSelected(checkedNodes.contains(node));
                result = cellRenderer;
            }
            return result;
        }
    }

    private final class RefreshKeyAdapter extends KeyAdapter {
        private final TreeSource<T> source;

        private RefreshKeyAdapter(TreeSource<T> source) {
            this.source = source;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_F5) {
                Path<T> root = Path.root(source.getRoot());
                sourceWithRestructuredTrapped.getListener().restructured(root);
            }
            if ((checkBoxMode != CheckBoxMode.NONE) && (e.getKeyCode() == KeyEvent.VK_SPACE)) {
                TreePath selectionPath = tree.getSelectionPath();
                Object selectedComponent = selectionPath == null ? null : selectionPath.getLastPathComponent();
                if (selectedComponent != null) {
                    T node = cast(selectedComponent);
                    setChecked(node, !getCheckedNodes().contains(node));
                }
            }
        }
    }

    public TreeWindow(TreeSource<T> source, TreeDecorator<T> decorator,
            TreePopupProvider<T> popupProvider, Consumer<T> doubleClickAction) {

        this.popupProvider = popupProvider;
        this.doubleClickAction = doubleClickAction;
        init(source, decorator);
    }

    public T selected() {
        TreePath selectionPath = tree.getSelectionPath();
        return selectionPath == null ? null : cast(selectionPath.getLastPathComponent());
    }

    public void setCheckBoxMode(CheckBoxMode mode) {
        this.checkBoxMode = mode;
        checkedNodes.clear();
        Path<T> root = Path.root(sourceWithRestructuredTrapped.getRoot());
        sourceWithRestructuredTrapped.getListener().restructured(root);
    }

    public Set<T> getCheckedNodes() {
        return Collections.unmodifiableSet(checkedNodes);
    }

    public void init(final TreeSource<T> source, final TreeDecorator<T> decorator) {
        tree = new JTree();
        tree.setFocusable(true);
        tree.setBorder(GuiUtils.getEmptyBorder());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

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

    public void makeVisible(Path<T> node) {
        tree.makeVisible(new TreePath(Path.getPath(node).toArray()));
    }

    public void setChecked(T node, boolean value) {
        boolean needsRepaint = value ? checkedNodes.add(node) : checkedNodes.remove(node);
        if (needsRepaint) {
            tree.repaint();
        }
    }

    @SuppressWarnings("unchecked")
    private T cast(Object node) {
        return (T) node;
    }

    public void refresh() {
        Path<T> root = Path.root(sourceWithRestructuredTrapped.getRoot());
        sourceWithRestructuredTrapped.getListener().restructured(root);
    }

    public void setAutoExpand(boolean value) {
        autoExpand = value;
    }

    private List<TreePath> calcExpandPaths() {
        List<TreePath> result = new ArrayList<>();
        for (int i = 0; i < tree.getRowCount(); i++) {
            if (tree.isExpanded(i)) {
                final TreePath treePath = tree.getPathForRow(i);
                result.add(treePath);
            }
        }
        TreeDecorator<T> decorator = sourceWithRestructuredTrapped.getDecorator();
        if (decorator != null) {
            T root = sourceWithRestructuredTrapped.getRoot();
            for (T node : sourceWithRestructuredTrapped.getChildren(root)) {
                String nodeName = decorator.getName(node);
                if (Workspace.EXTERNAL_PATH.equals(nodeName)) {
                    Path<T> nodePath = sourceWithRestructuredTrapped.getPath(node);
                    result.add(new TreePath(Path.getPath(nodePath).toArray()));
                    break;
                }
            }
        }
        return result;
    }

    private void expandPaths(List<TreePath> expandPaths) {
        for (TreePath treePath : expandPaths) {
            tree.expandPath(treePath);
        }
    }

}
