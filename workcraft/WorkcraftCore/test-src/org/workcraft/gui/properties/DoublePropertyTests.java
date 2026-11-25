package org.workcraft.gui.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.workcraft.exceptions.ArgumentException;

class DoublePropertyTests {

    @Test
    void testValidValue() {
        DoubleProperty property = new DoubleProperty();
        Assertions.assertEquals(1.5, property.fromCellEditorValue("1.5"));
        Assertions.assertEquals(1.5, property.fromCellEditorValue(" 1,5 "));
        Assertions.assertEquals(-2.0, property.fromCellEditorValue("-2"));
        Assertions.assertEquals(2.0, property.fromCellEditorValue("+2"));
        Assertions.assertEquals(0.5, property.fromCellEditorValue(".5"));
        Assertions.assertEquals(5.0, property.fromCellEditorValue("5."));
        Assertions.assertEquals(1000.0, property.fromCellEditorValue("1e3"));
        Assertions.assertEquals(0.0015, property.fromCellEditorValue("1.5E-3"));
    }

    @Test
    void testValidBeginning() {
        DoubleProperty property = new DoubleProperty();
        for (String text : new String[] {"", "-", ".", "1,", "1e", "1.5E-", " 1.5e-3 "}) {
            Assertions.assertTrue(property.isValidBeginning(text), text);
        }
        for (String text : new String[] {"1.5o", "1.0d", "0x", "NaN", "1..5", "1e5.5"}) {
            Assertions.assertFalse(property.isValidBeginning(text), text);
        }
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"", "1.5o", "1.0d", "2F", "0x1p3", "NaN", "Infinity", "-Infinity", "1e999"})
    void testInvalidValue(String text) {
        DoubleProperty property = new DoubleProperty();
        Assertions.assertThrows(ArgumentException.class, () -> property.fromCellEditorValue(text));
    }

}
