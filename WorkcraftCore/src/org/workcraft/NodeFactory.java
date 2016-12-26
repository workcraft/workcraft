package org.workcraft;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Node;
import org.workcraft.dom.VisualComponentGeneratorAttribute;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.util.ConstructorParametersMatcher;

public class NodeFactory {

    public static VisualConnection createVisualConnection(MathConnection connection)
            throws NodeCreationException {

        // Find the corresponding visual class
        VisualClass vcat = connection.getClass().getAnnotation(VisualClass.class);

        // The component/connection does not define a visual representation
        if (vcat == null) {
            return null;
        }

        try {
            Class<?> visualClass = vcat.value();
            Constructor<?> ctor = visualClass.getConstructor();
            return (VisualConnection) ctor.newInstance();

        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException |
                InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new NodeCreationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> T createNode(Class<T> cls) throws NodeCreationException {
        try {
            Constructor<?> ctor = cls.getConstructor();
            return (T) ctor.newInstance();
        } catch (ClassCastException | SecurityException | NoSuchMethodException |
                IllegalArgumentException | InstantiationException |
                IllegalAccessException | InvocationTargetException e) {
            throw new NodeCreationException(e);
        }
    }

    public static VisualComponent createVisualComponent(MathNode component) throws NodeCreationException {
        return createVisualComponentInternal(component);
    }

    private static VisualComponent createVisualComponentInternal(MathNode component, Object ... constructorParameters) throws NodeCreationException {
        VisualComponentGeneratorAttribute generator = component.getClass().getAnnotation(VisualComponentGeneratorAttribute.class);
        if (generator != null) {
            try {
                return ((org.workcraft.dom.VisualComponentGenerator) Class.forName(generator.generator())
                        .getConstructor().newInstance())
                        .createComponent(component, constructorParameters);
            } catch (Exception e) {
                throw new NodeCreationException(e);
            }
        } else {
            return createVisualComponentSimple(component, constructorParameters);
        }
    }

    private static VisualComponent createVisualComponentSimple(MathNode component, Object ... constructorParameters) throws NodeCreationException {
        // Find the corresponding visual class
        VisualClass vcat = component.getClass().getAnnotation(VisualClass.class);

        // The component/connection does not define a visual representation
        if (vcat == null) {
            return null;
        }
        try {
            Class<?> visualClass = vcat.value();

            Object[] args = new Object[constructorParameters.length + 1];
            args[0] = component;
            for (int i = 0; i < constructorParameters.length; i++) {
                args[i + 1] = constructorParameters[i];
            }
            Class<?>[] types = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i].getClass();
            }
            Constructor<?> ctor = new ConstructorParametersMatcher().match(visualClass, types);
            return (VisualComponent) ctor.newInstance(args);

        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException |
                InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new NodeCreationException(e);
        }
    }

}
