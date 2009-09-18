package org.workcraft.plugins.balsa.components;

public class ActiveEagerFalseVariable extends Component {
	private int width;
	private int readPortCount;
	private String specification;

	public int getWidth() {
		return width;
	}
	public void setWidth(int width)
	{
		this.width = width;
	}
	public int getReadPortCount() {
		return readPortCount;
	}
	public void setReadPortCount(int readPortCount)
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
