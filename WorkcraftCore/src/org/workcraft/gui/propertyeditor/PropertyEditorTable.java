package org.workcraft.gui.propertyeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.PluginInfo;

@SuppressWarnings("serial")
public class PropertyEditorTable extends JTable implements PropertyEditor {
    public static final Border BORDER_RENDER = BorderFactory.createEmptyBorder(1, 3, 1, 1);
    public static final Border BORDER_EDIT = BorderFactory.createEmptyBorder(1, 3, 1, 1);

    HashMap<Class<?>, PropertyClass> propertyClasses;
    TableCellRenderer[] cellRenderers;
    TableCellEditor[] cellEditors;
    PropertyEditorTableModel model;

    public PropertyEditorTable() {
        super();

        model = new PropertyEditorTableModel();
        setModel(model);

        setTableHeader(null);
        setFocusable(false);
        setRowHeight(SizeHelper.getComponentHeightFromFont(getFont()));

        propertyClasses = new HashMap<Class<?>, PropertyClass>();
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
        PluginManager pluginManager = framework.getPluginManager();
        for (PluginInfo<? extends PropertyClassProvider> plugin : pluginManager.getPlugins(PropertyClassProvider.class)) {
            PropertyClassProvider instance = plugin.newInstance();
            propertyClasses.put(instance.getPropertyType(), instance.getPropertyGui());
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
                    label.setText(value.toString());
                    label.setBorder(BORDER_RENDER);
                    PropertyDescriptor descriptor = model.getRowDeclaration(row);
                    try {
                        if ((descriptor.getValue() == null) && descriptor.isCombinable()) {
                            Font boldFont = label.getFont().deriveFont(Font.BOLD);
                            label.setFont(boldFont);
                        }
                    } catch (InvocationTargetException e) {
                    }
                    //label.setEnabled(false);
                    return label;
                }
            };
        }
    }

    @Override
    public void clearObject() {
        model.clearObject();
    }

    @Override
    public void setObject(Properties o) {
        model.setObject(o);
        cellRenderers = new TableCellRenderer[model.getRowCount()];
        cellEditors = new TableCellEditor[model.getRowCount()];
        for (int i = 0; i < model.getRowCount(); i++) {
            PropertyDescriptor decl = model.getRowDeclaration(i);

            // If object declares a predefined set of values, use a ComboBox to edit the property regardless of class
            if (decl.getChoice() != null) {
                model.setRowClass(i, null);
                cellRenderers[i] = new DefaultCellRenderer();
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
                    System.err.println("Data class '" + decl.getType().getName() + "' is not supported by the Property Editor.");
                    cellRenderers[i] = new DefaultTableCellRenderer();
                    cellEditors[i] = null;
                }
            }
        }
    }

    public Properties getObject() {
        return model.getObject();
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        TableCellEditor editor = getCellEditor();
        if (editor != null) {
            Object value = editor.getCellEditorValue();
            try {
                setValueAt(value, editingRow, editingColumn);
                removeEditor();
            } catch (Throwable t) {
                JOptionPane.showMessageDialog(null, t.getMessage(), "Cannot change property", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

}
