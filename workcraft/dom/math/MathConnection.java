package org.workcraft.dom.math;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;


@VisualClass("org.workcraft.dom.visual.connections.VisualConnection")
public class MathConnection extends MathNode implements Connection {
	private MathNode first;
	private MathNode second;

	public MathConnection () {
	}

	public MathConnection (MathNode first, MathNode second) {
		super();
		setComponents(first, second);
	}

	final public MathNode getFirst() {
		return first;
	}

	final public MathNode getSecond() {
		return second;
	}

	final public void setComponents(MathNode first, MathNode second) {
		this.first = first;	 this.second = second;
	}
}