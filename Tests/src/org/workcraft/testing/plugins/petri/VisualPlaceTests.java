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

package org.workcraft.testing.plugins.petri;

import java.awt.geom.Point2D;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;

public class VisualPlaceTests {
    @Test
    public void testHitTest() {
        Place p = new Place();
        VisualPlace vp = new VisualPlace(p);

        Assert.assertTrue(vp.hitTest(new Point2D.Double(0,0)));
        Assert.assertFalse(vp.hitTest(new Point2D.Double(5,5)));

        vp.setX(5);
        vp.setY(5);

        Assert.assertTrue(vp.hitTest(new Point2D.Double(5,5)));
        Assert.assertFalse(vp.hitTest(new Point2D.Double(0,0)));
    }
}
