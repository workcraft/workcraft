package org.workcraft.plugins.graph;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;

@DisplayName("Vertex")
@VisualClass("org.workcraft.plugins.graph.VisualVertex")
public class Vertex extends Component {
	public Vertex() {
		super();
	}

	public Vertex(Element xmlElement) {
		super(xmlElement);
	}
}
