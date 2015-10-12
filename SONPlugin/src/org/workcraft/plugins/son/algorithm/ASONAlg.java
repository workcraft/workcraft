package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.UnboundedException;
import org.workcraft.plugins.son.util.MarkingRef;

public class ASONAlg extends RelationAlgorithm{

	public ASONAlg(SON net) {
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
				throw new UnboundedException(net.getNodeReference(n), n);
			else
				result.add(net.getNodeReference(n));
		}

		for(Node n : nodes){
			if(!getPrePNSet(t).contains(n))
				result.add(net.getNodeReference(n));
		}

		return result;
	}

	@SuppressWarnings("serial")
	public Collection<MarkingRef> getReachableMarkings(ONGroup group) throws UnboundedException{
		Collection<MarkingRef> result = new ArrayList<MarkingRef>();
		Collection<MarkingRef> search = new ArrayList<MarkingRef>();

		Map<TransitionNode, ArrayList<MarkingRef>> visited = new HashMap<TransitionNode, ArrayList<MarkingRef>>();

		MarkingRef initial = new MarkingRef();
		for(Condition c : getONInitial(group.getConditions())){
			initial.add(net.getNodeReference(c));
		}

		result.add(initial);
		search.add(initial);

    	boolean hasEnabled = true;
        while(hasEnabled){
    		Collection<MarkingRef> newMarkings = new ArrayList<MarkingRef>();

    		for(MarkingRef marking : search){
    	        for(TransitionNode t : group.getTransitionNodes()){
        	        if(!visited(visited, t, marking) && isEnabled(marking, t)){
        	        	MarkingRef newMarking = fire(marking, t);
        	        	if(!contains(result, newMarking) && !contains(newMarkings, newMarking))
        	        		newMarkings.add(newMarking);
    	        	}
        	        if(visited.containsKey(t)){
        	        	 ArrayList<MarkingRef> markings = visited.get(t);
        	        	 markings.add(marking);
        	        	 visited.put(t, markings);
        	        }else{
        	        	visited.put(t, new ArrayList<MarkingRef>(){
						{
        	        		add(marking);
        	        	}});
        	        }
    	        }
    		}

    		if(newMarkings.isEmpty()){
    			hasEnabled = false;
    		}else{
    			search = newMarkings;
    			result.addAll(newMarkings);
    		}
        }

		return result;
	}

	private boolean contains(Collection<MarkingRef> result, MarkingRef marking){
		for(MarkingRef ref : result){
			if(ref.equals(marking)){
				return true;
			}
		}
		return false;
	}
	private boolean visited(Map<TransitionNode, ArrayList<MarkingRef>> visited, TransitionNode t, MarkingRef marking){
		if(visited.containsKey(t))
			for(MarkingRef ref : visited.get(t)){
				if(ref.equals(marking))
					return true;
			}
		return false;
	}

}
