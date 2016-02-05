package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.exception.AlternativeStructureException;
import org.workcraft.plugins.son.exception.TimeEstimationException;
import org.workcraft.plugins.son.exception.TimeInconsistencyException;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;

public class EntireEstimationAlg extends EstimationAlg{

	private boolean narrow;
	private boolean twoDir;
	ConsistencyAlg consistency;

	public EntireEstimationAlg(SON net, Interval d, Granularity g, ScenarioRef s, boolean narrow, boolean twoDir) {
		super(net, d, g, s);
		this.narrow = narrow;
		this.twoDir = twoDir;
		consistency = new ConsistencyAlg(net);
	}

	public void entireEst() throws AlternativeStructureException, TimeInconsistencyException, TimeEstimationException{
		if(scenario == null)
			throw new AlternativeStructureException("select a scenario first");
		//add super initial to SON
		Condition superIni = net.createCondition(null, null);
		scenario.add(net.getNodeReference(superIni));

		SONAlg sonAlg = new SONAlg(net);
		Collection<PlaceNode> initial = sonAlg.getSONInitial();
		Collection<PlaceNode> finalM = sonAlg.getSONFinal();

		//add arcs from super initial to SON initial
		for(PlaceNode p : initial){
			try {
				SONConnection con = net.connect(superIni, p, Semantics.PNLINE);
				scenario.add(net.getNodeReference(con));
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
		}

		Interval end = null;

		try {
			end = getEstimatedEndTime(superIni);
		}
		catch (TimeEstimationException e){
			net.remove(superIni);
			throw new TimeEstimationException("");
		} catch (TimeOutOfBoundsException e){
			net.remove(superIni);
			e.printStackTrace();
			return;
		}

		superIni.setEndTime(end);

		LinkedList<Time> visited = new LinkedList<Time>();
		visited.add(superIni);

		//assign specified value from connections to nodes
		for(SONConnection con : net.getSONConnections()){
			if(con.getSemantics() == Semantics.PNLINE){
				if(con.getTime().isSpecified()){
					Node first = con.getFirst();
					if(first instanceof Time){
						((Time)first).setEndTime(con.getTime());
					}
					Node second = con.getSecond();
					if(second instanceof Time){
						((Time)second).setStartTime(con.getTime());
					}
				}
			}
		}

		try {
			forwardDFSEntire(visited, scenario.getNodes(net), initial, finalM);
		} catch (TimeOutOfBoundsException e) {
			net.remove(superIni);
			e.printStackTrace();
			return;
		}

		//move estimated time to connections
		for(SONConnection con : net.getSONConnections()){
			if(con.getSemantics() == Semantics.PNLINE){
				if(!con.getTime().isSpecified()){
					Node first = con.getFirst();
					if(first instanceof Time){
						con.setTime(((Time)first).getEndTime());
						con.setTimeLabelColor(color);
					}
				}
			}
		}

		for(Time time : net.getTimeNodes()){
			Interval defTime = new Interval();
			if(!initial.contains(time)){
				time.setStartTime(defTime);
			}
			if(!finalM.contains(time)){
				time.setEndTime(defTime);
			}
		}
		//remove super initial
		net.remove(superIni);
	}

    private void forwardDFSEntire(LinkedList<Time> visited, Collection<Node> nodes, Collection<PlaceNode> initial, Collection<PlaceNode> finalM) throws TimeOutOfBoundsException, TimeInconsistencyException  {
        Time last = visited.getLast();
    	LinkedList<Time> neighbours = getCausalPostset(last, nodes);

        for(Time t : neighbours){
        	if(!visited.contains(t)){
     			if(!t.getStartTime().isSpecified()){
    				t.setStartTime(last.getEndTime());
    				if(initial.contains(t)){
    					((Condition)t).setStartTimeColor(color);
    				}
    			}else{
    				if(!t.getStartTime().equals(last.getEndTime()))
    					throw new TimeInconsistencyException("Time inconsistency: "+net.getNodeReference(t));
    				if(narrow){
    					System.out.println("narrow");
    					t.setStartTime(Interval.getOverlapping(t.getStartTime(), last.getStartTime()));
    				}
    			}
     			if(!t.getDuration().isSpecified()){
     				t.setDuration(defaultDuration);
     				if(t instanceof PlaceNode){
     					((PlaceNode)t).setDurationColor(color);
     				}else if(t instanceof Block){
     					((Block)t).setDurationColor(color);
     				}
     			}
     			if(!t.getEndTime().isSpecified()){
     				Interval time = granularity.plusTD(t.getStartTime(), t.getDuration());
     				t.setEndTime(time);
    				if(finalM.contains(t)){
    					((Condition)t).setEndTimeColor(color);
    				}
     			}else{
     				ArrayList<String> check= consistency.nodeConsistency(t, t.getStartTime(), t.getEndTime(), t.getDuration(), g);
    				if(!check.isEmpty())
    					throw new TimeInconsistencyException("Time inconsistency: "+net.getNodeReference(t));
    				if(narrow){
    					t.setEndTime(Interval.getOverlapping(t.getStartTime(), last.getStartTime()));
    				}
    			}

        		visited.add(t);
        		forwardDFSEntire(visited, nodes, initial, finalM);
        	}
        }
    }
}
