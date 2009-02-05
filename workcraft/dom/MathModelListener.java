package org.workcraft.dom;

public interface MathModelListener {
	public void onModelStructureChanged();
	public void onComponentPropertyChanged(Component c);
	public void onConnectionPropertyChanged(Connection c);
}
