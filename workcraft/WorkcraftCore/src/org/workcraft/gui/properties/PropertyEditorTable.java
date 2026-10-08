package org.workcraft.gui.properties;

import org.workcraft.Framework;
import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.controls.FlatHeaderRenderer;
import org.workcraft.plugins.PluginManager;
import org.workcraft.utils.DialogUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.TableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

public class PropertyEditorTable extends JTable {

    private final PropertyEditorTableModel model;
    private static final HashMap<Class<?>, PropertyClass<?, ?>> propertyClasses = new HashMap<>();

    static {
        propertyClasses.put(int.class, new IntegerProperty());
        propertyClasses.put(Integer.class, new IntegerProperty());
        propertyClasses.put(double.class, new DoubleProperty());
        propertyClasses.put(Double.class, new DoubleProperty());
        propertyClasses.put(String.class, new StringProperty());
        propertyClasses.put(boolean.class, new BooleanProperty());
        propertyClasses.put(Boolean.class, new BooleanProperty());
        propertyClasses.put(Color.class, new ColorProperty());
        propertyClasses.put(File.class, new FileProperty());
        propertyClasses.put(FileReference.class, new FileReferenceProperty());
        propertyClasses.put(Action.class, new ActionProperty());
        propertyClasses.put(TextAction.class, new TextActionProperty());
        propertyClasses.put(ActionList.class, new ActionListProperty());
        propertyClasses.put(Toggle.class, new ToggleProperty());
        propertyClasses.put(LegendList.class, new LegendListProperty());

        PluginManager pm = Framework.getInstance().getPluginManager();
        for (PropertyClassProvider p : pm.getPropertyProviders()) {
            propertyClasses.put(p.getPropertyType(), p.getPropertyGui());
        }
    }

    private TableCellRenderer[] cellRenderers;
    private TableCellEditor[] cellEditors;

    private Runnable editingFinishedHandler = null;

    // Component that had focus before editing started. Kept while switching between cells.
    private Component previousFocusOwner = null;

    public PropertyEditorTable() {
        this("", "");
    }

    public PropertyEditorTable(String propertyHeader, String valueHeader) {
        super();
        model = new PropertyEditorTableModel(propertyHeader, valueHeader);
        setModel(model);

        // setUI is overridden to forbid changing it externally, therefore call super.setUI
        super.setUI(new PropertyEditorTableUI(model));

        // Make header flat and fixed order
        getTableHeader().setDefaultRenderer(new FlatHeaderRenderer());
        getTableHeader().setReorderingAllowed(false);

        // Disable drag and selection
        setDragEnabled(false);
        setFocusable(false);

        // Adjust row height to fit the current font size
        setRowHeight(SizeHelper.getComponentHeightFromFont(getFont()));
    }

    @Override
    public PropertyEditorTableModel getModel() {
        return model;
    }

    @Override
    public void setUI(TableUI ui) {
        // Forbid changing UI
    }

    public void assign(Properties properties) {
        model.assign(properties);
        update();
        // Structure change resets column widths, and they are only fixed by a later layout. Do it now, as
        // otherwise a click in the same event (e.g. that stopped the edit) is mapped to a wrong column.
        doLayout();
    }

    public void clear() {
        // Clearing removes the active editor without finishing the edit
        previousFocusOwner = null;
        model.clear();
        update();
    }

    // Handler is called when an edit is finished (stopped or cancelled)
    public void setEditingFinishedHandler(Runnable handler) {
        editingFinishedHandler = handler;
    }

    private void notifyEditingFinished() {
        if (editingFinishedHandler != null) {
            editingFinishedHandler.run();
        }
    }

    @Override
    public void editingCanceled(ChangeEvent event) {
        super.editingCanceled(event);
        restoreFocus();
        notifyEditingFinished();
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        // When switching from another cell the original owner is kept: the current focus owner is either that
        // editor or a component that has just got the focus after the editor was removed
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        // (the previous edit is already stopped here, but it has kept the owner as it was a cell switch)
        boolean switching = (previousFocusOwner != null) && previousFocusOwner.isShowing();
        boolean result = super.editCellAt(row, column, e);
        if (result && !switching && (focusOwner != null) && !SwingUtilities.isDescendingFrom(focusOwner, this)) {
            previousFocusOwner = focusOwner;
        }
        return result;
    }

    /**
     * Focus listener helper for editors with a focusable component: stops the edit when the focus has left it.
     * Does nothing if the editor is no longer the active one (e.g. replaced by an editor of another cell),
     * and if the focus has gone outside the table on purpose, it is not taken back.
     */
    public static void stopEditingOnFocusLost(JTable table, TableCellEditor editor, FocusEvent e) {
        if (e.isTemporary() || (table == null) || (table.getCellEditor() != editor)) {
            return;
        }
        Component opposite = e.getOppositeComponent();
        if ((opposite != null) && !SwingUtilities.isDescendingFrom(opposite, table)
                && (table instanceof PropertyEditorTable propertyTable)) {

            propertyTable.previousFocusOwner = null;
        }
        editor.stopCellEditing();
    }

    /**
     * Cancels the edit, but only if the editor is still the active one of the table (i.e. the edit has not been
     * stopped or replaced by an editor of another cell in the meantime).
     */
    public static void cancelEditingIfActive(JTable table, TableCellEditor editor) {
        if ((table != null) && (table.getCellEditor() == editor)) {
            editor.cancelCellEditing();
        }
    }

    private void restoreFocus() {
        // Click on another table cell stops the current edit and starts a new one that must keep the focus
        // and inherit the previous focus owner
        if ((EventQueue.getCurrentEvent() instanceof MouseEvent mouseEvent) && (mouseEvent.getSource() == this)) {
            return;
        }
        Component owner = previousFocusOwner;
        previousFocusOwner = null;
        // Deferred, as stopping the edit makes the table request focus for itself
        if (owner != null) {
            SwingUtilities.invokeLater(() -> {
                if (owner.isShowing()) {
                    owner.requestFocusInWindow();
                }
            });
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void update() {
        cellRenderers = new TableCellRenderer[model.getRowCount()];
        cellEditors = new TableCellEditor[model.getRowCount()];
        for (int i = 0; i < model.getRowCount(); i++) {
            PropertyDescriptor decl = model.getDeclaration(i);
            Class<?> type = decl.getType();
            if (type.isEnum()) {
                model.setRowClass(i, null);
                cellRenderers[i] = new ChoiceCellRenderer();
                cellEditors[i] = new ChoiceCellEditor(decl);
            } else {
                PropertyClass cls = propertyClasses.get(type);
                model.setRowClass(i, cls);
                if (cls != null) {
                    Map<?, String> predefinedValues = decl.getChoice();
                    cellRenderers[i] = cls.getCellRenderer(predefinedValues != null);
                    cellEditors[i] = cls.getCellEditor(predefinedValues);
                } else {
                    // no PropertyClass exists for this class, fall back to read-only mode using Object.toString()
                    System.err.println("Data class '" + type.getName() + "' is not supported by the Property editor.");
                    cellRenderers[i] = new DefaultTableCellRenderer();
                    cellEditors[i] = null;
                }
            }
        }
    }

    @Override
    public TableCellEditor getCellEditor(int row, int col) {
        if (col > 0) {
            return cellEditors[row];
        }
        return super.getCellEditor(row, col);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int col) {
        return (col > 0) ? cellRenderers[row] : new PropertyDeclarationRenderer(model.getDeclaration(row));
    }

    @SuppressWarnings("CatchMayIgnoreException")
    @Override
    public void editingStopped(ChangeEvent event) {
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            Object value = editor.getCellEditorValue();
            try {
                setValueAt(value, editingRow, editingColumn);
            } catch (Throwable t) {
                String msg = t.getMessage();
                if ((msg != null) && !msg.isEmpty()) {
                    DialogUtils.showError(t.getMessage(), "Cannot change property");
                }
            } finally {
                removeEditor();
                update();
                restoreFocus();
                notifyEditingFinished();
            }
        }
    }

    @Override
    public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
        Rectangle result = super.getCellRect(row, column, includeSpacing);
        PropertyDescriptor<?> declaration = model.getDeclaration(row);
        if ((declaration != null) && declaration.isSpan()) {
            if (column == 0) {
                result.width = 0;
            } else {
                Rectangle rect = super.getCellRect(row, 0, includeSpacing);
                result.width += result.x - rect.x;
                result.x = rect.x;
            }
        }
        return result;
    }

    @Override
    public int columnAtPoint(Point point) {
        int column = super.columnAtPoint(point);
        if (column == 0) {
            int row = super.rowAtPoint(point);
            PropertyDescriptor<?> declaration = model.getDeclaration(row);
            if ((declaration != null) && declaration.isSpan()) {
                column = 1;
            }
        }
        return column;
    }

    public Point getClipMinCell(Rectangle clip) {
        Point p = clip.getLocation();
        int column = getPositiveOrDefaultValue(super.columnAtPoint(p), 0);
        int row = getPositiveOrDefaultValue(super.rowAtPoint(p), 0);
        return new Point(column, row);
    }

    public Point getClipMaxCell(Rectangle clip) {
        Point p = new Point(clip.x + clip.width - 1, clip.y + clip.height - 1);
        int column = getPositiveOrDefaultValue(super.columnAtPoint(p), getColumnCount() - 1);
        int row = getPositiveOrDefaultValue(super.rowAtPoint(p), getRowCount() - 1);
        return new Point(column, row);
    }

    private int getPositiveOrDefaultValue(int value, int defaultValue) {
        return value < 0 ? defaultValue : value;
    }

}
