package org.workcraft.plugins.petri;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.Connection;
import org.workcraft.exceptions.InvalidConnectionException;

class DOMTests {

    @Test
    void test1() throws InvalidConnectionException {
        Petri petri = new Petri();

        Place p1 = new Place();
        Place p2 = new Place();
        Transition t1 = new Transition();

        petri.add(p1);
        petri.add(p2);
        petri.add(t1);
        Connection con1 = petri.connect(p1, t1);
        Connection con2 = petri.connect(t1, p2);

        Assertions.assertSame(p1, petri.getNodeByReference(petri.getNodeReference(p1)));
        Assertions.assertSame(p2, petri.getNodeByReference(petri.getNodeReference(p2)));

        Assertions.assertTrue(petri.getPreset(p2).contains(t1));
        Assertions.assertTrue(petri.getPostset(p1).contains(t1));

        Assertions.assertTrue(petri.getConnections(p1).contains(con1));

        petri.remove(p1);

        Assertions.assertTrue(petri.getConnections(t1).contains(con2));
        Assertions.assertFalse(petri.getConnections(t1).contains(con1));

        boolean thrown = true;
        try {
            petri.getNodeReference(null);
            thrown = false;
        } catch (Throwable th) { }

        Assertions.assertTrue(thrown);
    }

}
