package org.workcraft.dom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
	private int componentIDCounter = 0;
	private int connectionIDCounter = 0;;

	private Hashtable<Integer, Integer> componentRenames = new Hashtable<Integer, Integer>();
	private Hashtable<Integer, Component> components = new Hashtable<Integer, Component>();
	private Hashtable<Integer, Integer> connectionRenames = new Hashtable<Integer, Integer>();
	private Hashtable<Integer, Connection> connections = new Hashtable<Integer, Connection>();

	private LinkedList<MathModelListener> listeners = new LinkedList<MathModelListener>();;

	private HashSet<Class<? extends Component>> supportedComponents = new HashSet<Class<? extends Component>>();

	private XMLSerialisation serialisation = new XMLSerialisation();

	private String title = "";

	final public static void componentsToXML(Element parentElement,
			Collection<? extends Component> components) {
		for (Component c : components) {
			Element componentElement = XmlUtil.createChildElement("component",
					parentElement);
			componentElement.setAttribute("class", c.getClass().getName());
			c.serialiseToXML(componentElement);
		}
	}

	final public static void connectionsToXML(Element parentElement,
			Collection<? extends Connection> connections) {
		for (Connection c : connections) {
			Element connectionElement = XmlUtil.createChildElement(
					"connection", parentElement);
			connectionElement.setAttribute("class", c.getClass().getName());
			c.serialiseToXML(connectionElement);
		}
	}

	private void addSerialisationObjects() {
		serialisation.addSerialiser(new XMLSerialiser() {
			public String getTagName() {
				return MathModel.class.getSimpleName();
			}
			public void serialise(Element element) {
				XmlUtil.writeStringAttr(element, "title", title);
				componentsToXML(element, components.values());
				connectionsToXML(element, connections.values());
			}
		});

		serialisation.addDeserialiser(new XMLDeserialiser() {
			public String getTagName() {
				return MathModel.class.getSimpleName();
			}
			public void deserialise(Element element) throws ModelLoadFailedException {
				title = XmlUtil.readStringAttr(element, "title");
				pasteFromXML(element);
			}
		});
	}

	public MathModel() {
		addSerialisationObjects();
	}

	abstract protected void onComponentAdded(Component component);

	abstract protected void onComponentRemoved(Component component);

	abstract protected void onConnectionAdded(Connection connection);

	abstract protected void onConnectionRemoved(Connection connection);

	final public void handleDeferredEvents() {
		for (Component c : getComponents())
			onComponentAdded(c);
		for (Connection c: getConnections())
			onConnectionAdded(c);
	}

	final public int addComponent(Component component) {
		component.setID(getNextComponentID());
		components.put(component.getID(), component);

		onComponentAdded(component);

		return component.getID();
	}

	final public int addConnection(Connection connection)
			throws InvalidConnectionException {
		// first validate that this connection is allowed, e.g. disallow user
		// to connect Petri net place to another Petri net place
		validateConnection(connection);

		connection.getFirst().addConnection(connection);
		connection.getFirst().addToPostset(connection.getSecond());
		connection.getSecond().addConnection(connection);
		connection.getSecond().addToPreset(connection.getFirst());

		connection.setID(getNextConnectionID());
		connections.put(connection.getID(), connection);

		onConnectionAdded(connection);

		return connection.getID();
	}

	public void addListener(MathModelListener listener) {
		listeners.add(listener);
	}

	final public void addComponentSupport(Class<? extends Component> componentClass) {
		supportedComponents.add(componentClass);
	}

	final public void removeComponentSupport(Class<? extends Component> componentClass) {
		supportedComponents.remove(componentClass);
	}

	final public boolean isComponentSupported(Component component) {
		return supportedComponents.contains(component.getClass());
	}

	public final Connection connect(Component first, Component second)
			throws InvalidConnectionException {
		return createConnection(first, second);
	}

	public Connection createConnection(Component first, Component second)
			throws InvalidConnectionException {
		Connection con = new Connection(first, second);
		addConnection(con);
		return con;
	}

	public void fireComponentPropertyChanged(Component c) {
		for (MathModelListener l : listeners)
			l.onComponentPropertyChanged(c);
	}

	public void fireConnectionPropertyChanged(Connection c) {
		for (MathModelListener l : listeners)
			l.onConnectionPropertyChanged(c);
	}

	public void fireModelStructureChanged() {
		for (MathModelListener l : listeners)
			l.onModelStructureChanged();
	}

	final public Component getComponentByID(int ID) {
		return components.get(ID);
	}

	final public Component getComponentByRenamedID(int oldID) {
		Integer newID = componentRenames.get(oldID);
		if (newID == null)
			return null;
		return getComponentByID(newID);
	}

	final public Collection<Component> getComponents() {
		return components.values();
	}

	final public Connection getConnectionByID(int ID) {
		return connections.get(ID);
	}

	final public Connection getConnectionByRenamedID(int oldID) {
		Integer newID = connectionRenames.get(oldID);
		if (newID == null)
			return null;
		return getConnectionByID(newID);
	}

	final public Collection<Connection> getConnections() {
		return connections.values();
	}

	public String getDisplayName() {
		DisplayName name = this.getClass().getAnnotation(DisplayName.class);
		if (name == null)
			return this.getClass().getSimpleName();
		else
			return name.value();
	}

	final public MathModel getMathModel() {
		return this;
	}

	final public int getNextComponentID() {
		return componentIDCounter++;
	}

	final public int getNextConnectionID() {
		return connectionIDCounter++;
	}

	final public Set<Class<? extends Component>> getSupportedComponents() {
		return new HashSet<Class<? extends Component>>(supportedComponents);
	}

	final public String getTitle() {
		return title;
	}

	final public VisualModel getVisualModel() {
		return null;
	}

	final public void pasteFromXML(Element modelElement)
			throws ModelLoadFailedException {
		componentRenames.clear();
		connectionRenames.clear();

		try {
			List<Element> componentNodes = XmlUtil.getChildElements(
					"component", modelElement);

			for (Element e : componentNodes) {
				Component component = ComponentFactory.createComponent(e);

				if (!isComponentSupported(component))
					throw new InvalidComponentException("Unsupported component: " + component.getClass().getName());

				Integer oldID = component.getID();
				Integer newID = addComponent(component);

				componentRenames.put(oldID, newID);
			}

			List<Element> connectionNodes = XmlUtil.getChildElements(
					"connection", modelElement);

			for (Element e : connectionNodes) {
				Connection connection = ConnectionFactory.createConnection(e,
						this);

				Integer oldID = connection.getID();
				Integer newID = addConnection(connection);

				connectionRenames.put(oldID, newID);
			}

		} catch (InvalidComponentException e1) {
			throw new ModelLoadFailedException(
					"(in MathModel.appendFromXml) Invalid component: "
							+ e1.getMessage());
		} catch (ComponentCreationException e1) {
			throw new ModelLoadFailedException(
					"(in MathModel.appendFromXml) Cannot create component: "
							+ e1.getMessage());
		} catch (InvalidConnectionException e) {
			throw new ModelLoadFailedException(
					"(in MathModel.appendFromXml) Invalid connection: "
							+ e.getMessage());
		} catch (ConnectionCreationException e) {
			throw new ModelLoadFailedException(
					"(in MathModel.appendFromXml) Cannot create connection: "
							+ e.getMessage());
		}
	}

	public final void removeComponent(Component component) {
		HashSet<Connection> connectionsToRemove = new HashSet<Connection>(
				component.getConnections());

		for (Connection con : connectionsToRemove)
			removeConnection(con);

		components.remove(component.getID());
		onComponentRemoved(component);
	}

	final public void removeConnection(Connection connection) {
		connection.getFirst().removeFromPostset(connection.getSecond());
		connection.getSecond().removeFromPreset(connection.getFirst());
		connection.getFirst().removeConnection(connection);
		connection.getSecond().removeConnection(connection);

		connections.remove(connection.getID());
		onConnectionRemoved(connection);
	}

	final public void removeListener(MathModelListener listener) {
		listeners.remove(listener);
	}

	final public void renameComponent(Component component, int newID) {
		components.remove(component.getID());
		components.put(newID, component);
	}

	final public void setTitle(String title) {
		this.title = title;
	}

	abstract public void validate() throws ModelValidationException;

	abstract public void validateConnection(Connection connection)
			throws InvalidConnectionException;

	public final void addXMLSerialiser(XMLSerialiser serialiser) {
		serialisation.addSerialiser(serialiser);
	}

	public final void addXMLDeserialiser(XMLDeserialiser deserialiser) {
		serialisation.addDeserialiser(deserialiser);
	}

	public final void serialiseToXML(Element componentElement) {
		serialisation.serialise(componentElement);
	}

	public final void deserialiseFromXML(Element modelElement) throws ModelLoadFailedException {
		serialisation.deserialise(modelElement);
	}

}