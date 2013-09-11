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

import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;

public class MathGroup extends MathNode implements ObservableHierarchy, Container {
	private DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

	@Override
	public void add(Node node) {
		groupImpl.add(node);
	}

	@Override
	public void addObserver(HierarchyObserver obs) {
		groupImpl.addObserver(obs);
	}

	@Override
	public Collection<Node> getChildren() {
		return groupImpl.getChildren();
	}

	@Override
	public Node getParent() {
		return groupImpl.getParent();
	}

	@Override
	public void remove(Node node) {
		groupImpl.remove(node);
	}

	@Override
	public void removeObserver(HierarchyObserver obs) {
		groupImpl.removeObserver(obs);
	}

	@Override
	public void setParent(Node parent) {
		groupImpl.setParent(parent);
	}

	@Override
	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}

	@Override
	public void remove(Collection<Node> nodes) {
		groupImpl.remove(nodes);
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}

	@Override
	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}

}