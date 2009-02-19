package org.workcraft.dom;


public interface ReferenceResolver {
	public Component getComponentByID(int ID);
	public Connection getConnectionByID(int ID);
}
