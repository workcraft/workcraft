package org.workcraft.plugins.son.verify;

public class VerificationResult {

	private int result;

	public VerificationResult(int result){
		this.result = result;
	}

	public int getOutcome(){
		return result;
	}
}
