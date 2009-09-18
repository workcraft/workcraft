package org.workcraft.plugins.balsa.components;

public class CaseFetch extends Component {
	private int width;
	private int indexWidth;
	private int inputCount;
	private String specification;

	public void setWidth(int width) {
		this.width = width;
	}
	public int getWidth() {
		return width;
	}
	public void setIndexWidth(int indexWidth) {
		this.indexWidth = indexWidth;
	}
	public int getIndexWidth() {
		return indexWidth;
	}
	public void setInputCount(int inputCount) {
		this.inputCount = inputCount;
	}
	public int getInputCount() {
		return inputCount;
	}
	public void setSpecification(String specification) {
		this.specification = specification;
	}
	public String getSpecification() {
		return specification;
	}
}
