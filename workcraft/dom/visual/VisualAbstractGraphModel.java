package org.workcraft.dom.visual;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.AbstractGraphModel;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.VisualClass;
import org.workcraft.framework.exceptions.VisualModelConstructionException;
import org.workcraft.util.XmlUtil;

public class VisualAbstractGraphModel {
	protected AbstractGraphModel model;
	protected VisualComponentGroup root;

	protected static Object createVisualClassFor (Object object, Class<?> expectedClass) throws VisualModelConstructionException {
		// Find the corresponding visual class
		VisualClass vcat = object.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor(object.getClass());
			Object visual = ctor.newInstance(object);

			if (!expectedClass.isAssignableFrom(visual.getClass()))
				throw new VisualModelConstructionException ("visual class " + visual.getClass().getName() +
						", created for object of class " + object.getClass().getName() + ", is not inherited from "
						+ expectedClass.getName());

			return visual;

		} catch (ClassNotFoundException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be loaded for class " + object.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualModelConstructionException("visual class " + vcat.value() +
					" does not declare the required constructor " + vcat.value() +
					"(" + object.getClass().getName() +")" );
		} catch (IllegalArgumentException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: " + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}

	protected static Object createVisualClassFor (Object object, Class<?> expectedClass, Element xmlElement) throws VisualModelConstructionException {
		// Find the corresponding visual class
		VisualClass vcat = object.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor(object.getClass(), Element.class);
			Object visual = ctor.newInstance(object, xmlElement);

			if (!expectedClass.isAssignableFrom(visual.getClass()))
				throw new VisualModelConstructionException ("visual class " + visual.getClass().getName() +
						", created for object of class " + object.getClass().getName() + ", is not inherited from "
						+ expectedClass.getName());

			return visual;

		} catch (ClassNotFoundException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be loaded for class " + object.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualModelConstructionException("visual class " + vcat.value() +
					" does not declare the required constructor " + vcat.value() +
					"(" + object.getClass().getName() + ", " + Element.class.getName()+")" );
		} catch (IllegalArgumentException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: " + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualModelConstructionException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}

	public VisualAbstractGraphModel(AbstractGraphModel model) throws VisualModelConstructionException {
		this.model = model;
		root = new VisualComponentGroup();

		// create a default flat structure
		for (Component component : model.getComponents()) {
			VisualComponent visualComponent = (VisualComponent)createVisualClassFor(component, VisualComponent.class);
			if (visualComponent != null)
				root.add(visualComponent);
		}

		for (Connection connection : model.getConnections()) {
			VisualConnection visualConnection = (VisualConnection)createVisualClassFor(connection, VisualConnection.class);
			if (visualConnection != null)
				root.add(visualConnection);
		}
	}

	public VisualAbstractGraphModel(AbstractGraphModel model, Element visualElement) throws VisualModelConstructionException {
		this.model = model;

		// load structure from XML
		NodeList nodes = visualElement.getElementsByTagName("group");

		if (nodes.getLength() != 1)
			throw new VisualModelConstructionException ("<visual> section of the document must contain one, and only one root group");

		root = new VisualComponentGroup ((Element)nodes.item(0), model);
	}

	public void toXML(Element xmlVisualElement) {
		// create root group element
		Element rootGroupElement = xmlVisualElement.getOwnerDocument().createElement("group");
		root.toXML(rootGroupElement);
		xmlVisualElement.appendChild(rootGroupElement);
	}
}