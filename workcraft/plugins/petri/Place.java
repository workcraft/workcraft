package org.workcraft.plugins.petri;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.XMLSerialisable;
import org.workcraft.util.XmlUtil;

@DisplayName("Place")
@VisualClass("org.workcraft.plugins.petri.VisualPlace")
public class Place extends Component {
	protected int tokens = 0;

	public Place(Element componentElement) {
		super(componentElement);

		Element e = XmlUtil.getChildElement(Place.class.getSimpleName(), componentElement);
		tokens = XmlUtil.readIntAttr(e, "tokens", 0);

		addXMLSerialisable();
	}

	public Place() {
		super();

		addXMLSerialisable();
	}

	public int getTokens() {
		return tokens;
	}

	public void setTokens(int tokens) {
		this.tokens = tokens;
	}

	private void addXMLSerialisable() {
		addXMLSerialisable(new XMLSerialisable(){
			public String getTagName() {
				return Place.class.getSimpleName();
			}
			public void serialise(Element element) {
				XmlUtil.writeIntAttr(element, "tokens", tokens);
			}
		});
	}
}
