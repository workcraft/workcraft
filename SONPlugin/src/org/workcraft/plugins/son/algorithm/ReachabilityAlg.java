	package org.workcraft.plugins.son.algorithm;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Before;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;

public class ReachabilityAlg extends RelationAlgorithm{

	private static Collection<Node> predecessors =new HashSet<Node>();
	private SON net;

	public ReachabilityAlg(SON net) {
		super(net);
		this.net = net;
	}

    //get path between a given initial node and a set of final nodes. (recursion)
//    private void dfs(LinkedList<Node> visited, Collection<Node> v,  Before before) {
//        LinkedList<Node> post = getCausalPreset(visited.getLast(), before);
//
//        if (v.contains(visited.getLast())) {
//            pathResult.addAll(visited);
//        }
//
//        // examine post nodes
//        for (Node node : post) {
//            if (visited.contains(node)) {
//                continue;
//            }
//            if (v.contains(node)) {
//                visited.add(node);
//                pathResult.addAll(visited);
//                visited.removeLast();
//                break;
//            }
//        }
//        // in depth-first, recursion needs to come after visiting post nodes
//        for (Node node : post) {
//            if (visited.contains(node) || node.equals(v)) {
//                continue;
//            }
//            visited.addLast(node);
//            dfs(visited, v, before);
//            visited.removeLast();
//
//        }
//    }

    private void CausalPredecessors(LinkedList<Node> visited, Node n, Before before){
    	predecessors.add(n);
 		visited.add(n);

    	for(Node n2 : getCausalPreset(n, before)){
    		if(!visited.contains(n2))
    			CausalPredecessors(visited, n2, before);
    	}
    }

    public Collection<Node> getCausalPredecessors (Node s){
    	predecessors.clear();
    	LinkedList<Node> visited = new LinkedList<Node>();
    	BSONAlg bsonAlg = new BSONAlg(net);
    	Before before =  bsonAlg.getBeforeList();
    	visited.add(s);
    	//dfs(visited, v, before);
    	CausalPredecessors(visited, s, before);
    	return predecessors;
    }


    private LinkedList<Node> getCausalPreset(Node n, Before before){
    	LinkedList<Node> result = new LinkedList<Node>();

    	if(isInitial(n) && (n instanceof Condition)){
    		result.addAll(getPostBhvSet((Condition)n));
    	}

    	for(TransitionNode[] pre : before){
    		if(pre[1] == n)
    			result.add(pre[0]);
    	}

    	result.addAll(getPrePNSet(n));

    	if(n instanceof TransitionNode){
    		result.addAll(getPreASynEvents((TransitionNode)n));
    	}

    	return result;
    }
}
