package org.workcraft.plugins.son.algorithm;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.exception.AlternativeStructureException;
import org.workcraft.plugins.son.granularity.HourMins;
import org.workcraft.plugins.son.granularity.TimeGranularity;
import org.workcraft.plugins.son.granularity.YearYear;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;

public class TimeAlg extends RelationAlgorithm {
	
    protected ScenarioRef scenario;
    protected Granularity g;
    protected TimeGranularity granularity;
    
    //default duration provided by user
    protected final Interval defaultDuration;

    public TimeAlg(SON net, Interval d, Granularity g, ScenarioRef s) throws AlternativeStructureException {
        super(net);
        this.scenario = s;
        this.g = g;
        this.defaultDuration = d;
        
        if (scenario == null && !hasConflict()) {
            scenario = getNonbranchSON();
        }           
		
		if(scenario == null ){
			throw new AlternativeStructureException("Model involves more than one scenarios, generate and select a scenario first");
		}
        
        if (g == Granularity.YEAR_YEAR) {
            granularity = new YearYear();
        } else if (g == Granularity.HOUR_MINS) {
            granularity = new HourMins();
        }
    }
    
    //assign specified value from connections to nodes
    public void initialize(){
        for (SONConnection con : scenario.getConnections(net)) {
            if (con.getSemantics() == Semantics.PNLINE) {
                if (con.getTime().isSpecified()) {
                    Node first = con.getFirst();
                    if (first instanceof Time) {
                    	Interval end = ((Time) first).getEndTime();
                        ((Time) first).setEndTime(Interval.getOverlapping(end, con.getTime()));
                    }
                    Node second = con.getSecond();
                    if (second instanceof Time) {
                    	Interval start = ((Time) second).getStartTime();
                        ((Time) second).setStartTime(Interval.getOverlapping(start, con.getTime()));
                    }
                }
            }
        }
	}
	
    //assign estimated time value from nodes to connections
    public void finalize(){
        SONAlg sonAlg = new SONAlg(net);
        Collection<PlaceNode> initial = sonAlg.getSONInitial();
        Collection<PlaceNode> finalM = sonAlg.getSONFinal();
       
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
      
    private ScenarioRef getNonbranchSON() {
        ScenarioRef scenario = new ScenarioRef();
        for (Node node : net.getComponents()) {
            scenario.add(net.getNodeReference(node));
        }
        for (SONConnection con : net.getSONConnections()) {
            scenario.add(net.getNodeReference(con));
        }
        return scenario;
    }
    
    private boolean hasConflict() {
        RelationAlgorithm alg = new RelationAlgorithm(net);
        for (Condition c : net.getConditions()) {
            if (alg.hasPostConflictEvents(c)) {
                return true;
            } else if (alg.hasPreConflictEvents(c)) {
                return true;
            }
        }
        return false;
    }

    public static void setProperties(SON net) {
        for (Condition c : net.getConditions()) {
            if (net.getInputPNConnections(c).isEmpty()) {
                c.setInitial(true);
            }
            if (net.getOutputPNConnections(c).isEmpty()) {
                c.setFinal(true);
            }
        }
    }

    public static void removeProperties(SON net) {
        for (PlaceNode c : net.getPlaceNodes()) {
            if (c instanceof Condition) {
                ((Condition) c).setInitial(false);
                ((Condition) c).setFinal(false);
            }
        }
    }

    public void setDefaultTime(Node node) {
        Interval input = new Interval(0000, 9999);
        if (node instanceof Condition) {
            Condition c = (Condition) node;
            if (c.isInitial() && !c.isFinal()) {
                c.setEndTime(input);
                return;
            } else if (c.isFinal() && !c.isInitial()) {
                c.setStartTime(input);
                return;
            } else if (!c.isFinal() && !c.isInitial()) {
                c.setStartTime(input);
                c.setEndTime(input);
            } else {
                return;
            }
        } else if (node instanceof Time) {
            ((Time) node).setStartTime(input);
            ((Time) node).setEndTime(input);
        }
    }
    
	
    protected Collection<ChannelPlace> getSyncCPs() {
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
}
