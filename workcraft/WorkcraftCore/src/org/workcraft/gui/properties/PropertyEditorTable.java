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
import java.io.File;
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
        propertyClasses.put(LegendList.class, new LegendListProperty());

        PluginManager pm = Framework.getInstance().getPluginManager();
        for (PropertyClassProvider p : pm.getPropertyProviders()) {
            propertyClasses.put(p.getPropertyType(), p.getPropertyGui());
        }
    }

    private TableCellRenderer[] cellRenderers;
    private TableCellEditor[] cellEditors;

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
    }

    public void clear() {
        model.clear();
        update();
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
