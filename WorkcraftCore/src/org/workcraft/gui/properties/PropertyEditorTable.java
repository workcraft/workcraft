package org.workcraft.gui.properties;

import org.workcraft.Framework;
import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.actions.Action;
import org.workcraft.plugins.PluginInfo;
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

@SuppressWarnings("serial")
public class PropertyEditorTable extends JTable {

    private final PropertyEditorTableModel model;
    private final HashMap<Class<?>, PropertyClass> propertyClasses;

    private TableCellRenderer[] cellRenderers;
    private TableCellEditor[] cellEditors;

    public PropertyEditorTable() {
        this("", "");
    }

    public PropertyEditorTable(String propertyHeader, String valueHeader) {
        super();

        model = new PropertyEditorTableModel(propertyHeader, valueHeader);
        setModel(model);
        super.setUI(new PropertyEditorTableUI(model));

        getTableHeader().setDefaultRenderer(new FlatHeaderRenderer());
        setFocusable(false);
        setRowHeight(SizeHelper.getComponentHeightFromFont(getFont()));

        propertyClasses = new HashMap<>();
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

        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        for (PluginInfo<? extends PropertyClassProvider> plugin : pm.getPropertyPlugins()) {
            PropertyClassProvider instance = plugin.newInstance();
            propertyClasses.put(instance.getPropertyType(), instance.getPropertyGui());
        }
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

    private void update() {
        cellRenderers = new TableCellRenderer[model.getRowCount()];
        cellEditors = new TableCellEditor[model.getRowCount()];
        for (int i = 0; i < model.getRowCount(); i++) {
            PropertyDescriptor decl = model.getDeclaration(i);
            if (decl.getChoice() != null) {
                // If object declares a predefined set of values, use a ComboBox to edit the property regardless of class
                model.setRowClass(i, null);
                cellRenderers[i] = new ChoiceCellRenderer();
                cellEditors[i] = new ChoiceCellEditor(decl);
            } else {
                // otherwise, try to get a corresponding PropertyClass object, that knows how to edit a property of this class
                Class type = decl.getType();
                PropertyClass cls = propertyClasses.get(type);
                model.setRowClass(i, cls);
                if (cls != null) {
                    cellRenderers[i] = cls.getCellRenderer();
                    cellEditors[i] = cls.getCellEditor();
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
        if (col > 0) {
            return cellRenderers[row];
        }

        return new TableCellRenderer() {

            private final JLabel label = new JLabel() {
                @Override
                public void paint(Graphics g) {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                    super.paint(g);
                }
            };

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                String text = (value == null) ? "" : value.toString();
                label.setText(text);
                label.setBorder(SizeHelper.getTableCellBorder());

                Font font = label.getFont();
                PropertyDescriptor descriptor = model.getDeclaration(row);
                if ((descriptor.getValue() == null) && descriptor.isCombinable()) {
                    label.setFont(font.deriveFont(Font.BOLD));
                }

                int availableWidth = table.getColumnModel().getColumn(column).getWidth();
                availableWidth -= table.getIntercellSpacing().getWidth();
                Insets borderInsets = label.getBorder().getBorderInsets(label);
                availableWidth -= borderInsets.left + borderInsets.right;
                FontMetrics fontMetrics = getFontMetrics(font);
                if (fontMetrics.stringWidth(text) > availableWidth) {
                    label.setToolTipText(text);
                }
                return label;
            }
        };
    }

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
        PropertyDescriptor declaration = model.getDeclaration(row);
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
            PropertyDescriptor declaration = model.getDeclaration(row);
            if ((declaration != null) && declaration.isSpan()) {
                column = 1;
            }
        }
        return column;
    }

}
