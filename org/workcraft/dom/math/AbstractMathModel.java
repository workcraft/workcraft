package org.workcraft.dom.math;

import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.util.Hierarchy;

public abstract class AbstractMathModel extends AbstractModel implements MathModel {
	public AbstractMathModel(Container root) {
		super( (root == null) ? new MathGroup() : root);
	}

	public Connection connect(Node first, Node second)
			throws InvalidConnectionException {

		validateConnection (first, second);

		Connection con =  new MathConnection((MathNode)first, (MathNode)second);

		Container group =
			Hierarchy.getNearestAncestor(
					Hierarchy.getCommonParent(first, second),
					Container.class);

		group.add(con);

		return con;
	}
}