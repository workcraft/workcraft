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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;

public class DefaultNodeSerialiser {
	private SerialiserFactory fac;
	private NodeSerialiser serialiser;

	public DefaultNodeSerialiser(SerialiserFactory factory, NodeSerialiser serialiser) {
		this.fac = factory;
		this.serialiser = serialiser;
	}

	private void autoSerialiseProperties(Element element, Object object, Class<?> currentLevel) throws IntrospectionException, InstantiationException, IllegalAccessException, IllegalArgumentException, SerialisationException, InvocationTargetException {
		// type explicitly requested to be excluded from auto serialisation
		if (currentLevel.getAnnotation(NoAutoSerialisation.class) != null)
			return;

		BeanInfo info = Introspector.getBeanInfo(currentLevel, currentLevel.getSuperclass());

		for (PropertyDescriptor desc : info.getPropertyDescriptors())
		{
			if (desc.getPropertyType() == null)
				continue;

			if (desc.getWriteMethod() == null || desc.getReadMethod() == null)
				continue;

			// property explicitly requested to be excluded from auto serialisation
			if (
					desc.getReadMethod().getAnnotation(NoAutoSerialisation.class) != null ||
					desc.getWriteMethod().getAnnotation(NoAutoSerialisation.class) != null
					)
				continue;

			// the property is writable and is not of array type, try to get a serialiser
			XMLSerialiser serialiser = fac.getSerialiserFor(desc.getPropertyType());

			if (!(serialiser != null  && serialiser instanceof BasicXMLSerialiser))
			{
				// no serialiser, try to use the special case enum serialiser
				if (desc.getPropertyType().isEnum())
				{
					serialiser = fac.getSerialiserFor(Enum.class);
					if (serialiser == null)
						continue;
				} else
					continue;
			}

			Element propertyElement = element.getOwnerDocument().createElement("property");
			element.appendChild(propertyElement);
			propertyElement.setAttribute("class", desc.getPropertyType().getName());
			propertyElement.setAttribute("name", desc.getName());

			((BasicXMLSerialiser)serialiser).serialise(propertyElement, desc.getReadMethod().invoke(object));
		}
	}

	private void doSerialisation(Element parentElement, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences, Class<?> currentLevel)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, IntrospectionException,
			SerialisationException, InvocationTargetException {

		Element curLevelElement = parentElement.getOwnerDocument()
				.createElement(currentLevel.getSimpleName());


		autoSerialiseProperties(curLevelElement, object, currentLevel);

		XMLSerialiser serialiser = fac.getSerialiserFor(currentLevel);

		if (serialiser != null)
			if (serialiser instanceof BasicXMLSerialiser)
				((BasicXMLSerialiser)serialiser).serialise(curLevelElement, object);
			else if (serialiser instanceof CustomXMLSerialiser)
				((CustomXMLSerialiser)serialiser).serialise(curLevelElement, object, internalReferences, externalReferences, this.serialiser);

		if (curLevelElement.getAttributes().getLength() > 0 || curLevelElement.getChildNodes().getLength() > 0)
			parentElement.appendChild(curLevelElement);

		if (currentLevel.getSuperclass() != Object.class)
			doSerialisation(parentElement, object, internalReferences, externalReferences, currentLevel.getSuperclass());
	}

	public void serialise(Element parentElement, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences) throws SerialisationException {
		try {
			doSerialisation(parentElement, object, internalReferences, externalReferences, object.getClass());

			parentElement.setAttribute("ref", internalReferences.getReference(object));
		} catch (IllegalArgumentException e) {
			throw new SerialisationException(e);
		} catch (InstantiationException e) {
			throw new SerialisationException(e);
		} catch (IllegalAccessException e) {
			throw new SerialisationException(e);
		} catch (IntrospectionException e) {
			throw new SerialisationException(e);
		} catch (SerialisationException e) {
			throw new SerialisationException(e);
		} catch (InvocationTargetException e) {
			throw new SerialisationException(e);
		}
	}
}
