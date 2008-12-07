package org.workcraft.dom;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

public class Connection {
	protected Component first;
	protected Component second;
	protected int ID;

	public int getID() {
		return this.ID;
	}

	public void setID(int id) {
		this.ID = id;
	}

	public Component getFirst() {
		return this.first;
	}

	public Component getSecond() {
		return this.second;
	}

	public Connection (Component first, Component second) {
		this.first = first;
		this.second = second;
	}

	public void toXML (Element connectionElement) {
		XmlUtil.writeIntAttr(connectionElement, "first", this.first.getID());
		XmlUtil.writeIntAttr(connectionElement, "second", this.second.getID());
		XmlUtil.writeIntAttr(connectionElement, "ID", getID());
	}

	public Connection (Element xmlElement, MathModel model) {
		this.ID = XmlUtil.readIntAttr(xmlElement, "ID", -1);

		int firstID = XmlUtil.readIntAttr(xmlElement, "first", -1);
		int secondID = XmlUtil.readIntAttr(xmlElement, "first", -1);

		this.first = model.getComponentByID(firstID);
		this.second = model.getComponentByID(secondID);
	}
}