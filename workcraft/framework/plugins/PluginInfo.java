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
		className = cls.getName();

		DisplayName name = cls.getAnnotation(DisplayName.class);

		if(name == null)
			displayName = className.substring(className.lastIndexOf('.')+1);
		else
			displayName = name.value();

		Class<?>[] interfaces = cls.getInterfaces();
		interfaceNames = new String[interfaces.length];
		int j = 0;

		for (Class<?> i : interfaces) {
			interfaceNames[j++] = i.getName();
		}

		LinkedList<String> list = new LinkedList<String>();
		addSuperclass(cls, list);
		superclassNames = list.toArray(new String[0]);
	}

	protected void addSuperclass(Class<?> cls, LinkedList<String> list) {
		Class<?> scls = cls.getSuperclass();
		if (scls != null && !scls.equals(Object.class)) {
			list.add(scls.getName());
			addSuperclass(scls, list);
		}
	}

	public PluginInfo(Element element) throws DocumentFormatException {
		className = XmlUtil.readStringAttr(element, "class");
		if(className==null || className.isEmpty())
			throw new DocumentFormatException();

		displayName = XmlUtil.readStringAttr(element, "displayName");
		if (displayName.isEmpty())
			displayName = className.substring(className.lastIndexOf('.')+1);

		NodeList nl = element.getElementsByTagName("interface");
		interfaceNames = new String[nl.getLength()];

		for (int i=0; i<nl.getLength(); i++) {
			interfaceNames[i] = ((Element)nl.item(i)).getAttribute("name");
		}

		nl = element.getElementsByTagName("superclass");
		superclassNames = new String[nl.getLength()];

		for (int i=0; i<nl.getLength(); i++) {
			superclassNames[i] = ((Element)nl.item(i)).getAttribute("name");
		}
	}

	public void toXml(Element element) {
		XmlUtil.writeStringAttr(element, "class", className);
		XmlUtil.writeStringAttr(element, "displayName", displayName);

		for (String i : interfaceNames) {
			Element e = element.getOwnerDocument().createElement("interface");
			e.setAttribute("name", i);
			element.appendChild(e);
		}

		for (String i : superclassNames) {
			Element e = element.getOwnerDocument().createElement("superclass");
			e.setAttribute("name", i);
			element.appendChild(e);
		}
	}

	public Class<?> loadClass() throws ClassNotFoundException {
		return Class.forName(className);
	}

	public String[] getInterfaces() {
		return interfaceNames.clone();
	}

	public String[] getSuperclasses() {
		return superclassNames.clone();
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getClassName() {
		return className;
	}

	public String toString() {
		return displayName;
	}


	public int compareTo(PluginInfo o) {
		return toString().compareTo(o.toString());
	}
}
