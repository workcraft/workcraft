package org.workcraft.plugins.petri;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceProducer;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.util.XmlUtil;

@DisplayName("Place")
@VisualClass("org.workcraft.plugins.petri.VisualPlace")
public class Place extends Component {
	protected int tokens = 0;
	protected int capacity = 1;

	public Place() {
		addXMLSerialisable();
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int c) {
		this.capacity = c;
	}

	public int getTokens() {
		return tokens;
	}

	public void setTokens(int tokens) {
		this.tokens = tokens;
	}

	private void addXMLSerialisable() {
		addXMLSerialiser(new XMLSerialiser(){
			public String getTagName() {
				return Place.class.getSimpleName();
			}
			public void deserialise(Element element,
					ReferenceResolver refResolver) throws DeserialisationException {
				tokens = XmlUtil.readIntAttr(element, "tokens", 0);
				capacity = XmlUtil.readIntAttr(element, "capacity", 1);
			}
			@Override
			public void serialise(Element element,
					ReferenceProducer refResolver) {
				XmlUtil.writeIntAttr(element, "tokens", tokens);
				if (capacity!=1)
					XmlUtil.writeIntAttr(element, "capacity", capacity);
			}
		});
	}
}
