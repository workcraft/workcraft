package org.workcraft.plugins.mpsat.gui;

import java.util.List;

import org.workcraft.Trace;

public class Solution {
	final private Trace mainTrace;
	final private Trace branchTrace;

	public Solution(Trace mainTrace, Trace branchTrace) {
		this.mainTrace = mainTrace;
		this.branchTrace = branchTrace;
	}

	public Trace getMainTrace() {
		return mainTrace;
	}

	public Trace getBranchTrace() {
		return branchTrace;
	}

	public String toString() {
		String result = "";
		if (mainTrace != null) {
			result += mainTrace.toString();
		}
		result += "\n";
		if (branchTrace != null) {
			result += branchTrace.toString();
		}
		return result;
	}

	public static boolean hasTraces(List<Solution> solutions) {
		boolean result = false;
		for (Solution solution : solutions) {
			if ((solution.getMainTrace() != null) || (solution.getBranchTrace() != null)) {
				result = true;
				break;
			}
		}
		return result;
	}

}
