package org.workcraft.dom;

import java.util.Set;

import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;

public interface MathModel extends Model {

	/**
	 * Adds a component to the model. The component will be assigned
	 * an unique ID, and <code>onComponentAdded</code> method
	 * @param component
	 * @return
	 */
	public int addComponent(Component component);

	/**
	 * Adds a connection to the model.
	 * @param connection -- the connection object.
	 * @return the unique ID of the connection node in this <type>MathModel</type>
	 * @throws InvalidConnectionException
	 */
	public int addConnection(Connection connection)
			throws InvalidConnectionException;

	/**
	 * Adds a <type>MathModelListener</type> to monitor
	 * changes in this model.
	 * @param listener -- the <type>MathModelListener</type> to add to the listener list.
	 */
	public void addListener(MathModelListener listener);

	/**
	 * Checks if the given component class is present in this model's supported
	 * components list.
	 * @param component -- a component to test
	 * @return true if the component's class is supported by this model, false
	 * otherwise
	 */
	public boolean isComponentSupported(Component component);

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
	public Connection connect(Component first, Component second)
			throws InvalidConnectionException;

	public MathNode getNodeByID(int ID);

	/**
	 * @param ID -- the unique ID of the required component.
	 * @return the component that is registered under the given ID.
	 */
	public Component getComponentByID(int ID);

	/**
	 * @return a set of all components currently present in the model.
	 */
	public Set<Component> getComponents();

	/**
	 * @param ID -- the unique ID of the required connection.
	 * @return the connection that is registered under the given ID.
	 */
	public Connection getConnectionByID(int ID);

	/**
	 * @return a set of all connections currently present in the model.
	 */
	public Set<Connection> getConnections();

	/**
	 * @see org.workcraft.dom.Model#getDisplayName()
	 */
	public String getDisplayName();

	/**
	 * @return a set of component classes supported by this model.
	 */
	public  Set<Class<? extends Component>> getSupportedComponents();

	/**
	 * @see org.workcraft.dom.Model#getTitle()
	 */
	public String getTitle();

	/**
	 * <p>Removes a component from the model.</p>
	 * <p>This will cause all connections either starting or ending on
	 * this component to be removed as well.</p>
	 * <p>After the component is removed, an event will be sent to
	 * all registered listeners.</p>
	 * @param component -- the component to be removed.
	 */
	public void removeComponent(Component component);

	/**
	 * <p>Removes a connection from the model.</p>
	 * <p>After the connection is removed, an event will be sent to
	 * all registered listeners.</p>
	 * @param connection -- the connection to be removed.
	 */
	public void removeConnection(Connection connection);

	/**
	 * Remove a <type>MathModelListener</type> from the listener list.
	 * @param listener -- the listener to be removed.
	 */
	public void removeListener(MathModelListener listener);

	/**
	 * Sets the title of this model.
	 * @param title -- the model title.
	 */
	public void setTitle(String title);

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
	public void validate() throws ModelValidationException;

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

	public Group getRoot();

}