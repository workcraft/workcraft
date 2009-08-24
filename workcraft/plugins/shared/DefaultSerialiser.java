package org.workcraft.plugins.shared;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.Exporter;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.util.XmlUtil;

public class DefaultSerialiser implements Exporter {

	public void export(Model model, WritableByteChannel out) throws IOException,
	ModelValidationException, ExportException {
		try{
			Document doc = XmlUtil.createDocument();

			Element root = doc.createElement("workcraft");
			root.setAttribute("version", Framework.FRAMEWORK_VERSION_MAJOR+"."+Framework.FRAMEWORK_VERSION_MINOR);
			doc.appendChild(root);

			Element modelElement = doc.createElement("model");
			modelElement.setAttribute("class", model.getMathModel().getClass().getName());
			model.getMathModel().serialiseToXML(modelElement);
			root.appendChild(modelElement);

			VisualModel visualModel = model.getVisualModel();

			if (visualModel != null) {
				Element visualModelElement = doc.createElement("visual-model");
				visualModelElement.setAttribute("class", visualModel.getClass().getName());
				visualModel.serialiseToXML(visualModelElement);
				root.appendChild(visualModelElement);
			}

			TransformerFactory tFactory = TransformerFactory.newInstance();
			tFactory.setAttribute("indent-number", new Integer(2));
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(doc);
			//OutputStreamWriter writer = new OutputStreamWriter(output);
			StreamResult result = new StreamResult(Channels.newOutputStream(out));
			transformer.transform(source, result);
//			writer.close();
		} catch (ParserConfigurationException e) {
			throw new ExportException("XML Parser configuration error: "+ e.getMessage());
		} catch (TransformerConfigurationException e) {
			throw new ExportException("XML transformer configuration error: "+ e.getMessage());
		} catch (TransformerException e) {
			throw new ExportException("XML transformer error: "+ e.getMessage());
		}
	}

	public String getDescription() {
		return ".work (Workcraft)";
	}

	public String getExtenstion() {
		return ".work";
	}

	public boolean isApplicableTo(Model model) {
		return true;
	}
}
