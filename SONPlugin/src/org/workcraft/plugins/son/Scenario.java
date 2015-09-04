package org.workcraft.plugins.son;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.connections.SONConnection;

@SuppressWarnings("serial")
public class Scenario extends ArrayList<String>{

	public Collection<Node> getNodes(SON net){
		Collection<Node> result = new HashSet<Node>();
		for(String ref : this){
			result.add(net.getNodeByReference(ref));
		}
		return result;
	}

	public Collection<SONConnection> getConnections(SON net){
		Collection<SONConnection> result = new ArrayList<SONConnection>();
		Collection<Node> nodes = getNodes(net);
		for(Node node : nodes){
			Collection<SONConnection> connections = net.getSONConnections(node);
			for(SONConnection con : connections){
				if(con.getFirst() != node && nodes.contains(con.getFirst())){
					result.add(con);
				}else if(con.getSecond() != node && nodes.contains(con.getSecond())){
					result.add(con);
				}
			}
		}
		return result;
	}
}
