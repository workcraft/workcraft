package org.workcraft.dom;

public interface MathModelListener {
	public void modelStructureChanged();
	public void componentPropertyChanged(Component c);
	public void connectionPropertyChanged(Connection c);
}
