package org.workcraft.plugins.petri;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;

@DisplayName("Place")
@VisualClass("org.workcraft.plugins.petri.VisualPlace")
public class Place extends Component {

	protected int tokens = 0;

	public Place(Element xmlElement) {
		super(xmlElement);
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
}
