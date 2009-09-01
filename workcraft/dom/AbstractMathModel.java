package org.workcraft.dom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.framework.serialisation.ReferenceProducer;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.util.XmlUtil;

/**
 * A base class for all mathematical interpreted graph models. "Mathematical" in this
 * context means that this class only contains information about logical objects,
 * such as nodes and arcs, but does not carry any information regarding their
 * visual representation and layout.
 * @author Ivan Poliakov
 *
 */
public abstract class AbstractMathModel implements Plugin, XMLSerialisable, MathModel {
	private int nodeIDCounter = 0;

	private Hashtable<Integer, Component> components = new Hashtable<Integer, Component>();
	private Hashtable<Integer, Connection> connections = new Hashtable<Integer, Connection>();

	private LinkedList<MathModelListener> listeners = new LinkedList<MathModelListener>();

	private HashSet<Class<? extends Component>> supportedComponents = new HashSet<Class<? extends Component>>();

	private XMLSerialisation serialisation = new XMLSerialisation();

	private String title = "";


	private Group root = new Group();

	private void addSerialisationObjects() {
		serialisation.addSerialiser(new XMLSerialiser() {
			public String getTagName() {
				return AbstractMathModel.class.getSimpleName();
			}
			public void serialise(Element element, ReferenceProducer refResolver) {
				XmlUtil.writeStringAttr(element, "title", title);
			}
			public void deserialise(Element element, ReferenceResolver refResolver) throws DeserialisationException {
				title = XmlUtil.readStringAttr(element, "title");
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
				n.serialise(element, null);
			}

		for (MathNode n : nodes)
			if (n instanceof Connection) {
				element = XmlUtil.createChildElement("connection",
						parentElement);
				element.setAttribute("class", n.getClass().getName());
				n.serialise(element, null);
			}
	}


	/**
	 * Creates an empty model.
	 */
	public AbstractMathModel() {
		addSerialisationObjects();
	}



	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#addComponent(org.workcraft.dom.Component)
	 */
	final public int addComponent(Component component) {
		component.setID(getNextNodeID());
		components.put(component.getID(), component);
		root.add(component);

		fireComponentAdded(component);

		return component.getID();
	}

	private void fireComponentAdded(Component component) {
		for (MathModelListener l : listeners)
			l.onComponentAdded(component);
	}


	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#addConnection(org.workcraft.dom.Connection)
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
		root.add(connection);

		fireConnectionAdded(connection);

		return connection.getID();
	}

	private void fireConnectionAdded(Connection connection) {
		for (MathModelListener l : listeners)
			l.onConnectionAdded(connection);
	}


	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#addListener(org.workcraft.dom.MathModelListener)
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

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#isComponentSupported(org.workcraft.dom.Component)
	 */
	final public boolean isComponentSupported(Component component) {
		return supportedComponents.contains(component.getClass());
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#connect(org.workcraft.dom.Component, org.workcraft.dom.Component)
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

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#getNodeByID(int)
	 */
	final public MathNode getNodeByID(int ID) {
		MathNode result = getComponentByID(ID);
		if (result == null)
			result = getConnectionByID(ID);
		return result;
	}


	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#getComponentByID(int)
	 */
	final public Component getComponentByID(int ID) {
		return components.get(ID);
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#getComponents()
	 */
	final public Set<Component> getComponents() {
		return new HashSet<Component>(components.values());
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#getConnectionByID(int)
	 */
	final public Connection getConnectionByID(int ID) {
		return connections.get(ID);
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#getConnections()
	 */
	final public Set<Connection> getConnections() {
		return new HashSet<Connection>(connections.values());
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#getDisplayName()
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
	final public AbstractMathModel getMathModel() {
		return this;
	}


	final private int getNextNodeID() {
		return nodeIDCounter++;
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.MathModel#getSupportedComponents()
	 */
	final public Set<Class<? extends Component>> getSupportedComponents() {
		return new HashSet<Class<? extends Component>>(supportedComponents);
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.MathModel#getTitle()
	 */
	final public String getTitle() {
		return title;
	}

	public String getReference() {
		return "#mathModel";
	}

	public void setReference(String reference) {
	}

	/**
	 * @see org.workcraft.dom.Model#getVisualModel()
	 */
	final public VisualModel getVisualModel() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#removeComponent(org.workcraft.dom.Component)
	 */
	public final void removeComponent(Component component) {
		HashSet<Connection> connectionsToRemove = new HashSet<Connection>(
				component.getConnections());

		for (Connection con : connectionsToRemove)
			removeConnection(con);

		components.remove(component.getID());
		root.remove(component);

		fireComponentRemoved(component);
	}

	private void fireComponentRemoved(Component component) {
		for (MathModelListener l : listeners)
			l.onComponentRemoved(component);
	}


	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#removeConnection(org.workcraft.dom.Connection)
	 */
	final public void removeConnection(Connection connection) {
		connection.getFirst().removeFromPostset(connection.getSecond());
		connection.getSecond().removeFromPreset(connection.getFirst());
		connection.getFirst().removeConnection(connection);
		connection.getSecond().removeConnection(connection);

		connections.remove(connection.getID());
		root.remove(connection);
		fireConnectionRemoved(connection);
	}

	private void fireConnectionRemoved(Connection connection) {
		for (MathModelListener l : listeners)
			l.onConnectionRemoved(connection);
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#removeListener(org.workcraft.dom.MathModelListener)
	 */
	final public void removeListener(MathModelListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#setTitle(java.lang.String)
	 */
	final public void setTitle(String title) {
		this.title = title;
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#validate()
	 */
	abstract public void validate() throws ModelValidationException;

	/* (non-Javadoc)
	 * @see org.workcraft.dom.Kojo#validateConnection(org.workcraft.dom.Connection)
	 */
	abstract public void validateConnection(Connection connection)
	throws InvalidConnectionException;

	final protected void addXMLSerialiser(XMLSerialiser serialiser) {
		serialisation.addSerialiser(serialiser);
	}

	public final void serialise(Element element, ReferenceProducer refResolver) {
		serialisation.serialise(element, refResolver);
	}

	public final void deserialise(Element modelElement, ReferenceResolver refResolver) throws DeserialisationException {
		serialisation.deserialise(modelElement, refResolver);
	}

	public final Group getRoot() {
		return root;
	}

	public final void setRoot(HierarchyNode root) {
		if (root instanceof Group)
		{
			this.root = (Group)root;
		} else
			throw new RuntimeException("The root node of a math model must be a group.");
	}

	public ReferenceProducer getReferenceProducer() {
		return new ReferenceProducer() {
			public String getReference(Object obj) {
				if (obj instanceof MathNode)
					return Integer.toString(((MathNode)obj).getID());
				else
					return null;
			}
		};
	}


	public ReferenceResolver getReferenceResolver() {
		return new ReferenceResolver() {
			public Object getObject(String reference) {
				return getNodeByID(Integer.parseInt(reference));
			}
		};
	}
}