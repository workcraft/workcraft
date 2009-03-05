package org.workcraft.plugins.balsa.components;

public class Case extends Component {
	private int inputWidth;
	private int outputCount;
	private String specification;
	public void setInputWidth(int inputWidth) {
		this.inputWidth = inputWidth;
	}
	public int getInputWidth() {
		return inputWidth;
	}
	public void setOutputCount(int outputCount) {
		this.outputCount = outputCount;
	}
	public int getOutputCount() {
		return outputCount;
	}
	public void setSpecification(String specification) {
		this.specification = specification;
	}
	public String getSpecification() {
		return specification;
	}
}
