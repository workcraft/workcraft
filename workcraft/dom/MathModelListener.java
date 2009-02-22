package org.workcraft.dom;

public interface MathModelListener {
	public void onModelStructureChanged();
	public void onNodePropertyChanged(String propertyName, MathNode n);
}
