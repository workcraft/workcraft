package org.workcraft.dom.visual;

import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;

public interface VisualModelEventListener {
	public void onComponentPropertyChanged(String propertyName, VisualComponent component);
	public void onConnectionPropertyChanged(String propertyName, VisualConnection connection);
	public void onComponentAdded(VisualComponent component);
	public void onComponentRemoved(VisualComponent component);
	public void onConnectionAdded (VisualConnection connection);
	public void onConnectionRemoved (VisualConnection connection);
	public void onSelectionChanged(Collection<Node> selection);
	public void onLayoutChanged();
}