package org.workcraft.plugins.balsa.components;

public class Combine extends Component {
	private int outputWidth;
	private int LSInputWidth;
	private int MSInputWidth;
	public void setOutputWidth(int outputWidth) {
		this.outputWidth = outputWidth;
	}
	public int getOutputWidth() {
		return outputWidth;
	}
	public void setLSInputWidth(int lSInputWidth) {
		LSInputWidth = lSInputWidth;
	}
	public int getLSInputWidth() {
		return LSInputWidth;
	}
	public void setMSInputWidth(int mSInputWidth) {
		MSInputWidth = mSInputWidth;
	}
	public int getMSInputWidth() {
		return MSInputWidth;
	}
}
