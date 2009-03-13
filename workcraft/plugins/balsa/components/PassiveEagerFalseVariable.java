package org.workcraft.plugins.balsa.components;

public class PassiveEagerFalseVariable extends Component {
	private int width;
	private int readPortCount;
	private String specification;

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
	public void setSpecification(String specification) {
		this.specification = specification;
	}
	public String getSpecification() {
		return specification;
	}
}
