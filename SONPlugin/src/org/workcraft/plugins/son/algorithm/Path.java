package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;

@SuppressWarnings("serial")
public class Path extends ArrayList<Node>{

	public String toString(SON net) {
		StringBuffer result = new StringBuffer("");

		boolean first = true;
		for (Node node : this) {
			if (!first) {
				result.append(' ');
				result.append(',' + net.getName(node));
			}else{
				result.append(' ');
				result.append(net.getName(node));
				first = false;
			}
		}
		return result.toString();
	}
}
