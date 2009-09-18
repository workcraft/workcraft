package org.workcraft.plugins.balsa.components;

public class BinaryFunc extends Component {
	private int outputWidth;
	private int inputAWidth;
	private int inputBWidth;
	private BinaryOperator op;
	private boolean outputIsSigned;
	private boolean inputAIsSigned;
	private boolean inputBIsSigned;

	public void setOutputWidth(int outputWidth) {
		this.outputWidth = outputWidth;
	}
	public int getOutputWidth() {
		return outputWidth;
	}
	public void setInputAWidth(int inputAWidth) {
		this.inputAWidth = inputAWidth;
	}
	public int getInputAWidth() {
		return inputAWidth;
	}
	public void setInputBWidth(int inputBWidth) {
		this.inputBWidth = inputBWidth;
	}
	public int getInputBWidth() {
		return inputBWidth;
	}
	public void setOp(BinaryOperator op) {
		this.op = op;
	}
	public BinaryOperator getOp() {
		return op;
	}
	public void setOutputIsSigned(boolean outputIsSigned) {
		this.outputIsSigned = outputIsSigned;
	}
	public boolean getOutputIsSigned() {
		return outputIsSigned;
	}
	public void setInputAIsSigned(boolean inputAIsSigned) {
		this.inputAIsSigned = inputAIsSigned;
	}
	public boolean getInputAIsSigned() {
		return inputAIsSigned;
	}
	public void setInputBIsSigned(boolean inputBIsSigned) {
		this.inputBIsSigned = inputBIsSigned;
	}
	public boolean getInputBIsSigned() {
		return inputBIsSigned;
	}
}
