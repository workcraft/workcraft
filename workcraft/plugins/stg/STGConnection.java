package org.workcraft.plugins.stg;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualConnection;

public class STGConnection extends VisualConnection {

	public STGConnection(Connection refConnection, Element xmlElement,
			VisualComponent first, VisualComponent second) {
		super(refConnection, xmlElement, first, second);
	}

	public STGConnection(Connection refConnection, VisualComponent first,
			VisualComponent second) {
		super(refConnection, first, second);
	}

}
