package org.workcraft.plugins.petri;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.references.IDGenerator;
import org.workcraft.exceptions.DuplicateIDException;

class IDGeneratorTests {

    @Test
    void testReserveID() {
        IDGenerator generator = new IDGenerator();
        generator.reserveID(8);
        Assertions.assertEquals(0, generator.getNextID());
        for (int i = 1; i < 8; i++) {
            Assertions.assertEquals(i, generator.getNextID());
        }
        Assertions.assertEquals(9, generator.getNextID());
    }

    @Test
    void testReleaseID() {
        IDGenerator generator = new IDGenerator();
        generator.reserveID(8);
        Assertions.assertEquals(0, generator.getNextID());
        for (int i = 1; i < 8; i++) {
            Assertions.assertEquals(i, generator.getNextID());
        }
        Assertions.assertEquals(9, generator.getNextID());

        generator.releaseID(4);
        Assertions.assertEquals(4, generator.getNextID());
        Assertions.assertEquals(10, generator.getNextID());

        try {
            generator.reserveID(4);
            Assertions.fail("Duplicate ID issued");
        } catch (DuplicateIDException e) {
            Assertions.assertEquals(4, e.getId());
        }

        generator.releaseID(4);
        generator.releaseID(0);

        Assertions.assertEquals(0, generator.getNextID());
        generator.reserveID(4);
        Assertions.assertEquals(11, generator.getNextID());
    }

    @Test
    void testGetNextID() {
        IDGenerator generator = new IDGenerator();
        Assertions.assertEquals(0, generator.getNextID());
        Assertions.assertEquals(1, generator.getNextID());
    }

}
