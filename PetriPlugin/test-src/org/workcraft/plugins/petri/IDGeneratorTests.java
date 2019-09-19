package org.workcraft.plugins.petri;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.references.IDGenerator;
import org.workcraft.exceptions.DuplicateIDException;

public class IDGeneratorTests {

    @Test
    public void testReserveID() {
        IDGenerator generator = new IDGenerator();
        generator.reserveID(8);
        Assert.assertEquals(0, generator.getNextID());
        for (int i = 1; i < 8; i++) {
            Assert.assertEquals(i, generator.getNextID());
        }
        Assert.assertEquals(9, generator.getNextID());
    }

    @Test
    public void testReleaseID() {
        IDGenerator generator = new IDGenerator();
        generator.reserveID(8);
        Assert.assertEquals(0, generator.getNextID());
        for (int i = 1; i < 8; i++) {
            Assert.assertEquals(i, generator.getNextID());
        }
        Assert.assertEquals(9, generator.getNextID());

        generator.releaseID(4);
        Assert.assertEquals(4, generator.getNextID());
        Assert.assertEquals(10, generator.getNextID());

        try {
            generator.reserveID(4);
            Assert.fail("Duplicate ID issued");
        } catch (DuplicateIDException e) {
            Assert.assertEquals(4, e.getId());
        }

        generator.releaseID(4);
        generator.releaseID(0);

        Assert.assertEquals(0, generator.getNextID());
        generator.reserveID(4);
        Assert.assertEquals(11, generator.getNextID());
    }

    @Test
    public void testGetNextID() {
        IDGenerator generator = new IDGenerator();
        Assert.assertEquals(0, generator.getNextID());
        Assert.assertEquals(1, generator.getNextID());
    }
}
