package org.workcraft.plugins.son.tasks;

public class VerificationResult {

	private int result;

	public VerificationResult(int result){
		this.result = result;
	}

	public int getOutcome(){
		return result;
	}
}
