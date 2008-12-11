package org.workcraft.plugins.petri;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;

@DisplayName("Place")
@VisualClass("org.workcraft.plugins.petri.VisualPlace")
public class Place extends Component {

	protected int tokens = 0;
	private static int counter = 0;

	public Place() {
		super();
		tokens = (counter++) % 20;
	}

	public Place(Element xmlElement) {
		super(xmlElement);
	}
}
