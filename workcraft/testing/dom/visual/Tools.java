package org.workcraft.testing.dom.visual;

import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualComponentGroup;
import org.workcraft.dom.visual.VisualConnection;

class Tools {
	static VisualComponentGroup createGroup(VisualComponentGroup parent)
	{
		VisualComponentGroup node = new VisualComponentGroup(parent);
		if(parent!=null)
			parent.add(node);
		return node;
	}

	static VisualComponent createComponent(VisualComponentGroup parent)
	{
		SquareNode node = new SquareNode(parent, new Rectangle2D.Double(0, 0, 1, 1));
		parent.add(node);
		return node;
	}

	static VisualConnection createConnection(VisualComponent c1, VisualComponent c2, VisualComponentGroup parent)
	{
		VisualConnection connection = new VisualConnection(null, c1, c2, parent);
		parent.add(connection);
		return connection;
	}

}
