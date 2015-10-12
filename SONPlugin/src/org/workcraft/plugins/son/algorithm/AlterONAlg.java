package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.UnboundedException;
import org.workcraft.plugins.son.util.MarkingRef;

public class AlterONAlg extends RelationAlgorithm{

	public AlterONAlg(SON net) {
		super(net);
	}

	public boolean isEnabled (MarkingRef marking, TransitionNode t) {
		// gather number of connections for each pre-condition
		for (Node n : getPrePNSet(t)) {
			if (!marking.containsNode(n, net)) {
				return false;
			}
		}
		return true;
	}

	public MarkingRef fire(MarkingRef marking, TransitionNode t) throws UnboundedException{
		MarkingRef result = new MarkingRef();
		Collection<Node> nodes = marking.getNodes(net);

		for(Node n : getPostPNSet(t)){
			if(nodes.contains(n))
				throw new UnboundedException(net.getNodeReference(n)+ marking.toString());
			else
				result.add(net.getNodeReference(n));
		}

		for(Node n : nodes){
			if(!getPrePNSet(t).contains(n))
				result.add(net.getNodeReference(n));
		}

		return result;
	}

	public Collection<MarkingRef> getReachableMarkings(ONGroup group) throws UnboundedException{
		Collection<MarkingRef> result = new ArrayList<MarkingRef>();

		Map<TransitionNode, MarkingRef> visited = new HashMap<TransitionNode, MarkingRef>();

		MarkingRef initial = new MarkingRef();
		for(Condition c : getONInitial(group.getConditions())){
			initial.add(net.getNodeReference(c));
		}
		result.add(initial);

    	boolean hasEnabled = true;
        while(hasEnabled){
    		Collection<MarkingRef> newMarkings = new ArrayList<MarkingRef>();
    		for(MarkingRef marking : result){
    	        for(TransitionNode t : group.getTransitionNodes()){
        	        if(!visited(visited, t, marking)){
        	        	if(visited.containsKey(t) && !visited.get(t).equals(marking))
        	        		System.out.println(net.getNodeReference(t)+"1"+ visited.get(t) +visited.get(t).equals(marking)+ marking);
    	        		if(isEnabled(marking, t)){
	    	        		newMarkings.add(fire(marking, t));
    	        		}
    	        	}
	        		visited.put(t, marking);
    	        }
    		}

    		if(newMarkings.isEmpty()){
    			hasEnabled = false;
    		}else{
    			result.addAll(newMarkings);
    		}
        }

		return result;
	}

	private boolean visited(Map<TransitionNode, MarkingRef> visited, TransitionNode t, MarkingRef marking){
		for(TransitionNode key : visited.keySet()){
			if(key == t)
				if(visited.get(key).equals(marking)){
					System.out.println("visited");
					return true;
				}
		}
		return false;
	}

	}
