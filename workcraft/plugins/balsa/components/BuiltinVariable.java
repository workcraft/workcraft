package org.workcraft.plugins.balsa.components;

public class BuiltinVariable extends Component {
	private int readPortCount;
	private String name;

	public void setReadPortCount(int readPortCount) {
		this.readPortCount = readPortCount;
	}

	public int getReadPortCount() {
		return readPortCount;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
