package org.workcraft.plugins.balsa.components;

public class Split extends Component {
	private int inputWidth;
	private int lsOutputWidth;
	private int msOutputWidth;
	public void setInputWidth(int inputWidth) {
		this.inputWidth = inputWidth;
	}
	public int getInputWidth() {
		return inputWidth;
	}
	public void setLsOutputWidth(int lsOutputWidth) {
		this.lsOutputWidth = lsOutputWidth;
	}
	public int getLsOutputWidth() {
		return lsOutputWidth;
	}
	public void setMsOutputWidth(int msOutputWidth) {
		this.msOutputWidth = msOutputWidth;
	}
	public int getMsOutputWidth() {
		return msOutputWidth;
	}
}
