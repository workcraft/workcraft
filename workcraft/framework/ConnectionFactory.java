package org.workcraft.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.workcraft.dom.Connection;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.exceptions.VisualConnectionCreationException;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.framework.util.ConstructorParametersMatcher;

public class ConnectionFactory {

	public static VisualConnection createVisualConnection (Connection connection, ReferenceResolver mathIDtoVisualObjectResolver)
		throws VisualConnectionCreationException {

		VisualComponent first = getReferencingVisualComponent(connection.getFirst().getID(), mathIDtoVisualObjectResolver);
		VisualComponent second = getReferencingVisualComponent(connection.getSecond().getID(), mathIDtoVisualObjectResolver);

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

	@SuppressWarnings("unchecked")
	private static VisualComponent getReferencingVisualComponent(int id,
			ReferenceResolver mathIDtoVisualObjectResolver)
			throws VisualConnectionCreationException {
		List<VisualNode> nodes = (List<VisualNode>) mathIDtoVisualObjectResolver.getObject(Integer.toString(id));

		if (nodes == null)
			throw new VisualConnectionCreationException ("no visual objects exist that reference math objects connected by the given math connection");

		VisualComponent vcomp = null;

		for (VisualNode n : nodes) {
			if (n instanceof VisualComponent) {
				vcomp = (VisualComponent)n;
				break;
			}
		}

		if (vcomp == null)
			throw new VisualConnectionCreationException ("no objects of type VisualComponents reference the math objects");

		return vcomp;
	}


}
