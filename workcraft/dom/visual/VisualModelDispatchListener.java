/**
 *
 */
package org.workcraft.dom.visual;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.EventListener0;
import org.workcraft.framework.EventListener1;
import org.workcraft.framework.EventListener2;

class VisualModelDispatchListener implements VisualModelEventListener, VisualModelEventDispatcher {
	private LinkedList<EventListener1<VisualComponent>> componentAddedListeners
						= new LinkedList<EventListener1<VisualComponent>>();

	private LinkedList<EventListener1<VisualComponent>> componentRemovedListeners
						= new LinkedList<EventListener1<VisualComponent>>();

	private LinkedList<EventListener2<VisualComponent, String>> componentPropertyChangedListeners
						= new LinkedList<EventListener2<VisualComponent, String>>();

	private LinkedList<EventListener1<VisualConnection>> connectionAddedListeners
						= new LinkedList<EventListener1<VisualConnection>>();

	private LinkedList<EventListener1<VisualConnection>> connectionRemovedListeners
						= new LinkedList<EventListener1<VisualConnection>>();

	private LinkedList<EventListener2<VisualConnection, String>> connectionPropertyChangedListeners
						= new LinkedList<EventListener2<VisualConnection, String>>();

	private LinkedList<EventListener0> layoutChangedListeners
						= new LinkedList<EventListener0>();

	private LinkedList<EventListener1<Set<VisualNode>>> selectionChangedListeners
						= new LinkedList<EventListener1<Set<VisualNode>>>();

	public void onComponentAdded(VisualComponent component) {
		for (EventListener1<VisualComponent> l : componentAddedListeners)
			l.eventFired(component);
	}

	public void onComponentPropertyChanged(String propertyName,
			VisualComponent component) {
		for (EventListener2<VisualComponent, String> l : componentPropertyChangedListeners)
			l.eventFired(component, propertyName);
	}

	public void onComponentRemoved(VisualComponent component) {
		for (EventListener1<VisualComponent> l : componentRemovedListeners)
			l.eventFired(component);
	}

	public void onConnectionAdded(VisualConnection connection) {
		for (EventListener1<VisualConnection> l : connectionAddedListeners)
			l.eventFired(connection);
	}

	public void onConnectionPropertyChanged(String propertyName,
			VisualConnection connection) {
		for (EventListener2<VisualConnection, String> l : connectionPropertyChangedListeners)
			l.eventFired(connection, propertyName);
	}

	public void onConnectionRemoved(VisualConnection connection) {
		for (EventListener1<VisualConnection> l : connectionRemovedListeners)
			l.eventFired(connection);
	}

	public void onLayoutChanged() {
		for (EventListener0 l : layoutChangedListeners)
			l.eventFired();
	}

	public void onSelectionChanged(Set<VisualNode> selection) {
		for (EventListener1<Set<VisualNode>> l : selectionChangedListeners)
			l.eventFired(selection);
	}

	public void addComponentAddedListener(
			EventListener1<VisualComponent> listener) {
		componentAddedListeners.add(listener);
	}

	public void addComponentPropertyChangedListener(
			EventListener2<VisualComponent, String> listener) {
		componentPropertyChangedListeners.add(listener);
	}

	public void addComponentRemovedListener(
			EventListener1<VisualComponent> listener) {
		componentRemovedListeners.add(listener);
	}

	public void addConnectionAddedListener(
			EventListener1<VisualConnection> listener) {
		connectionAddedListeners.add(listener);

	}

	public void addConnectionPropertyChangedListener(
			EventListener2<VisualConnection, String> listener) {
		connectionPropertyChangedListeners.add(listener);
	}

	public void addConnectionRemovedListener(
			EventListener1<VisualConnection> listener) {
		connectionRemovedListeners.add(listener);
	}

	public void addLayoutChangedListener(EventListener0 listener) {
		layoutChangedListeners.add(listener);
	}

	public void addSelectionChangedListener(
			EventListener1<Set<VisualNode>> listener) {
		selectionChangedListeners.add(listener);
	}

	public void removeComponentAddedListener(
			EventListener1<VisualComponent> listener) {
		componentRemovedListeners.remove(listener);
	}

	public void removeComponentPropertyChangedListener(
			EventListener2<VisualComponent, String> listener) {
		componentPropertyChangedListeners.remove(listener);
	}

	public void removeComponentRemovedListener(
			EventListener1<VisualComponent> listener) {
		componentRemovedListeners.remove(listener);
	}

	public void removeConnectionAddedListener(
			EventListener1<VisualConnection> listener) {
		connectionAddedListeners.remove(listener);
	}

	public void removeConnectionPropertyChangedListener(
			EventListener2<VisualConnection, String> listener) {
		connectionPropertyChangedListeners.remove(listener);
	}

	public void removeConnectionRemovedListener(
			EventListener1<VisualConnection> listener) {
		connectionRemovedListeners.remove(listener);
	}

	public void removeLayoutChangedListener(EventListener0 listener) {
		layoutChangedListeners.remove(listener);
	}

	public void removeSelectionChangedListener(
			EventListener1<Set<VisualNode>> listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public void onSelectionChanged(Collection<HierarchyNode> selection) {
		// TODO Auto-generated method stub

	}
}