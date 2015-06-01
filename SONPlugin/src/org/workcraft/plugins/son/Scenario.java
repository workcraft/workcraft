package org.workcraft.plugins.son;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;

@SuppressWarnings("serial")
public class Scenario extends ArrayList<ArrayList<? extends Node>>{

	public Collection<Node> getAllNodes(){
		Collection<Node> result = new HashSet<Node>();
		for(ArrayList<? extends Node> list : this){
			result.addAll(list);
		}
		return result;
	}

	public String toString(SON net) {
		StringBuffer result = new StringBuffer("");

		boolean first = true;
		for (ArrayList<? extends Node> sub : this) {
			result.append(System.getProperty("line.separator"));
			for(Node node : sub){
				if (!first) {
					result.append(',');
					result.append(' ' + net.getNodeReference(node));
				}else{
					result.append(' ');
					result.append('[');
					result.append(net.getNodeReference(node));
					first = false;
				}
			}
			if(!sub.isEmpty())
				result.append(']');
		}
		return result.toString();
	}
}
