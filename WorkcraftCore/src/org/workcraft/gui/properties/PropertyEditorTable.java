package org.workcraft.gui.properties;

import org.workcraft.Framework;
import org.workcraft.plugins.PluginManager;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.utils.DialogUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.HashMap;

@SuppressWarnings("serial")
public class PropertyEditorTable extends JTable {

    HashMap<Class<?>, PropertyClass> propertyClasses;
    TableCellRenderer[] cellRenderers;
    TableCellEditor[] cellEditors;
    PropertyEditorTableModel model;

    public PropertyEditorTable() {
        super();

        model = new PropertyEditorTableModel();
        setModel(model);

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

        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        for (PluginInfo<? extends PropertyClassProvider> plugin : pm.getPropertyPlugins()) {
            PropertyClassProvider instance = plugin.newInstance();
            propertyClasses.put(instance.getPropertyType(), instance.getPropertyGui());
        }
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
            PropertyDescriptor decl = model.getRowDeclaration(i);

            // If object declares a predefined set of values, use a ComboBox to edit the property regardless of class
            if (decl.getChoice() != null) {
                model.setRowClass(i, null);
                cellRenderers[i] = new ChoiceCellRenderer();
                cellEditors[i] = new ChoiceCellEditor(decl);
            } else {
                // otherwise, try to get a corresponding PropertyClass object, that knows how to edit a property of this class
                PropertyClass cls = propertyClasses.get(decl.getType());
                model.setRowClass(i, cls);
                if (cls != null) {
                    cellRenderers[i] = cls.getCellRenderer();
                    cellEditors[i] = cls.getCellEditor();
                } else {
                    // no PropertyClass exists for this class, fall back to read-only mode using Object.toString()
                    System.err.println("Data class '" + decl.getType().getName() + "' is not supported by the Property editor.");
                    cellRenderers[i] = new DefaultTableCellRenderer();
                    cellEditors[i] = null;
                }
            }
        }
    }

    @Override
    public TableCellEditor getCellEditor(int row, int col) {
        if (col == 0) {
            return super.getCellEditor(row, col);
        } else {
            return cellEditors[row];
        }
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int col) {
        if (col > 0) {
            return cellRenderers[row];
        } else {
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

                    String text = value.toString();
                    label.setText(text);
                    label.setBorder(SizeHelper.getTableCellBorder());

                    Font font = label.getFont();
                    PropertyDescriptor descriptor = model.getRowDeclaration(row);
                    if ((descriptor.getValue() == null) && descriptor.isCombinable()) {
                        Font boldFont = font.deriveFont(Font.BOLD);
                        label.setFont(boldFont);
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

}
