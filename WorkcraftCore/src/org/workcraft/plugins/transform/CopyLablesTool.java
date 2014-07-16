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

import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class CopyLablesTool implements Tool {

	@Override
	public String getSection() {
		return "Names and labels";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, VisualModel.class);
	}

	@Override
	public void run(WorkspaceEntry we) {
		VisualModel visualModel = WorkspaceUtils.getAs(we, VisualModel.class);
		if (visualModel != null) {
			MathModel mathModel = (MathModel)visualModel.getMathModel();
			for (VisualComponent visualComponent : Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualComponent.class)) {
				Node refComponent = visualComponent.getReferencedComponent();
				visualComponent.setLabel(mathModel.getName(refComponent));
			}
		}
	}

	@Override
	public String getDisplayName() {
		return "Set component labels from their unique names";
	}
}