package org.workcraft.plugins.stg;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.petri.Transition;

@DisplayName("Signal transition")
@VisualClass("org.workcraft.plugins.stg.VisualSignalTransition")
public class SignalTransition extends Transition {
	public enum Type {
		INPUT,
		OUTPUT,
		INTERNAL,
		DUMMY
	}

	public enum Direction {
		PLUS,
		MINUS
	}

	private Type type = Type.INTERNAL;
	private Direction direction = Direction.PLUS;
	private String signalName = "";
	private int instance = 1;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getSignalName() {
		return signalName;
	}

	public void setSignalName(String name) {
		if (name.endsWith("+"))
			setDirection(Direction.PLUS);
		else if (name.endsWith("-"))
			setDirection(Direction.MINUS);

		name = name.replace("+", "");
		name = name.replace("-", "");
		name = name.replace("/", "");

		signalName = name;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public int getInstance() {
		return instance;
	}

	public void setInstance(int instance) {
		this.instance = instance;
	}
}
