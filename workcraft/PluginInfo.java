package org.workcraft;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.DisplayName;
import org.workcraft.exceptions.DocumentFormatException;
import org.workcraft.util.XmlUtil;


public class PluginInfo implements Comparable<PluginInfo> {
	private String displayName;
	private String className;
	private String[] interfaceNames;

	private void addInterfaces (Class<?> cls, Set<String> set) {
		if (cls == null || cls.equals(Object.class))
			return;

		for (Class<?> interf : cls.getInterfaces())
		{
			set.add(interf.getName());
			addInterfaces (interf, set);
		}

		addInterfaces(cls.getSuperclass(), set);
	}

	public PluginInfo(Class<?> cls) {
		className = cls.getName();

		DisplayName name = cls.getAnnotation(DisplayName.class);

		if(name == null)
			displayName = className.substring(className.lastIndexOf('.')+1);
		else
			displayName = name.value();

		HashSet<String> interfaces = new HashSet<String>();
		addInterfaces (cls, interfaces);
		interfaceNames = interfaces.toArray(new String[0]);

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

		for (int i=0; i<nl.getLength(); i++)
			interfaceNames[i] = ((Element)nl.item(i)).getAttribute("name");
	}

	public void toXml(Element element) {
		XmlUtil.writeStringAttr(element, "class", className);
		XmlUtil.writeStringAttr(element, "displayName", displayName);

		for (String i : interfaceNames) {
			Element e = element.getOwnerDocument().createElement("interface");
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

	public boolean isInterfaceImplemented(String interfaceClassName) {
		for (String s : interfaceNames)
			if (s.equals(interfaceClassName))
				return true;
		return false;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public String toString() {
		return displayName;
	}


	public int compareTo(PluginInfo o) {
		return toString().compareTo(o.toString());
	}
}
