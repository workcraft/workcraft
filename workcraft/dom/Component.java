package org.workcraft.dom;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

public abstract class Component {
	protected int ID = -1;
	protected String label = "";
	protected Set<Connection> connections = new HashSet<Connection>();

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	protected Hashtable<String, String> customProperties = new Hashtable<String, String>();

	protected HashSet<Component> preset = new HashSet<Component>();
	protected HashSet<Component> postset = new HashSet<Component>();

	public Component() {
	}

	public Component (Element xmlElement) {
		ID = XmlUtil.readIntAttr(xmlElement, "ID", ID);
		label = XmlUtil.readStringAttr(xmlElement, "label");
	}

	public void setID(Integer id) {
		ID = id;
	}

	public Integer getID() {
		return ID;
	}

	public void addToPreset (Component component) {
		preset.add(component);
	}

	public void removeFromPreset(Component component) {
		preset.remove(component);
	}

	public void addConnection(Connection connection) {
		connections.add(connection);
	}

	public void removeConnection(Connection connection) {
		connections.remove(connection);
	}

	public Set<Connection> getConnections() {
		return new HashSet<Connection>(connections);
	}

	@SuppressWarnings("unchecked")
	public Set<Component> getPreset() {
		return (Set<Component>)preset.clone();
	}

	public void addToPostset (Component component) {
		postset.add(component);
	}

	public void removeFromPostset(Component component) {
		postset.remove(component);
	}

	@SuppressWarnings("unchecked")
	public Set<Component> getPostset() {
		return (Set<Component>)postset.clone();
	}

	public void toXML (Element componentElement) {
		XmlUtil.writeIntAttr(componentElement, "ID", ID);
		XmlUtil.writeStringAttr(componentElement, "label", label);
		XmlUtil.writeStringAttr(componentElement, "class", this.getClass().getName());
	}
}