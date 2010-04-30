package org.workcraft.plugins.desij;

public enum DesiJOperation {
	DECOMPOSITION ("operation=decompose", "STG decomposition", false),
	REMOVE_DUMMIES ("operation=killdummies", "Contract all dummy transitions", true),
	REDUNDANT_PLACE_DELETION ("operation=reddel", "Deletion of redundant places", true);

	private String argument;
	private String description;
	private boolean reach;

	public static final DesiJOperation[] operations =
	{
		DECOMPOSITION,
		REMOVE_DUMMIES,
		REDUNDANT_PLACE_DELETION
	};

	public static DesiJOperation getOperation (String arg) {
		for (int i=0; i<operations.length; i++)
			if (operations[i].getArgument().equals(arg))
				return operations[i];
		return null;
	}

	DesiJOperation(String argument, String description, boolean reach) {
		this.argument = argument;
		this.description = description;
		this.reach = reach;
	}

	public String toString() {
		return description;
	}

	public String getArgument() {
		return argument;
	}

	public boolean isReach() {
		return reach;
	}

}
