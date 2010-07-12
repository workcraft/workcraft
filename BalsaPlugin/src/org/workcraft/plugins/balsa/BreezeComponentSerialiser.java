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

package org.workcraft.plugins.balsa;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.ReferenceResolver;

// TODO: Stick this class somewhere
public class BreezeComponentSerialiser {

	static final String charsetName = "UTF-8";

	public String getTagName() {
			return "breeze";
		}

		public void serialise(Element element, ReferenceProducer refResolver, BreezeComponent component) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			XMLEncoder q = new XMLEncoder(bytes);
			q.writeObject(component.getUnderlyingComponent());
			q.close();

			String s;
			try {
				s = bytes.toString(charsetName);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
			element.setTextContent(s);
		}

		public void deserialise(Element element,
				ReferenceResolver refResolver, BreezeComponent component) throws DeserialisationException {
			component.setUnderlyingComponent(readUnderlyingComponent(element));
		}


		private DynamicComponent readUnderlyingComponent(Element e) {
			//TODO: make sure DynamicComponent works (it probably doesn't)
			NodeList breezeElements = e.getElementsByTagName("breeze");
			if(breezeElements.getLength() != 1)
				throw new RuntimeException("Breeze component description must have at least one <breeze> tag");
			Element breeze = (Element) breezeElements.item(0);
			byte[] bytes;
			try {
				bytes = breeze.getTextContent().getBytes(charsetName);
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			}

			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			XMLDecoder dec = new XMLDecoder(stream);
			Object obj = dec.readObject();
			return (DynamicComponent) obj;
		}

}
