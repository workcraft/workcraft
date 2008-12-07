package org.workcraft.dom;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

public abstract class Component {
	protected int ID = -1;
	protected String label = "";

	public String getLabel() {
		return this.label;
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
		this.ID = XmlUtil.readIntAttr(xmlElement, "ID", this.ID);
		this.label = XmlUtil.readStringAttr(xmlElement, "label");
	}

	public void setID(Integer id) {
		this.ID = id;
	}

	public Integer getID() {
		return this.ID;
	}

	public void addToPreset (Component component) {
		this.preset.add(component);
	}

	public void removeFromPreset(Component component) {
		this.preset.remove(component);
	}

	@SuppressWarnings("unchecked")
	public Set<Component> getPreset() {
		return (Set<Component>)this.preset.clone();
	}

	public void addToPostset (Component component) {
		this.preset.add(component);
	}

	public void removeFromPostset(Component component) {
		this.preset.remove(component);
	}

	@SuppressWarnings("unchecked")
	public Set<Component> getPostset() {
		return (Set<Component>)this.postset.clone();
	}

	public void toXML (Element componentElement) {
		XmlUtil.writeIntAttr(componentElement, "ID", this.ID);
		XmlUtil.writeStringAttr(componentElement, "label", this.label);
		XmlUtil.writeStringAttr(componentElement, "class", this.getClass().getName());
	}
}