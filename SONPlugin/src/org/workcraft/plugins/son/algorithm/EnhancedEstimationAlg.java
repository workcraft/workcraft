package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.exception.AlternativeStructureException;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;


public class EnhancedEstimationAlg extends EstimationAlg{

	protected Map<Time, Boolean[]> modify;
	
	public EnhancedEstimationAlg(SON net, Interval d, Granularity g, ScenarioRef s) {
		super(net, d, g, s);		
		modify = new HashMap<Time, Boolean[]>();
	}
	
	public void estimateFinish(Time n) throws TimeOutOfBoundsException, AlternativeStructureException{
		//nearest right nodes of n with specified finish time intervals
		Set<Time> rBoundary = new HashSet<Time>();
		// nodes on paths from n to RBoundary nodes
		Set<Time> rNeighbourhood = new HashSet<Time>();
		rNeighbourhood.add(n);
		init();
		
		if(scenario != null ){
			findRightBoundary(n, rBoundary, rNeighbourhood);
			backwardBFSTimes(n, rBoundary, rNeighbourhood);
		}else{
			 throw new AlternativeStructureException("A scenario is required for the estimation");
		}
	}
	
	protected void init(){
        //assign specified value from connections to nodes
        for (SONConnection con : net.getSONConnections()) {
            if (con.getSemantics() == Semantics.PNLINE) {
                if (con.getTime().isSpecified()) {
                    Node first = con.getFirst();
                    if (first instanceof Time) {
                        ((Time) first).setEndTime(con.getTime());
                    }
                    Node second = con.getSecond();
                    if (second instanceof Time) {
                        ((Time) second).setStartTime(con.getTime());
                    }
                }
            }
        }
	}
	
	private void findRightBoundary (Time n, Set<Time> boundary, Set<Time> neighbourhood){
		Collection<Node> nodes = scenario.getNodes(net);
		// nodes used for forward boundary searching
		Set<Time> working = new HashSet<Time>();
		working.add(n);
		while(!working.isEmpty()){
			Set<Time> nextWorking = new HashSet<Time>();
			for (Time t : working){
				Collection<Time> postset = getCausalPostset(t, nodes);
				if(postset.isEmpty()){
					boundary.add(t);
				}else{
					for(Time nd : postset){
						neighbourhood.add(nd);
						if(nd.getEndTime().isSpecified()){
							boundary.add(nd);
						}else{
							nextWorking.add(nd);
						}
					}
				}
			}
			working = nextWorking;
		}
	}
	
	private void backwardBFSTimes(Time n, Set<Time> boundary, Set<Time> neighbourhood) throws TimeOutOfBoundsException{
		Collection<Node> nodes = scenario.getNodes(net);
		Map<Time, Integer> visit = new HashMap<Time, Integer>();
		Set<Time> working = boundary;
		
		while((working.size()!=1) || (!working.contains(n))){
			//System.out.println("working"+net.toString(working));
			Set<Time> nextWorking = new HashSet<Time>();
			for (Time t : working){
				if(!t.getDuration().isSpecified()){
					t.setDuration(defaultDuration);
					addModify(t, 2);
				}
				Collection<Time> intersection = new HashSet<Time>(neighbourhood);
				Collection<Time> preset = getCausalPreset(t, nodes);
				intersection.retainAll(preset);
				//System.out.println("intersection"+net.toString(intersection));
				for(Time nd : intersection){
					nextWorking.add(nd);
					if(!visit.containsKey(nd))
						visit.put(nd, 1);
					else
						visit.put(nd, visit.get(nd)+1);
					
					if(t.getEndTime().isSpecified()){
						Interval end = granularity.subtractTD(t.getEndTime(), t.getDuration());
						if(!nd.getEndTime().isSpecified()){
							nd.setEndTime(end);
							addModify(t, 3);
						}else{
							if(!nd.getEndTime().equals(end)){
								nd.setEndTime(Interval.getOverlapping(end, nd.getEndTime())); 
								addModify(t, 3);
							}
						}
					}
				}
				
			}
			
			Collection<Time> remove = new ArrayList<Time>();
			for(Time nd : nextWorking){
				Collection<Time> postset = getCausalPostset(nd, nodes);
				if(visit.get(nd) != postset.size()){
					remove.add(nd);
					//System.out.println("remove"+net.getNodeReference(nd));
				}else{
					//visit.put(nd, 0);
				}
			}
			nextWorking.removeAll(remove);
			//System.out.println("next working "+net.toString(nextWorking));
			working = nextWorking;
		}
	}
	
	private void addModify(Time time, int b){
		if(!modify.containsKey(time)){
			Boolean[] p = new Boolean[3];
			p[b-1] = true;
			modify.put(time, p);
		}
		else{
			Boolean[] p = modify.get(time);
			p[b-1] = true;
			modify.put(time, p);
		}
	}
	
	
}
