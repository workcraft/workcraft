package org.workcraft.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.ModelInstantiationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;

public class ModelFactory {
	public static MathModel createModel (Element modelElement) throws ModelInstantiationException {
		try{
			String className = modelElement.getAttribute("class");
			Class<?> modelClass = Class.forName(className);
			Constructor<?> ctor = modelClass.getConstructor(Element.class);
			return (MathModel)ctor.newInstance(modelElement);
		} catch (IllegalArgumentException e) {
			throw new ModelInstantiationException("Cannot instantiate model: \n" + e.getMessage());
		} catch (SecurityException e) {
			throw new ModelInstantiationException("Cannot instantiate model: \n" + e.getMessage());
		} catch (InstantiationException e) {
			throw new ModelInstantiationException("Cannot instantiate model: \n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new ModelInstantiationException("Cannot instantiate model: \n" + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new ModelInstantiationException("Cannot instantiate model: \n" + e.getTargetException().getMessage());
		} catch (NoSuchMethodException e) {
			throw new ModelInstantiationException("Cannot instantiate model: missing constructor: \n" + e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new ModelInstantiationException("Cannot instatniate model: \n" + e.getMessage());
		}
	}


	public static VisualModel createVisualModel (MathModel model) throws VisualModelInstantiationException {
		// Find the corresponding visual class
		VisualClass vcat = model.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor(model.getClass());
			Object visual = ctor.newInstance(model);

			if (!VisualModel.class.isAssignableFrom(visual.getClass()))
				throw new VisualModelInstantiationException ("visual class " + visual.getClass().getName() +
						", created for object of class " + model.getClass().getName() + ", is not inherited from "
						+ VisualModel.class.getName());

			return (VisualModel)visual;

		} catch (ClassNotFoundException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be loaded for class " + model.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception:\n" + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualModelInstantiationException("visual class " + vcat.value() +
					" does not declare the required constructor: \n" + vcat.value() +
					"(" + model.getClass().getName() +")" );
		} catch (IllegalArgumentException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: \n" + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: \n" + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be instantiated: " + e.getTargetException().getMessage());
		}
	}

	public static VisualModel createVisualModel (MathModel model, Element xmlElement) throws VisualModelInstantiationException {
		// Find the corresponding visual class
		VisualClass vcat = model.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = visualClass.getConstructor(model.getClass(), Element.class);
			Object visual = ctor.newInstance(model, xmlElement);

			if (!VisualModel.class.isAssignableFrom(visual.getClass()))
				throw new VisualModelInstantiationException ("visual class " + visual.getClass().getName() +
						", created for object of class " + model.getClass().getName() + ", is not inherited from "
						+ VisualModel.class.getName());

			return (VisualModel)visual;

		} catch (ClassNotFoundException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be loaded for class " + model.getClass().getName());
		} catch (SecurityException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be instantiated due to security exception: \n " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new VisualModelInstantiationException("visual class " + vcat.value() +
					" does not declare the required constructor \n" + vcat.value() +
					"(" + model.getClass().getName() + ", " + Element.class.getName()+")" );
		} catch (IllegalArgumentException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be instantiated due to illegal argument exception: \n" + e.getMessage());
		} catch (InstantiationException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be instantiated due to inaccesibility of the constructor: \n " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new VisualModelInstantiationException ("visual class " + vcat.value() +
					" could not be instantiated: \n" + e.getTargetException().getMessage());
		}
	}
}
