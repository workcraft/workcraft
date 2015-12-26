package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.UnboundedException;
import org.workcraft.plugins.son.util.Marking;

public class ASONAlg extends RelationAlgorithm{

	public ASONAlg(SON net) {
		super(net);
	}

	public boolean isEnabled (Marking marking, TransitionNode t) {
		// gather number of connections for each pre-condition
		for (Node n : getPrePNSet(t)) {
			if (!marking.contains(n)) {
				return false;
			}
		}
		return true;
	}

	public Marking fire(Marking marking, TransitionNode t) throws UnboundedException{
		Marking result = new Marking();

		for(Node n : getPostPNSet(t)){
			if(marking.contains(n))
				throw new UnboundedException(net.getNodeReference(n), n);
			else
				result.add((PlaceNode)n);
		}

		for(Node n : marking){
			if(!getPrePNSet(t).contains(n))
				result.add((PlaceNode)n);
		}

		return result;
	}

	public List<Marking> getReachableMarkings(ONGroup group) throws UnboundedException{
		List<Marking> result = new ArrayList<Marking>();
		Collection<Marking> search = new ArrayList<Marking>();

		Map<TransitionNode, ArrayList<Marking>> visited = new HashMap<TransitionNode, ArrayList<Marking>>();

		Marking initial = new Marking();
		for(Condition c : getInitial(group)){
			initial.add(c);
		}

		result.add(initial);
		search.add(initial);

    	boolean hasEnabled = true;
        while(hasEnabled){
    		Collection<Marking> newMarkings = new ArrayList<Marking>();

    		for(Marking marking : search){
    	        for(TransitionNode t : group.getTransitionNodes()){
        	        if(!visited(visited, t, marking) && isEnabled(marking, t)){
        	        	Marking newMarking = fire(marking, t);
        	        	if(!contains(result, newMarking) && !contains(newMarkings, newMarking))
        	        		newMarkings.add(newMarking);
    	        	}
        	        if(visited.containsKey(t)){
        	        	 ArrayList<Marking> markings = visited.get(t);
        	        	 markings.add(marking);
        	        	 visited.put(t, markings);
        	        }else{
        	        	ArrayList<Marking> markings = new ArrayList<Marking>();
        	        	markings.add(marking);
        	        	visited.put(t, markings);
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

	public boolean contains(Collection<Marking> result, Marking marking){
		for(Marking ref : result){
			if(ref.equals(marking)){
				return true;
			}
		}
		return false;
	}
	private boolean visited(Map<TransitionNode, ArrayList<Marking>> visited, TransitionNode t, Marking marking){
		if(visited.containsKey(t))
			for(Marking ref : visited.get(t)){
				if(ref.equals(marking))
					return true;
			}
		return false;
	}

}
