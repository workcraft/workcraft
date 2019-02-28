package org.workcraft.serialisation;

import static org.workcraft.serialisation.BeanInfoCache.getBeanInfo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.w3c.dom.Element;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Dependent;
import org.workcraft.exceptions.SerialisationException;

public class DefaultNodeSerialiser {
    private final SerialiserFactory fac;
    private final NodeSerialiser serialiser;

    public DefaultNodeSerialiser(SerialiserFactory factory, NodeSerialiser serialiser) {
        this.fac = factory;
        this.serialiser = serialiser;
    }

    private void autoSerialiseProperties(Element element, Object object, Class<?> currentLevel) throws IntrospectionException, InstantiationException, IllegalAccessException, IllegalArgumentException, SerialisationException, InvocationTargetException {
        // type explicitly requested to be excluded from auto serialisation
        if (currentLevel.getAnnotation(NoAutoSerialisation.class) != null) {
            return;
        }

        BeanInfo info = getBeanInfo(currentLevel);

        for (PropertyDescriptor desc : info.getPropertyDescriptors()) {
            if (desc.getPropertyType() == null) {
                continue;
            }

            if (desc.getWriteMethod() == null || desc.getReadMethod() == null) {
                continue;
            }

            // property explicitly requested to be excluded from auto serialisation
            if (
                    desc.getReadMethod().getAnnotation(NoAutoSerialisation.class) != null ||
                    desc.getWriteMethod().getAnnotation(NoAutoSerialisation.class) != null) {
                continue;
            }

            // the property is writable and is not of array type, try to get a serialiser
            XMLSerialiser serialiser = fac.getSerialiserFor(desc.getPropertyType());

            if (!(serialiser instanceof BasicXMLSerialiser)) {
                // no serialiser, try to use the special case enum serialiser
                if (desc.getPropertyType().isEnum()) {
                    serialiser = fac.getSerialiserFor(Enum.class);
                    if (serialiser == null) {
                        continue;
                    }
                } else {
                    continue;
                }
            }

            Element propertyElement = element.getOwnerDocument().createElement("property");
            element.appendChild(propertyElement);
            propertyElement.setAttribute("class", desc.getPropertyType().getName());
            propertyElement.setAttribute("name", desc.getName());

            ((BasicXMLSerialiser) serialiser).serialise(propertyElement, desc.getReadMethod().invoke(object));
        }
    }

    private void doSerialisation(Element parentElement, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, Class<?> currentLevel)
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, IntrospectionException,
            SerialisationException, InvocationTargetException {

        Element curLevelElement = parentElement.getOwnerDocument().createElement(currentLevel.getSimpleName());

        autoSerialiseProperties(curLevelElement, object, currentLevel);

        XMLSerialiser serialiser = fac.getSerialiserFor(currentLevel);

        if (serialiser != null) {
            if (serialiser instanceof BasicXMLSerialiser) {
                ((BasicXMLSerialiser) serialiser).serialise(curLevelElement, object);
            } else if (serialiser instanceof CustomXMLSerialiser) {
                ((CustomXMLSerialiser) serialiser).serialise(curLevelElement, object, internalReferences, externalReferences, this.serialiser);
            }
        } else {
            if (object.getClass().equals(currentLevel) && (object instanceof Dependent)) {
                Collection<MathNode> refs = ((Dependent) object).getMathReferences();
                if (refs.size() == 1) {
                    curLevelElement.setAttribute("ref", externalReferences.getReference(refs.iterator().next()));
                }
            }
        }

        if (curLevelElement.getAttributes().getLength() > 0 || curLevelElement.getChildNodes().getLength() > 0) {
            parentElement.appendChild(curLevelElement);
        }

        if (currentLevel.getSuperclass() != Object.class) {
            doSerialisation(parentElement, object, internalReferences, externalReferences, currentLevel.getSuperclass());
        }
    }

    public void serialise(Element parentElement, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences) throws SerialisationException {
        try {
            doSerialisation(parentElement, object, internalReferences, externalReferences, object.getClass());

            parentElement.setAttribute("ref", internalReferences.getReference(object));
        } catch (IllegalArgumentException | InstantiationException | IllegalAccessException |
                IntrospectionException | SerialisationException | InvocationTargetException e) {
            throw new SerialisationException(e);
        }
    }
}
