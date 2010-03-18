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
import java.io.OutputStream;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.PluginConsumer;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.Format;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.XMLSerialisationManager;
import org.workcraft.util.XmlUtil;

public class XMLSerialiser implements ModelSerialiser, PluginConsumer {
	XMLSerialisationManager serialisation = new XMLSerialisationManager();

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

	public ReferenceProducer serialise(final Model model, OutputStream out, ReferenceProducer externalReferences)
	throws SerialisationException {
		try{

			ReferenceProducer internalReferences = new ReferenceProducer() {
				public String getReference(Object obj) {
					if (obj instanceof Node)
						return model.getNodeReference((Node)obj);
					else
						return null;
				}
			};

			Document doc = XmlUtil.createDocument();

			Element modelElement = doc.createElement("model");
			Element rootElement = doc.createElement("root");

			serialisation.begin(internalReferences, externalReferences);

			serialisation.serialise(modelElement, model);
			serialisation.serialise(rootElement, model.getRoot());

			serialisation.end();

			doc.appendChild(modelElement);
			modelElement.appendChild(rootElement);
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