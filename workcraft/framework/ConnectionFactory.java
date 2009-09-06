package org.workcraft.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.workcraft.dom.VisualClass;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.exceptions.VisualConnectionCreationException;

public class ConnectionFactory {

	public static VisualConnection createVisualConnection (MathConnection connection)
		throws VisualConnectionCreationException {

		// Find the corresponding visual class
		VisualClass vcat = connection.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor();
			VisualConnection visual = (VisualConnection)ctor.newInstance();
			return visual;

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
}
