package org.workcraft.plugins.stg;

import org.w3c.dom.Element;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.util.XmlUtil;

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
		MINUS,
		TOGGLE
	}

	private Type type = Type.INTERNAL;
	private Direction direction = Direction.TOGGLE;
	private String signalName = "";
	private int instance = 0;

	private void addXMLSerialiser() {
		addXMLSerialiser(new XMLSerialiser() {
			public String getTagName() {
				return SignalTransition.class.getSimpleName();
			}

			public void serialise(Element element) {
				XmlUtil.writeStringAttr(element, "signalName", signalName);
				XmlUtil.writeStringAttr(element, "direction", direction.name());
				XmlUtil.writeStringAttr(element, "type", type.name());
				XmlUtil.writeIntAttr(element, "instance", instance);
			}
		});
	}

	public SignalTransition() {
		super();

		addXMLSerialiser();
	}

	public SignalTransition(Element xmlElement) {
		super(xmlElement);

		Element element = XmlUtil.getChildElement(SignalTransition.class.getSimpleName(), xmlElement);

		setSignalName(XmlUtil.readStringAttr(element, "signalName"));
		setDirection(Direction.valueOf(XmlUtil.readStringAttr(element, "direction")));
		setType(Type.valueOf(XmlUtil.readStringAttr(element, "type")));
		setInstance(XmlUtil.readIntAttr(element, "instance", 0));

		addXMLSerialiser();
	}

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
		else if (name.endsWith("~"))
			setDirection(Direction.TOGGLE);

		name = name.replace("+", "");
		name = name.replace("-", "");
		name = name.replace("/", "");
		name = name.replace("~", "");

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
