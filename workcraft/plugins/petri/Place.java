package org.workcraft.plugins.petri;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.util.XmlUtil;

@DisplayName("Place")
@VisualClass("org.workcraft.plugins.petri.VisualPlace")
public class Place extends Component {
	protected int tokens = 0;

	public Place(Element xmlElement) {
		super(xmlElement);
		NodeList l = xmlElement.getElementsByTagName("place");
		tokens = XmlUtil.readIntAttr( ((Element)l.item(0)), "tokens", 0);
	}

	public Place() {
		super();
	}

	public int getTokens() {
		return tokens;
	}

	public void setTokens(int tokens) {
		this.tokens = tokens;
	}

	@Override
	public void toXML(Element componentElement) {
		super.toXML(componentElement);
		Element place = componentElement.getOwnerDocument().createElement("place");
		XmlUtil.writeIntAttr(place, "tokens", tokens);
		componentElement.appendChild(place);
	}


}
