package org.workcraft.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.Dependent;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.reflection.ConstructorParametersMatcher;
import org.workcraft.utils.XmlUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

class DefaultNodeDeserialiser {
    private final DeserialiserFactory fac;
    private final NodeInitialiser initialiser;
    private final NodeFinaliser finaliser;

    DefaultNodeDeserialiser(DeserialiserFactory factory, NodeInitialiser initialiser, NodeFinaliser finaliser) {
        this.fac = factory;
        this.initialiser = initialiser;
        this.finaliser = finaliser;
    }

    private void autoDeserialiseProperties(Element currentLevelElement, Object instance, Class<?> currentLevel)
            throws DeserialisationException {

        if (currentLevel.getAnnotation(NoAutoSerialisation.class) != null) {
            return;
        }

        try {
            List<Element> propertyElements = XmlUtils.getChildElements("property", currentLevelElement);
            HashMap<String, Element> nameMap = new HashMap<>();

            for (Element e : propertyElements) {
                nameMap.put(e.getAttribute("name"), e);
            }

            BeanInfo info = BeanInfoCache.getBeanInfo(currentLevel);

            for (PropertyDescriptor desc : info.getPropertyDescriptors()) {
                if (nameMap.containsKey(desc.getName()) && needDeserialisation(desc)) {
                    // The property is writable and is not of array type, try to get a deserialiser.
                    XMLDeserialiser deserialiser = fac.getDeserialiserFor(desc.getPropertyType().getName());
                    if (!(deserialiser instanceof BasicXMLDeserialiser) && desc.getPropertyType().isEnum()) {
                        // No basic deserialiser, try to use the special case enum deserialiser
                        deserialiser = fac.getDeserialiserFor(Enum.class.getName());
                    }
                    if (deserialiser instanceof BasicXMLDeserialiser<?> basicDeserialiser) {
                        Element element = nameMap.get(desc.getName());
                        Object value = basicDeserialiser.deserialise(element);
                        desc.getWriteMethod().invoke(instance, value);
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException | IntrospectionException e) {
            throw new DeserialisationException(e);
        } catch (InvocationTargetException e) {
            throw new DeserialisationException(instance.getClass().getName() + ' ' + currentLevel.getName() + ' ' + e.getMessage(), e);
        }
    }

    private boolean needDeserialisation(PropertyDescriptor desc) {
        if (desc.getPropertyType() == null) {
            return false;
        }
        Method writeMethod = desc.getWriteMethod();
        if ((writeMethod != null) && (writeMethod.getAnnotation(ForceDeserialisation.class) != null)) {
            return true;
        }
        Method readMethod = desc.getReadMethod();
        if ((writeMethod == null) || (readMethod == null)) {
            return false;
        }
        return (writeMethod.getAnnotation(NoAutoSerialisation.class) == null)
                && (readMethod.getAnnotation(NoAutoSerialisation.class) == null);
    }

    public Object initInstance(Element element, ReferenceResolver externalReferenceResolver, Object... constructorParameters)
            throws DeserialisationException {

        String className = element.getAttribute("class");
        if (className.isEmpty()) {
            throw new DeserialisationException("Class name attribute is not set\n" + element);
        }

        try {
            Class<?> cls = Class.forName(className);
            String shortClassName = cls.getSimpleName();
            Element currentLevelElement = XmlUtils.getChildElement(shortClassName, element);
            Object instance;

            // Check for a custom deserialiser first
            XMLDeserialiser deserialiser = fac.getDeserialiserFor(className);

            if (deserialiser instanceof CustomXMLDeserialiser<?> customDeserialiser) {
                instance = customDeserialiser.createInstance(currentLevelElement, externalReferenceResolver, constructorParameters);
            } else if (deserialiser instanceof BasicXMLDeserialiser<?> basicDeserialiser) {
                instance = basicDeserialiser.deserialise(currentLevelElement);
            } else {
                // Check for incoming parameters - these may be supplied when a custom deserialiser requests
                // a sub-node to be deserialised which should know how to construct this class and pass
                // the proper constructor arguments
                if (constructorParameters.length != 0) {
                    Class<?>[] parameterTypes = new Class<?>[constructorParameters.length];
                    for (int i = 0; i < constructorParameters.length; i++) {
                        parameterTypes[i] = constructorParameters[i].getClass();
                    }
                    Constructor<?> ctor = new ConstructorParametersMatcher().match(Class.forName(className), parameterTypes);
                    instance = ctor.newInstance(constructorParameters);
                } else {
                    // Still don't know how to deserialise the class.
                    // Let's see if it is a dependent node.
                    if (Dependent.class.isAssignableFrom(cls)) {
                        // Check for the simple case when there is only one reference to the underlying model.
                        String ref = currentLevelElement.getAttribute("ref");
                        if (ref.isEmpty()) {
                            // Bad luck, we probably can't do anything.
                            // But let's try a default constructor just in case.
                            instance = cls.getDeclaredConstructor().newInstance();
                        } else {
                            // Hooray, we've got a reference, so there is likely an appropriate constructor.
                            Object refObject = externalReferenceResolver.getObject(ref);
                            Constructor<?> ctor = new ConstructorParametersMatcher().match(cls, refObject.getClass());
                            instance = ctor.newInstance(refObject);
                        }
                    } else {
                        // It is not a dependent node, so there should be a default constructor.
                        instance = cls.getDeclaredConstructor().newInstance();
                    }
                }
            }

            doInitialisation(element, instance, instance.getClass(), externalReferenceResolver);
            return instance;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException |
                NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            throw new DeserialisationException(e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void doInitialisation(Element element, Object instance, Class<?> currentLevel, ReferenceResolver externalReferenceResolver)
            throws DeserialisationException {

        Element currentLevelElement = XmlUtils.getChildElement(currentLevel.getSimpleName(), element);
        if (currentLevelElement != null) {
            autoDeserialiseProperties(currentLevelElement, instance, currentLevel);
        }

        try {
            XMLDeserialiser deserialiser = fac.getDeserialiserFor(currentLevel.getName());
            if (deserialiser instanceof CustomXMLDeserialiser customDeserialiser) {
                customDeserialiser.initInstance(currentLevelElement, instance, externalReferenceResolver, initialiser);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DeserialisationException(e);
        }

        if (currentLevel.getSuperclass() != Object.class) {
            doInitialisation(element, instance, currentLevel.getSuperclass(), externalReferenceResolver);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void doFinalisation(Element element, Object instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, Class<?> currentLevel)
            throws DeserialisationException {

        Element currentLevelElement = XmlUtils.getChildElement(currentLevel.getSimpleName(), element);
        if (currentLevelElement != null) {
            try {
                XMLDeserialiser deserialiser = fac.getDeserialiserFor(currentLevel.getName());
                if (deserialiser instanceof CustomXMLDeserialiser customDeserialiser) {
                    customDeserialiser.finaliseInstance(currentLevelElement, instance,
                            internalReferenceResolver, externalReferenceResolver, finaliser);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new DeserialisationException(e);
            }
        }
        if (currentLevel.getSuperclass() != Object.class) {
            doFinalisation(element, instance, internalReferenceResolver, externalReferenceResolver, currentLevel.getSuperclass());
        }
    }

    public void finaliseInstance(Element element, Object instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver) throws DeserialisationException {

        doFinalisation(element, instance, internalReferenceResolver, externalReferenceResolver, instance.getClass());
    }

}
