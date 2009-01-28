package org.workcraft.dom;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.DuplicateIDException;
import org.workcraft.framework.exceptions.InvalidComponentException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.util.XmlUtil;

public abstract class MathModel implements Plugin, Model {
	protected int componentIDCounter = 1;
	protected int connectionIDCounter = 1;

	protected Hashtable<Integer, Component> components;
	protected Hashtable<Integer, Connection> connections;

	protected LinkedList<MathModelListener> listeners;

	protected Framework framework = null;
	protected String sourcePath = null;
	protected String title = null;

	public MathModel (Framework framework) {
		components = new Hashtable<Integer, Component>();
		connections = new Hashtable<Integer, Connection>();
		listeners = new LinkedList<MathModelListener>();
		this.framework = framework;
		title = "Untitled";
	}

	public MathModel (Framework framework, Element xmlModelElement, String sourcePath) throws ModelLoadFailedException{
		this(framework);

		this.sourcePath = sourcePath;

		title = XmlUtil.readStringAttr(xmlModelElement, "title");

		List<Element> componentNodes = XmlUtil.getChildElements("component", xmlModelElement);
		for (Element e : componentNodes) {

			String className = e.getAttribute("class");

			try {
				Class<?> elementClass = Class.forName(className);
				Constructor<?> ctor = elementClass.getConstructor(Element.class);
				Component component = (Component)ctor.newInstance(e);

				addComponent(component, false);

			} catch (ClassCastException ex) {
				throw new ModelLoadFailedException ("Cannot cast the class \"" + className +"\" to org.workcraft.dom.Component: " + ex.getMessage());
			}	catch (ClassNotFoundException ex) {
				throw new ModelLoadFailedException ("Cannot load component class: " + ex.getMessage());
			} catch (SecurityException ex) {
				throw new ModelLoadFailedException ("Security exception: " + ex.getMessage());
			} catch (NoSuchMethodException ex) {
				throw new ModelLoadFailedException ("Component class \"" + className + "\" does not declare the required constructor " + className + "(org.w3c.dom.Element element)");
			} catch (IllegalArgumentException ex) {
				throw new ModelLoadFailedException ("Component class instantiation failed: " + ex.getMessage());
			} catch (InstantiationException ex) {
				throw new ModelLoadFailedException ("Component class instantiation failed: " + ex.getMessage());
			} catch (IllegalAccessException ex) {
				throw new ModelLoadFailedException ("Component class instantiation failed: " + ex.getMessage());
			} catch (InvocationTargetException ex) {
				throw new ModelLoadFailedException ("Component class instantiation failed: " + ex.getTargetException().getMessage());
			} catch (InvalidComponentException ex) {
				throw new ModelLoadFailedException ("Failed to add component: " + ex.getMessage());
			} catch (DuplicateIDException ex) {
				throw new ModelLoadFailedException ("Failed to add component: duplicate ID " + ex.getMessage() + "encountered");
			}
		}

		List<Element> connectionNodes = XmlUtil.getChildElements("connection", xmlModelElement);

		for (Element e: connectionNodes) {
			String className = e.getAttribute("class");

			try {
				Class<?> elementClass = Class.forName(className);
				Constructor<?> ctor = elementClass.getConstructor(Element.class, MathModel.class);
				Connection connection = (Connection)ctor.newInstance(e, this);

				addConnection(connection, false);

			} catch (ClassCastException ex) {
				throw new ModelLoadFailedException ("Cannot cast the class \"" + className +"\" to org.workcraft.dom.Connection: " + ex.getMessage());
			}	catch (ClassNotFoundException ex) {
				throw new ModelLoadFailedException ("Cannot load connection class: " + ex.getMessage());
			} catch (SecurityException ex) {
				throw new ModelLoadFailedException ("Security exception: " + ex.getMessage());
			} catch (NoSuchMethodException ex) {
				throw new ModelLoadFailedException ("Connection class \"" + className + "\" does not declare the required constructor " + className + "(org.w3c.dom.Element, org.workcraft.dom.AbstractGraphModel)");
			} catch (IllegalArgumentException ex) {
				throw new ModelLoadFailedException ("Connection class instantiation failed: " + ex.getMessage());
			} catch (InstantiationException ex) {
				throw new ModelLoadFailedException ("Connection class instantiation failed: " + ex.getMessage());
			} catch (IllegalAccessException ex) {
				throw new ModelLoadFailedException ("Connection class instantiation failed: " + ex.getMessage());
			} catch (InvocationTargetException ex) {
				throw new ModelLoadFailedException ("Connection class instantiation failed: " + ex.getTargetException().getMessage());
			} catch (DuplicateIDException ex) {
				throw new ModelLoadFailedException ("Failed to add connection: duplicate ID " + ex.getMessage() + "encountered");
			} catch (InvalidConnectionException ex) {
				throw new ModelLoadFailedException ("Failed to add connection: " + ex.getMessage());
			}
		}
	}

	public static void componentsToXML (Element parentElement, Collection<? extends Component> components) {
		for (Component c: components) {
			Element componentElement = XmlUtil.createChildElement("component", parentElement);
			componentElement.setAttribute("class", c.getClass().getName());
			c.toXML(componentElement);
		}
	}

	public static void connectionsToXML (Element parentElement, Collection<? extends Connection> connections) {
		for (Connection c: connections) {
			Element connectionElement = XmlUtil.createChildElement("connection", parentElement);
			connectionElement.setAttribute("class", c.getClass().getName());
			c.toXML(connectionElement);
		}
	}

	public void toXML (Element modelElement) {
		XmlUtil.writeStringAttr(modelElement, "title", title);
		componentsToXML(modelElement, components.values());
		connectionsToXML(modelElement, connections.values());
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

	public final int addComponent (Component component) throws InvalidComponentException, DuplicateIDException {
		return addComponent(component, true);
	}

	public final int addComponent (Component component, boolean autoAssignID) throws InvalidComponentException, DuplicateIDException {
		//		if (getSupportedComponents() == null || !getSupportedComponents().contains(component.getClass()))
		//			if (autoAssignID)
		//				throw new InvalidComponentException ("unsupported component (class="+component.getClass().getName()+")");
		//			else
		//				throw new InvalidComponentException ("unsupported component (class="+component.getClass().getName()+", ID="+component.getID()+")");

		if (autoAssignID)
			component.setID(generateComponentID());
		else {
			if (components.get(component.getID()) != null)
				throw new DuplicateIDException (component.getID());
			if (component.getID() >= componentIDCounter)
				componentIDCounter = component.getID()+1;
		}

		components.put(component.getID(), component);
		componentAdded(component);

		return component.getID();
	}

	public final int addConnection(Connection connection) throws InvalidConnectionException, DuplicateIDException {
		return addConnection (connection, true);
	}

	public final int addConnection(Connection connection, boolean autoAssignID) throws InvalidConnectionException, DuplicateIDException {
		// first validate that this connection is allowed, e.g. disallow user
		// to connect Petri net place to another Petri net place
		validateConnection (connection);

		connection.first.addConnection(connection);
		connection.second.addConnection(connection);

		if (autoAssignID)
			connection.setID(generateConnectionID());
		else {
			if (connections.get(connection.getID()) != null)
				throw new DuplicateIDException (connection.getID());

			// ensure that the provided ID will not get generated automatically for another object
			if (connection.getID() >= connectionIDCounter)
				connectionIDCounter = connection.getID()+1;
		}

		connections.put(connection.getID(), connection);
		connectionAdded(connection);
		return connection.getID();
	}

	public final Connection createConnection (Component first, Component second) throws InvalidConnectionException {
		Connection con = new Connection(first, second);
		try {
			addConnection(con);
		} catch (DuplicateIDException e) {
			// Should never happen
			e.printStackTrace();
		}
		return con;
	}

	public Connection connect (Component first, Component second) throws InvalidConnectionException {
		return createConnection (first, second);
	}

	public final void removeConnection (Connection connection) {
		connection.getFirst().removeFromPostset(connection.getSecond());
		connection.getSecond().removeFromPreset(connection.getFirst());
		connection.getFirst().removeConnection(connection);
		connection.getSecond().removeConnection(connection);

		connections.remove(connection);
		connectionRemoved(connection);
	}

	public final void removeComponent (Component component) {
		HashSet<Connection> connectionsToRemove = new HashSet<Connection>(component.getConnections());

		for (Connection con : connectionsToRemove)
			removeConnection(con);

		components.remove(component);
		componentRemoved(component);
	}

	abstract public void validateConnection(Connection connection) throws InvalidConnectionException;
	abstract public void validate() throws ModelValidationException;

	protected void connectionAdded(Connection connection) {
	}

	protected void componentAdded(Component component) {
	}

	protected void connectionRemoved(Connection connection) {
	}

	protected void componentRemoved(Component component) {
	}

	public Connection getConnectionByID(int ID) {
		return connections.get(ID);
	}

	public MathModel getMathModel() {
		return this;
	}


	public VisualModel getVisualModel() {
		return null;
	}

	public ArrayList<Class<? extends Component>> getSupportedComponents() {
		return new ArrayList<Class<? extends Component>>();
	}

	public String getDisplayName() {
		DisplayName name = this.getClass().getAnnotation(DisplayName.class);
		if (name == null)
			return this.getClass().getSimpleName();
		else
			return name.value();
	}

	public void addListener(MathModelListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MathModelListener listener) {
		listeners.remove(listener);
	}

	public void fireModelStructureChanged() {
		for (MathModelListener l : listeners)
			l.modelStructureChanged();
	}

	public void fireComponentPropertyChanged(Component c) {
		for (MathModelListener l : listeners)
			l.componentPropertyChanged(c);
	}

	public void fireConnectionPropertyChanged(Connection c) {
		for (MathModelListener l : listeners)
			l.connectionPropertyChanged(c);
	}
}