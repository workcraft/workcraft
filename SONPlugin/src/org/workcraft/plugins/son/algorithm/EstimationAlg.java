package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Before;
import org.workcraft.plugins.son.Interval;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.InconsistentTimeException;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.granularity.HourMins;
import org.workcraft.plugins.son.granularity.TimeGranularity;
import org.workcraft.plugins.son.granularity.YearYear;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;

public class EstimationAlg extends TimeAlg{

	private SON net;
	//interval[0] is first found specified time interval, interval[1] is the accumulated durations
	private Collection<Interval[]> timeDuation =new ArrayList<Interval[]>();
	//default duration provided by user
	private final Interval defaultDuration;
	private TimeGranularity granularity = null;

	public EstimationAlg(SON net, Interval defaultDuration, Granularity g) {
		super(net);
		this.net = net;
		this.defaultDuration = defaultDuration;

		if(g == Granularity.YEAR_YEAR){
			granularity = new YearYear();
		}else if (g == Granularity.HOUR_MINS){
			granularity = new HourMins();
		}
	}

	public Interval EstimateStartTime(Node n, Before before) throws InconsistentTimeException, TimeOutOfBoundsException{
		Interval result = new Interval();

		timeDuation.clear();
    	BSONAlg bsonAlg = new BSONAlg(net);
    	if(before == null)
    		before =  bsonAlg.getBeforeList();

    	LinkedList<Time> visited = new LinkedList<Time>();
    	visited.add((Time)n);

    	backwardDFS(visited, before, net.getComponents());

		Collection<Interval> possibleTimes = new ArrayList<Interval>();
		for(Interval[] interval : timeDuation){
			possibleTimes.add(granularity.plusTD(interval[0], interval[1]));
    	}

		for(Interval interval : possibleTimes){
			result = result.getOverlapping(interval);
		}

		if(result != null)
			return result;
		else
			throw new InconsistentTimeException("");
	}

	public Interval EstimateEndTime(Node n, Before before) throws InconsistentTimeException, TimeOutOfBoundsException{
		Interval result = new Interval();

		timeDuation.clear();
    	BSONAlg bsonAlg = new BSONAlg(net);
    	if(before == null)
    		before =  bsonAlg.getBeforeList();

    	LinkedList<Time> visited = new LinkedList<Time>();
    	visited.add((Time)n);

    	forwardDFS(visited, before, net.getComponents());

		Collection<Interval> possibleTimes = new ArrayList<Interval>();
		for(Interval[] interval : timeDuation){
			possibleTimes.add(granularity.subtractTD(interval[0], interval[1]));
    	}

		for(Interval interval : possibleTimes){
			result = result.getOverlapping(interval);
		}

		if(result != null)
			return result;
		else
			throw new InconsistentTimeException("");

	}

    private void forwardDFS(LinkedList<Time> visited, Before before, Collection<Node> nodes)  {
        LinkedList<Time> neighbours = getCausalPostset(visited.getLast(), before, nodes);

        if (visited.getLast().getEndTime().isSpecified()) {
        	Interval[] result = new Interval[2];
        	result[0] = visited.getLast().getEndTime();
        	result[1] = durationAccumulator(visited);
            timeDuation.add(result);
        }

        // examine post nodes
        for (Time node : neighbours) {
        	SONConnection con = net.getSONConnection(node, visited.getLast());
            if (visited.contains(node)) {
                continue;
            }
            if (con.getTime().isSpecified()) {
            	Interval[] result = new Interval[2];
            	result[0] = con.getTime();
            	result[1] = durationAccumulator(visited);
                timeDuation.add(result);
                break;
            }
        }
        // in depth-first, recursion needs to come after visiting post nodes
        for (Time node : neighbours) {
        	SONConnection con = net.getSONConnection(node, visited.getLast());
            if (visited.contains(node) || con.getTime().isSpecified()) {
                continue;
            }
            visited.addLast(node);
            forwardDFS(visited, before, nodes);
            visited.removeLast();
        }
    }

    private void backwardDFS(LinkedList<Time> visited, Before before, Collection<Node> nodes) {
        LinkedList<Time> neighbours = getCausalPreset(visited.getLast(), before, nodes);

        if (visited.getLast().getStartTime().isSpecified()) {
        	Interval[] result = new Interval[2];
        	result[0] = visited.getLast().getStartTime();
        	result[1] = durationAccumulator(visited);
            timeDuation.add(result);
        }

        // examine post nodes
        for (Time node : neighbours) {
        	SONConnection con = net.getSONConnection(visited.getLast(), node);
            if (visited.contains(node)) {
                continue;
            }
            if (con.getTime().isSpecified()) {
            	Interval[] result = new Interval[2];
            	result[0] = con.getTime();
            	result[1] = durationAccumulator(visited);
                timeDuation.add(result);
                break;
            }
        }
        // in depth-first, recursion needs to come after visiting post nodes
        for (Time node : neighbours) {
        	SONConnection con = net.getSONConnection(visited.getLast(), node);
            if (visited.contains(node) || con.getTime().isSpecified()) {
                continue;
            }
            visited.addLast(node);
            backwardDFS(visited, before, nodes);
            visited.removeLast();

        }
    }

    private Interval durationAccumulator (LinkedList<Time> visited){
    	Interval result = new Interval(0000, 0000);
    	Time first = visited.getFirst();
    	for(Time time : visited){
    		if(time != first){
	    		if (time.getDuration().isSpecified())
	    			result = result.add(time.getDuration());
	    		else{
	    			result = result.add(defaultDuration);
	    		}
    		}
    	}
    	return result;
    }

    private LinkedList<Time> getCausalPreset(Time n, Before before, Collection<Node> nodes){
    	LinkedList<Time> preSet = new LinkedList<Time>();
    	LinkedList<Time> result = new LinkedList<Time>();

    	if(isInitial(n) && (n instanceof Condition)){
    		preSet.addAll(getPostBhvSet((Condition)n));
    	}

    	for(TransitionNode[] pre : before){
    		if(pre[1] == n)
    			preSet.add(pre[0]);
    	}

    	for(Node node : getPrePNSet(n)){
    		if(node instanceof Time)
    			preSet.add((Time)node);
    	}

    	if(n instanceof TransitionNode){
    		preSet.addAll(getPreASynEvents((TransitionNode)n));
    	}

    	for(Time node : preSet){
    		if(nodes.contains(node))
    			result.add(node);
    	}

    	return result;
    }

    private LinkedList<Time> getCausalPostset(Time n, Before before, Collection<Node> nodes){
    	LinkedList<Time> postSet = new LinkedList<Time>();
    	LinkedList<Time> result = new LinkedList<Time>();

    	if(isFinal(n) && (n instanceof Condition)){
    		postSet.addAll(getPostBhvSet((Condition)n));
    	}

    	for(TransitionNode[] post : before){
    		if(post[0] == n)
    			postSet.add(post[1]);
    	}

    	for(Node node :getPostPNSet(n)){
    		if(node instanceof Time)
    			postSet.add((Time)node);
    	}

    	if(n instanceof TransitionNode){
    		postSet.addAll(getPostASynEvents((TransitionNode)n));
    	}

    	for(Time node : postSet){
    		if(nodes.contains(node))
    			result.add(node);
    	}

    	return result;
    }
}
