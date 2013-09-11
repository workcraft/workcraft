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
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.DualDeserialisationResult;
import org.workcraft.serialisation.DualModelDeserialiser;
import org.workcraft.serialisation.Format;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.References;
import org.workcraft.serialisation.xml.XMLDeserialisationManager;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class XMLDualModelDeserialiser implements DualModelDeserialiser {
	XMLDeserialisationManager deserialisation1 = new XMLDeserialisationManager();
	XMLDeserialisationManager deserialisation2 = new XMLDeserialisationManager();

	public XMLDualModelDeserialiser(PluginProvider mock) {
		deserialisation1.processPlugins(mock);
		deserialisation2.processPlugins(mock);
	}

	public DualDeserialisationResult deserialise(InputStream is1, InputStream is2,
			ReferenceResolver rr1, ReferenceResolver rr2, Model underlyingModel) throws DeserialisationException {
		try {
			Document doc1 = XmlUtil.loadDocument(is1);
			Element modelElement1 = doc1.getDocumentElement();

			Document doc2 = XmlUtil.loadDocument(is2);
			Element modelElement2 = doc2.getDocumentElement();

			deserialisation1.begin(rr1);
			deserialisation2.begin(rr2);

			// 1st pass -- init instances
			Element rootElement1 = XmlUtil.getChildElement("root", modelElement1);
			Node root1 = (Node) deserialisation1.initInstance(rootElement1);

			Element rootElement2 = XmlUtil.getChildElement("root", modelElement2);
			Node root2 = (Node) deserialisation2.initInstance(rootElement2);

			// 2nd pass -- finalise instances
			deserialisation1.finaliseInstances();
			deserialisation2.finaliseInstances();

			// create model
			String modelClassName1 = modelElement1.getAttribute("class");
			String modelClassName2 = modelElement2.getAttribute("class");
			if(!modelClassName1.equals(modelClassName2)) {
				throw new DeserialisationException();
			}
			if (modelClassName1 == null || modelClassName1.isEmpty()) {
				throw new DeserialisationException("Class name attribute is not set\n" + modelElement1.toString());
			}
			Class<?> cls = Class.forName(modelClassName1);

			System.out.printf("> root1 = %d, root2 = %d\n", root1.getChildren().size(), root2.getChildren().size());
			Collection<Node> nodes = new HashSet<Node>(((Container)root2).getChildren());
			// Option 1) Why reparent does not work here?
			((Container)root2).reparent(nodes, (Container)root1);
			// Probably because of unmodifiableCollection returned by groupImplementation.getChildren()
			/* // Option 2) As a quick fix we do it node-by-node.
			for (Node node : ((Container)root2).getChildren()) {
				((Container)root2).remove(node);
				((Container)root1).add(node);
			} */
			System.out.printf("< root1 = %d, root2 = %d\n", root1.getChildren().size(), root2.getChildren().size());

			final References r1 = deserialisation1.getReferenceResolver();
			final References r2 = deserialisation2.getReferenceResolver();
			References r = new References() {
				final String suffix1 = " ";
				final String suffix2 = "_";

				@Override
				public Object getObject(String reference) {
					if (reference.endsWith(suffix2)) {
						return r2.getObject(reference.substring(0, reference.length() - suffix2.length()));
					} else {
						return r1.getObject(reference.substring(0, reference.length() - suffix1.length()));
					}
				}

				@Override
				public String getReference(Object obj) {
					String s1 = r1.getReference(obj);
					String s2 = r2.getReference(obj);
					if(s1 != null) {
						return s1 + suffix1;
					} else {
						return s2 + suffix2;
					}
				}
			};

			// create model
			Model model = XMLDeserialisationManager.createModel(cls, root1, underlyingModel, r);
			return new DualDeserialisationResult(model, r, r1, r2);
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
		} catch (ClassNotFoundException e) {
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