package org.workcraft.plugins.shared.tasks;

public class ExternalProcessResult {
	private byte[] output;
	private byte[] errors;
	private int returnCode;

	public ExternalProcessResult(int returnCode, byte[] output, byte[] errors) {
		this.output = output;
		this.errors = errors;
		this.returnCode = returnCode;
	}

	public byte[] getOutput() {
		return output;
	}

	public byte[] getErrors() {
		return errors;
	}

	public int getReturnCode() {
		return returnCode;
	}
}