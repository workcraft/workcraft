package org.workcraft.dom;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all mathematical objects that act
 * as a graph node.
 * @author Ivan Poliakov
 *
 */
public abstract class Component extends MathNode {
	private Set<Connection> connections = new HashSet<Connection>();

	private HashSet<Component> preset = new HashSet<Component>();
	private HashSet<Component> postset = new HashSet<Component>();

	/**
	 * <p>Adds another component to this component's preset. Called
	 * by <type>MathModel</type> on <i>connect</i> operation.</p>
	 * <p><b>Note:</b> for internal use only, should not be called directly.</p>
	 * @param component -- the component to be added to preset
	 */
	final protected void addToPreset (Component component) {
		preset.add(component);
	}

	/**
	 * <p>Removes another component from this component's preset. Called
	 * by <type>MathModel</type> on <i>removeComponent/removeConnection</i>
	 * operation.</p>
	 *  <p><b>Note:</b> for internal use only, should not be called directly.</p>
	 * @param component -- the component to be removed from preset.
	 */
	final protected void removeFromPreset(Component component) {
		preset.remove(component);
	}

	/**
	 * Adds another component to this component's postset. Called
	 * by <type>MathModel</type> on <i>connect</i> operation.
	 * <p><b>Note:</b> for internal use only, should not be called directly.</p>
	 * @param component -- the component to be added to postset.
	 */
	final public void addToPostset (Component component) {
		postset.add(component);
	}

	/**
	 * <p>Removes another component from this component's postset. Called
	 * by <type>MathModel</type> on <i>removeComponent/removeConnection</i>
	 * operation.</p>
	 *  <p><b>Note:</b> for internal use only, should not be called directly.</p>
	 * @param component -- the component to be removed from postset.
	 */
	final public void removeFromPostset(Component component) {
		postset.remove(component);
	}

	/**
	 * Adds a connection to this component's connection list. Used by
	 * <type>MathModel</type> to cascade removal of connections
	 * on <i>removeComponent</i> operation.
	 * <p><b>Note:</b> for internal use only, should not be called directly.</p>
	 * @param connection
	 */
	final protected void addConnection(Connection connection) {
		connections.add(connection);
	}

	/**
	 * Removes a connection from this component's connection list. Used by
	 * <type>MathModel</type> to cascade removal of connections
	 * on <i>removeComponent</i> operation.
	 * <p><b>Note:</b> for internal use only, should not be called directly.</p>
	 * @param connection
	 */
	final protected void removeConnection(Connection connection) {
		connections.remove(connection);
	}

	/**
	 * <p><b>Note:</b> for internal use only, should not be called directly.</p>
	 *  @return A set of connections involving this component.
	 */
	final protected Set<Connection> getConnections() {
		return new HashSet<Connection>(connections);
	}

	/**
	 * @return A set of components directly preceding this component in the
	 * directed graph.
	 */
	final public Set<Component> getPreset() {
		return new HashSet<Component>(preset);
	}

	/**
	 * @return A set of components directly succeeding this component in the
	 * directed graph.
	 */
	final public Set<Component> getPostset() {
		return new HashSet<Component>(postset);
	}
}