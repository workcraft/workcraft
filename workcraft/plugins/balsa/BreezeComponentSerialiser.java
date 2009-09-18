package org.workcraft.plugins.balsa;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.exceptions.DeserialisationException;
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


		private org.workcraft.plugins.balsa.components.Component readUnderlyingComponent(Element e) {
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
			return (org.workcraft.plugins.balsa.components.Component) obj;
		}

}
