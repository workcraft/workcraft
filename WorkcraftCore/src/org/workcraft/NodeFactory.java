/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.VisualComponentGeneratorAttribute;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.util.ConstructorParametersMatcher;

public class NodeFactory {

	public static VisualConnection createVisualConnection (MathConnection connection)
	throws NodeCreationException {

		// Find the corresponding visual class
		VisualClass vcat = connection.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = vcat.value();
			Constructor<?> ctor = visualClass.getConstructor();
			VisualConnection visual = (VisualConnection)ctor.newInstance();
			return visual;

		} catch (SecurityException e) {
			throw new NodeCreationException (e);
		} catch (NoSuchMethodException e) {
			throw new NodeCreationException (e);
		} catch (IllegalArgumentException e) {
			throw new NodeCreationException (e);
		} catch (InstantiationException e) {
			throw new NodeCreationException (e);
		} catch (IllegalAccessException e) {
			throw new NodeCreationException (e);
		} catch (InvocationTargetException e) {
			throw new NodeCreationException (e);
		}
	}

	public static MathNode createNode (Class<?> cls) throws NodeCreationException {
		try {
			Constructor<?> ctor = cls.getConstructor();
			MathNode component = (MathNode)ctor.newInstance();
			return component;
		} catch (ClassCastException ex) {
			throw new NodeCreationException (ex);
		}	catch (SecurityException ex) {
			throw new NodeCreationException (ex);
		} catch (NoSuchMethodException ex) {
			throw new NodeCreationException (ex);
		} catch (IllegalArgumentException ex) {
			throw new NodeCreationException (ex);
		} catch (InstantiationException ex) {
			throw new NodeCreationException (ex);
		} catch (IllegalAccessException ex) {
			throw new NodeCreationException (ex);
		} catch (InvocationTargetException ex) {
			throw new NodeCreationException (ex);
		}
	}
	public static MathNode createNode (String className) throws NodeCreationException {
		try {
			return createNode (Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new NodeCreationException(e);
		}
	}

	public static VisualComponent createVisualComponent (MathNode component) throws NodeCreationException {
		return createVisualComponentInternal(component);
	}

	public static VisualComponent createVisualComponentInternal (MathNode component, Object ... constructorParameters) throws NodeCreationException {
		VisualComponentGeneratorAttribute generator = component.getClass().getAnnotation(VisualComponentGeneratorAttribute.class);
		if(generator != null) {
			try {
				return ((org.workcraft.dom.VisualComponentGenerator)Class.forName(generator.generator()).
						getConstructor().newInstance()).
						createComponent(component, constructorParameters);
			} catch (Exception e) {
				throw new NodeCreationException (e);
			}
		} else {
			return createVisualComponentSimple(component, constructorParameters);
		}
	}

	private static VisualComponent createVisualComponentSimple (MathNode component, Object ... constructorParameters) throws NodeCreationException {
		// Find the corresponding visual class
		VisualClass vcat = component.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null) {
			return null;
		}
		try {
			Class<?> visualClass = vcat.value();

			Object [] args = new Object[constructorParameters.length+1];
			args[0] = component;
			for(int i=0;i<constructorParameters.length;i++) {
				args[i+1] = constructorParameters[i];
			}
			Class <?> [] types = new Class <?> [args.length];
			for(int i=0;i<args.length;i++) {
				types[i] = args[i].getClass();
			}
			Constructor<?> ctor = new ConstructorParametersMatcher().match(visualClass, types);
			VisualComponent visual = (VisualComponent) ctor.newInstance(args);
			return visual;

		} catch (SecurityException e) {
			throw new NodeCreationException (e);
		} catch (NoSuchMethodException e) {
			throw new NodeCreationException (e);
		} catch (IllegalArgumentException e) {
			throw new NodeCreationException (e);
		} catch (InstantiationException e) {
			throw new NodeCreationException (e);
		} catch (IllegalAccessException e) {
			throw new NodeCreationException (e);
		} catch (InvocationTargetException e) {
			throw new NodeCreationException (e);
		}
	}

}
