package org.workcraft.dom.visual;

import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnectionInfo;

public interface VisualModelEventListener {
	public void onComponentPropertyChanged(String propertyName, VisualComponent component);
	public void onConnectionPropertyChanged(String propertyName, VisualConnectionInfo connection);
	public void onComponentAdded(VisualComponent component);
	public void onComponentRemoved(VisualComponent component);
	public void onConnectionAdded (VisualConnectionInfo connection);
	public void onConnectionRemoved (VisualConnectionInfo connection);
	public void onSelectionChanged(Collection<Node> selection);
	public void onLayoutChanged();
}