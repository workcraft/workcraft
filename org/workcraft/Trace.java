package org.workcraft;

import java.util.LinkedList;

@SuppressWarnings("serial")
public class Trace extends LinkedList<String>{
	public String toString() {
		StringBuffer result = new StringBuffer("");

		boolean first = true;

		for (String t : this) {
			if (first)
				first = false;
			else
				result.append(',');
			result.append(t);
		}

		return result.toString();
	}
}
