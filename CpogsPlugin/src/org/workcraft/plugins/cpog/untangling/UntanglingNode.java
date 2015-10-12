package org.workcraft.plugins.cpog.untangling;

public class UntanglingNode {

	private int id;
	private String label;

	public UntanglingNode(int id, String label) {
		super();
		this.id = id;
		this.label = label;
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
}
