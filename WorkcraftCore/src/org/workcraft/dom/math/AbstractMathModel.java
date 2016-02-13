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

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.NodeFactory;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.util.Hierarchy;

public abstract class AbstractMathModel extends AbstractModel implements MathModel {

    public AbstractMathModel() {
        this(null, null);
    }

    public AbstractMathModel(Container root) {
        this(root, null);
    }

    public AbstractMathModel(Container root, ReferenceManager man) {
        super((root == null) ? new MathGroup() : root, man);
        new DefaultHangingConnectionRemover(this).attach(getRoot());
    }

    @SuppressWarnings("unchecked")
    public <T extends MathNode> T createNode(String name, Container container, Class<T> type) {
        if (container == null) {
            container = getRoot();
        }
        MathNode node = null;
        try {
            node = NodeFactory.createNode(type);
            container.add(node);
            if (name != null) {
                setName(node, name);
            }
        } catch (NodeCreationException e) {
            throw new RuntimeException("Cannot create math node '" + name + "' of class '" + type + "'");
        }
        return (T) node;
    }

    private void setNamespaceRecursively(HierarchicalUniqueNameReferenceManager dstRefManager, Container dstContainer,
            Model srcModel, Container srcRoot, Collection<Node> srcChildren) {

        // Collect the nodes to reparent - need to assign the whole tree to new providers
        Collection<Node> nodes = null;
        if (srcChildren != null) {
            nodes = new HashSet<Node>(srcChildren);
        } else {
            nodes = Hierarchy.getChildrenOfType(srcRoot, Node.class);
        }

        NamespaceProvider dstProvider = dstRefManager.getNamespaceProvider(dstContainer);
        if (dstContainer instanceof NamespaceProvider) {
            dstProvider = (NamespaceProvider) dstContainer;
        }

        HierarchicalUniqueNameReferenceManager srcRefManager = (HierarchicalUniqueNameReferenceManager) srcModel.getReferenceManager();
        dstRefManager.setNamespaceProvider(nodes, srcRefManager, dstProvider);

        srcRoot.reparent(nodes, dstContainer);

        // Propagate the name data into the nodes. This may be necessary after setDefaultNameIfUnnamed was called.
        for (Node node: nodes) {
            String name = dstRefManager.getName(node);
            dstRefManager.setName(node, name);
        }

        for (Node node: nodes) {
            if (node instanceof Container) {
                Container container = (Container) node;
                setNamespaceRecursively(dstRefManager, container, srcModel, container, null);
            }
        }
    }

    @Override
    public void reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<Node> srcChildren) {
        if (srcModel == null) {
            srcModel = this;
        }
        HierarchicalUniqueNameReferenceManager manager = null;
        if (getReferenceManager() instanceof HierarchicalUniqueNameReferenceManager) {
            manager = (HierarchicalUniqueNameReferenceManager) getReferenceManager();
        }
        if (manager != null) {
            NamespaceProvider provider = null;
            if (dstContainer instanceof NamespaceProvider) {
                provider = (NamespaceProvider) dstContainer;
            } else {
                provider = manager.getNamespaceProvider(dstContainer);
            }
            setNamespaceRecursively(manager, provider, srcModel, srcRoot, srcChildren);
        }
    }

}
