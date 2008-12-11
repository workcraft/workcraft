package org.workcraft.plugins.petri;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;

@DisplayName("Transition")
@VisualClass("org.workcraft.plugins.petri.VisualTransition")
public class Transition extends Component {

	public Transition() {
		super();
	}

	public Transition(Element xmlElement) {
		super(xmlElement);
	}
}
