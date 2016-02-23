package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.InvalidStructureException;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.granularity.HourMins;
import org.workcraft.plugins.son.granularity.TimeGranularity;
import org.workcraft.plugins.son.granularity.YearYear;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Before;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.Phase;
import org.workcraft.plugins.son.util.ScenarioRef;

public class ConsistencyAlg extends TimeAlg {

    private SON net;

    public ConsistencyAlg(SON net) {
        super(net);
        this.net = net;
    }

    public ArrayList<String> nodeConsistency(Node node, Interval start, Interval end, Interval dur, Granularity g) {
        ArrayList<String> result = new ArrayList<String>();

        if (!start.isSpecified() || !end.isSpecified() || !dur.isSpecified()) {
            result.add("Fail to run node consistency checking: node has unspecified value.");
            return result;
        }

        Integer tsl = start.getMin();
        Integer tsu = start.getMax();
        Integer tfl = end.getMin();
        Integer tfu = end.getMax();

        TimeGranularity granularity = null;
        if (g == Granularity.YEAR_YEAR) {
            granularity = new YearYear();
        } else if (g == Granularity.HOUR_MINS) {
            granularity = new HourMins();
        }

        //Equation 3
        if (!(tsl <= tfl)) {
            result.add("Node inconsistency: minStart"
                     + node(node) + value(tsl.toString()) + " > minEnd" + node(node) + value(tfl.toString()) + ".");
        }
        if (!(tsu <= tfu)) {
            result.add("Node inconsistency: maxStart"
                     + node(node) + value(tsu.toString()) + " > maxEnd" + node(node) + value(tfu.toString()) + ".");
        }
        //Equation 6
        Interval i = null;
        try {
            i = granularity.plusTD(start, dur);
        } catch (TimeOutOfBoundsException e) {
            result.add(e.getMessage());
            return result;
        }
        if (!i.isOverlapping(end)) {
            result.add("Node inconsistency: start"
                     + node(node) + " + duration" + node(node) + "=" + value(i.toString())
                     + " is not consistent with end" + node(node) + value(end.toString()) + ".");
        }

        //Equation 7
        Interval i2 = null;
        try {
            i2 = granularity.subtractTD(end, dur);
        } catch (TimeOutOfBoundsException e) {
            result.add(e.getMessage());
            return result;
        }

        if (!i2.isOverlapping(start)) {
            result.add("Node inconsistency: end"
                     + node(node) + " - duration" + node(node) + "=" + value(i2.toString())
                     + " is not consistent with start" + node(node) + value(start.toString()) + ".");
        }

        //Equation 8
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
                     + node(node) + " - start" + node(node) + "=" + value(i3.toString())
                     + " is not consistent with duration" + node(node) + value(dur.toString()) + ".");
        }

        return result;
    }

    private ArrayList<String> concurConsistency(TransitionNode t) {
        ArrayList<String> result = new ArrayList<String>();
        Collection<SONConnection> inputConnections =  net.getInputPNConnections(t);
        Collection<SONConnection> outputConnections = net.getOutputPNConnections(t);

        if (inputConnections.size() > 1 && !t.getStartTime().isSpecified()) {
            SONConnection con = inputConnections.iterator().next();
            Interval time = con.getTime();
            for (SONConnection con1 : inputConnections) {
                Interval time1 = con1.getTime();
                if (!time.equals(time1)) {
                    result.add("Concurrently inconsistency: start"
                             + node(t) + value(time.toString())
                             + " != start'" + node(t) + value(time1.toString()) + ".");
                }
            }
        }

        if (outputConnections.size() > 1 && !t.getEndTime().isSpecified()) {
            SONConnection con = outputConnections.iterator().next();
            Interval time = con.getTime();
            for (SONConnection con1 : outputConnections) {
                Interval time1 = con1.getTime();
                if (!time.equals(time1)) {
                    result.add("Concurrently inconsistency: end"
                             + node(t) + value(time.toString())
                             + " != end'" + node(t) + value(time1.toString()) + ".");
                }
            }
        }
        return result;
    }

    private ArrayList<String> alterConsistency(Condition c, ScenarioRef s, Granularity g) {
        ArrayList<String> result = new ArrayList<String>();

        Collection<SONConnection> inputConnections;
        Collection<SONConnection> outputConnections;

        if (s != null) {
            inputConnections = net.getInputScenarioPNConnections(c, s);
            outputConnections = net.getOutputScenarioPNConnections(c, s);
        } else {
            inputConnections =  net.getInputPNConnections(c);
            outputConnections = net.getOutputPNConnections(c);
        }

        boolean isConsisent = false;

        if (c.isInitial() && c.isFinal()) {
            if (nodeConsistency(c, c.getStartTime(), c.getEndTime(), c.getDuration(), g).isEmpty()) {
                isConsisent = true;
            }
        } else if (c.isInitial() && !c.isFinal()) {
            for (SONConnection con : outputConnections) {
                if (nodeConsistency(c, c.getStartTime(), con.getTime(), c.getDuration(), g).isEmpty()) {
                    c.setEndTime(con.getTime());
                    isConsisent = true;
                    break;
                }
            }
        } else if (!c.isInitial() && c.isFinal()) {
            for (SONConnection con : inputConnections) {
                if (nodeConsistency(c, con.getTime(), c.getEndTime(), c.getDuration(), g).isEmpty()) {
                    c.setStartTime(con.getTime());
                    isConsisent = true;
                    break;
                }
            }
        } else {
            for (SONConnection con : inputConnections) {
                for (SONConnection con2 : outputConnections) {
                    if (nodeConsistency(c, con.getTime(), con2.getTime(), c.getDuration(), g).isEmpty()) {
                        c.setStartTime(con.getTime());
                        c.setEndTime(con2.getTime());
                        isConsisent = true;
                        break;
                    }
                }
            }
        }

        if (!isConsisent) {
            result.add("Alternatively inconsistency: cannot find node consistent scenario " +
                    "for node " + node(c) + ".");
        }

        return result;
    }

    public ArrayList<String> csonConsistency(ChannelPlace cp, Collection<ChannelPlace> sync, Granularity g) throws InvalidStructureException {
        ArrayList<String> result = new ArrayList<String>();
        //check all transitionNodes first
        TransitionNode input = null;
        if (net.getInputSONConnections(cp).size() == 1) {
            SONConnection con = net.getInputSONConnections(cp).iterator().next();
            input = (TransitionNode) con.getFirst();
        }

        TransitionNode output = null;
        if (net.getOutputSONConnections(cp).size() == 1) {
            SONConnection con = net.getOutputSONConnections(cp).iterator().next();
            output = (TransitionNode) con.getSecond();
        }

        if (input == null || output == null) {
            throw new InvalidStructureException("Empty channel place input/output: " + net.getNodeReference(cp));
        }

        //input and output must be node consistency
        if (!input.getEndTime().isSpecified() || !input.getStartTime().isSpecified()
                || !output.getEndTime().isSpecified() || !output.getStartTime().isSpecified()) {
            result.add("Fail to run time consistency checking, connected events are inconsistent or unspecified.");
            return result;
        }

        if (sync.contains(cp)) {
            if (cp.getDuration().isSpecified()
                    && !cp.getDuration().toString().equals("0000-0000")
                            && (cp.getDuration() != input.getDuration() || cp.getDuration() != output.getDuration())) {
                result.add("Sync inconsistency: duration" + node(cp) + " != Duration" + node(input) + " != Duration" + node(output) + ".");
            }
            //Equation 18
            if (!(input.getStartTime().equals(output.getStartTime()))) {
                result.add("Sync inconsistency: start" + node(input) + value(input.getStartTime().toString())
                         + " != start" + node(output) + value(output.getStartTime().toString()));
            }
            if (!(input.getDuration().equals(output.getDuration()))) {
                result.add("Sync inconsistency: duration" + node(input) + value(input.getDuration().toString())
                         + " != duration" + node(output) + value(output.getDuration().toString()));
            }
            if (!(input.getEndTime().equals(output.getEndTime()))) {
                result.add("Sync inconsistency: end" + node(input) + node(input) + value(input.getEndTime().toString())
                         + " != end" + node(output) + value(output.getEndTime().toString()));
            }
        } else {
            cp.setStartTime(input.getEndTime());
            cp.setEndTime(output.getStartTime());
            if (!nodeConsistency(cp, cp.getStartTime(), cp.getEndTime(), cp.getDuration(), g).isEmpty()) {
                result.add("Async inconsistency: " + node(cp)
                         + "is not node consistency");
            }
        }
        return result;
    }

    public ArrayList<String> bsonConsistency(TransitionNode t, Map<Condition, Collection<Phase>> phases, ScenarioRef s) {
        ArrayList<String> result = new ArrayList<String>();
        BSONAlg bsonAlg = new BSONAlg(net);
        Before before = bsonAlg.before(t, phases);

        for (TransitionNode[] v : before) {
            TransitionNode v0 = v[0];
            TransitionNode v1 = v[1];
            if (s != null) {
                Collection<Node> scenarioNodes = s.getNodes(net);
                if (!scenarioNodes.contains(v1) || !scenarioNodes.contains(v0))continue;
            }

            //Equation 17
            if (!v0.getStartTime().isSpecified() || !v1.getStartTime().isSpecified()) {
                result.add("Fail to run behavioural consistency checking: " + node(v0) + " or " + node(v1)
                         + " is node inconsistency.");
                continue;
            }

            Integer gsl = v0.getStartTime().getMin();
            Integer gsu = v0.getStartTime().getMax();
            Integer hsl = v1.getStartTime().getMin();
            Integer hsu = v1.getStartTime().getMax();

            //Equation 20
            if (!(gsl <= hsl)) {
                result.add("Behavioural inconsistency: minStart" + node(v0) + value(gsl.toString())
                         + " > " + "minStart" + node(v1) + value(hsl.toString()) + ".");
            } else if (!(gsu <= hsu)) {
                result.add("Behavioural inconsistency: maxStart" + node(v0) + value(gsu.toString())
                         + " > " + "maxStart" + node(v1) + value(hsu.toString()) + ".");
            }
        }
        return result;
    }

    public ArrayList<String> bsonConsistency2(Condition initialLow, ScenarioRef s) {
        ArrayList<String> result = new ArrayList<String>();
        for (SONConnection con : net.getSONConnections()) {
            if (s != null && !s.getConnections(net).contains(con)) {
                continue;
            }
            if ((con.getSemantics() == Semantics.BHVLINE) && (con.getFirst() == initialLow)) {
                Condition c = (Condition) con.getSecond();
                if (!c.getStartTime().isSpecified() || !initialLow.getStartTime().isSpecified()) {
                    result.add("Fail to run behavioural consistency checking: "
                            + node(c) + " or " + node(initialLow) + " is node inconsistency.");
                    return result;
                }
                if (!initialLow.getStartTime().equals(c.getStartTime())) {
                    result.add("Behavioural inconsistency: start" + node(initialLow)
                             + " != " + "start" + node(c) + ".");
                }
            }
        }
        return result;
    }

    public ArrayList<String> bsonConsistency3(Condition finalLow, ScenarioRef s) {
        ArrayList<String> result = new ArrayList<String>();
        for (SONConnection con : net.getSONConnections()) {
            if (s != null && !s.getConnections(net).contains(con)) {
                continue;
            }
            if ((con.getSemantics() == Semantics.BHVLINE) && (con.getFirst() == finalLow)) {
                Condition c = (Condition) con.getSecond();
                if (!c.getStartTime().isSpecified() || !finalLow.getStartTime().isSpecified()) {
                    result.add("Fail to run behavioural consistency checking: "
                            + node(c) + " or " + node(finalLow) + " is node inconsistency.");
                    return result;
                }
                if (finalLow.getStartTime().equals(c.getStartTime())) {
                    result.add("Behavioural inconsistency: end" + node(finalLow)
                             + " != " + "end" + node(c) + ".");
                }
            }
        }
        return result;
    }

    public ArrayList<String> granularityHourMinsTask(Node node) {
        ArrayList<String> result = new ArrayList<String>();
        if (node instanceof Time) {
            Time t = (Time) node;
            Integer value = null;
            try {
                value = t.getStartTime().getMin();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: startMin" + node(t) + value(value.toString()) + ".");
            }
            try {
                value = t.getStartTime().getMax();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: startMax" + node(t) + value(value.toString()) + ".");
            }
            try {
                value = t.getEndTime().getMin();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: endMin" + node(t) + value(value.toString()) + ".");
            }
            try {
                value = t.getEndTime().getMax();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: endMax" + node(t) + value(value.toString()) + ".");
            }
            try {
                value = t.getDuration().getMin();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: durationMin" + node(t) + value(value.toString()) + ".");
            }
            try {
                value = t.getDuration().getMax();
                HourMins.validValue(value);
            } catch (TimeOutOfBoundsException e) {
                result.add("Time out of bound: durationMax" + node(t) + value(value.toString()) + ".");
            }
        }
        return result;
    }

    public boolean hasSpecifiedDur(Node node, boolean isSync, ScenarioRef s) {

        if ((node instanceof Time) && !((Time) node).getDuration().isSpecified() && !isSync) {
            return false;
        }

        return true;
    }

    public boolean hasSpecifiedStart(Node node, ScenarioRef s) {

        Collection<SONConnection> inputConnections;

        if (s != null) {
            inputConnections = net.getInputScenarioPNConnections(node, s);
        } else {
            inputConnections =  net.getInputPNConnections(node);
        }

        if (node instanceof TransitionNode) {
            for (SONConnection con : inputConnections) {
                if (!con.getTime().isSpecified()) {
                    return false;
                }
            }
        } else if (node instanceof Condition) {
            Condition c = (Condition) node;

            boolean hasSpecifiedInput = false;
            //initial state
            if (inputConnections.isEmpty()) {
                if (c.getStartTime().isSpecified()) {
                    hasSpecifiedInput = true;
                }
            } else {
                for (SONConnection con : inputConnections) {
                    if (con.getTime().isSpecified()) {
                        hasSpecifiedInput = true;
                        break;
                    }
                }
            }

            if (!hasSpecifiedInput) {
                return false;
            }
        }
        return true;
    }

    public boolean hasSpecifiedEnd(Node node, ScenarioRef s) {

        Collection<SONConnection> outputConnections;

        if (s != null) {
            outputConnections = net.getOutputScenarioPNConnections(node, s);
        } else {
            outputConnections = net.getOutputPNConnections(node);
        }

        if (node instanceof TransitionNode) {
            for (SONConnection con : outputConnections) {
                if (!con.getTime().isSpecified()) {
                    return false;
                }
            }
        } else if (node instanceof Condition) {
            Condition c = (Condition) node;
            boolean hasSpecifiedOutput = false;
            //final state
            if (outputConnections.isEmpty()) {
                if (c.getEndTime().isSpecified()) {
                    hasSpecifiedOutput = true;
                }
            } else {
                for (SONConnection con : outputConnections) {
                    if (con.getTime().isSpecified()) {
                        hasSpecifiedOutput = true;
                        break;
                    }
                }
            }
            if (!hasSpecifiedOutput) {
                return false;
            }
        }
        return true;
    }

    public String hasSpecifiedCP(Node node, boolean isSync, ScenarioRef s) {
        ChannelPlace cp = null;
        if (node instanceof ChannelPlace) {
            cp = (ChannelPlace) node;
        } else {
            return "";
        }

        //get input and output events
        TransitionNode input = null;
        Collection<SONConnection> inputAsynConnection = net.getInputSONConnections(cp);

        if (inputAsynConnection.size() == 1) {
            SONConnection con = inputAsynConnection.iterator().next();
            input = (TransitionNode) con.getFirst();
        }

        TransitionNode output = null;
        if (net.getOutputSONConnections(cp).size() == 1) {
            SONConnection con = net.getOutputSONConnections(cp).iterator().next();
            output = (TransitionNode) con.getSecond();
        }

        if (input == null) {
            return "input";
        }
        if (output == null) {
            return "output";
        }

        if (isSync) {
            if (!hasSpecifiedStart(input, s)
                    && !hasSpecifiedEnd(input, s)
                    && !hasSpecifiedStart(output, s)
                    && !hasSpecifiedEnd(output, s)) {
                return "connected events";
            } else if (!hasSpecifiedStart(input, s)
                    || !hasSpecifiedEnd(input, s)
                    || !hasSpecifiedStart(output, s)
                    || !hasSpecifiedEnd(output, s)) {
                return "partial";
            }
        } else {
            if (!hasSpecifiedEnd(input, s) && !hasSpecifiedStart(output, s)) {
                return "connected events";
            } else if (!hasSpecifiedEnd(input, s) || !hasSpecifiedStart(output, s)) {
                return "partial";
            }
        }
        return "";
    }

    public ArrayList<String> onConsistency(Node node, ScenarioRef s, Granularity g) throws InvalidStructureException {
        ArrayList<String> result = new ArrayList<String>();

        //ON time consistency checking.
        if (node instanceof TransitionNode) {
            TransitionNode t = (TransitionNode) node;
            ArrayList<String> concurResult = concurConsistency(t);

            if (concurResult.isEmpty()) {
                if (net.getInputSONConnections(t).size() > 0) {
                    SONConnection con = net.getInputSONConnections(t).iterator().next();
                    if (!t.getStartTime().isSpecified()) {
                        t.setStartTime(con.getTime());
                    }
                } else {
                    throw new InvalidStructureException("Empty event input/output: " + net.getNodeReference(t));
                }

                if (net.getOutputSONConnections(t).size() > 0) {
                    SONConnection con = net.getOutputSONConnections(t).iterator().next();
                    if (!t.getEndTime().isSpecified()) {
                        t.setEndTime(con.getTime());
                    }
                } else {
                    throw new InvalidStructureException("Empty event input/output: " + net.getNodeReference(t));
                }

                result.addAll(nodeConsistency(t, t.getStartTime(), t.getEndTime(), t.getDuration(), g));

            } else {
                result.addAll(concurResult);
            }
        //ON time consistency checking
        } else if (node instanceof Condition) {
            Condition c = (Condition) node;

            if (net.getInputPNConnections(c).size() > 1 || net.getOutputPNConnections(c).size() > 1) {
                result.addAll(alterConsistency(c, s, g));
            } else {
                if (c.isInitial() && !c.isFinal()) {
                    SONConnection con = net.getOutputPNConnections(c).iterator().next();
                    if (!c.getEndTime().isSpecified()) {
                        c.setEndTime(con.getTime());
                    }
                } else if (!c.isInitial() && c.isFinal()) {
                    SONConnection con = net.getInputPNConnections(c).iterator().next();
                    if (!c.getStartTime().isSpecified()) {
                        c.setStartTime(con.getTime());
                    }
                } else if (!c.isInitial() && !c.isFinal()) {
                    SONConnection con = net.getInputPNConnections(c).iterator().next();
                    SONConnection con2 = net.getOutputPNConnections(c).iterator().next();
                    if (!c.getStartTime().isSpecified()) {
                        c.setStartTime(con.getTime());
                    }
                    if (!c.getEndTime().isSpecified()) {
                        c.setEndTime(con2.getTime());
                    }
                }
                result.addAll(nodeConsistency(c, c.getStartTime(), c.getEndTime(), c.getDuration(), g));
            }
        }

        return result;
    }

    private String node(Node node) {
        return "(" + net.getNodeReference(node) + ")";
    }

    private String value(String value) {
        return "[" + value + "]";
    }
}
