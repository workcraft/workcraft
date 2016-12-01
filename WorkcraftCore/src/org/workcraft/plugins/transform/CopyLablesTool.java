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
import java.util.HashSet;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;

public class CopyLablesTool extends TransformationTool {

    @Override
    public String getDisplayName() {
        return "Copy unique names into labels (selected or all)";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, VisualModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public Collection<Node> collect(Model model) {
        Collection<Node> result = new HashSet<>();
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            result.addAll(Hierarchy.getDescendantsOfType(model.getRoot(), VisualComponent.class));
            Collection<Node> selection = visualModel.getSelection();
            if (!selection.isEmpty()) {
                result.retainAll(selection);
            }
        }
        return result;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualModel) && (node instanceof VisualComponent)) {
            VisualModel visualModel = (VisualModel) model;
            VisualComponent visualComponent = (VisualComponent) node;
            MathModel mathModel = visualModel.getMathModel();
            Node refComponent = visualComponent.getReferencedComponent();
            visualComponent.setLabel(mathModel.getName(refComponent));
        }
    }

}
