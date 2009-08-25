package org.workcraft.plugins.balsa;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.Component;
import org.workcraft.dom.VisualComponentGeneratorAttribute;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.framework.exceptions.ImportException;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

@VisualComponentGeneratorAttribute(generator="org.workcraft.plugins.balsa.BreezeVisualComponentGenerator")
public class BreezeComponent extends Component {

	private org.workcraft.plugins.balsa.components.Component underlyingComponent;

	static final String charsetName = "UTF-8";

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

	public BreezeComponent() {
		init();
	}

	private void init() {
		addXMLSerialiser(new XMLSerialiser(){
			public String getTagName() {
				return "breeze";
			}
			public void serialise(Element element, ExternalReferenceResolver refResolver) {
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				XMLEncoder q = new XMLEncoder(bytes);
				q.writeObject(underlyingComponent);
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
					ReferenceResolver refResolver) throws ImportException {
				setUnderlyingComponent(readUnderlyingComponent(element));
			}
		});
	}

	public void setUnderlyingComponent(org.workcraft.plugins.balsa.components.Component underlyingComponent) {
		this.underlyingComponent = underlyingComponent;
	}

	public org.workcraft.plugins.balsa.components.Component getUnderlyingComponent() {
		return underlyingComponent;
	}

	public void setHandshakeComponents(Map<Handshake, HandshakeComponent> handshakes) {
		this.handshakeComponents = handshakes;
	}

	public final HandshakeComponent getHandshakeComponentByName(String name) {
		return getHandshakeComponents().get(getHandshakes().get(name));
	}

	public Map<Handshake, HandshakeComponent> getHandshakeComponents() {
		return handshakeComponents;
	}

	public void setHandshakes(Map<String, Handshake> handshakes) {
		this.handshakes = handshakes;
	}

	public Map<String, Handshake> getHandshakes() {
		return handshakes;
	}

	private Map<Handshake, HandshakeComponent> handshakeComponents;
	private Map<String, Handshake> handshakes;
}
