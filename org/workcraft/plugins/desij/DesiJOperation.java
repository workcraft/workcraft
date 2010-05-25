package org.workcraft.plugins.desij;

public enum DesiJOperation {
	DECOMPOSITION ("operation=decompose", "STG decomposition", true),
	REMOVE_DUMMIES ("operation=killdummies", "Contract all dummy transitions", true),
	IMPLICIT_PLACE_DELETION ("operation=reddel", "Deletion of implicit places", false);

	private String argument;
	private String description;
	private boolean inclContraction;

	public static final DesiJOperation[] operations =
	{
		DECOMPOSITION,
		REMOVE_DUMMIES,
		IMPLICIT_PLACE_DELETION
	};

	public static DesiJOperation getOperation (String arg) {
		for (int i=0; i<operations.length; i++)
			if (operations[i].getArgument().equals(arg))
				return operations[i];
		return null;
	}

	DesiJOperation(String argument, String description, boolean inclContraction) {
		this.argument = argument;
		this.description = description;
		this.inclContraction = inclContraction;
	}

	public String toString() {
		return description;
	}

	public String getArgument() {
		return argument;
	}

	public boolean usesContraction() {
		return inclContraction;
	}

}
