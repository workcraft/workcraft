package org.workcraft.framework;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;


public class Component {
	protected Component parent = null;
	protected Document ownerDocument = null;
	protected HashSet<Component> children = new HashSet<Component>();
	protected HashSet<Component> preset = new HashSet<Component>();
	protected HashSet<Component> postset = new HashSet<Component>();

	protected int id = -1;
	protected String label = "";
	protected Hashtable<String, String> customProperties = new Hashtable<String, String>();

	public Component() {
	}

	public Component (Element xmlElement) {
		id = XmlUtil.readIntAttr(xmlElement, "id", id);
		label = XmlUtil.readStringAttr(xmlElement, "label");
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void addToPreset (Component component) {
		preset.add(component);
	}

	public void removeFromPreset(Component component) {
		preset.remove(component);
	}

	public Set<Component> getPreset() {
		return (Set<Component>)preset.clone();
	}

	public void addToPostset (Component component) {
		preset.add(component);
	}

	public void removeFromPostset(Component component) {
		preset.remove(component);
	}

	public Set<Component> getPostset() {
		return (Set<Component>)postset.clone();
	}

	public void addChild (Component component) {
		children.add(component);
	}

	public HashSet<Component> getChildren() {
		return (HashSet<Component>)children.clone();
	}

	public void toXml (Element componentElement) {
		XmlUtil.writeIntAttr(componentElement, "id", id);
		XmlUtil.writeStringAttr(componentElement, "label", label);
		XmlUtil.writeStringAttr(componentElement, "class", this.getClass().getName());
	}
}