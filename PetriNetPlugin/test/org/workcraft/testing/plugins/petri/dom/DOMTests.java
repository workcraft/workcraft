/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.testing.plugins.petri.dom;

import org.junit.Test;
import org.workcraft.dom.Connection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;

import static org.junit.Assert.*;

public class DOMTests {

    @Test
    public void test1() throws InvalidConnectionException {
        PetriNet pn = new PetriNet();

        Place p1 = new Place();
        Place p2 = new Place();
        Transition t1 = new Transition();

        pn.add(p1);
        pn.add(p2);
        pn.add(t1);
        Connection con1 = pn.connect(p1, t1);
        Connection con2 = pn.connect(t1, p2);

        assertSame(p1, pn.getNodeByReference(pn.getNodeReference(p1)));
        assertSame(p2, pn.getNodeByReference(pn.getNodeReference(p2)));

        assertTrue(pn.getPreset(p2).contains(t1));
        assertTrue(pn.getPostset(p1).contains(t1));

        assertTrue(pn.getConnections(p1).contains(con1));

        pn.remove(p1);

        assertTrue(pn.getConnections(t1).contains(con2));
        assertFalse(pn.getConnections(t1).contains(con1));

        boolean thrown = true;
        try {
            pn.getNodeReference(null);
            thrown = false;
        } catch (Throwable th) { }

        assertTrue(thrown);
    }

}
