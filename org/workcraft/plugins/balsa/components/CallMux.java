package org.workcraft.plugins.balsa.components;

public class CallMux extends Component {
	private int width;
	private int inputCount = 2;

	public void setWidth(int width) {
		this.width = width;
	}
	public int getWidth() {
		return width;
	}
	public void setInputCount(int inputCount) {
		this.inputCount = inputCount;
	}
	public int getInputCount() {
		return inputCount;
	}
}
