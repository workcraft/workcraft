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
import org.workcraft.serialisation.xml.XMLSerialisationManager;
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
	HashMap <Object, Element> instances;
	// LinkedHashMap <Container, LinkedList<HierarchyNode>> children;

	InternalRefrenceResolver internalReferenceResolver;

	private Object initInstance(Element element, ReferenceResolver externalReferenceResolver) throws DeserialisationException {
		Object instance = serialisation.initInstance(element, externalReferenceResolver);
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
				serialisation.finalise(instances.get(o), o, internalReferenceResolver, externalReferenceResolver);

			// create model
			Model model = (Model)serialisation.createModel(modelElement, root, internalReferenceResolver,
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
		serialisation.processPlugins(pluginManager);
	}
}