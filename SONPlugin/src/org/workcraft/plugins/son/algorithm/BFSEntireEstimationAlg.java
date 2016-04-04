package org.workcraft.plugins.son.algorithm;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.exception.AlternativeStructureException;
import org.workcraft.plugins.son.exception.SyncCycleException;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.exception.TimeEstimationException;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;

public class BFSEntireEstimationAlg extends DFSEstimationAlg{
	
    private boolean twoDir;
    private Condition superIni;
    private Condition superFinal;

	public BFSEntireEstimationAlg(SON net, Interval d, Granularity g, ScenarioRef s, boolean twoDir) throws AlternativeStructureException, SyncCycleException {
		super(net, d, g, s);
        this.twoDir = twoDir;	
        if(!getSyncCPs().isEmpty())
        	throw new SyncCycleException();
		// TODO Auto-generated constructor stub
	}
	
	public void estimateEntire() throws TimeEstimationException, TimeOutOfBoundsException{
		addSuperNodes();
		forwardBFSsonTimes((Time)superIni);
		if(twoDir) backwardBFSsonTimes((Time)superFinal);

	}
	
    public void finalize(){
	    net.remove(superIni);
	    if (twoDir) net.remove(superFinal);
	    
        SONAlg sonAlg = new SONAlg(net);
        Collection<PlaceNode> initial = sonAlg.getSONInitial();
        Collection<PlaceNode> finalM = sonAlg.getSONFinal();
                
        for(SONConnection con : scenario.getConnections(net)){
        	if(con.getSemantics() == Semantics.PNLINE){
        		con.setTime(((Time)con.getFirst()).getEndTime());
        	}
        }
       
        Interval defTime = new Interval();
        for (Time time : net.getTimeNodes()) {
            if (!initial.contains(time)) {
                time.setStartTime(defTime);
            }
            if (!finalM.contains(time)) {
                time.setEndTime(defTime);
            }
        }
    }

	private void addSuperNodes() throws TimeEstimationException {
	    //add super initial to SON
	    superIni = net.createCondition("superIni", null);
	    scenario.add(net.getNodeReference(superIni));
	    SONAlg sonAlg = new SONAlg(net);
	    Collection<PlaceNode> initial = sonAlg.getSONInitial();
	    Collection<PlaceNode> finalM = sonAlg.getSONFinal();
	
	    //add arcs from super initial to SON initial
	    for (PlaceNode p : initial) {
	        try {
	            SONConnection con = net.connect(superIni, p, Semantics.PNLINE);
	            scenario.add(net.getNodeReference(con));
	        } catch (InvalidConnectionException e) {
	            e.printStackTrace();
	        }
	    }
	
	    try {
	        estimateEndTime(superIni);
	    } catch (TimeEstimationException e) {
	        net.remove(superIni);
	        throw new TimeEstimationException("Fail to set estimated time for super initial node");
	    } catch (TimeOutOfBoundsException e) {
	        net.remove(superIni);
	        e.printStackTrace();
	        return;
	    }
	    
	    LinkedList<Time> visited = new LinkedList<>();
	    visited.add(superIni);
	
	    superFinal = null;
	    //super final
	    if (twoDir) {
	        superFinal = net.createCondition("superFinal", null);
	        scenario.add(net.getNodeReference(superFinal));
	
	        for (PlaceNode p : finalM) {
	            try {
	                SONConnection con = net.connect(p, superFinal, Semantics.PNLINE);
	                scenario.add(net.getNodeReference(con));
	            } catch (InvalidConnectionException e) {
	                e.printStackTrace();
	            }
	        }

	        try {
	            estimateStartTime(superFinal);
	        } catch (TimeEstimationException e) {
	            net.remove(superFinal);
	            throw new TimeEstimationException("");
	        } catch (TimeOutOfBoundsException e) {
	            net.remove(superFinal);
	            e.printStackTrace();
	            return;
	        }
	    }
	}
	
	private void forwardBFSsonTimes(Time n) throws TimeOutOfBoundsException, TimeEstimationException{
		List<Time> working = new ArrayList<>();
		Collection<Node> nodes = scenario.getNodes(net);
		Map<Time, Integer> visit = new HashMap<Time, Integer>();
		working.add(n);
		
		while(!working.isEmpty()){
			List<Time> nextWorking = new ArrayList<Time>();
			for(Time m : working){
				for(Time nd :  getCausalPostset(m, nodes)){
					nextWorking.add(nd);
					if(!visit.containsKey(nd))
						visit.put(nd, 1);
					else
						visit.put(nd, visit.get(nd)+1);
					
					if(nd.getStartTime().isSpecified()){
						Interval i = Interval.getOverlapping(m.getEndTime(), nd.getStartTime());
						if(i != null){
							m.setEndTime(i);
						}
						else{
				            throw new TimeEstimationException(net.getNodeReference(m)+".finish (" + m.getEndTime().toString()+
				                    ") is inconsistent with " + net.getNodeReference(nd) + ".start (" + nd.getStartTime().toString()+")");
						}
					}
				}
				for(Time nd :  getCausalPostset(m, nodes)){
					nd.setStartTime(m.getEndTime());
				}
			}
			
			Collection<Time> remove = new ArrayList<Time>();
			for(Time nd : nextWorking){
				Collection<Time> preset = getCausalPreset(nd, nodes);
				if(visit.get(nd) == preset.size()){
					if(preset.size() > 1){
						for(Time ndin : preset){
							ndin.setEndTime(nd.getStartTime());
						}
					}
					
					if(!nd.getDuration().isSpecified()){
						nd.setDuration(defaultDuration);
					}
					
					Interval i = granularity.plusTD(nd.getStartTime(), nd.getDuration());
					if(!nd.getEndTime().isSpecified()){
						nd.setEndTime(i);					
					}else{
						nd.setEndTime(Interval.getOverlapping(nd.getEndTime(), i));
					}
					
					Interval i2 = granularity.subtractTT(nd.getStartTime(), nd.getEndTime());
					nd.setDuration(Interval.getOverlapping(nd.getDuration(), i2));
					visit.put(nd, 0);
				}else{
					remove.add(nd);
				}
			}
			nextWorking.removeAll(remove);
			working = nextWorking;
		}
	}
	
	private void backwardBFSsonTimes(Time n) throws TimeOutOfBoundsException, TimeEstimationException{
		List<Time> working = new ArrayList<>();
		Collection<Node> nodes = scenario.getNodes(net);
		Map<Time, Integer> visit = new HashMap<Time, Integer>();
		working.add(n);
		
		while(!working.isEmpty()){
			List<Time> nextWorking = new ArrayList<Time>();
			for(Time m : working){
				for(Time nd :  getCausalPreset(m, nodes)){
					nextWorking.add(nd);
					if(!visit.containsKey(nd))
						visit.put(nd, 1);
					else
						visit.put(nd, visit.get(nd)+1);
					
					if(nd.getEndTime().isSpecified()){
						Interval i = Interval.getOverlapping(m.getStartTime(), nd.getEndTime());					
						if(i != null){
							m.setEndTime(i);
						}
						else{
				            throw new TimeEstimationException(net.getNodeReference(nd)+".finish (" + nd.getEndTime().toString()+
				                    ") is inconsistent with " + net.getNodeReference(m) + ".start (" + m.getEndTime().toString()+")");
						}
						
					}
				}
				for(Time nd :  getCausalPreset(m, nodes)){
					nd.setEndTime(m.getStartTime());
				}
			}
			
			Collection<Time> remove = new ArrayList<Time>();
			for(Time nd : nextWorking){
				Collection<Time> postset = getCausalPostset(nd, nodes);
				if(visit.get(nd) == postset.size()){
					if(postset.size() > 1){
						for(Time ndin : postset){
							ndin.setStartTime(nd.getEndTime());
						}
					}
					
					if(!nd.getDuration().isSpecified()){
						nd.setDuration(defaultDuration);
					}
					
					Interval i = granularity.subtractTD(nd.getEndTime(), nd.getDuration());
					if(!nd.getStartTime().isSpecified()){
						nd.setStartTime(i);					
					}else{
						nd.setStartTime(Interval.getOverlapping(nd.getStartTime(), i));
					}
					
					Interval i2 = granularity.subtractTT(nd.getStartTime(), nd.getEndTime());
					nd.setDuration(Interval.getOverlapping(nd.getDuration(), i2));
					visit.put(nd, 0);
				}else{
					remove.add(nd);
				}
			}
			nextWorking.removeAll(remove);
			working = nextWorking;
		}
	}
}