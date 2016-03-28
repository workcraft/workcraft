package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.AlternativeStructureException;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.granularity.HourMins;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Before;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;

public class ConsistencyAlg extends TimeAlg {
	
	private Before before;
    private Collection<ChannelPlace> sync;
    private  BSONAlg bsonAlg;

	public ConsistencyAlg(SON net, Interval d, Granularity g, ScenarioRef s) throws AlternativeStructureException {
        super(net, d, g, s);
        
        bsonAlg = new BSONAlg(net);
        before  =  bsonAlg.getBeforeList();
        sync = getSyncCPs();
	}
	
    @Override
    public void initialize(){
    	super.initialize();
    	for(ChannelPlace cp : scenario.getChannelPlaces(net)){
            TransitionNode input = (TransitionNode)net.getPreset(cp).iterator().next();
            TransitionNode output = (TransitionNode)net.getPostset(cp).iterator().next();
            
            cp.setStartTime(input.getEndTime());
            cp.setEndTime(output.getStartTime());
    	}
	}
	
    private Collection<ChannelPlace> getSyncCPs() {
        Collection<ChannelPlace> result = new HashSet<ChannelPlace>();
        HashSet<Node> nodes = new HashSet<>();
        nodes.addAll(net.getTransitionNodes());
        nodes.addAll(net.getChannelPlaces());
        CSONCycleAlg cycleAlg = new CSONCycleAlg(net);

        for (Path path : cycleAlg.syncCycleTask(nodes)) {
            for (Node node : path) {
                if (node instanceof ChannelPlace) {
                    result.add((ChannelPlace) node);
                }
            }
        }
        return result;
    }
	
    public ArrayList<String> nodeConsistency(Node n) {
        ArrayList<String> result = new ArrayList<>();
        
        Time node = (Time)n;
        
        Interval start = node.getStartTime();
        Interval end = node.getEndTime();
        Interval dur = node.getDuration();
        
        if (!start.isSpecified() || !end.isSpecified() || !dur.isSpecified()) {
            result.add("Fail to run node consistency checking: node has unspecified value.");
            return result;
        }
        
        Integer tsl = start.getMin();
        Integer tsu = start.getMax();
        Integer tfl = end.getMin();
        Integer tfu = end.getMax();
        
        //Condition 3
        if (!(tsl <= tfl)) {
            result.add("Node inconsistency: minStart"
                     + nodeStr(node) + timeStr(tsl.toString()) 
                     + " > minEnd" + nodeStr(node) + timeStr(tfl.toString()) + ".");
        }
        if (!(tsu <= tfu)) {
            result.add("Node inconsistency: maxStart"
                     + nodeStr(node) + timeStr(tsu.toString()) 
                     + " > maxEnd" + nodeStr(node) + timeStr(tfu.toString()) + ".");
        }
        
        //Condition 6
        Interval i = null;
        try {
            i = granularity.plusTD(start, dur);
        } catch (TimeOutOfBoundsException e) {
            result.add(e.getMessage());
            return result;
        }
        if (!i.isOverlapping(end)) {
            result.add("Node inconsistency: "
            		 + "start" + nodeStr(node) + 
            		 " + duration" + nodeStr(node) + "=" + timeStr(i.toString())
                     + " is not consistent with end" 
            		 + nodeStr(node) + timeStr(end.toString()) + ".");
        }
        
        //Condition 7
        Interval i2 = null;
        try {
            i2 = granularity.subtractTD(end, dur);
        } catch (TimeOutOfBoundsException e) {
            result.add(e.getMessage());
            return result;
        }

        if (!i2.isOverlapping(start)) {
            result.add("Node inconsistency: end"
                     + nodeStr(node) + " - duration" 
            		 + nodeStr(node) + "=" + timeStr(i2.toString())
                     + " is not consistent with start" 
            		 + nodeStr(node) + timeStr(start.toString()) + ".");
        }
        
        //Condition 8
        int lowBound3;
        int upBound3;
        try {
            lowBound3 = Math.max(0, granularity.subtractTT(tsu, tfl));
            upBound3 = granularity.subtractTT(tsl, tfu);
        } catch (TimeOutOfBoundsException e) {
            result.add(e.getMessage());
            return result;
        }
        Interval i3 = new Interval(lowBound3, upBound3);

        if (!i3.isOverlapping(dur)) {
            result.add("Node inconsistency: end"
                     + nodeStr(node) + " - start" 
            		 + nodeStr(node) + "=" + timeStr(i3.toString())
                     + " is not consistent with duration" 
            		 + nodeStr(node) + timeStr(dur.toString()) + ".");
        }
        
        return result;
    }
    
    private ArrayList<String> concurConsistency(TransitionNode t) {
        ArrayList<String> result = new ArrayList<>();
        Interval start = t.getStartTime();
        Interval end = t.getEndTime();
        
        //Condition 9
        for(Node post : getPostPNSet(t)){
        	Interval start2 = ((Time)post).getStartTime();
        	if(!start2.equals(end)){
        		result.add("Concurrently inconsistency: end"
                        + nodeStr(t) + timeStr(end.toString())
                        + " != start" + nodeStr(post) 
                        + timeStr(start2.toString()) + ".");
        	}
        }
        
        //Condition 10
        for(Node pre : getPrePNSet(t)){
        	Interval end2 = ((Time)pre).getEndTime();
        	if(!end2.equals(start)){
        		result.add("Concurrently inconsistency: start"
                        + nodeStr(t) + timeStr(start.toString())
                        + " != end" + nodeStr(pre) 
                        + timeStr(end2.toString()) + ".");
        	}
        }
        return result;
    }
    
    public Map<Node, ArrayList<String>> ON_Consistency(Collection<Node> checkList) {
        Map<Node, ArrayList<String>> result = new HashMap<>();
        
        Collection<Node> filter = new ArrayList<>();
        for(Node n : checkList){
        	if(n instanceof Condition || n instanceof TransitionNode)
        		filter.add(n);
        }
        
        for(Node node : filter){
        	//result string
        	ArrayList<String> subResult = new ArrayList<>();
        	
        	subResult.addAll(nodeConsistency(node));
        	
        	if(node instanceof TransitionNode){
        		subResult.addAll(concurConsistency((TransitionNode)node));
        	}
        	
        	result.put(node, subResult);
        }
        
    	return result;
    }

    public Map<Node, ArrayList<String>> CSON_Consistency(Collection<Node> checkList){
        Map<Node, ArrayList<String>> result = new HashMap<>();
        
        Collection<ChannelPlace> filter = new ArrayList<>();
        for(Node n : checkList){
        	if(n instanceof ChannelPlace)
        		filter.add((ChannelPlace)n);
        }
        
        for(ChannelPlace cp : filter){
        	//result string
        	ArrayList<String> subResult = new ArrayList<>();
        	
            TransitionNode input = (TransitionNode)net.getPreset(cp).iterator().next();
            TransitionNode output = (TransitionNode)net.getPostset(cp).iterator().next();
            
            if (!checkList.contains(input) || !checkList.contains(output)) {
            	subResult.add("Fail to run CSON consistency checking: input and output has unspecified value.");
            	result.put(cp, subResult);
                return result;
            }
            
            if (sync.contains(cp)) {
                if (cp.getDuration().isSpecified()
                    && !cp.getDuration().toString().equals("0000-0000")
                    && (cp.getDuration() != input.getDuration() || cp.getDuration() != output.getDuration())) {
                	
                	subResult.add("Sync inconsistency: duration" + nodeStr(cp) 
                    + " != Duration" + nodeStr(input) 
                    + " != Duration" + nodeStr(output) + ".");
                    
                }
                
                //Equation 18
                if (!(input.getStartTime().equals(output.getStartTime()))) {
                	
                	subResult.add("Sync inconsistency: start" + nodeStr(input) 
                    + timeStr(input.getStartTime().toString())
                    + " != start" + nodeStr(output) + timeStr(output.getStartTime().toString()));
                    
                }
                if (!(input.getDuration().equals(output.getDuration()))) {
                	
                	subResult.add("Sync inconsistency: duration" + nodeStr(input) 
                    + timeStr(input.getDuration().toString())
                    + " != duration" + nodeStr(output) + timeStr(output.getDuration().toString()));
                    
                }
                if (!(input.getEndTime().equals(output.getEndTime()))) {
                	
                	subResult.add("Sync inconsistency: end" + nodeStr(input) 
                    + nodeStr(input) + timeStr(input.getEndTime().toString())
                    + " != end" + nodeStr(output) + timeStr(output.getEndTime().toString()));
                    
                }
            }else{
                if (!nodeConsistency(cp).isEmpty()) {
                	subResult.add("Async inconsistency: " + nodeStr(cp)+ "is not node consistency");
                }       
            }
            
        	result.put(cp, subResult);
        }
        
        return result;
    }
    
    public  Map<HashSet<Node>, ArrayList<String>> BSON_Consistency(Collection<Node> checkList) {
        Map<HashSet<Node>, ArrayList<String>> result = new HashMap<>();

        for (TransitionNode[] v : before) {
            TransitionNode v0 = v[0];
            TransitionNode v1 = v[1];

            if (!checkList.contains(v1) || !checkList.contains(v0)) {
//                result.add("Fail to run behavioural consistency checking: " 
//                		 + nodeStr(v0) + " and " + nodeStr(v1)
//                         + "has unspecified time value.");
                continue;
            }

            ArrayList<String> subStr = new ArrayList<>();
            HashSet<Node> subNodes = new HashSet<>();
            subNodes.add(v0);
            subNodes.add(v1);
            		
            Integer gsl = v0.getStartTime().getMin();
            Integer gsu = v0.getStartTime().getMax();
            Integer hsl = v1.getStartTime().getMin();
            Integer hsu = v1.getStartTime().getMax();

            //Condition 18
            if (!(gsl <= hsl)) {
            	subStr.add("Behavioural inconsistency: minStart" + nodeStr(v0) + timeStr(gsl.toString())
                         + " > " + "minStart" + nodeStr(v1) + timeStr(hsl.toString()) + ".");
            }   
            if (!(gsu <= hsu)) {
            	subStr.add("Behavioural inconsistency: maxStart" + nodeStr(v0) + timeStr(gsu.toString())
                         + " > " + "maxStart" + nodeStr(v1) + timeStr(hsu.toString()) + ".");
            }
            
            result.put(subNodes, subStr);
        }
        
        Collection<Condition> lower_c = getLowerConditions();
        
        for(Condition lc : lower_c){
            ArrayList<String> subStr = new ArrayList<>();
            HashSet<Node> subNodes = new HashSet<>();
            
            //Condition 19
        	if(isInitial(lc)){
        		for(Condition uc : getUpperConditions(lc)){
                    if (!uc.getStartTime().isSpecified() || !lc.getStartTime().isSpecified()) {
                        continue;
                    }
                    subNodes.add(lc);
                    subNodes.add(uc);
                    if (!lc.getStartTime().equals(uc.getStartTime())) {
                    	subStr.add("Behavioural inconsistency: start" + nodeStr(lc)
                                 + " != " + "start" + nodeStr(uc) + ".");
                    }
        		}
        	}
        	
            //Condition 20
        	if(isFinal(lc)){
        		for(Condition uc : getUpperConditions(lc)){
                    if (!uc.getEndTime().isSpecified() || !lc.getEndTime().isSpecified()) {
                        continue;
                    }
                    subNodes.add(lc);
                    subNodes.add(uc);
                    if (!lc.getEndTime().equals(uc.getEndTime())) {
                    	subStr.add("Behavioural inconsistency: end" + nodeStr(lc)
                                 + " != " + "end" + nodeStr(uc) + ".");
                    }
        		}
        	}
        	
        	if(!subNodes.isEmpty()) result.put(subNodes, subStr);
        }
        
        return result;
    }
    
    private Collection<Condition> getUpperConditions(Condition lc){
    	Collection<Condition> result = new ArrayList<Condition>();
    	for(SONConnection con : net.getOutputSONConnections(lc)){
    		if(con.getSemantics() == Semantics.BHVLINE && scenario.getConditions(net).contains(con.getSecond())){
    			result.add((Condition)con.getSecond());
    		}
    	}
    	return result;
    }

    private Collection<Condition> getLowerConditions() {
        Collection<Condition> result = new ArrayList<>();
        Collection<ONGroup> lowerGroups = bsonAlg.getLowerGroups(net.getGroups());

        for (ONGroup group : lowerGroups) {
            result.addAll(group.getConditions());
        }
        return result;
    }
    
    public ArrayList<String> granularityHourMinsTask(Node node) {
        ArrayList<String> result = new ArrayList<>();
        if (node instanceof Time) {
            Time t = (Time) node;
            Integer value = null;
            try {
                value = t.getStartTime().getMin();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: startMin" + nodeStr(t) + timeStr(value.toString()) + ".");
            }
            try {
                value = t.getStartTime().getMax();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: startMax" + nodeStr(t) + timeStr(value.toString()) + ".");
            }
            try {
                value = t.getEndTime().getMin();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: endMin" + nodeStr(t) + timeStr(value.toString()) + ".");
            }
            try {
                value = t.getEndTime().getMax();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: endMax" + nodeStr(t) + timeStr(value.toString()) + ".");
            }
            try {
                value = t.getDuration().getMin();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: durationMin" + nodeStr(t) + timeStr(value.toString()) + ".");
            }
            try {
                value = t.getDuration().getMax();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: durationMax" + nodeStr(t) + timeStr(value.toString()) + ".");
            }
        }
        return result;
    }
    
    private String nodeStr(Node node) {
        return "(" + net.getNodeReference(node) + ")";
    }

    private String timeStr(String value) {
        return "[" + value + "]";
    }
}
