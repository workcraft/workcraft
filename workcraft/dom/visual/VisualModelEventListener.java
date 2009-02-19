package org.workcraft.dom.visual;

public interface VisualModelEventListener {
	public void onComponentPropertyChanged(String propertyName, VisualComponent component);
	public void onConnectionPropertyChanged(String propertyName, VisualConnection connection);
	public void onComponentAdded(VisualComponent component);
	public void onComponentRemoved(VisualComponent component);
	public void onConnectionAdded (VisualConnection connection);
	public void onConnectionRemoved (VisualConnection connection);
	public void onSelectionChanged();
	public void onLayoutChanged();
}