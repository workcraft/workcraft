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

import org.workcraft.dom.Node;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;

/**
 * Base type for mathematical objects -- components (graph nodes)
 * and connections (graph arcs).
 * @author Ivan Poliakov
 *
 */
public abstract class MathNode implements Node, ObservableState {
	private ObservableStateImpl observableStateImpl = new ObservableStateImpl();

	private Node parent = null;

	public Collection<Node> getChildren() {
		return new HashSet<Node>();
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	final public void sendNotification(StateEvent e) {
		observableStateImpl.sendNotification(e);
	}

	@Override
	public void addObserver(StateObserver obs) {
		observableStateImpl.addObserver(obs);
	}

	@Override
	public void removeObserver(StateObserver obs) {
		observableStateImpl.removeObserver(obs);
	}

	public boolean requireReferenceConflictResolution() {
		return true;
	}

}
