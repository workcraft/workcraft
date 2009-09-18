package org.workcraft.plugins.balsa.components;

import java.util.BitSet;

public class InitVariable extends Component {
	private int width;
	private int readPortCount;
	private String name;
	private BitSet initValue;

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
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setInitValue(BitSet initValue) {
		this.initValue = initValue;
	}
	public BitSet getInitValue() {
		return initValue;
	}
}
