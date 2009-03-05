package org.workcraft.plugins.balsa.components;

public class Adapt extends Component {
	private int outputWidth;
	private int inputWidth;
	private boolean outputIsSigned;
	private boolean inputIsSigned;

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
	public void setOutputIsSigned(boolean outputIsSigned) {
		this.outputIsSigned = outputIsSigned;
	}
	public boolean getOutputIsSigned() {
		return outputIsSigned;
	}
	public void setInputIsSigned(boolean inputIsSigned) {
		this.inputIsSigned = inputIsSigned;
	}
	public boolean getInputIsSigned() {
		return inputIsSigned;
	}
}
