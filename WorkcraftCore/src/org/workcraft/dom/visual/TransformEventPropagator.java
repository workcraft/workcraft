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

package org.workcraft.dom.visual;

import java.util.HashMap;
import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.NodesReparentedEvent;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.TransformEvent;
import org.workcraft.observation.TransformObserver;

public class TransformEventPropagator extends HierarchySupervisor implements StateObserver, TransformDispatcher {
	HashMap <Node, LinkedList<TransformObserver>> nodeToObservers
	= new HashMap<Node, LinkedList<TransformObserver>>();

	HashMap <TransformObserver, LinkedList<Node>> observerToNodes
	= new HashMap<TransformObserver, LinkedList<Node>>();

	private void addObserver (Node node, TransformObserver to) {
		LinkedList<TransformObserver> list = nodeToObservers.get(node);
		if ( list == null ) {
			list = new LinkedList<TransformObserver>();
			nodeToObservers.put(node, list);
		}
		list.add(to);
	}

	private void removeObserver (Node node, TransformObserver to) {
		LinkedList<TransformObserver> list = nodeToObservers.get(node);
		list.remove(to);
		if (list.isEmpty())
			nodeToObservers.remove(node);
	}

	private void addObservedNode (TransformObserver to, Node node) {
		LinkedList<Node> list = observerToNodes.get(to);
		if ( list == null ) {
			list = new LinkedList<Node>();
			observerToNodes.put(to, list);
		}
		list.add(node);
	}

	private void removeObservedNode (TransformObserver to, Node node) {
		LinkedList<Node> list = observerToNodes.get(to);
		list.remove(node);

		if (list.isEmpty())
			observerToNodes.remove(to);
	}


	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesAddedEvent || e instanceof NodesReparentedEvent) {
			for (Node n:e.getAffectedNodes())
				nodeAdded(n);
		} else if (e instanceof NodesDeletedEvent) {
			for (Node n:e.getAffectedNodes())
				nodeRemoved(n);
		}
	}

	private void nodeRemoved(Node node) {
		if (node instanceof ObservableState) {
			((ObservableState)node).removeObserver(this);

			// remove node from all observer lists
			LinkedList<TransformObserver> observers = nodeToObservers.get(node);
			if (observers != null)
				for (TransformObserver to : observers)
					removeObservedNode(to, node);

			nodeToObservers.remove(node);
		}

		if (node instanceof TransformObserver) {
			TransformObserver to = (TransformObserver)node;
			LinkedList<Node> nodes = observerToNodes.get(to);
			if (nodes != null)
				for (Node n : nodes)
					removeObserver(n, to);
		}

		for (Node n : node.getChildren())
			nodeRemoved(n);
	}

	private void nodeAdded(Node node) {
		if (node instanceof ObservableState)
			((ObservableState)node).addObserver(this);

		if (node instanceof TransformObserver) {
			TransformObserver to = (TransformObserver) node;
			to.subscribe(this);
		}

		for (Node n : node.getChildren())
			nodeAdded(n);
	}

	private void propagate(Node node, TransformEvent e) {
		LinkedList<TransformObserver> list = nodeToObservers.get(node);
		if (list != null)
			for (TransformObserver to : list)
				to.notify(e);

		for (Node n : node.getChildren())
			propagate(n,e);
	}

	@Override
	public void notify(StateEvent e) {
		if (e instanceof TransformEvent)
			propagate(((TransformEvent)e).getSender(), ((TransformEvent)e));
	}

	@Override
	public void subscribe(TransformObserver observer, Node observed) {
		addObserver(observed, observer);
		addObservedNode(observer, observed);
	}

	@Override
	public void unsubscribe(TransformObserver observer, Node observed) {
		removeObserver(observed, observer);
		removeObservedNode(observer, observed);
	}
}