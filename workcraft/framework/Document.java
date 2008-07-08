package org.workcraft.framework;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DocumentOpenFailedException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;


public abstract class Document {
	protected int idCounter = 0;
	protected Hashtable<Integer, Component> idMap = new Hashtable<Integer, Component>();

	protected ComponentGroup root = new ComponentGroup();
	protected LinkedList<Connection> connections = new LinkedList<Connection>();

	protected Framework framework = null;
	protected String sourcePath = null;
	protected String title = null;

	public Document (Framework framework) throws DocumentOpenFailedException {
		this.framework = framework;
	}

	public Document (Framework framework, Element xmlRootElement) {

	}


	public List<Component> getTopLevelComponents() {
		LinkedList<Component> result = new LinkedList<Component>();
		for (Component c : root.getChildren()) {
			result.add(c);
		}
		return result;
	}

	public List<Connection> getConnections() {
		return (List<Connection>)connections.clone();
	}

	public Component getComponentById(int id) {
		return idMap.get(id);
	}

	public int getNextId() {
		return idCounter++;
	}

	public ComponentGroup getRootGroup() {
		return root;
	}


	public String getSourcePath() {
		return sourcePath;
	}

	public void renameComponent(Component component, int newId) {
		idMap.remove(component.getId());
		idMap.put(newId, component);
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Connection createConnection(Component first, Component second) throws InvalidConnectionException {
		if (first.getPostset().contains(second) && second.getPreset().contains(first))
			throw new InvalidConnectionException ("Connection already exists");
		Connection connection = new Connection (first, second);
		connections.add(connection);
		return connection;
	}

	public void removeConnection (Connection connection) {
		connection.getFirst().removeFromPostset(connection.getSecond());
		connection.getSecond().removeFromPreset(connection.getFirst());
		connections.remove(connection);
	}

	 abstract public void validate() throws ModelValidationException;
}