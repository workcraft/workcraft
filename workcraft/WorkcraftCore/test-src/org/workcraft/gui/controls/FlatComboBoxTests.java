package org.workcraft.gui.controls;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

class FlatComboBoxTests {

    @Test
    void testCommitTypedTextDoesNotPressEnterOfWindow() {
        FlatComboBox comboBox = new FlatComboBox();
        comboBox.setEditable(true);
        comboBox.addItem("1");

        // Root pane with an Enter binding, as the OK action of a dialog
        JRootPane rootPane = new JRootPane();
        rootPane.getContentPane().add(comboBox);
        AtomicBoolean enterPressed = new AtomicBoolean(false);
        rootPane.registerKeyboardAction(event -> enterPressed.set(true),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
        textField.setText("42");
        comboBox.commitTypedText();

        Assertions.assertEquals("42", comboBox.getSelectedItem());
        Assertions.assertFalse(enterPressed.get());
    }

}
