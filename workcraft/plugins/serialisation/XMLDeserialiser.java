package org.workcraft.plugins.serialisation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Container;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.IntIdentifiable;
import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.plugins.PluginConsumer;
import org.workcraft.framework.plugins.PluginProvider;
import org.workcraft.framework.serialisation.DeserialisationResult;
import org.workcraft.framework.serialisation.Format;
import org.workcraft.framework.serialisation.ModelDeserialiser;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.framework.serialisation.xml.XMLSerialisationManager;
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

	XMLSerialisationManager serialisation = new XMLSerialisationManager();
	HashMap <Object, Element> instances = new HashMap<Object, Element>();
	InternalRefrenceResolver internalReferenceResolver;

	private Object initInstance(Element element) throws DeserialisationException {
		Object instance = serialisation.initInstance(element);
		instances.put(instance, element);

		if (instance instanceof IntIdentifiable)
			internalReferenceResolver.addObject(instance, Integer.toString(((IntIdentifiable)instance).getID()));

		if (instance instanceof Container) {
			for (Element subNodeElement : XmlUtil.getChildElements("node", element)) {
				Object subNode = initInstance (subNodeElement);
				if (subNode instanceof HierarchyNode)
					((Container)instance).add((HierarchyNode)subNode);
			}
		}

		return instance;
	}

	public DeserialisationResult deserialise(InputStream inputStream,
			ReferenceResolver externalReferenceResolver)
	throws DeserialisationException {
		try {
			internalReferenceResolver = new InternalRefrenceResolver();

			Document doc = XmlUtil.loadDocument(inputStream);
			Element modelElement = doc.getDocumentElement();

			// 1st pass -- init instances
			Model model = (Model)serialisation.initInstance(modelElement);
			Element rootElement = XmlUtil.getChildElement("node", modelElement);
			HierarchyNode root = (HierarchyNode) initInstance(rootElement);

			// 2nd pass -- finalise instances
			for (Object o : instances.keySet())
				serialisation.finalise(instances.get(o), o, internalReferenceResolver, externalReferenceResolver);

			model.setRoot(root);

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
		serialisation.processPlugins(pluginManager);
	}
}