package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;

public class ReachabilityAlg {

	private BSONAlg bsonAlg;
	private RelationAlgorithm relationAlg;
	private Collection<ONGroup> upperGroups;
	private Map<Condition, Collection<Phase>> phases;

	private static Collection<Node> pathResult =new ArrayList<Node>();

	public ReachabilityAlg(SON net) {
		bsonAlg = new BSONAlg(net);
		relationAlg = new RelationAlgorithm(net);

		upperGroups = bsonAlg.getUpperGroups(net.getGroups());
		phases = bsonAlg.getAllPhases();
	}

    //get path between a given initial node and a set of final nodes. (recursion)
    private void dfs(LinkedList<Node> visited, Collection<Node> v,  Collection<TransitionNode[]> before) {
        LinkedList<Node> post = getCausalPreset(visited.getLast(), before);

        if (v.contains(visited.getLast())) {
            pathResult.addAll(visited);
        }

        // examine post nodes
        for (Node node : post) {
            if (visited.contains(node)) {
                continue;
            }
            if (v.contains(node)) {
                visited.add(node);
                pathResult.addAll(visited);
                visited.removeLast();
                break;
            }
        }
        // in depth-first, recursion needs to come after visiting post nodes
        for (Node node : post) {
            if (visited.contains(node) || node.equals(v)) {
                continue;
            }
            visited.addLast(node);
            dfs(visited, v, before);
            visited.removeLast();

        }
    }

    public Collection<Node> getCausalPredecessors (Node s, Collection<Node> v){
    	pathResult.clear();
    	LinkedList<Node> visited = new LinkedList<Node>();
    	Collection<TransitionNode[]> before = getBeforeRelations();
    	visited.add(s);
    	dfs(visited, v, before);
    	return pathResult;
    }

	private Collection<TransitionNode[]> getBeforeRelations(){
		 Collection<TransitionNode[]>  result = new ArrayList<TransitionNode[]>();

		for(ONGroup group : upperGroups){
			for(TransitionNode e : group.getTransitionNodes()){
				result.addAll( bsonAlg.before(e, phases));
			}
		}

		return result;
	}

    private LinkedList<Node> getCausalPreset(Node n, Collection<TransitionNode[]> before){
    	LinkedList<Node> result = new LinkedList<Node>();

    	if(relationAlg.isInitial(n) && (n instanceof Condition)){
    		result.addAll(relationAlg.getPostBhvSet((Condition)n));
    	}

    	for(TransitionNode[] pre : before){
    		if(pre[1] == n)
    			result.add(pre[0]);
    	}

    	result.addAll(relationAlg.getPrePNSet(n));

    	if(n instanceof TransitionNode){
    		result.addAll(relationAlg.getPreASynEvents((TransitionNode)n));
    	}

    	return result;
    }
}
