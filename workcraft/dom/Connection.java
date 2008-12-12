package org.workcraft.dom;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

public class Connection {
	protected Component first;
	protected Component second;
	protected int ID;

	public int getID() {
		return ID;
	}

	public void setID(int id) {
		ID = id;
	}

	public Component getFirst() {
		return first;
	}

	public Component getSecond() {
		return second;
	}

	public Connection (Component first, Component second) {
		this.first = first;
		this.second = second;
	}

	public void toXML (Element connectionElement) {
		XmlUtil.writeIntAttr(connectionElement, "first", first.getID());
		XmlUtil.writeIntAttr(connectionElement, "second", second.getID());
		XmlUtil.writeIntAttr(connectionElement, "ID", getID());
	}

	public Connection (Element xmlElement, MathModel model) {
		ID = XmlUtil.readIntAttr(xmlElement, "ID", -1);

		int firstID = XmlUtil.readIntAttr(xmlElement, "first", -1);
		int secondID = XmlUtil.readIntAttr(xmlElement, "first", -1);

		first = model.getComponentByID(firstID);
		second = model.getComponentByID(secondID);
	}
}