package org.workcraft.dom;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

public abstract class Component {
	private int ID = -1;
	private String label = "";

	private Set<Connection> connections = new HashSet<Connection>();

	private HashSet<Component> preset = new HashSet<Component>();
	private HashSet<Component> postset = new HashSet<Component>();

	private XMLSerialiser serialiser = new XMLSerialiser();

	private void addXMLSerialisable() {
		serialiser.addXMLSerialisable(new XMLSerialisable() {
			public void serialise(Element element) {
				XmlUtil.writeIntAttr(element, "ID", Component.this.ID);
				XmlUtil.writeStringAttr(element, "label", Component.this.label);
			}
			public String getTagName() {
				return Component.class.getSimpleName();
			}
		});
	}

	public Component() {
		addXMLSerialisable();
	}

	public Component (Element componentElement) {
		Element element = XmlUtil.getChildElement(Component.class.getSimpleName(), componentElement);
		ID = XmlUtil.readIntAttr(element, "ID", ID);
		label = XmlUtil.readStringAttr(element, "label");
		addXMLSerialisable();
	}

	final public String getLabel() {
		return label;
	}

	final public void setLabel(String label) {
		this.label = label;
	}

	final public void setID(Integer id) {
		ID = id;
	}

	final public Integer getID() {
		return ID;
	}

	final public void addToPreset (Component component) {
		preset.add(component);
	}

	final public void removeFromPreset(Component component) {
		preset.remove(component);
	}

	final public void addConnection(Connection connection) {
		connections.add(connection);
	}

	final public void removeConnection(Connection connection) {
		connections.remove(connection);
	}

	final public Set<Connection> getConnections() {
		return new HashSet<Connection>(connections);
	}

	@SuppressWarnings("unchecked")
	final public Set<Component> getPreset() {
		return (Set<Component>)preset.clone();
	}

	final public void addToPostset (Component component) {
		postset.add(component);
	}

	final public void removeFromPostset(Component component) {
		postset.remove(component);
	}

	@SuppressWarnings("unchecked")
	final public Set<Component> getPostset() {
		return (Set<Component>)postset.clone();
	}

	final public void addXMLSerialisable(XMLSerialisable serialisable) {
		serialiser.addXMLSerialisable(serialisable);
	}

	final public void serialiseToXML(Element componentElement) {
		serialiser.serialiseToXML(componentElement);
	}
}