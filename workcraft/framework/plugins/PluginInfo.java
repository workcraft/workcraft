package org.workcraft.framework.plugins;

import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.DisplayName;
import org.workcraft.framework.exceptions.DocumentFormatException;
import org.workcraft.util.XmlUtil;


public class PluginInfo implements Comparable<PluginInfo> {
	private String displayName;

	private String className;
	private String[] interfaceNames;
	private String[] superclassNames;


	public PluginInfo(Class<?> cls) {
		this.className = cls.getName();

		DisplayName name = cls.getAnnotation(DisplayName.class);

		if(name == null)
			this.displayName = this.className.substring(this.className.lastIndexOf('.')+1);
		else
			this.displayName = name.value();

		Class<?>[] interfaces = cls.getInterfaces();
		this.interfaceNames = new String[interfaces.length];
		int j = 0;

		for (Class<?> i : interfaces)
			this.interfaceNames[j++] = i.getName();

		LinkedList<String> list = new LinkedList<String>();
		addSuperclass(cls, list);
		this.superclassNames = list.toArray(new String[0]);
	}

	protected void addSuperclass(Class<?> cls, LinkedList<String> list) {
		Class<?> scls = cls.getSuperclass();
		if (scls != null && !scls.equals(Object.class)) {
			list.add(scls.getName());
			addSuperclass(scls, list);
		}
	}

	public PluginInfo(Element element) throws DocumentFormatException {
		this.className = XmlUtil.readStringAttr(element, "class");
		if(this.className==null || this.className.isEmpty())
			throw new DocumentFormatException();

		this.displayName = XmlUtil.readStringAttr(element, "displayName");
		if (this.displayName.isEmpty())
			this.displayName = this.className.substring(this.className.lastIndexOf('.')+1);

		NodeList nl = element.getElementsByTagName("interface");
		this.interfaceNames = new String[nl.getLength()];

		for (int i=0; i<nl.getLength(); i++)
			this.interfaceNames[i] = ((Element)nl.item(i)).getAttribute("name");

		nl = element.getElementsByTagName("superclass");
		this.superclassNames = new String[nl.getLength()];

		for (int i=0; i<nl.getLength(); i++)
			this.superclassNames[i] = ((Element)nl.item(i)).getAttribute("name");
	}

	public void toXml(Element element) {
		XmlUtil.writeStringAttr(element, "class", this.className);
		XmlUtil.writeStringAttr(element, "displayName", this.displayName);

		for (String i : this.interfaceNames) {
			Element e = element.getOwnerDocument().createElement("interface");
			e.setAttribute("name", i);
			element.appendChild(e);
		}

		for (String i : this.superclassNames) {
			Element e = element.getOwnerDocument().createElement("superclass");
			e.setAttribute("name", i);
			element.appendChild(e);
		}
	}

	public Class<?> loadClass() throws ClassNotFoundException {
		return Class.forName(this.className);
	}

	public String[] getInterfaces() {
		return this.interfaceNames.clone();
	}

	public String[] getSuperclasses() {
		return this.superclassNames.clone();
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public String getClassName() {
		return this.className;
	}

	@Override
	public String toString() {
		return this.displayName;
	}


	public int compareTo(PluginInfo o) {
		return toString().compareTo(o.toString());
	}
}
