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
import org.workcraft.framework.exceptions.LoadFromXMLException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.util.XmlUtil;

/**
 * A base class for all mathematical interpreted graph models. "Mathematical" in this
 * context means that this class only contains information about logical objects,
 * such as nodes and arcs, but does not carry any information regarding their
 * visual representation and layout.
 * @author Ivan Poliakov
 *
 */
public abstract class MathModel implements Plugin, Model {
	public class RenamedReferenceResolver implements ReferenceResolver {
		public Component getComponentByID(int ID) {
			return getComponentByRenamedID(ID);
		}
		public Connection getConnectionByID(int ID) {
			return getConnectionByRenamedID(ID);
		}
	}

	private int nodeIDCounter = 0;

	private Hashtable<Integer, Integer> connectionRenames = new Hashtable<Integer, Integer>();
	private Hashtable<Integer, Integer> componentRenames = new Hashtable<Integer, Integer>();
	private Hashtable<Integer, Component> components = new Hashtable<Integer, Component>();
	private Hashtable<Integer, Connection> connections = new Hashtable<Integer, Connection>();

	private LinkedList<MathModelListener> listeners = new LinkedList<MathModelListener>();

	private HashSet<Class<? extends Component>> supportedComponents = new HashSet<Class<? extends Component>>();

	private XMLSerialisation serialisation = new XMLSerialisation();
	private RenamedReferenceResolver referenceResolver = new RenamedReferenceResolver();

	private String title = "";

	private void addSerialisationObjects() {
		serialisation.addSerialiser(new XMLSerialiser() {
			public String getTagName() {
				return MathModel.class.getSimpleName();
			}
			public void serialise(Element element) {
				XmlUtil.writeStringAttr(element, "title", title);
				nodesToXML(element, components.values());
				nodesToXML(element, connections.values());
			}
		});

		serialisation.addDeserialiser(new XMLDeserialiser() {
			public String getTagName() {
				return MathModel.class.getSimpleName();
			}
			public void deserialise(Element element) throws LoadFromXMLException {
				title = XmlUtil.readStringAttr(element, "title");
				pasteFromXML(element);
			}
		});
	}


	/**
	 * Serialises a collection of nodes into an XML fragment.
	 * @param parentElement -- the parent XML element to add child elements
	 * representing nodes to.
	 * @param nodes -- the collection of nodes to serialise.
	 */
	final public static void nodesToXML(Element parentElement,
			Collection<? extends MathNode> nodes) {
		Element element;

		for (MathNode n : nodes)
			if (n instanceof Component) {
				element = XmlUtil.createChildElement("component",
						parentElement);
				element.setAttribute("class", n.getClass().getName());
				n.serialiseToXML(element);
			}

		for (MathNode n : nodes)
			if (n instanceof Connection) {
				element = XmlUtil.createChildElement("connection",
						parentElement);
				element.setAttribute("class", n.getClass().getName());
				n.serialiseToXML(element);
			}
	}


	/**
	 * Creates an empty model.
	 */
	public MathModel() {
		addSerialisationObjects();
	}



	/**
	 * Adds a component to the model. The component will be assigned
	 * an unique ID, and <code>onComponentAdded</code> method
	 * @param component
	 * @return
	 */
	final public int addComponent(Component component) {
		component.setID(getNextNodeID());
		components.put(component.getID(), component);

		fireComponentAdded(component);

		return component.getID();
	}

	private void fireComponentAdded(Component component) {
		for (MathModelListener l : listeners)
			l.onComponentAdded(component);
	}


	/**
	 * Adds a connection to the model.
	 * @param connection -- the connection object.
	 * @return the unique ID of the connection node in this <type>MathModel</type>
	 * @throws InvalidConnectionException
	 */
	final public int addConnection(Connection connection)
	throws InvalidConnectionException {
		// first validate that this connection is allowed, e.g. disallow user
		// to connect Petri net place to another Petri net place
		validateConnection(connection);

		connection.getFirst().addConnection(connection);
		connection.getFirst().addToPostset(connection.getSecond());
		connection.getSecond().addConnection(connection);
		connection.getSecond().addToPreset(connection.getFirst());

		connection.setID(getNextNodeID());
		connections.put(connection.getID(), connection);

		fireConnectionAdded(connection);

		return connection.getID();
	}

	private void fireConnectionAdded(Connection connection) {
		for (MathModelListener l : listeners)
			l.onConnectionAdded(connection);
	}


	/**
	 * Adds a <type>MathModelListener</type> to monitor
	 * changes in this model.
	 * @param listener -- the <type>MathModelListener</type> to add to the listener list.
	 */
	public void addListener(MathModelListener listener) {
		listeners.add(listener);
	}

	/**
	 * <p>Called by types that inherit from <type>MathModel</type> to declare
	 * a support for a component class. The class will be included to subsequent
	 * calls to <code>getSupportedComponents()</code></p>
	 * <p><b>Note: </b>must be called from a constructor. </p>
	 * @param componentClass
	 */
	final protected void addComponentSupport(Class<? extends Component> componentClass) {
		supportedComponents.add(componentClass);
	}

	/**
	 * <p>Called by types that inherit from <type>MathModel</type> to remove
	 * a support for a component class. The class will not be listed in subsequent
	 * calls to <code>getSupportedComponents()</code>. This may be required
	 * when a model inherits from another model, but does not provide full support
	 * for all of the components supported by the inherited model.</p>
	 * <p>E.g.: STG inherits from Petri Net, but replaces Petri Net tranistions
	 * with its own Signal transitions that inherit from Petri Net transitions. In this
	 * case, support for plain Petri Net transition is not required in STG.
	 * <p><b>Note: </b>must be called from a constructor. </p>
	 * @param componentClass
	 */
	final protected void removeComponentSupport(Class<? extends Component> componentClass) {
		supportedComponents.remove(componentClass);
	}

	/**
	 * Checks if the given component class is present in this model's supported
	 * components list.
	 * @param component -- a component to test
	 * @return true if the component's class is supported by this model, false
	 * otherwise
	 */
	final public boolean isComponentSupported(Component component) {
		return supportedComponents.contains(component.getClass());
	}

	/**
	 * <p>Creates a connection between the two components and adds it to the model.</p>
	 * <p>This is the preferred method to use by client code, instead of manually adding
	 * connections to the model using <code>addConnection</code></p>
	 * @param first -- the component that the connection starts from.
	 * @param second -- the component that the connection goes to.
	 * @return the newly created <type>Connection</type> object that has been
	 * added to the model.
	 * @throws InvalidConnectionException thrown when the connection between
	 * the specified components is not allowed by this model, or if the specified
	 * components are not contained in this model.
	 */
	public final Connection connect(Component first, Component second)
	throws InvalidConnectionException {
		if ( !components.contains(first) || !components.contains(second))
			throw new InvalidConnectionException("The specified components are not contained in this model.");
		return createConnection(first, second);
	}

	/**
	 * <p>An overridable method that creates a connection between two components.</p>
	 * <p>This method should be overriden by types inherited from <type>MathModel</type>
	 * that require non-standard behaviour when a connection is being created.
	 * @param first -- the component that the connection starts from.
	 * @param second -- the component that the connection goes to.
	 * @return the newly created <type>Connection</type> object that has been
	 * added to the model.
	 * @throws InvalidConnectionException
	 */
	protected Connection createConnection(Component first, Component second)
	throws InvalidConnectionException {
		Connection con = new Connection(first, second);
		addConnection(con);
		return con;
	}

	/**
	 * <p>Calling this method will cause all registered listeners to receive a
	 * <code>onPropertyChanged</code> event.</p>
	 * <p>This method must be called manually whenever a change to the
	 * node properties has been made.</p>
	 * @param propertyName -- the name of the changed property
	 * @param n -- the node that had its property changed
	 */
	public void fireNodePropertyChanged(String propertyName, MathNode n) {
		for (MathModelListener l : listeners)
			l.onNodePropertyChanged(propertyName, n);
	}

	/**
	 * @param ID -- the unique ID of the required component.
	 * @return the component that is registered under the given ID.
	 */
	final public Component getComponentByID(int ID) {
		return components.get(ID);
	}

	/**
	 * <p>Finds a component given its old ID.</p>
	 * <p>This operation is only valid immediately after <code>pasteFromXML</code>
	 * operation, i.e. either after load from XML or paste from clipboard, and will
	 * return a component using the ID listed in the XML source.</p>
	 * <p>This is required because the IDs are non-persistent: every time a component
	 * is being added to the model, it will be assigned a new ID, regardless of whether
	 * its old ID was unique in context of this model.</p>
	 * @param oldID -- the ID that is listed in the XML source used in the last
	 * <code>pasteFromXML</code> operation.
	 * @return the component that is referred to by the given ID.
	 */
	final public Component getComponentByRenamedID(int oldID) {
		Integer newID = componentRenames.get(oldID);
		if (newID == null)
			return null;
		return getComponentByID(newID);
	}

	/**
	 * @return a set of all components currently present in the model.
	 */
	final public Set<Component> getComponents() {
		return new HashSet<Component>(components.values());
	}

	/**
	 * @param ID -- the unique ID of the required connection.
	 * @return the connection that is registered under the given ID.
	 */
	final public Connection getConnectionByID(int ID) {
		return connections.get(ID);
	}

	/**
	 * <p>Finds a connection given its old ID.</p>
	 * <p>This operation is only valid immediately after <code>pasteFromXML</code>
	 * operation, i.e. either after load from XML or paste from clipboard, and will
	 * return a component using the ID listed in the XML source.</p>
	 * <p>This is required because the IDs are non-persistent: every time a connection
	 * is being added to the model, it will be assigned a new ID, regardless of whether
	 * its old ID was unique in context of this model.</p>
	 * @param oldID -- the ID that is listed in the XML source used in the last
	 * <code>pasteFromXML</code> operation.
	 * @return the connection that is referred to by the given ID.
	 */
	final public Connection getConnectionByRenamedID(int oldID) {
		Integer newID = connectionRenames.get(oldID);
		if (newID == null)
			return null;
		return getConnectionByID(newID);
	}

	/**
	 * @return a set of all connections currently present in the model.
	 */
	final public Set<Connection> getConnections() {
		return new HashSet<Connection>(connections.values());
	}

	/**
	 * @see org.workcraft.dom.Model#getDisplayName()
	 */
	public String getDisplayName() {
		DisplayName name = this.getClass().getAnnotation(DisplayName.class);
		if (name == null)
			return this.getClass().getSimpleName();
		else
			return name.value();
	}

	/**
	 * @see org.workcraft.dom.Model#getMathModel()
	 */
	final public MathModel getMathModel() {
		return this;
	}

	final private int getNextNodeID() {
		return nodeIDCounter++;
	}

	/**
	 * @return a set of component classes supported by this model.
	 */
	final public Set<Class<? extends Component>> getSupportedComponents() {
		return new HashSet<Class<? extends Component>>(supportedComponents);
	}

	/**
	 * @see org.workcraft.dom.Model#getTitle()
	 */
	final public String getTitle() {
		return title;
	}

	/**
	 * @see org.workcraft.dom.Model#getVisualModel()
	 */
	final public VisualModel getVisualModel() {
		return null;
	}

	/**
	 * Deserialises a number of nodes from an XML document fragment, and adds
	 * them to the model.
	 * @param modelElement -- the parent &lt;model&gt; element that the node elements will be read from.
	 * @throws LoadFromXMLException thrown if a problem is encountered during deserialisation.
	 * The particular cause can be established by calling <code>getCause</code> on this exception object.
	 */
	final public void pasteFromXML(Element modelElement)
	throws LoadFromXMLException {
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
				Connection connection = ConnectionFactory.createConnection(e, getReferenceResolver());

				Integer oldID = connection.getID();
				Integer newID = addConnection(connection);

				connectionRenames.put(oldID, newID);
			}

		} catch (InvalidComponentException e) {
			throw new LoadFromXMLException(e);
		} catch (ComponentCreationException e) {
			throw new LoadFromXMLException(e);
		} catch (InvalidConnectionException e) {
			throw new LoadFromXMLException(e);
		} catch (ConnectionCreationException e) {
			throw new LoadFromXMLException(e);
		}
	}

	/**
	 * <p>Removes a component from the model.</p>
	 * <p>This will cause all connections either starting or ending on
	 * this component to be removed as well.</p>
	 * <p>After the component is removed, an event will be sent to
	 * all registered listeners.</p>
	 * @param component -- the component to be removed.
	 */
	public final void removeComponent(Component component) {
		HashSet<Connection> connectionsToRemove = new HashSet<Connection>(
				component.getConnections());

		for (Connection con : connectionsToRemove)
			removeConnection(con);

		components.remove(component.getID());

		fireComponentRemoved(component);
	}

	private void fireComponentRemoved(Component component) {
		for (MathModelListener l : listeners)
			l.onComponentRemoved(component);
	}


	/**
	 * <p>Removes a connection from the model.</p>
	 * <p>After the connection is removed, an event will be sent to
	 * all registered listeners.</p>
	 * @param connection -- the connection to be removed.
	 */
	final public void removeConnection(Connection connection) {
		connection.getFirst().removeFromPostset(connection.getSecond());
		connection.getSecond().removeFromPreset(connection.getFirst());
		connection.getFirst().removeConnection(connection);
		connection.getSecond().removeConnection(connection);

		connections.remove(connection.getID());
		fireConnectionRemoved(connection);
	}

	private void fireConnectionRemoved(Connection connection) {
		for (MathModelListener l : listeners)
			l.onConnectionRemoved(connection);
	}

	/**
	 * Remove a <type>MathModelListener</type> from the listener list.
	 * @param listener -- the listener to be removed.
	 */
	final public void removeListener(MathModelListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Sets the title of this model.
	 * @param title -- the model title.
	 */
	final public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * This method may be called to test that
	 * the model is in a valid state (which is defined by the inheriting
	 * types) before some operations take place.</p>
	 * <p>If a model is valid, then this method does nothing. Otherwise,
	 * a <type>ModelValidationException</type> is thrown.</p>
	 * @throws ModelValidationException an exception giving
	 * an explanation of the problem.
	 *
	 */
	abstract public void validate() throws ModelValidationException;

	/**
	 * This method may be called to test that
	 * the connection is allowed by the model (which is defined by the inheriting
	 * types) before it is added.</p>
	 * <p>If a connection is valid, then this method does nothing. Otherwise,
	 * an <type>InvalidConnectionException</type> is thrown.</p>
	 * @param connection
	 * @throws InvalidConnectionException
	 */
	abstract public void validateConnection(Connection connection)
	throws InvalidConnectionException;

	final protected void addXMLSerialiser(XMLSerialiser serialiser) {
		serialisation.addSerialiser(serialiser);
	}

	final protected void addXMLDeserialiser(XMLDeserialiser deserialiser) {
		serialisation.addDeserialiser(deserialiser);
	}

	public final void serialiseToXML(Element componentElement) {
		serialisation.serialise(componentElement);
	}

	public final void deserialiseFromXML(Element modelElement) throws LoadFromXMLException {
		serialisation.deserialise(modelElement);
	}

	public RenamedReferenceResolver getReferenceResolver() {
		return referenceResolver;
	}
}