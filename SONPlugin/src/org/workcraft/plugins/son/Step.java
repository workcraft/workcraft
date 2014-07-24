package org.workcraft.plugins.son;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Step extends ArrayList<String>{

	public String toString() {
		StringBuffer result = new StringBuffer("");
		// step
		boolean first = true;
		for (String t : this) {
			if (!first) {
				result.append(',');
			}
			result.append(' ');
			result.append(t);
			first = false;
		}
		return result.toString();
	}

	public void fromString(String str) {
		clear();
		for (String s : str.trim().split(",")) {
				add(s.trim());
		}
	}
}
