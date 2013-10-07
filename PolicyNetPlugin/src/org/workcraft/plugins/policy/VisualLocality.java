package org.workcraft.plugins.policy;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;

public class VisualLocality extends VisualGroup {

	private Locality locality;

	public VisualLocality() {
		this(null);
	}

	public VisualLocality(Locality locality) {
		this.locality = locality;
	}

	@Override
	public void add(Node node) {
		if(node instanceof VisualComponent){
			Node mathNode = ((VisualComponent)node).getReferencedComponent();
			Locality oldLocality = (Locality)mathNode.getParent();
			HashSet<Node> mathNodes = new HashSet<Node>();
			mathNodes.add(mathNode);
			oldLocality.reparent(mathNodes, locality);
		}
		super.add(node);
	}

	@Override
	public void remove(Node node) {
		super.remove(node);
	}

	@Override
	public void add(Collection<Node> nodes){
		for(Node node : nodes)
			this.add(node);
	}

	@Override
	public void remove(Collection<Node> nodes){
		super.remove(nodes);
	}

	public Locality getLocality() {
		return locality;
	}

	public void setLocality(Locality locality) {
		this.locality = locality;
	}
}
