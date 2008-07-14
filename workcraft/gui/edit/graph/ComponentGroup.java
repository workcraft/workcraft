package org.workcraft.gui.edit.graph;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.Component;

public class ComponentGroup extends Component {
	public ComponentGroup() {
	}

	public ComponentGroup (Element xmlElement) {
		super(xmlElement);
		NodeList nl = xmlElement.getElementsByTagName("component");
		for (int i=0; i<nl.getLength(); i++ ) {
			Element e = (Element)nl.item(i);
			String class_name = e.getAttribute("class");
			try {
				Class<?> cls = ClassLoader.getSystemClassLoader().loadClass(class_name);
				Constructor<?> ctor = cls.getConstructor(new Class[] {Element.class});
				Component n = (Component)ctor.newInstance();
				//addChild(n);
			} catch (ClassNotFoundException ex) {
				System.err.println("Failed to load class: "+ex.getMessage());
				ex.printStackTrace();
			} catch (InstantiationException ex) {
				ex.printStackTrace();
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			} catch (InvocationTargetException ex) {
				ex.getTargetException().printStackTrace();
				ex.printStackTrace();
			} catch (SecurityException ex) {
				ex.printStackTrace();
			} catch (NoSuchMethodException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void toXml(Element componentElement) {
		super.toXml(componentElement);
//		for (Component c: children) {
//			Element childElement = componentElement.getOwnerDocument().createElement("component");
//			c.toXml(childElement);
//			componentElement.appendChild(childElement);
//		}
	}
}