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

package org.workcraft.testing.dom.visual;

import java.awt.geom.Rectangle2D;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;

class Tools {
    static VisualGroup createGroup(Container parent)
    {
        VisualGroup node = new VisualGroup();
        if(parent!=null)
            parent.add(node);
        return node;
    }

    static VisualComponent createComponent(VisualGroup parent)
    {
        SquareNode node = new SquareNode(parent, new Rectangle2D.Double(0, 0, 1, 1));
        parent.add(node);
        return node;
    }

    static VisualConnection createConnection(VisualComponent c1, VisualComponent c2, VisualGroup parent)
    {
        VisualConnection connection = new VisualConnection(null, c1, c2);
        parent.add(connection);
        return connection;
    }

}
