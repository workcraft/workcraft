package org.workcraft.dom;

/**
 * An interface that is used by classes that need
 * to monitor changes in a <code>MathModel</code>,
 * such as addition or removal of nodes.
 * @author Ivan Poliakov
 *
 */
public interface MathModelListener {
	/**
	 * An event method that is called whenever a new
	 * component is added to the model.
	 * @param component -- the newly added component
	 */
	public void onComponentAdded(Component component);

	/**
	 * An event method that is called whenever an existing
	 * component is removed from the model.
	 * @param component -- the removed component
	 */
	public void onComponentRemoved(Component component);

	/**
	 * An event method that is called whenever a new
	 * connection is added to the model.
	 * @param connection -- the newly added connection
	 */
	public void onConnectionAdded(Connection connection);

	/**
	 * An event method that is called whenever an existing
	 * connection is removed from the model.
	 * @param connection -- the removed connection
	 */
	public void onConnectionRemoved(Connection connection);


	/**
	 * An event method that is called whenever a property of
	 * a <code>MathNode</code> contained in this model is
	 * changed.
	 * @param propertyName -- the name of the changed property
	 * @param n -- the node that had its property changed
	 */
	public void onNodePropertyChanged(String propertyName, MathNode n);
}
