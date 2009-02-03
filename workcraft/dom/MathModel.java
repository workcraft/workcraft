package org.workcraft.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.ComponentFactory;
import org.workcraft.framework.ConnectionFactory;
import org.workcraft.framework.exceptions.ComponentCreationException;
import org.workcraft.framework.exceptions.ConnectionCreationException;
import org.workcraft.framework.exceptions.InvalidComponentException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.util.XmlUtil;

public abstract class MathModel implements Plugin, Model {
	protected int componentIDCounter = 0;
	protected int connectionIDCounter = 0;

	protected Hashtable<Integer, Component> components = new Hashtable<Integer, Component>();
	protected Hashtable<Integer, Connection> connections = new Hashtable<Integer, Connection>();;

	protected Hashtable<Integer, Integer> componentRenames = new Hashtable<Integer, Integer>();
	protected Hashtable<Integer, Integer> connectionRenames =  new Hashtable<Integer, Integer>();

	protected LinkedList<MathModelListener> listeners = new LinkedList<MathModelListener>();;

	protected String title = "";

	public MathModel () {
	}

	public MathModel (Element xmlModelElement) throws ModelLoadFailedException{
		title = XmlUtil.readStringAttr(xmlModelElement, "title");
		pasteFromXML (xmlModelElement);
	}

	public void pasteFromXML (Element modelElement) throws ModelLoadFailedException {
		componentRenames.clear();
		connectionRenames.clear();

		try {
			List<Element> componentNodes = XmlUtil.getChildElements("component", modelElement);

			for (Element e : componentNodes) {
				Component component = ComponentFactory.createComponent(e);

				Integer oldID = component.getID();
				Integer newID = addComponent(component);

				componentRenames.put(oldID, newID);
			}

			List<Element> connectionNodes = XmlUtil.getChildElements("connection", modelElement);

			for (Element e: connectionNodes) {
				Connection connection = ConnectionFactory.createConnection(e, this);

				Integer oldID = connection.getID();
				Integer newID = addConnection(connection);

				connectionRenames.put(oldID, newID);
			}

		} catch (InvalidComponentException e1) {
			throw new ModelLoadFailedException ("(in MathModel.appendFromXml) Invalid component: " + e1.getMessage());
		}  catch (ComponentCreationException e1) {
			throw new ModelLoadFailedException ("(in MathModel.appendFromXml) Cannot create component: " + e1.getMessage());
		} catch (InvalidConnectionException e) {
			throw new ModelLoadFailedException ("(in MathModel.appendFromXml) Invalid connection: " + e.getMessage());
		} catch (ConnectionCreationException e) {
			throw new ModelLoadFailedException ("(in MathModel.appendFromXml) Cannot create connection: " + e.getMessage());
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

	public Component getComponentByRenamedID(int oldID) {
		Integer newID = componentRenames.get(oldID);
		if (newID == null)
			return null;
		return getComponentByID(newID);
	}

	public Connection getConnectionByRenamedID(int oldID) {
		Integer newID = connectionRenames.get(oldID);
		if (newID == null)
			return null;
		return getConnectionByID(newID);
	}

	public int getNextComponentID() {
		return componentIDCounter++;
	}

	public int getNextConnectionID() {
		return connectionIDCounter++;
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

	public final int addComponent (Component component) {
		if (!getSupportedComponents().contains(component.getClass()))
			throw new InvalidComponentException("Unsupported component " + component.getClass().getName() + " added to a " + this.getDisplayName());

		component.setID(getNextComponentID());

		components.put(component.getID(), component);
		componentAdded(component);

		return component.getID();
	}

	public final int addConnection(Connection connection) throws InvalidConnectionException {
		// first validate that this connection is allowed, e.g. disallow user
		// to connect Petri net place to another Petri net place
		validateConnection (connection);

		connection.first.addConnection(connection);
		connection.second.addConnection(connection);

		connection.setID(getNextConnectionID());
		connections.put(connection.getID(), connection);
		connectionAdded(connection);

		return connection.getID();
	}

	public final Connection createConnection (Component first, Component second) throws InvalidConnectionException {
		Connection con = new Connection(first, second);
		addConnection(con);
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