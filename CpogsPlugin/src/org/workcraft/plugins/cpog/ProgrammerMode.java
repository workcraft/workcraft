package org.workcraft.plugins.cpog;

public enum ProgrammerMode {
	MICROCONTROLLER("-M", "Microcontroller synthesising", false),
	CPOG_SIZE("-C", "Element controller synthesising", false);

	private String argument;
	private String description;
	private boolean reach;

	public static final ProgrammerMode[] modes = {
		MICROCONTROLLER,
		CPOG_SIZE
	};

	public static ProgrammerMode getMode (String arg) {
		for (int i=0; i<modes.length; i++)
			if (modes[i].getArgument().equals(arg))
				return modes[i];
		return null;
	}

	ProgrammerMode(String argument, String description, boolean reach) {
		this.argument = argument;
		this.description = description;
		this.reach = reach;
	}

	@Override
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