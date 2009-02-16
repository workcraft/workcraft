package org.workcraft.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.ComponentCreationException;
import org.workcraft.framework.exceptions.VisualComponentCreationException;
import org.workcraft.framework.plugins.HotKeyDeclaration;
import org.workcraft.framework.util.ConstructorParametersMatcher;
import org.workcraft.util.XmlUtil;

public class ComponentFactory {
	public static int getHotKeyCodeForClass (Class <? extends Component> cls) {
		// Find the corresponding visual class
		VisualClass vcat = cls.getAnnotation(VisualClass.class);
		// The component/connection does not define a visual representation
		if (vcat == null)
			return -1;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			HotKeyDeclaration hkd = visualClass.getAnnotation(HotKeyDeclaration.class);
			if (hkd == null)
				return -1;
			else
				return hkd.value();

		} catch (ClassNotFoundException e) {
			return -1;
		}

	}

	public static Component createComponent (String className) throws ComponentCreationException {
		try {
			Class<?> elementClass = Class.forName(className);
			Constructor<?> ctor = elementClass.getConstructor();
			Component component = (Component)ctor.newInstance();
			return component;
		} catch (ClassCastException ex) {
			throw new ComponentCreationException ("Cannot cast the class \"" + className +"\" to org.workcraft.dom.Component: " + ex.getMessage());
		}	catch (ClassNotFoundException ex) {
			throw new ComponentCreationException ("Cannot load component class: " + ex.getMessage());
		} catch (SecurityException ex) {
			throw new ComponentCreationException ("Security exception: " + ex.getMessage());
		} catch (NoSuchMethodException ex) {
			throw new ComponentCreationException ("Component class \"" + className + "\" does not declare the required constructor " + className + "(org.w3c.dom.Element element)");
		} catch (IllegalArgumentException ex) {
			throw new ComponentCreationException ("Component class instantiation failed: " + ex.getMessage());
		} catch (InstantiationException ex) {
			throw new ComponentCreationException ("Component class instantiation failed: " + ex.getMessage());
		} catch (IllegalAccessException ex) {
			throw new ComponentCreationException ("Component class instantiation failed: " + ex.getMessage());
		} catch (InvocationTargetException ex) {
			throw new ComponentCreationException ("Component class instantiation failed: " + ex.getTargetException().getMessage());
		}

	}

	public static Component createComponent (Element element) throws ComponentCreationException {
		String className = element.getAttribute("class");

		try {
			Class<?> elementClass = Class.forName(className);
			Constructor<?> ctor = elementClass.getConstructor(Element.class);
			Component component = (Component)ctor.newInstance(element);
			return component;
		} catch (ClassCastException ex) {
			throw new ComponentCreationException ("Cannot cast the class \"" + className +"\" to org.workcraft.dom.Component: " + ex.getMessage());
		}	catch (ClassNotFoundException ex) {
			throw new ComponentCreationException ("Cannot load component class: " + ex.getMessage());
		} catch (SecurityException ex) {
			throw new ComponentCreationException ("Security exception: " + ex.getMessage());
		} catch (NoSuchMethodException ex) {
			throw new ComponentCreationException ("Component class \"" + className + "\" does not declare the required constructor " + className + "(org.w3c.dom.Element element)");
		} catch (IllegalArgumentException ex) {
			throw new ComponentCreationException ("Component class instantiation failed: " + ex.getMessage());
		} catch (InstantiationException ex) {
			throw new ComponentCreationException ("Component class instantiation failed: " + ex.getMessage());
		} catch (IllegalAccessException ex) {
			throw new ComponentCreationException ("Component class instantiation failed: " + ex.getMessage());
		} catch (InvocationTargetException ex) {
			throw new ComponentCreationException ("Component class instantiation failed: " + ex.getTargetException().getMessage());
		}
	}

	public static VisualComponent createVisualComponent (Component component) throws VisualComponentCreationException {
		// Find the corresponding visual class
		VisualClass vcat = component.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = new ConstructorParametersMatcher().match(visualClass, component.getClass());
			VisualComponent visual = (VisualComponent) ctor.newInstance(component);
			return visual;

		} catch (ClassNotFoundException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be loaded for class " + component.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception:\n" + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualComponentCreationException("visual class " + vcat.value() +
					" does not declare the required constructor: \n" + vcat.value() +
					"(" + component.getClass().getName() +")" );
		} catch (IllegalArgumentException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: \n" + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: \n" + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}



	public static VisualComponent createVisualComponent (Element element, VisualModel refModel) throws VisualComponentCreationException {
		// Find the component
		int ref = XmlUtil.readIntAttr(element, "ref", -1);
		Component component = refModel.getMathModel().getComponentByRenamedID(ref);

		// Find the corresponding visual class
		VisualClass vcat = component.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor(component.getClass(), Element.class);
			VisualComponent visual = (VisualComponent)ctor.newInstance(component, element);
			return visual;

		} catch (ClassNotFoundException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be loaded for class " + component.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception:\n" + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualComponentCreationException("visual class " + vcat.value() +
					" does not declare the required constructor: \n" + vcat.value() +
					"(" + component.getClass().getName() +"," + Element.class.getName() + ")" );
		} catch (IllegalArgumentException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: \n" + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: \n" + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualComponentCreationException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}
}
