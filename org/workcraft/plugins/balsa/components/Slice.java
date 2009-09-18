package org.workcraft.plugins.balsa.components;

public class Slice extends Component {
	private int outputWidth;
	private int inputWidth;
	private int lowIndex;
	public void setOutputWidth(int outputWidth) {
		this.outputWidth = outputWidth;
	}
	public int getOutputWidth() {
		return outputWidth;
	}
	public void setInputWidth(int inputWidth) {
		this.inputWidth = inputWidth;
	}
	public int getInputWidth() {
		return inputWidth;
	}
	public void setLowIndex(int lowIndex) {
		this.lowIndex = lowIndex;
	}
	public int getLowIndex() {
		return lowIndex;
	}
}
