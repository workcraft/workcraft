package org.workcraft.plugins.balsa.components;

import java.util.BitSet;

public class BinaryFuncConstR extends Component {
	private int outputWidth;
	private int inputWidth;
	private BinaryOperator op;
	private boolean outputIsSigned;
	private boolean inputIsSigned;
	private boolean constIsSigned;
	private BitSet constant;

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
	public void setInputIsSigned(boolean inputIsSigned) {
		this.inputIsSigned = inputIsSigned;
	}
	public boolean getInputIsSigned() {
		return inputIsSigned;
	}
	public void setConstIsSigned(boolean constIsSigned) {
		this.constIsSigned = constIsSigned;
	}
	public boolean getConstIsSigned() {
		return constIsSigned;
	}
	public void setConstant(BitSet constant) {
		this.constant = constant;
	}
	public BitSet getConstant() {
		return constant;
	}
}
