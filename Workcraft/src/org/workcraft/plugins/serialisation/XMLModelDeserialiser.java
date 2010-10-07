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
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.PluginProvider;
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

public class XMLModelDeserialiser implements ModelDeserialiser {
	XMLDeserialisationManager deserialisation = new XMLDeserialisationManager();

	public XMLModelDeserialiser(PluginProvider mock) {
		deserialisation.processPlugins(mock);
	}

	public DeserialisationResult deserialise(InputStream inputStream,
			ReferenceResolver externalReferenceResolver)
	throws DeserialisationException {
		try {
			Document doc = XmlUtil.loadDocument(inputStream);
			Element modelElement = doc.getDocumentElement();

			deserialisation.begin(externalReferenceResolver);

			// 1st pass -- init instances
			Element rootElement = XmlUtil.getChildElement("root", modelElement);
			Node root = (Node) deserialisation.initInstance(rootElement);

			// 2nd pass -- finalise instances
			deserialisation.finaliseInstances();

			// create model
			Model model = (Model)deserialisation.createModel(modelElement, root);

			return new DeserialisationResult(model, deserialisation.end());
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
}