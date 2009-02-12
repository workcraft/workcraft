package org.workcraft.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualConnection;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.ConnectionCreationException;
import org.workcraft.framework.exceptions.VisualConnectionCreationException;
import org.workcraft.util.XmlUtil;

public class ConnectionFactory {

	public static Connection createConnection (Element element, MathModel referenceModel) throws ConnectionCreationException {
		String className = element.getAttribute("class");
		try {
			Class<?> elementClass = Class.forName(className);
			Constructor<?> ctor = elementClass.getConstructor(Element.class, MathModel.class);
			Connection connection = (Connection)ctor.newInstance(element, referenceModel);
			return connection;
		} catch (ClassCastException ex) {
			throw new ConnectionCreationException ("Cannot cast the class \"" + className +"\" to org.workcraft.dom.Connection: " + ex.getMessage());
		}	catch (ClassNotFoundException ex) {
			throw new ConnectionCreationException ("Cannot load connection class: " + ex.getMessage());
		} catch (SecurityException ex) {
			throw new ConnectionCreationException ("Security exception: " + ex.getMessage());
		} catch (NoSuchMethodException ex) {
			throw new ConnectionCreationException ("Connection class \"" + className + "\" does not declare the required constructor "
					+ className + "(org.w3c.dom.Element, org.workcraft.dom.MathModel");
		} catch (IllegalArgumentException ex) {
			throw new ConnectionCreationException ("Connection class instantiation failed: " + ex.getMessage());
		} catch (InstantiationException ex) {
			throw new ConnectionCreationException ("Connection class instantiation failed: " + ex.getMessage());
		} catch (IllegalAccessException ex) {
			throw new ConnectionCreationException ("Connection class instantiation failed: " + ex.getMessage());
		} catch (InvocationTargetException ex) {
			throw new ConnectionCreationException ("Connection class instantiation failed: " + ex.getTargetException().getMessage());
		}
	}

	public static VisualConnection createVisualConnection (Element element, VisualModel referenceModel)
		throws VisualConnectionCreationException {
		int ref = XmlUtil.readIntAttr(element, "ref", -1);
		Connection connection = referenceModel.getMathModel().getConnectionByRenamedID(ref);

		VisualComponent first = referenceModel.getComponentByRefID(connection.getFirst().getID());
		VisualComponent second = referenceModel.getComponentByRefID(connection.getSecond().getID());

		// Find the corresponding visual class
		VisualClass vcat = connection.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor;
			try {
				ctor = visualClass.getConstructor(connection.getClass(), Element.class, first.getClass(),
						second.getClass());
			}
			catch (NoSuchMethodException e) {
				ctor = visualClass.getConstructor(Connection.class, Element.class, VisualComponent.class,
						VisualComponent.class);
			}
			VisualConnection visual = (VisualConnection)ctor.newInstance(connection, element, first, second);
			return visual;

		} catch (ClassNotFoundException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be loaded for class " + connection.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception:\n" + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualConnectionCreationException("visual class " + vcat.value() +
			" does not declare the required constructor. \n");
		} catch (IllegalArgumentException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: \n" + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: \n" + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}

	public static VisualConnection createVisualConnection (Connection connection, VisualModel referenceModel)
		throws VisualConnectionCreationException {

		VisualComponent first = referenceModel.getComponentByRefID(connection.getFirst().getID());
		VisualComponent second = referenceModel.getComponentByRefID(connection.getSecond().getID());

		// Find the corresponding visual class
		VisualClass vcat = connection.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor;

			try {
				ctor = visualClass.getConstructor(connection.getClass(), first.getClass(),
						second.getClass());
			}
			catch (NoSuchMethodException e) {
				ctor = visualClass.getConstructor(Connection.class, VisualComponent.class,
						VisualComponent.class);
			}

			VisualConnection visual = (VisualConnection)ctor.newInstance(connection, first, second);
			return visual;

		} catch (ClassNotFoundException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be loaded for class " + connection.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception:\n" + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualConnectionCreationException("visual class " + vcat.value() +
					" does not declare the required constructor: \n" + vcat.value() +
					"(" + connection.getClass().getName() +"," + Element.class.getName() + ")" );
		} catch (IllegalArgumentException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: \n" + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: \n" + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualConnectionCreationException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}
}
