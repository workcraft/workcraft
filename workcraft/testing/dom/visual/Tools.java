package org.workcraft.testing.dom.visual;

import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualConnection;

class Tools {
	static VisualGroup createGroup(VisualGroup parent)
	{
		VisualGroup node = new VisualGroup(parent);
		if(parent!=null)
			parent.add(node);
		return node;
	}

	static VisualComponent createComponent(VisualGroup parent)
	{
		SquareNode node = new SquareNode(parent, new Rectangle2D.Double(0, 0, 1, 1));
		parent.add(node);
		return node;
	}

	static VisualConnection createConnection(VisualComponent c1, VisualComponent c2, VisualGroup parent)
	{
		VisualConnection connection = new VisualConnection(null, c1, c2, parent);
		parent.add(connection);
		return connection;
	}

}
