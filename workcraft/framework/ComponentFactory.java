package org.workcraft.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.ReferenceResolver;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.VisualComponentGeneratorAttribute;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
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
			throw new ComponentCreationException (ex);
		}	catch (ClassNotFoundException ex) {
			throw new ComponentCreationException (ex);
		} catch (SecurityException ex) {
			throw new ComponentCreationException (ex);
		} catch (NoSuchMethodException ex) {
			throw new ComponentCreationException (ex);
		} catch (IllegalArgumentException ex) {
			throw new ComponentCreationException (ex);
		} catch (InstantiationException ex) {
			throw new ComponentCreationException (ex);
		} catch (IllegalAccessException ex) {
			throw new ComponentCreationException (ex);
		} catch (InvocationTargetException ex) {
			throw new ComponentCreationException (ex);
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
			throw new ComponentCreationException (ex);
		}	catch (ClassNotFoundException ex) {
			throw new ComponentCreationException (ex);
		} catch (SecurityException ex) {
			throw new ComponentCreationException (ex);
		} catch (NoSuchMethodException ex) {
			throw new ComponentCreationException (ex);
		} catch (IllegalArgumentException ex) {
			throw new ComponentCreationException (ex);
		} catch (InstantiationException ex) {
			throw new ComponentCreationException (ex);
		} catch (IllegalAccessException ex) {
			throw new ComponentCreationException (ex);
		} catch (InvocationTargetException ex) {
			throw new ComponentCreationException (ex);
		}
	}

	public static Component createComponent (Element element, ReferenceResolver resolver) throws ComponentCreationException {
		String className = element.getAttribute("class");

		try {
			Class<?> elementClass = Class.forName(className);
			Constructor<?> ctor = elementClass.getConstructor(Element.class, ReferenceResolver.class);
			Component component = (Component)ctor.newInstance(element, resolver);
			return component;
		} catch (ClassCastException ex) {
			throw new ComponentCreationException (ex);
		} catch (ClassNotFoundException ex) {
			throw new ComponentCreationException (ex);
		} catch (SecurityException ex) {
			throw new ComponentCreationException (ex);
		} catch (NoSuchMethodException ex) {
			return createComponent(element);
		} catch (IllegalArgumentException ex) {
			throw new ComponentCreationException (ex);
		} catch (InstantiationException ex) {
			throw new ComponentCreationException (ex);
		} catch (IllegalAccessException ex) {
			throw new ComponentCreationException (ex);
		} catch (InvocationTargetException ex) {
			throw new ComponentCreationException (ex);
		}
	}

	public static VisualNode createVisualComponent (Component component) throws VisualComponentCreationException
	{
		return createVisualComponentInternal(component);
	}

	public static VisualNode createVisualComponentInternal (Component component, Object ... constructorParameters) throws VisualComponentCreationException {
		VisualComponentGeneratorAttribute generator = component.getClass().getAnnotation(VisualComponentGeneratorAttribute.class);
		if(generator != null)
			try {
				return ((org.workcraft.dom.VisualComponentGenerator)Class.forName(generator.generator()).
						getConstructor().newInstance()).
						createComponent(component, constructorParameters);
			} catch (Exception e) {
				throw new VisualComponentCreationException (e);
			}
		else
			return createVisualComponentSimple(component, constructorParameters);
	}

	private static VisualComponent createVisualComponentSimple (Component component, Object ... constructorParameters) throws VisualComponentCreationException {
		// Find the corresponding visual class
		VisualClass vcat = component.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());

			Object [] args = new Object[constructorParameters.length+1];
			args[0] = component;
			for(int i=0;i<constructorParameters.length;i++)
				args[i+1] = constructorParameters[i];

			Class <?> [] types = new Class <?> [args.length];
			for(int i=0;i<args.length;i++)
				types[i] = args[i].getClass();

			Constructor<?> ctor = new ConstructorParametersMatcher().match(visualClass, types);
			VisualComponent visual = (VisualComponent) ctor.newInstance(args);
			return visual;

		} catch (ClassNotFoundException e) {
			throw new VisualComponentCreationException (e);
		} catch (SecurityException e) {
			throw new VisualComponentCreationException (e);
		} catch (NoSuchMethodException e) {
			throw new VisualComponentCreationException (e);
		} catch (IllegalArgumentException e) {
			throw new VisualComponentCreationException (e);
		} catch (InstantiationException e) {
			throw new VisualComponentCreationException (e);
		} catch (IllegalAccessException e) {
			throw new VisualComponentCreationException (e);
		} catch (InvocationTargetException e) {
			throw new VisualComponentCreationException (e);
		}
	}

	public static VisualNode createVisualComponent (Element element, VisualModel refModel) throws VisualComponentCreationException {
		// Find the component
		int ref = XmlUtil.readIntAttr(element, "ref", -1);
		Component component = refModel.getMathModel().getComponentByRenamedID(ref);

		return createVisualComponentInternal(component, element);
	}
}
