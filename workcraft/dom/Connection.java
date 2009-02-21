package org.workcraft.dom;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

@VisualClass("org.workcraft.dom.visual.VisualConnection")
public class Connection {
	private Component first;
	private Component second;
	private int ID;

	private XMLSerialisation serialisation = new XMLSerialisation();

	private void addSerialisationObjects() {
		serialisation.addSerialiser(new XMLSerialiser() {
			public void serialise(Element element) {
				XmlUtil.writeIntAttr(element, "ID", ID);
				XmlUtil.writeIntAttr(element, "first", first.getID());
				XmlUtil.writeIntAttr(element, "second", second.getID());
			}
			public String getTagName() {
				return Connection.class.getSimpleName();
			}
		});
	}

	public Connection (Component first, Component second) {
		this.first = first;
		this.second = second;

		addSerialisationObjects();
	}

	public Connection (Element connectionElement, ReferenceResolver referenceResolver) {
		Element element = XmlUtil.getChildElement(Connection.class.getSimpleName(), connectionElement);
		ID = XmlUtil.readIntAttr(element, "ID", -1);

		int firstID = XmlUtil.readIntAttr(element, "first", -1);
		int secondID = XmlUtil.readIntAttr(element, "second", -1);

		first = referenceResolver.getComponentByID(firstID);
		second = referenceResolver.getComponentByID(secondID);

		addSerialisationObjects();
	}

	final public int getID() {
		return ID;
	}

	final public void setID(int id) {
		ID = id;
	}

	final public Component getFirst() {
		return first;
	}

	final public Component getSecond() {
		return second;
	}

	final public void addXMLSerialisable (XMLSerialiser serialisable) {
		serialisation.addSerialiser(serialisable);
	}

	final public void serialiseToXML(Element componentElement) {
		serialisation.serialise(componentElement);
	}
}