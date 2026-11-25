package org.workcraft.gui.properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.exceptions.ArgumentException;

class IntegerPropertyTests {

    @Test
    void testValidValue() {
        IntegerProperty property = new IntegerProperty();
        Assertions.assertEquals(42, property.fromCellEditorValue("42"));
        Assertions.assertEquals(-7, property.fromCellEditorValue(" -7 "));
    }

    @Test
    void testInvalidValue() {
        IntegerProperty property = new IntegerProperty();
        Assertions.assertThrows(ArgumentException.class, () -> property.fromCellEditorValue("9o"));
        Assertions.assertThrows(ArgumentException.class, () -> property.fromCellEditorValue(""));
        Assertions.assertThrows(ArgumentException.class, () -> property.fromCellEditorValue("1.5"));
    }

    @Test
    void testValidBeginning() {
        IntegerProperty property = new IntegerProperty();
        Assertions.assertTrue(property.isValidBeginning(""));
        Assertions.assertTrue(property.isValidBeginning("-"));
        Assertions.assertTrue(property.isValidBeginning(" +42 "));
        Assertions.assertFalse(property.isValidBeginning("9o"));
        Assertions.assertFalse(property.isValidBeginning("1."));
    }

}
