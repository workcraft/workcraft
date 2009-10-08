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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.workcraft.PluginInfo;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.util.ConstructorParametersMatcher;
import org.workcraft.util.XmlUtil;

public class XMLDeserialisationManager implements DeserialiserFactory, NodeInitialiser, NodeFinaliser {
	private HashMap<String, Class<? extends XMLDeserialiser>> deserialisers = new HashMap<String, Class<? extends XMLDeserialiser>>();
	private HashMap<String, XMLDeserialiser> deserialiserCache = new HashMap<String, XMLDeserialiser>();
	private DefaultNodeDeserialiser nodeDeserialiser = new DefaultNodeDeserialiser(this, this, this);

	private XMLDeserialiserState state = null;

	private void registerDeserialiser (Class<? extends XMLDeserialiser> cls) {
		XMLDeserialiser inst;
		try {
			inst = cls.newInstance();
			deserialisers.put(inst.getClassName(), cls);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	 public XMLDeserialiser getDeserialiserFor(String className) throws InstantiationException, IllegalAccessException {
		XMLDeserialiser deserialiser = deserialiserCache.get(className);

		if (deserialiser == null) {
			Class<? extends XMLDeserialiser> deserialiserClass = deserialisers.get(className);
			if (deserialiserClass != null)
			{
				deserialiser = deserialiserClass.newInstance();
				deserialiserCache.put(className, deserialiser);
			}
		}

		return deserialiser;
	}

	public void begin(ReferenceResolver externalReferenceResolver) {
		state = new XMLDeserialiserState(externalReferenceResolver);
	}

	public ReferenceResolver end() {
		XMLDeserialiserState result = state;
		state = null;
		return result;
	}

	@SuppressWarnings("unchecked")
	public void processPlugins(PluginProvider manager) {
		PluginInfo[] deserialiserInfos = manager.getPluginsImplementing(XMLDeserialiser.class.getName());

		for (PluginInfo info : deserialiserInfos) {
			try {
				registerDeserialiser( (Class<? extends XMLDeserialiser>) Class.forName(info.getClassName()));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public Object initInstance (Element element, Object ... constructorParameters) throws DeserialisationException
	{
		Object instance = nodeDeserialiser.initInstance(element, state.externalReferences, constructorParameters);

		state.setInstanceElement(instance, element);
		state.setObject(element.getAttribute("ref"), instance);

		if (instance instanceof Container) {
			for (Element subNodeElement : XmlUtil.getChildElements("node", element)) {
				Object subNode = initInstance (subNodeElement);

				 if (subNode instanceof Node)
					 state.addChildNode((Container)instance, (Node)subNode);
			}
		}
		return instance;
	}

	public Model createModel (Element element, Node root) throws DeserialisationException {

		String className = element.getAttribute("class");

		if (className == null || className.isEmpty())
			throw new DeserialisationException("Class name attribute is not set\n" + element.toString());

		Model result;
		Class<?> cls;

		try {
			org.workcraft.serialisation.xml.XMLDeserialiser deserialiser  = getDeserialiserFor(className);
			cls = Class.forName(className);

			if (deserialiser instanceof ModelXMLDeserialiser) {
				result = ((ModelXMLDeserialiser)deserialiser).deserialise(element, root, state.internalReferences, state.externalReferences);
			} else if (deserialiser != null) {
				throw new DeserialisationException ("Deserialiser for model class must implement ModelXMLDesiraliser interface");
			} else {
				Constructor<?> ctor = new ConstructorParametersMatcher().match(cls, root.getClass());
				result = (Model) ctor.newInstance(root);
			}

		} catch (InstantiationException e) {
			throw new DeserialisationException(e);
		} catch (IllegalAccessException e) {
			throw new DeserialisationException(e);
		} catch (ClassNotFoundException e) {
			throw new DeserialisationException(e);
		} catch (NoSuchMethodException e) {
			throw new DeserialisationException("In order to be deserialised automatically, the model must declare a constructor with parameter " + root.getClass(), e);
		} catch (IllegalArgumentException e) {
			throw new DeserialisationException(e);
		} catch (InvocationTargetException e) {
			throw new DeserialisationException(e);
		}

		nodeDeserialiser.doInitialisation(element, result, cls, state.externalReferences);
		nodeDeserialiser.doFinalisation(element, result, state.internalReferences, state.externalReferences, cls.getSuperclass());

		state.setObject("$model", result);

		return result;
	}

	public void finaliseInstances() throws DeserialisationException {
		// finalise all instances
		for (Object o : state.instanceElements.keySet())
			finaliseInstance(o);

		// now add children to their respective containers
		for (Object o : state.instanceElements.keySet()) {
			if (o instanceof Container) {
				Container c = (Container)o;
				c.add(state.getChildren(c));
			}
		}
	}

	public void finaliseInstance(Object instance) throws DeserialisationException {
		nodeDeserialiser.finaliseInstance(state.getInstanceElement(instance), instance, state.internalReferences, state.externalReferences);
	}
}
