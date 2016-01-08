package org.workcraft;

import org.workcraft.dom.Node;

public abstract class TransformationTool extends PromotedTool {

	@Override
	public String getSection() {
		return "!   Transformations";  // 3 spaces - positions 2nd
	}

	public boolean isApplicableToNode(Node node) {
		return false;
	}

}
