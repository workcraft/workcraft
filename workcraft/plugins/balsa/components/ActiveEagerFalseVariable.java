package org.workcraft.plugins.balsa.components;

public class ActiveEagerFalseVariable extends Component {
	private int width;
	private int readPortCount;
	private String specification;

	int getWidth() {
		return width;
	}
	void setWidth(int width)
	{
		this.width = width;
	}
	int getReadPortCount() {
		return readPortCount;
	}
	void setReadPortCount(int readPortCount)
	{
		this.readPortCount = readPortCount;
	}
	public String getSpecification()
	{
		return specification;
	}

	public void setSpecification(String specification)
	{
		this.specification = specification;
	}
}
