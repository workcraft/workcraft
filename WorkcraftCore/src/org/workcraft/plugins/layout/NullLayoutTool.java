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

package org.workcraft.plugins.layout;

import java.awt.geom.Point2D;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;

public class NullLayoutTool extends AbstractLayoutTool {

    @Override
    public String getDisplayName() {
        return "Reset";
    }

    @Override
    public void layout(VisualModel model) {
        setChildrenNullPosition(model.getRoot());
    }

    private void setChildrenNullPosition(Container container) {
        Point2D.Double pos = new Point2D.Double(0.0 ,0.0);
        for (Node node : container.getChildren()) {
            if (node instanceof VisualTransformableNode) {
                VisualTransformableNode transformableNode = (VisualTransformableNode)node;
                transformableNode.setRootSpacePosition(pos);
            }
            if (node instanceof Container) {
                setChildrenNullPosition((Container)node);
            }
        }
    }

}