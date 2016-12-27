package org.workcraft;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelInstantiationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.util.ConstructorParametersMatcher;

public class ModelFactory {
    public static Model createModel(String className) throws ModelInstantiationException {
        try {
            Class<?> modelClass = Class.forName(className);
            Constructor<?> ctor = modelClass.getConstructor();
            return (Model) ctor.newInstance();
        } catch (IllegalArgumentException | SecurityException | InstantiationException |
                IllegalAccessException | InvocationTargetException |
                NoSuchMethodException | ClassNotFoundException e) {
            throw new ModelInstantiationException(e);
        }
    }

    public static VisualModel createVisualModel(Model model) throws VisualModelInstantiationException {
        // Find the corresponding visual class
        VisualClass vcat = model.getClass().getAnnotation(VisualClass.class);

        // The component/connection does not define a visual representation
        if (vcat == null) {
            return null;
        }

        try {
            Class<?> visualClass = vcat.value();
            Constructor<?> ctor = new ConstructorParametersMatcher().match(visualClass, model.getClass());
            Object visual = ctor.newInstance(model);

            if (!VisualModel.class.isAssignableFrom(visual.getClass())) {
                throw new VisualModelInstantiationException("visual class " + visual.getClass().getName() +
                        ", created for object of class " + model.getClass().getName() + ", is not inherited from "
                        + VisualModel.class.getName());
            }

            return (VisualModel) visual;

        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException |
                InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new VisualModelInstantiationException(e);
        }
    }

}
