package org.workcraft.plugins.balsa.components;

public class Variable extends Component {
	private int width;
	private int readPortCount = 1;
	private String name = "Var";

	public void setWidth(int width) {
		this.width = width;
	}
	public int getWidth() {
		return width;
	}
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
