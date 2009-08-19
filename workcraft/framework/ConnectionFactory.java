package org.workcraft.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.dom.ReferenceResolver;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualReferenceResolver;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.exceptions.ConnectionCreationException;
import org.workcraft.framework.exceptions.VisualConnectionCreationException;
import org.workcraft.framework.util.ConstructorParametersMatcher;

public class ConnectionFactory {

	public static Connection createConnection (Element element, ReferenceResolver referenceResolver) throws ConnectionCreationException {
		String className = element.getAttribute("class");
		try {
			Class<?> elementClass = Class.forName(className);
			Constructor<?> ctor = elementClass.getConstructor(Element.class, ReferenceResolver.class);
			Connection connection = (Connection)ctor.newInstance(element, referenceResolver);
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

	public static VisualConnection createVisualConnection (Element element, VisualReferenceResolver referenceResolver)
		throws VisualConnectionCreationException {
		String className = element.getAttribute("class");

		try {
			Class<?> connectionClass = Class.forName(className);
			Constructor<?> ctor = connectionClass.getConstructor(Element.class, VisualReferenceResolver.class);
			VisualConnection connection = (VisualConnection)ctor.newInstance(element, referenceResolver);
			return connection;

		} catch (ClassNotFoundException e) {
			throw new VisualConnectionCreationException (e);
		} catch (SecurityException e) {
			throw new VisualConnectionCreationException (e);
		} catch (NoSuchMethodException e) {
			throw new VisualConnectionCreationException (e);
		} catch (IllegalArgumentException e) {
			throw new VisualConnectionCreationException (e);
		} catch (InstantiationException e) {
			throw new VisualConnectionCreationException (e);
		} catch (IllegalAccessException e) {
			throw new VisualConnectionCreationException (e);
		} catch (InvocationTargetException e) {
			throw new VisualConnectionCreationException (e);
		}
	}



	public static VisualConnection createVisualConnection (Connection connection, VisualReferenceResolver referenceResolver)
		throws VisualConnectionCreationException {

		VisualComponent first = referenceResolver.getVisualComponentByID(connection.getFirst().getID());
		VisualComponent second = referenceResolver.getVisualComponentByID(connection.getSecond().getID());


		// Find the corresponding visual class
		VisualClass vcat = connection.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = new ConstructorParametersMatcher().match(visualClass, connection.getClass(), first.getClass(), second.getClass());
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
					"(" + connection.getClass().getName() +"," + first.getClass().getName() +"," + second.getClass().getName() + ")" );
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
