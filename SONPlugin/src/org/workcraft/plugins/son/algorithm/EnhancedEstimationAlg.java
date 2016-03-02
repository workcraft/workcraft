package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;


public class EnhancedEstimationAlg extends EstimationAlg{

	protected SON net;
	protected Map<Time, Integer> modify;
	
	public EnhancedEstimationAlg(SON net, Interval d, Granularity g, ScenarioRef s) {
		super(net, d, g, s);		
		modify = new HashMap<Time, Integer>();
	}
	
	public void estimateFinish(Node n) throws TimeOutOfBoundsException{
		//nearest right nodes of n with specified finish time intervals
		Collection<Time> rBoundary = new ArrayList<Time>();
		// nodes on paths from n to RBoundary nodes
		Collection<Time> rNeighbourhood = new ArrayList<Time>();
		rNeighbourhood.add((Time)n);
		
		if(scenario != null ){
			findRightBoundary((Time)n, rBoundary, rNeighbourhood);
			backwardBFSTimes((Time)n, rBoundary, rNeighbourhood);
		}
	}
	
	private void findRightBoundary (Time n, Collection<Time> boundary, Collection<Time> neighbourhood){
		Collection<Node> nodes = scenario.getNodes(net);
		// nodes used for forward boundary searching
		List<Time> working = new ArrayList<Time>();
		working.add(n);
		while(!working.isEmpty()){
			List<Time> nextWorking = new ArrayList<Time>();
			for (Time t : working){
				Collection<Time> postset = getCausalPostset(t, nodes);
				if(postset.isEmpty()){
					boundary.add(t);
				}else{
					for(Time nt : postset){
						neighbourhood.add(nt);
						if(nt.getEndTime().isSpecified()){
							boundary.add(nt);
						}else{
							nextWorking.add(nt);
						}
					}
				}
			}
			working = nextWorking;
		}
	}
	
	private void backwardBFSTimes(Time n, Collection<Time> boundary, Collection<Time> neighbourhood) throws TimeOutOfBoundsException{
		Collection<Node> nodes = scenario.getNodes(net);
		Map<Time, Integer> visit = new HashMap<Time, Integer>();
		List<Time> working = new ArrayList<Time>();
		working.add(n);
		while(!working.isEmpty()){
			List<Time> nextWorking = new ArrayList<Time>();
			for (Time t : working){
				if(!t.getDuration().isSpecified()){
					t.setDuration(defaultDuration);
					if(modify.get(t)==null)
						modify.put(t, 0b010);
					else
						modify.put(t, modify.get(t)+0b010);
				}
				Collection<Time> intersection = new HashSet<Time>(neighbourhood);
				Collection<Time> postset = getCausalPostset(t, nodes);
				intersection.retainAll(postset);
				
				for(Time nd : intersection){
					if(visit.get(nd)==null)
						visit.put(nd, 1);
					else
						visit.put(nd, visit.get(t)+1);
					
					if(t.getEndTime().isSpecified()){
						Interval end = granularity.subtractTD(t.getEndTime(), t.getDuration());
						if(!nd.getEndTime().isSpecified()){
							nd.setEndTime(end);
						}else{
							nd.setEndTime(Interval.getOverlapping(end, nd.getEndTime())); 
							if(modify.get(nd)==null)
								modify.put(nd, 0b001);
							else
								modify.put(nd, modify.get(nd)+0b001);
						}
					}
				}
				
			}
			
		}
		
	}
	
}
