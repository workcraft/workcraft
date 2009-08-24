package org.workcraft.plugins.serialisation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.framework.serialisation.ExportReferenceResolver;
import org.workcraft.framework.serialisation.Format;
import org.workcraft.framework.serialisation.MathSerialiser;
import org.workcraft.util.XmlUtil;

public class XMLMathSerialiser implements MathSerialiser, Plugin {

	class ReferenceResolver implements ExportReferenceResolver
	{
		public String getReference(Object obj) {
			if (obj instanceof VisualNode)
				return Integer.toString(((VisualNode)obj).getID());
			else
				return "(null)";
		}
	}

	public ExportReferenceResolver export(MathModel model, OutputStream out) throws IOException,
	ModelValidationException, ExportException {
		try{
			Document doc = XmlUtil.createDocument();

/*			Element root = doc.createElement("workcraft");
			root.setAttribute("version", Framework.FRAMEWORK_VERSION_MAJOR+"."+Framework.FRAMEWORK_VERSION_MINOR);
			doc.appendChild(root); */

			Element root = doc.createElement("model");
			doc.appendChild(root);
			//modelElement.setAttribute("class", model.getMathModel().getClass().getName());
			model.getMathModel().serialiseToXML(root);


			/*
			VisualModel visualModel = model.getVisualModel();

			if (visualModel != null) {
				Element visualModelElement = doc.createElement("visual-model");
				visualModelElement.setAttribute("class", visualModel.getClass().getName());
				visualModel.serialiseToXML(visualModelElement);
				root.appendChild(visualModelElement);
			}*/

			XmlUtil.writeDocument(doc, out);

		} catch (ParserConfigurationException e) {
			throw new ExportException("XML Parser configuration error: "+ e.getMessage());
		}

		return new ReferenceResolver();
	}

	public String getDescription() {
		return "Workcraft default XML math model serialiser";
	}

	public boolean isApplicableTo(MathModel model) {
		return true;
	}

	public UUID getFormatUUID() {
		return Format.defaultMathXML;
	}

	public String getExtension() {
		return ".xml";
	}
}