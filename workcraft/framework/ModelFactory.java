package org.workcraft.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.workcraft.dom.Model;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.ModelInstantiationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.framework.util.ConstructorParametersMatcher;

public class ModelFactory {
	public static Model createModel (String className) throws ModelInstantiationException {
		try{
			Class<?> modelClass = Class.forName(className);
			Constructor<?> ctor = modelClass.getConstructor();
			Model model = (Model)ctor.newInstance();
			return model;
		} catch (IllegalArgumentException e) {
			throw new ModelInstantiationException(e);
		} catch (SecurityException e) {
			throw new ModelInstantiationException(e);
		} catch (InstantiationException e) {
			throw new ModelInstantiationException(e);
		} catch (IllegalAccessException e) {
			throw new ModelInstantiationException(e);
		} catch (InvocationTargetException e) {
			throw new ModelInstantiationException(e);
		} catch (NoSuchMethodException e) {
			throw new ModelInstantiationException(e);
		} catch (ClassNotFoundException e) {
			throw new ModelInstantiationException(e);
		}
	}


	public static VisualModel createVisualModel (Model model) throws VisualModelInstantiationException {
		// Find the corresponding visual class
		VisualClass vcat = model.getClass().getAnnotation(VisualClass.class);

		// The component/connection does not define a visual representation
		if (vcat == null)
			return null;

		try {
			Class<?> visualClass = Class.forName(vcat.value());
			Constructor<?> ctor = new ConstructorParametersMatcher().match(visualClass, model.getClass());
			Object visual = ctor.newInstance(model);

			if (!VisualModel.class.isAssignableFrom(visual.getClass()))
				throw new VisualModelInstantiationException ("visual class " + visual.getClass().getName() +
						", created for object of class " + model.getClass().getName() + ", is not inherited from "
						+ VisualModel.class.getName());

			return (VisualModel)visual;

		} catch (ClassNotFoundException e) {
			throw new VisualModelInstantiationException (e);
		} catch (SecurityException e) {
			throw new VisualModelInstantiationException (e);
		} catch (NoSuchMethodException e) {
			throw new VisualModelInstantiationException (e);
		} catch (IllegalArgumentException e) {
			throw new VisualModelInstantiationException (e);
		} catch (InstantiationException e) {
			throw new VisualModelInstantiationException (e);
		} catch (IllegalAccessException e) {
			throw new VisualModelInstantiationException (e);
		} catch (InvocationTargetException e) {
			throw new VisualModelInstantiationException (e);
		}
	}

}
