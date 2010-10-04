package org.workcraft.plugins.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;

@DisplayName("Node")
@VisualClass("org.workcraft.plugins.workflow.VisualWorkflowNode")
public class WorkflowNode extends MathNode {
	private ArrayList<Port> ports = new ArrayList<Port>();

	@Override
	public Collection<Node> getChildren() {
		return new ArrayList<Node>(ports);
	}

	public Collection<Port> getPorts()
	{
		return Collections.unmodifiableList(ports);
	}
}
