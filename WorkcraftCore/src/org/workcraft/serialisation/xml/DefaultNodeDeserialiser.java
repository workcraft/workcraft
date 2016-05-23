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

package org.workcraft.serialisation.xml;

import static org.workcraft.serialisation.xml.BeanInfoCache.getBeanInfo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.Dependent;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.util.ConstructorParametersMatcher;
import org.workcraft.util.XmlUtil;

class DefaultNodeDeserialiser {
    private final DeserialiserFactory fac;
    private final NodeInitialiser initialiser;
    private final NodeFinaliser finaliser;

    DefaultNodeDeserialiser(DeserialiserFactory factory, NodeInitialiser initialiser, NodeFinaliser finaliser) {
        this.fac = factory;
        this.initialiser = initialiser;
        this.finaliser = finaliser;
    }

    private void autoDeserialiseProperties(Element currentLevelElement,
            Object instance, Class<?> currentLevel,
            ReferenceResolver externalReferenceResolver)
            throws DeserialisationException {
        if (currentLevel.getAnnotation(NoAutoSerialisation.class) != null) {
            return;
        }

        try {
            List<Element> propertyElements = XmlUtil.getChildElements("property", currentLevelElement);
            HashMap<String, Element> nameMap = new HashMap<>();

            for (Element e : propertyElements) {
                nameMap.put(e.getAttribute("name"), e);
            }

            BeanInfo info = getBeanInfo(currentLevel);

            for (PropertyDescriptor desc : info.getPropertyDescriptors()) {
                if (!nameMap.containsKey(desc.getName())) {
                    continue;
                }

                if (desc.getPropertyType() == null) {
                    continue;
                }

                if (desc.getWriteMethod() == null || desc.getReadMethod() == null) {
                    continue;
                }

                // property explicitly requested to be excluded from auto serialisation
                if (
                        desc.getReadMethod().getAnnotation(NoAutoSerialisation.class) != null ||
                        desc.getWriteMethod().getAnnotation(NoAutoSerialisation.class) != null
                ) {
                    continue;
                }

                // the property is writable and is not of array type, try to get a deserialiser
                XMLDeserialiser deserialiser = fac.getDeserialiserFor(desc.getPropertyType().getName());

                if (!(deserialiser instanceof BasicXMLDeserialiser)) {
                    // no deserialiser, try to use the special case enum deserialiser
                    if (desc.getPropertyType().isEnum()) {
                        deserialiser = fac.getDeserialiserFor(Enum.class.getName());
                        if (deserialiser == null) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }

                Element element = nameMap.get(desc.getName());
                Object value = ((BasicXMLDeserialiser) deserialiser).deserialise(element);

                desc.getWriteMethod().invoke(instance, value);
            }
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException |
                IntrospectionException e) {
            throw new DeserialisationException(e);
        } catch (InvocationTargetException e) {
            throw new DeserialisationException(instance.getClass().getName() + " " + currentLevel.getName() + " " + e.getMessage(), e);
        }
    }

    public Object initInstance(Element element, ReferenceResolver externalReferenceResolver, Object ... constructorParameters) throws DeserialisationException {
        String className = element.getAttribute("class");

        if (className == null || className.isEmpty()) {
            throw new DeserialisationException("Class name attribute is not set\n" + element.toString());
        }

        //System.out.println("Initialising " + className);

        try {
            Class<?> cls = Class.forName(className);
            String shortClassName = cls.getSimpleName();

            Element currentLevelElement = XmlUtil.getChildElement(shortClassName, element);

            Object instance;

            // Check for a custom deserialiser first
            XMLDeserialiser deserialiser = fac.getDeserialiserFor(className);

            if (deserialiser instanceof CustomXMLDeserialiser) {
                //System.out.println("Using custom deserialiser " + deserialiser);
                instance = ((CustomXMLDeserialiser) deserialiser).createInstance(currentLevelElement, externalReferenceResolver, constructorParameters);
            } else if (deserialiser instanceof BasicXMLDeserialiser) {
                //System.out.println("Using basic deserialiser " + deserialiser);
                instance = ((BasicXMLDeserialiser) deserialiser).deserialise(currentLevelElement);
            } else {
                //System.out.println("Using default deserialiser " + deserialiser);

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
                            instance = cls.newInstance();
                        } else {
                            // Hooray, we've got a reference, so there is likely an appropriate constructor.
                            Object refObject = externalReferenceResolver.getObject(ref);
                            Constructor<?> ctor = new ConstructorParametersMatcher().match(cls, refObject.getClass());
                            instance = ctor.newInstance(refObject);
                        }
                    } else {
                        // It is not a dependent node, so there should be a default constructor.
                        instance = cls.newInstance();
                    }
                }
            }

            //System.out.println("Result = " + instance);

            doInitialisation(element, instance, instance.getClass(), externalReferenceResolver);

            return instance;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException |
                NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            throw new DeserialisationException(e);
        }
    }

    void doInitialisation(Element element, Object instance, Class<?> currentLevel, ReferenceResolver externalReferenceResolver) throws DeserialisationException {
        Element currentLevelElement = XmlUtil.getChildElement(currentLevel.getSimpleName(), element);
        if (currentLevelElement != null) {
            autoDeserialiseProperties(currentLevelElement, instance, currentLevel, externalReferenceResolver);
        }

        try {
            XMLDeserialiser deserialiser = fac.getDeserialiserFor(currentLevel.getName());

            if (deserialiser instanceof CustomXMLDeserialiser) {
                ((CustomXMLDeserialiser) deserialiser).initInstance(currentLevelElement, instance, externalReferenceResolver, initialiser);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DeserialisationException(e);
        }

        if (currentLevel.getSuperclass() != Object.class) {
            doInitialisation(element, instance, currentLevel.getSuperclass(), externalReferenceResolver);
        }
    }

    void doFinalisation(Element element, Object instance,
            ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver,
            Class<?> currentLevel)
    throws DeserialisationException {
        Element currentLevelElement = XmlUtil.getChildElement(currentLevel.getSimpleName(), element);
        if (currentLevelElement != null) {
            try {
                XMLDeserialiser deserialiser = fac.getDeserialiserFor(currentLevel.getName());
                if (deserialiser instanceof CustomXMLDeserialiser) {
                    //System.out.println("Using custom deserialiser " + deserialiser);
                    ((CustomXMLDeserialiser) deserialiser).finaliseInstance(currentLevelElement, instance, internalReferenceResolver, externalReferenceResolver, finaliser);
                }
            } catch (InstantiationException e) {
                throw new DeserialisationException(e);
            } catch (IllegalAccessException e) {
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
