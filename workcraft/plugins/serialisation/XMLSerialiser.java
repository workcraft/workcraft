package org.workcraft.plugins.serialisation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Container;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.framework.plugins.PluginConsumer;
import org.workcraft.framework.plugins.PluginManager;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.Format;
import org.workcraft.framework.serialisation.ModelSerialiser;
import org.workcraft.framework.serialisation.xml.XMLSerialisationManager;
import org.workcraft.util.XmlUtil;

public class XMLSerialiser implements ModelSerialiser, Plugin, PluginConsumer {
	XMLSerialisationManager serialisation = new XMLSerialisationManager();

	class ReferenceResolver implements ExternalReferenceResolver
	{
		public String getReference(Object obj) {
			if (obj instanceof MathNode)
				return Integer.toString(((MathNode)obj).getID());
			else
				return "(null)";
		}
	}

	private Element serialise(HierarchyNode node, Document doc, ExternalReferenceResolver inRef) throws SerialisationException {
		Element e = doc.createElement("node");
		e.setAttribute("class", node.getClass().getName());

		serialisation.serialise(e, node, inRef);

		/* OLD WAY
		 *
		 * if (node instanceof XMLSerialisable)
			((XMLSerialisable)node).serialise(e, refResolver);*/

		if (node instanceof Container)
			for (HierarchyNode child : node.getChildren())
				e.appendChild(serialise(child, doc, inRef));

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

	public ExternalReferenceResolver export(Model model, OutputStream out, ExternalReferenceResolver incomingReferenceResolver)
	throws SerialisationException {
		try{
			Document doc = XmlUtil.createDocument();

			Element root = doc.createElement("model");
			root.setAttribute("class", model.getClass().getName());

			serialisation.serialise(root, model, incomingReferenceResolver);

			/*  OLD WAY
			 *
			 *  if (model instanceof XMLSerialisable)
				((XMLSerialisable)model).serialise(root, refResolver); */

			doc.appendChild(root);

			root.appendChild(serialise(model.getRoot(), doc, incomingReferenceResolver));

			XmlUtil.writeDocument(doc, out);
		} catch (ParserConfigurationException e) {
			throw new SerialisationException(e);
		} catch (IOException e) {
			throw new SerialisationException(e);
		}
		return new ReferenceResolver();
	}

	public void processPlugins(PluginManager pluginManager) {
		serialisation.processPlugins(pluginManager);
	}
}