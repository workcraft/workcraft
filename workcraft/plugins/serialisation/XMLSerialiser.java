package org.workcraft.plugins.serialisation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.plugins.PluginConsumer;
import org.workcraft.framework.plugins.PluginProvider;
import org.workcraft.framework.serialisation.Format;
import org.workcraft.framework.serialisation.ModelSerialiser;
import org.workcraft.framework.serialisation.ReferenceProducer;
import org.workcraft.framework.serialisation.xml.XMLSerialisationManager;
import org.workcraft.util.XmlUtil;

public class XMLSerialiser implements ModelSerialiser, PluginConsumer {
	XMLSerialisationManager serialisation = new XMLSerialisationManager();

	private Element serialise(Node node, Document doc,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences) throws SerialisationException {
		Element e = doc.createElement("node");
		e.setAttribute("class", node.getClass().getName());

		serialisation.serialise(e, node, internalReferences, externalReferences);

		/* OLD WAY
		 *
		 * if (node instanceof XMLSerialisable)
			((XMLSerialisable)node).serialise(e, refResolver);*/

		if (node instanceof Container)
			for (Node child : node.getChildren())
				e.appendChild(serialise(child, doc, internalReferences, externalReferences));

		return e;
	}

	public String getDescription() {
		return "Workcraft XML serialiser";
	}

	public boolean isApplicableTo(Model model) {
		return true;
	}

	public boolean isApplicableTo(VisualModel model) {
		return true;
	}

	public UUID getFormatUUID() {
		return Format.workcraftXML;
	}

	public String getExtension() {
		return ".xml";
	}

	public ReferenceProducer export(final Model model, OutputStream out, ReferenceProducer externalReferences)
	throws SerialisationException {
		try{
			Document doc = XmlUtil.createDocument();

			Element root = doc.createElement("model");
			root.setAttribute("class", model.getClass().getName());

			ReferenceProducer internalReferences = new ReferenceProducer() {
				public String getReference(Object obj) {
					if (obj instanceof Node)
						return Integer.toString(model.getNodeID((Node)obj));
					else
						return null;
				}
			};

			serialisation.serialise(root, model, internalReferences, externalReferences);

			/*  OLD WAY
			 *
			 *  if (model instanceof XMLSerialisable)
				((XMLSerialisable)model).serialise(root, refResolver); */

			doc.appendChild(root);
			root.appendChild(serialise(model.getRoot(), doc, internalReferences, externalReferences));
			XmlUtil.writeDocument(doc, out);

			return internalReferences;
		} catch (ParserConfigurationException e) {
			throw new SerialisationException(e);
		} catch (IOException e) {
			throw new SerialisationException(e);
		}
	}

	public void processPlugins(PluginProvider pluginManager) {
		serialisation.processPlugins(pluginManager);
	}
}