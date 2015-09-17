package org.workcraft.plugins.son;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class StepExecution extends ArrayList<NodeRefs>{

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer("");
		// position
		result.append(String.valueOf(0));
		result.append(':');
		// trace
		boolean first = true;
		for(NodeRefs nodes : this){
			if (!first) {
				result.append(';');
			}
			result.append(' ');
			result.append(nodes.toString());
			first = false;
		}
		return result.toString();
	}
}
