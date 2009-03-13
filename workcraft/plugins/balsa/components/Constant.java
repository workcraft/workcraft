package org.workcraft.plugins.balsa.components;

import java.util.BitSet;

public class Constant extends Component {
	private int width;
	private BitSet value;
	public void setWidth(int width) {
		this.width = width;
	}
	public int getWidth() {
		return width;
	}
	public void setValue(BitSet value) {
		this.value = value;
	}
	public BitSet getValue() {
		return value;
	}
}
