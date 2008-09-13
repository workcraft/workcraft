package org.workcraft.plugins.petri;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;

@DisplayName ("Place")
public class Place extends Component {
	public Place() {
		super();
	}

	public Place(Element xmlElement) {
		super(xmlElement);
	}
}
