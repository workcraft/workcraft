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

package org.workcraft.plugins.transform;

import java.util.Collection;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class CopyLablesTool extends TransformationTool {

    @Override
    public String getDisplayName() {
        return "Copy unique names into labels (selected or all)";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, VisualModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        VisualModel visualModel = WorkspaceUtils.getAs(we, VisualModel.class);
        if (visualModel != null) {
            MathModel mathModel = visualModel.getMathModel();
            Collection<VisualComponent> components = Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualComponent.class);
            if (!visualModel.getSelection().isEmpty()) {
                components.retainAll(visualModel.getSelection());
            }
            if (!components.isEmpty()) {
                we.saveMemento();
                for (VisualComponent visualComponent : components) {
                    Node refComponent = visualComponent.getReferencedComponent();
                    visualComponent.setLabel(mathModel.getName(refComponent));
                }
            }
        }
    }

}
