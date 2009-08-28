package org.workcraft.dom;


/**
 * <p>Base class for all mathematical objects that act
 * as a graph arc.</p>
 * @author Ivan Poliakov
 *
 */
@VisualClass("org.workcraft.dom.visual.connections.VisualConnection")
public class Connection extends MathNode {
	private Component first;
	private Component second;

	public Connection () {
	}

	public Connection (Component first, Component second) {
		super();
		setComponents(first, second);
	}

	/**
	 * @return the component that this connection starts from.
	 */
	final public Component getFirst() {
		return first;
	}

	/**
	 * @return the component that this connection goes to.
	 */
	final public Component getSecond() {
		return second;
	}

	final public void setComponents(Component first, Component second) {
		this.first = first;	 this.second = second;
	}
}