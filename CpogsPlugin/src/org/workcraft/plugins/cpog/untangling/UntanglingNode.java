package org.workcraft.plugins.cpog.untangling;

public class UntanglingNode {

	public enum NodeType {
		PLACE,
		TRANSITION
	};

	private final int id;
	private String label;
	private final NodeType type;


	public UntanglingNode(int id, String label, NodeType type) {
		this.id = id;
		this.label = label;
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getId() {
		return id;
	}

	public NodeType getNodeType() {
		return type;
	}

}
