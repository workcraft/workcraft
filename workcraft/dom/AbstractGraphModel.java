package org.workcraft.dom;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.DuplicateIDException;
import org.workcraft.framework.exceptions.InvalidComponentException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;

public abstract class AbstractGraphModel {
	protected int componentIDCounter = 1;
	protected int connectionIDCounter = 1;

	protected Hashtable<Integer, Component> components;
	protected Hashtable<Integer, Connection> connections;

	protected Framework framework = null;
	protected String sourcePath = null;
	protected String title = null;

	public AbstractGraphModel (Framework framework) {
		components = new Hashtable<Integer, Component>();
		connections = new Hashtable<Integer, Connection>();
		this.framework = framework;
		this.title = "Untitled";
	}

	public AbstractGraphModel (Framework framework, Element xmlRootElement, String sourcePath) {
		this.framework = framework;
		this.sourcePath = sourcePath;
	}

	public Collection<Component> getComponents() {
		return components.values();
	}

	public Collection<Connection> getConnections() {
		return connections.values();
	}

	public Component getComponentByID(int ID) {
		return components.get(ID);
	}

	protected int generateComponentID() {
		return componentIDCounter++;
	}

	protected int generateConnectionID() {
		return connectionIDCounter++;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void renameComponent(Component component, int newID) {
		components.remove(component.getID());
		components.put(newID, component);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int addComponent (Component component) throws InvalidComponentException, DuplicateIDException {
		return addComponent(component, true);
	}

	public int addComponent (Component component, boolean autoAssignID) throws InvalidComponentException, DuplicateIDException {
		if (!getSupportedComponents().contains(component.getClass()))
			throw new InvalidComponentException ("Unsupported component");

		if (autoAssignID)
			component.setID(generateComponentID());
		else {
			if (components.get(component.getID()) != null)
					throw new DuplicateIDException (component.getID());
			if (component.getID() >= componentIDCounter)
				componentIDCounter = component.getID()+1;
		}

		components.put(component.getID(), component);
		return component.getID();
	}

	public int addConnection(Connection connection) throws InvalidConnectionException, DuplicateIDException {
		return addConnection (connection, true);
	}

	public int addConnection(Connection connection, boolean autoAssignID) throws InvalidConnectionException, DuplicateIDException {
		// first validate that this connection is allowed, e.g. disallow user
		// to connect Petri net place to another Petri net place
		validateConnection (connection);

		if (autoAssignID)
			connection.setID(generateComponentID());
		else {
			if (connections.get(connection.getID()) != null)
				throw new DuplicateIDException (connection.getID());

			// ensure that the provided ID will not get generated automatically for another object
			if (connection.getID() >= connectionIDCounter)
				connectionIDCounter = connection.getID()+1;
		}

		connections.put(connection.getID(), connection);
		return connection.getID();
	}

	public void removeConnection (Connection connection) {
		connection.getFirst().removeFromPostset(connection.getSecond());
		connection.getSecond().removeFromPreset(connection.getFirst());
		connections.remove(connection);
	}

	abstract public Set<Class<?>> getSupportedComponents();
	abstract protected void validateConnection(Connection connection) throws InvalidConnectionException;
	abstract public void validate() throws ModelValidationException;
}