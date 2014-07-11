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

package org.workcraft.dom;

import java.util.Collection;

import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.gui.propertyeditor.Properties;


public interface Model extends NodeContext {
	public void setTitle(String title);
	public String getTitle();

	/**
	 * @return a user-friendly display name for this model, which is either
	 * read from <type>DisplayName</type> annotation, or, if the annotation
	 * is missing, taken from the name of the model class.
	 */
	public String getDisplayName();


	/// methods for work with referenced component names
	public ReferenceManager getReferenceManager();
	public Node getNodeByReference(NamespaceProvider provider, String reference);
	public String getNodeReference(NamespaceProvider provider, Node node);
	public Node getNodeByReference(String reference);
	public String getNodeReference(Node node);
	public String getName(Node node);
	public void setName(Node node, String name);

	//
	public void reparent(Container targetContainer, Model sourceModel, Container sourceRoot, Collection<Node> sourceChildren);

	public Container getRoot();

	public void add (Node node);
	public void remove (Node node);
	public void remove (Collection<Node> nodes);

	public Properties getProperties(Node node);
}