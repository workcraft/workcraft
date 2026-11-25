package org.workcraft.gui.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

class BasicCellEditorTests {

    @Test
    void testInvalidTextKeepsEditing() {
        BasicCellEditor editor = new BasicCellEditor(null, new IntegerProperty());
        JTextField textField = (JTextField) editor.getTableCellEditorComponent(new JTable(), "5", false, 0, 1);
        Color validColor = textField.getForeground();

        // Valid beginning of a number is not marked while typing, but is refused when the edit is stopped
        textField.setText("-");
        Assertions.assertEquals(validColor, textField.getForeground());
        Assertions.assertFalse(editor.stopCellEditing());
        Assertions.assertEquals("-", textField.getText());
        Assertions.assertNotEquals(validColor, textField.getForeground());
        Assertions.assertNotNull(textField.getToolTipText());

        // Highlight is removed as soon as the text becomes valid
        textField.setText("-9");
        Assertions.assertEquals(validColor, textField.getForeground());
        Assertions.assertNull(textField.getToolTipText());
        Assertions.assertTrue(editor.stopCellEditing());
        Assertions.assertEquals("-9", editor.getCellEditorValue());
    }

    @Test
    void testInvalidTextMarkedWhileTyping() {
        BasicCellEditor editor = new BasicCellEditor(null, new DoubleProperty());
        JTextField textField = (JTextField) editor.getTableCellEditorComponent(new JTable(), "1.5", false, 0, 1);
        Color validColor = textField.getForeground();

        textField.setText("1e");
        Assertions.assertEquals(validColor, textField.getForeground());

        textField.setText("1.0d");
        Assertions.assertNotEquals(validColor, textField.getForeground());
        Assertions.assertEquals("Value must be a number", textField.getToolTipText());

        textField.setText("1.0");
        Assertions.assertEquals(validColor, textField.getForeground());
    }

    @Test
    void testCancelClearsHighlight() {
        BasicCellEditor editor = new BasicCellEditor(Map.of(9, "small"), new IntegerProperty());
        JComboBox<?> comboBox = (JComboBox<?>) editor.getTableCellEditorComponent(new JTable(), "5", false, 0, 1);
        JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
        Color validColor = textField.getForeground();

        textField.setText("x");
        Assertions.assertNotEquals(validColor, textField.getForeground());
        Assertions.assertFalse(editor.stopCellEditing());

        editor.cancelCellEditing();
        Assertions.assertEquals(validColor, textField.getForeground());
    }

    @Test
    void testAnyTextWithoutProperty() {
        BasicCellEditor editor = new BasicCellEditor();
        JTextField textField = (JTextField) editor.getTableCellEditorComponent(new JTable(), "a", false, 0, 1);
        Color validColor = textField.getForeground();

        textField.setText("anything");
        Assertions.assertEquals(validColor, textField.getForeground());
        Assertions.assertTrue(editor.stopCellEditing());
    }

}
