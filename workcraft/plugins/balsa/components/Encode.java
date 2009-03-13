package org.workcraft.plugins.balsa.components;

public class Encode extends Component {
	private int inputCount;
	private int outputWidth;
	private String specification;

	public void setInputCount(int inputCount) {
		this.inputCount = inputCount;
	}
	public int getInputCount() {
		return inputCount;
	}
	public void setOutputWidth(int outputWidth) {
		this.outputWidth = outputWidth;
	}
	public int getOutputWidth() {
		return outputWidth;
	}
	public void setSpecification(String specification) {
		this.specification = specification;
	}
	public String getSpecification() {
		return specification;
	}
}
