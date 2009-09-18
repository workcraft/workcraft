package org.workcraft.plugins.balsa.components;

public class UnaryFunc extends Component {
	private int outputWidth;
	private int inputWidth;
	private UnaryOperator op;
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
	public void setOp(UnaryOperator op) {
		this.op = op;
	}
	public UnaryOperator getOp() {
		return op;
	}
	public void setInputIsSigned(boolean inputIsSigned) {
		this.inputIsSigned = inputIsSigned;
	}
	public boolean isInputIsSigned() {
		return inputIsSigned;
	}
}
