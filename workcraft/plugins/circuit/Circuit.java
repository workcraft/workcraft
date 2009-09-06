package org.workcraft.plugins.circuit;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.Node;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchyObserver;
import org.workcraft.framework.observation.NodesAddedEvent;
import org.workcraft.framework.observation.NodesDeletedEvent;

@DisplayName ("Digital Circuit")
@VisualClass ("org.workcraft.plugins.circuit.VisualCircuit")

public class Circuit extends AbstractMathModel {


	public class Listener implements HierarchyObserver {
		@Override
		public void notify(HierarchyEvent e) {
			if(e instanceof NodesAddedEvent)
			{
				for(Node node : e.getAffectedNodes())
					if (node instanceof Formula || node instanceof Joint || node instanceof Contact)
						components.add((MathNode)node);
			}
			if(e instanceof NodesDeletedEvent)
			{
				for(Node node : e.getAffectedNodes())
					if (node instanceof Formula || node instanceof Joint || node instanceof Contact)
						components.remove(node);
			}
		}
	}


	private HashSet<MathNode> components = new HashSet<MathNode>();

	public Circuit() {
		super();
		addObserver(new Listener());
	}

	public void validate() throws ModelValidationException {
	}


	final public Set<MathNode> getCircuitComponenets() {
		return new HashSet<MathNode>(components);
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
	}
}
