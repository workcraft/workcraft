package org.workcraft.plugins.shared;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.Model;
import org.workcraft.framework.Framework;
import org.workcraft.framework.Importer;
import org.workcraft.framework.ModelFactory;
import org.workcraft.framework.exceptions.ImportException;
import org.workcraft.framework.exceptions.ModelInstantiationException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class DefaultDeserialiser implements Importer {
	public boolean accept(File file) {
		return false;
	}

	public String getDescription() {
		return null;
	}

	public Model importFrom(ReadableByteChannel in) throws ImportException {
		try {
			Document doc = XmlUtil.loadDocument(in);

			Element xmlroot = doc.getDocumentElement();

			if (xmlroot.getNodeName()!="workcraft")
				throw new ImportException("not a Workcraft document");

			String[] ver = xmlroot.getAttribute("version").split("\\.", 2);

			if (ver.length<2 || !ver[0].equals(Framework.FRAMEWORK_VERSION_MAJOR))
				throw new ImportException("Document was created by an incompatible version of Workcraft.");

			Element modelElement = XmlUtil.getChildElement("model", xmlroot);

			if (modelElement == null)
				throw new ImportException("<model> section is missing.");

			MathModel model = ModelFactory.createModel(modelElement);

			Element visualModelElement = XmlUtil.getChildElement("visual-model", xmlroot);

			if (visualModelElement == null)
				return model;

			return ModelFactory.createVisualModel(model, visualModelElement);
		} catch (ParserConfigurationException e) {
			throw new ImportException (e);
		} catch (SAXException e) {
			throw new ImportException (e);
		} catch (IOException e) {
			throw new ImportException (e);
		} catch (VisualModelInstantiationException e) {
			throw new ImportException (e);
		} catch (ModelInstantiationException e) {
			throw new ImportException (e);
		}
	}
}
