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

package org.workcraft.plugins.serialisation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.PluginConsumer;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.DeserialisationResult;
import org.workcraft.serialisation.Format;
import org.workcraft.serialisation.ModelDeserialiser;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.XMLDeserialisationManager;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class XMLDeserialiser implements ModelDeserialiser, PluginConsumer {
	class InternalRefrenceResolver implements ReferenceResolver	{
		HashMap<String, Object> map = new HashMap<String, Object>();

		public void addObject (Object obj, String reference) {
			map.put(reference, obj);
		}

		public Object getObject(String reference) {
			return map.get(reference);
		}
	};

	XMLDeserialisationManager deserialisation = new XMLDeserialisationManager();
	HashMap <Object, Element> instances;
	// LinkedHashMap <Container, LinkedList<HierarchyNode>> children;

	InternalRefrenceResolver internalReferenceResolver;

	private Object initInstance(Element element, ReferenceResolver externalReferenceResolver) throws DeserialisationException {
		Object instance = deserialisation.initInstance(element, externalReferenceResolver);
		instances.put(instance, element);

		internalReferenceResolver.addObject(instance, element.getAttribute("ref"));

		if (instance instanceof Container) {

			/* LinkedList<HierarchyNode> ch = children.get(instance);
			if (ch == null)
				ch = new LinkedList<HierarchyNode>(); */

			for (Element subNodeElement : XmlUtil.getChildElements("node", element)) {
				Object subNode = initInstance (subNodeElement, externalReferenceResolver);


				 if (subNode instanceof Node)
					 //ch.add((HierarchyNode)subNode);
					((Container)instance).add((Node)subNode);
			}
		}
		return instance;
	}



	public DeserialisationResult deserialise(InputStream inputStream,
			ReferenceResolver externalReferenceResolver)
	throws DeserialisationException {
		try {
			instances = new HashMap<Object, Element>();

			internalReferenceResolver = new InternalRefrenceResolver();

			Document doc = XmlUtil.loadDocument(inputStream);

			Element modelElement = doc.getDocumentElement();

			// 1st pass -- init instances
			Element rootElement = XmlUtil.getChildElement("node", modelElement);
			Node root = (Node) initInstance(rootElement, externalReferenceResolver);

			// 2nd pass -- finalise instances
			for (Object o : instances.keySet())
				deserialisation.finaliseInstance(instances.get(o), o, internalReferenceResolver, externalReferenceResolver);

			// create model
			Model model = (Model)deserialisation.createModel(modelElement, root, internalReferenceResolver,
					externalReferenceResolver);

			internalReferenceResolver.addObject(model, "$model");

			return new DeserialisationResult(model, internalReferenceResolver);
		} catch (ParserConfigurationException e) {
			throw new DeserialisationException(e);
		} catch (SAXException e) {
			throw new DeserialisationException(e);
		} catch (IOException e) {
			throw new DeserialisationException(e);
		} catch (SecurityException e) {
			throw new DeserialisationException(e);
		} catch (IllegalArgumentException e) {
			throw new DeserialisationException(e);
		}
	}

	public String getDescription() {
		return "Workcraft XML deserialiser";
	}

	public UUID getFormatUUID() {
		return Format.workcraftXML;
	}

	public void processPlugins(PluginProvider pluginManager) {
		deserialisation.processPlugins(pluginManager);
	}
}