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

package org.workcraft.dom.math;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.util.Hierarchy;

public abstract class AbstractMathModel extends AbstractModel implements MathModel {

	public AbstractMathModel() {
		this (null, null);
	}

	public AbstractMathModel (Container root) {
		this (root, null);
	}

	public AbstractMathModel(Container root, ReferenceManager referenceManager) {
		super((root == null) ? new MathGroup() : root, referenceManager);

		new DefaultHangingConnectionRemover(this, "Math").attach(getRoot());
	}



	private void setNamespaceRecursively(HierarchicalUniqueNameReferenceManager manager, Container targetContainer, Model sourceModel, Container sourceRoot) {
		// need to assign the whole tree to the new providers
		Collection<Node> nodes = Hierarchy.getChildrenOfType(sourceRoot, Node.class);
		for (Node node: nodes) {

			NamespaceProvider provider = manager.getNamespaceProvider(targetContainer);
			if (targetContainer instanceof NamespaceProvider) provider = (NamespaceProvider) targetContainer;

			manager.setNamespaceProvider(node, (HierarchicalUniqueNameReferenceManager)sourceModel.getReferenceManager(), provider);

		}
		sourceRoot.reparent(nodes, targetContainer);

		for (Node node: nodes) {

			if (!(node instanceof Container)) continue;

			setNamespaceRecursively(manager, (Container)node, sourceModel, (Container)node);
		}

	}



	@Override
	public void reparent(Container targetContainer, Model sourceModel, Container sourceRoot) {
		if (sourceModel==null) sourceModel = this;

		HierarchicalUniqueNameReferenceManager manager = null;

		if (getReferenceManager() instanceof HierarchicalUniqueNameReferenceManager)
			manager = (HierarchicalUniqueNameReferenceManager)getReferenceManager();

		if (manager!=null) {

			NamespaceProvider provider = null;
			if (targetContainer instanceof NamespaceProvider)
				provider = (NamespaceProvider)targetContainer;
			else
				provider = manager.getNamespaceProvider(targetContainer);

			setNamespaceRecursively(manager, provider, sourceModel, sourceRoot);

		}


	}

}