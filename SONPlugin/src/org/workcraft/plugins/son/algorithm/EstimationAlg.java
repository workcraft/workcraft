package org.workcraft.plugins.son.algorithm;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.AlternativeStructureException;
import org.workcraft.plugins.son.exception.TimeEstimationException;
import org.workcraft.plugins.son.exception.TimeEstimationValueException;
import org.workcraft.plugins.son.exception.TimeInconsistencyException;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.granularity.HourMins;
import org.workcraft.plugins.son.granularity.TimeGranularity;
import org.workcraft.plugins.son.granularity.YearYear;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Before;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;

public class EstimationAlg extends TimeAlg {

    protected SON net;
    //interval[0] is first found specified time interval, interval[1] is the accumulated durations
    private Collection<Interval[]> resultTimeAndDuration = new ArrayList<>();

    //default duration provided by user
    protected final Interval defaultDuration;
    protected TimeGranularity granularity = null;
    protected ScenarioRef scenario;
    protected Before before;
    protected Granularity g;
    protected Color color = Color.ORANGE;
    private Collection<Path> fwdPaths = new ArrayList<>();
    private Collection<Path> bwdPaths = new ArrayList<>();

    public EstimationAlg(SON net, Interval d, Granularity g, ScenarioRef s) {
        super(net);
        this.net = net;
        this.defaultDuration = d;
        this.scenario = s;
        this.g = g;

        if (scenario == null && !hasConflict()) {
            scenario = getUnalterSON();
        }

        if (g == Granularity.YEAR_YEAR) {
            granularity = new YearYear();
        } else if (g == Granularity.HOUR_MINS) {
            granularity = new HourMins();
        }

        BSONAlg bsonAlg = new BSONAlg(net);
        before  =  bsonAlg.getBeforeList();
    }

    public void setDefaultDuration() {
        for (Node n : scenario.getNodes(net)) {
            if (n instanceof PlaceNode || n instanceof Block) {
                Time node = (Time) n;
                if (!node.getDuration().isSpecified()) {
                    node.setDuration(defaultDuration);
                }
            }
        }
    }

    public ScenarioRef getUnalterSON() {
        ScenarioRef scenario = new ScenarioRef();
        for (Node node2 : net.getComponents()) {
            scenario.add(net.getNodeReference(node2));
        }
        for (SONConnection con : net.getSONConnections()) {
            scenario.add(net.getNodeReference(con));
        }
        return scenario;
    }

    public void twoDirEstimation(Node n, boolean interm) throws AlternativeStructureException, TimeEstimationException, TimeOutOfBoundsException, TimeInconsistencyException {
        ConsistencyAlg alg = new ConsistencyAlg(net);
        Time node = null;

        if (scenario == null) {
            throw new AlternativeStructureException("select a scenario for " + net.getNodeReference(n) + " first.");
        }

        boolean specifiedDur = alg.hasSpecifiedDur(n, false, scenario);
        boolean specifiedStart = alg.hasSpecifiedStart(n, scenario);
        boolean specifiedEnd = alg.hasSpecifiedEnd(n, scenario);

        Collection<SONConnection> iCons = net.getInputScenarioPNConnections(n, scenario);
        Collection<SONConnection> oCons = net.getOutputScenarioPNConnections(n, scenario);

        if (n instanceof Time) {
            node = (Time) n;
        } else return;

        //set default duration
        if (!specifiedDur) {
            node.setDuration(defaultDuration);
            if (n instanceof PlaceNode) {
                ((PlaceNode) n).setDurationColor(color);
            } else if (n instanceof Block) {
                ((Block) n).setDurationColor(color);
            }
        }
        //n has unspecified start and specified end
        if (!specifiedStart && specifiedEnd) {
            Interval start = null;
            Interval end = null;

            if (node instanceof Condition) {
                end = getSpecifiedEndTime(node);
                start = granularity.subtractTD(end, node.getDuration());

                if (iCons.size() == 1) {
                    iCons.iterator().next().setTime(start);
                } else {
                    node.setStartTime(start);
                    ((Condition) node).setStartTimeColor(color);
                }
            } else if (node instanceof TransitionNode) {
                Collection<SONConnection> iSpec = getSpecifiedConnections(iCons);

                if (!iSpec.isEmpty()) {
                    if (concurConsistency(iSpec)) {
                        end = iSpec.iterator().next().getTime();
                        start = getSpecifiedEndTime(node);
                    } else {
                        throw new TimeEstimationException("start times are concurrently inconsistency.");
                    }
                } else {
                    start = getSpecifiedEndTime(node);
                    end = granularity.subtractTD(start, node.getDuration());
                }
                iCons.removeAll(iSpec);
                for (SONConnection con : iCons) {
                    con.setTime(end);
                    con.setTimeLabelColor(color);
                }
            }
            if (!alg.nodeConsistency(node, start, end, node.getDuration(), g).isEmpty()) {
                throw new TimeInconsistencyException("Warning: Estimated start and end times are inconsistent.");
            }
            return;
            //n has specified start and unspecified end
        } else if (specifiedStart && !specifiedEnd) {
            Interval start = null;
            Interval end = null;

            if (node instanceof Condition) {
                start = getSpecifiedStartTime(n);
                end = granularity.plusTD(start, node.getDuration());

                if (oCons.size() == 1) {
                    oCons.iterator().next().setTime(end);
                } else {
                    node.setEndTime(end);
                    ((Condition) node).setEndTimeColor(color);
                }

            } else if (node instanceof TransitionNode) {
                Collection<SONConnection> oSpec = getSpecifiedConnections(oCons);

                if (!oSpec.isEmpty()) {
                    if (concurConsistency(oSpec)) {
                        end = oSpec.iterator().next().getTime();
                        start = getSpecifiedStartTime(n);
                    } else {
                        throw new TimeEstimationException("end times are concurrently inconsistency.");
                    }
                } else {
                    start = getSpecifiedStartTime(n);
                    end = granularity.plusTD(start, node.getDuration());
                }

                oCons.removeAll(oSpec);
                for (SONConnection con : oCons) {
                    con.setTime(end);
                    con.setTimeLabelColor(color);
                }
            }
            if (!alg.nodeConsistency(node, start, end, node.getDuration(), g).isEmpty()) {
                throw new TimeInconsistencyException("Warning: Estimated start and end times are inconsistent.");
            }
            return;
            //n has unspecified start and unspecified end
        } else if (!specifiedStart && !specifiedEnd) {
            Interval start = null;
            Interval end = null;

            //set partial specified values
            Collection<SONConnection> iSpec = getSpecifiedConnections(iCons);
            Collection<SONConnection> oSpec = getSpecifiedConnections(oCons);

            if (node instanceof TransitionNode) {

                if (!iSpec.isEmpty()) {
                    if (concurConsistency(iSpec)) {
                        start = iSpec.iterator().next().getTime();
                    } else {
                        throw new TimeEstimationException("start times are concurrently inconsistency.");
                    }
                }

                if (!oSpec.isEmpty()) {
                    if (concurConsistency(oSpec)) {
                        end = oSpec.iterator().next().getTime();
                    } else {
                        throw new TimeEstimationException("end times are concurrently inconsistency.");
                    }
                }
            }

            //unspec values for all connections
            if (start == null) {
                try {
                    start = getEstimatedStartTime(node);
                    //System.out.println("start" + start!=null?start.toString():"null");
                } catch (TimeEstimationException e) { }
            }
            if (end == null) {
                try {
                    end = getEstimatedEndTime(node);
                    //System.out.println("end" + end!=null?end.toString():"null");
                } catch (TimeEstimationException e) { }
            }

            if (start == null && end != null) {
                start = granularity.subtractTD(end, node.getDuration());
            } else if (start != null && end == null) {
                end = granularity.plusTD(start, node.getDuration());
            } else if (start == null && end == null) {
                throw new TimeEstimationException("cannot find causally time values.");
            }

            if (iCons.size() == 0) {
                node.setStartTime(start);
                if (node instanceof Condition) {
                    ((Condition) node).setStartTimeColor(color);
                }
            }

            if (oCons.size() == 0) {
                node.setEndTime(end);
                if (node instanceof Condition) {
                    ((Condition) node).setEndTimeColor(color);
                }
            }

            iCons.removeAll(iSpec);
            for (SONConnection con : iCons) {
                con.setTime(start);
                con.setTimeLabelColor(color);
            }

            oCons.removeAll(oSpec);
            for (SONConnection con : oCons) {
                con.setTime(end);
                con.setTimeLabelColor(color);
            }

            if (interm) intermediateEst();

            if (!alg.nodeConsistency(node, start, end, node.getDuration(), g).isEmpty()) {
                throw new TimeInconsistencyException("Warning: Estimated start and end times are inconsistent.");
            }
        }
    }

    private void intermediateEst() throws TimeOutOfBoundsException {

        for (Path path : fwdPaths) {
            for (int i = 1;  i < path.size(); i++) {
                Time n = (Time) path.get(i);
                if (!n.getDuration().isSpecified()) {
                    n.setDuration(defaultDuration);
                }
                SONConnection preCon = net.getSONConnection(path.get(i - 1), path.get(i));
                Interval time = granularity.plusTD(preCon.getTime(), n.getDuration());
                if ((i + 1) < path.size()) {
                    SONConnection postCon = net.getSONConnection(path.get(i), path.get(i + 1));
                    postCon.setTime(time);
                    postCon.setTimeLabelColor(color);
                }
            }
        }

        fwdPaths.clear();

        for (Path path : bwdPaths) {
            for (int i = 1;  i < path.size(); i++) {
                Time n = (Time) path.get(i);
                if (!n.getDuration().isSpecified()) {
                    n.setDuration(defaultDuration);
                }
                SONConnection preCon = net.getSONConnection(path.get(i - 1), path.get(i));
                Interval time = granularity.subtractTD(preCon.getTime(), n.getDuration());
                if ((i + 1) < path.size()) {
                    SONConnection postCon = net.getSONConnection(path.get(i), path.get(i + 1));
                    postCon.setTime(time);
                    postCon.setTimeLabelColor(color);
                }
            }
        }

        bwdPaths.clear();

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

    private Collection<SONConnection> getSpecifiedConnections(Collection<SONConnection> cons) {
        Collection<SONConnection> result = new ArrayList<>();
        for (SONConnection con : cons) {
            if (con.getTime().isSpecified()) {
                result.add(con);
            }
        }
        return result;
    }

    private Interval getSpecifiedEndTime(Node n) throws TimeEstimationException {

        if (n instanceof Condition) {
            Condition c = (Condition) n;
            Collection<SONConnection> cons = net.getOutputPNConnections(c);
            Collection<SONConnection> cons2 = net.getOutputScenarioPNConnections(c, scenario);

            //c is final
            if (cons.isEmpty() && cons2.isEmpty()) {
                return c.getEndTime();
                //c is final state of the scenario, but not the final state of ON
            } else if (!cons.isEmpty() && cons2.isEmpty()) {
                if (cons.size() == 1) {
                    SONConnection con = cons.iterator().next();
                    return con.getTime();
                }
                //c is not final state
            } else if (!cons.isEmpty() && !cons2.isEmpty()) {
                SONConnection con = null;
                if (cons2.size() == 1) {
                    con = cons2.iterator().next();
                }
                if (con == null) throw new RuntimeException("output connection != 1" + net.getNodeReference(n));
                return con.getTime();
            }
        } else if (n instanceof TransitionNode) {
            TransitionNode t = (TransitionNode) n;
            Collection<SONConnection> cons = net.getOutputScenarioPNConnections(t, scenario);
            if (concurConsistency(cons)) {
                return cons.iterator().next().getTime();
            } else {
                throw new TimeEstimationException("end times are concurrently inconsistency.");
            }

        }
        throw new TimeEstimationException("cannot find specified end time.");
    }

    private Interval getSpecifiedStartTime(Node n) throws TimeEstimationException {

        if (n instanceof Condition) {
            Condition c = (Condition) n;
            Collection<SONConnection> cons = net.getInputScenarioPNConnections(c, scenario);
            //p is initial
            if (cons.isEmpty()) {
                return c.getStartTime();
            } else {
                SONConnection con = null;
                if (cons.size() == 1) {
                    con = cons.iterator().next();
                }
                if (con == null) throw new RuntimeException(
                        "input connection != 1" + net.getNodeReference(n));
                //set estimated start time
                return con.getTime();
            }
        } else if (n instanceof TransitionNode) {
            TransitionNode t = (TransitionNode) n;
            Collection<SONConnection> cons = net.getInputScenarioPNConnections(t, scenario);
            if (concurConsistency(cons)) {
                return cons.iterator().next().getTime();
            } else {
                throw new TimeEstimationException("start times are concurrently inconsistency.");
            }
        }
        throw new TimeEstimationException("cannot find specified start time.");
    }

    public void setEstimatedEndTime(Node n) throws TimeOutOfBoundsException, TimeEstimationException, TimeEstimationValueException {
        Interval end = null;

        if (n instanceof Condition) {
            Condition c = (Condition) n;
            Collection<SONConnection> cons = net.getOutputPNConnections(c);
            Collection<SONConnection> cons2 = net.getOutputScenarioPNConnections(c, scenario);
            //c is final
            if (cons.isEmpty() && cons2.isEmpty()) {
                end = getEstimatedEndTime(n);
                //set estimated start time
                if (end != null) {
                    c.setEndTime(end);
                    throw new TimeEstimationValueException("Estimated end time = " + end.toString() + ", from " + intervals(resultTimeAndDuration));
                }
                //c is final state of the scenario, but not the final state of ON
            } else if (!cons.isEmpty() && cons2.isEmpty()) {
                if (cons.size() == 1) {
                    SONConnection con = cons.iterator().next();
                    c.setEndTime(con.getTime());
                    throw new TimeEstimationValueException("End time = " + con.getTime().toString());
                } else {
                    throw new TimeEstimationException(
                            "node has more than one possible end times (forward)");
                }
                //c is not final state
            } else if (!cons.isEmpty() && !cons2.isEmpty()) {
                SONConnection con = null;
                if (cons2.size() == 1) {
                    con = cons2.iterator().next();
                }
                if (con == null) throw new RuntimeException("output connection != 1" + net.getNodeReference(n));
                //set estimated value
                if (!con.getTime().isSpecified()) {
                    end = getEstimatedEndTime(n);
                    if (end != null) {
                        c.setEndTime(end);
                        throw new TimeEstimationValueException("Estimated end time = " + end.toString() + ", from " + intervals(resultTimeAndDuration));
                    }
                    //set value from output connection
                } else {
                    c.setEndTime(con.getTime());
                    throw new TimeEstimationValueException("End time = " + con.getTime().toString());
                }
            }
        } else if (n instanceof TransitionNode) {
            TransitionNode t = (TransitionNode) n;
            Collection<SONConnection> cons2 = net.getOutputScenarioPNConnections(t, scenario);

            Collection<SONConnection> specifiedCons = new ArrayList<>();
            for (SONConnection con : cons2) {
                if (con.getTime().isSpecified()) {
                    specifiedCons.add(con);
                }
            }

            //some or all of the connections have specified values
            if (!specifiedCons.isEmpty()) {
                if (concurConsistency(specifiedCons)) {
                    end =  specifiedCons.iterator().next().getTime();
                    if (end != null) {
                        t.setEndTime(end);
                        throw new TimeEstimationValueException("End time = " + end.toString());
                    }
                } else {
                    throw new TimeEstimationException(
                            "start times are concurrently inconsistency.");
                }
                //none of the connections has specified value.
            } else {
                end = getEstimatedEndTime(n);
                if (end != null) {
                    t.setEndTime(end);
                    throw new TimeEstimationValueException("Estimated end time = " + end.toString() + ", from " + intervals(resultTimeAndDuration));
                }
            }
        } else if (n instanceof ChannelPlace) {
            ChannelPlace cp = (ChannelPlace) n;
            end = getEstimatedEndTime(n);
            if (end != null) {
                cp.setEndTime(end);
                throw new TimeEstimationValueException("Estimated end time = " + end.toString() + ", from " + intervals(resultTimeAndDuration));
            }
        }
    }

    public void setEstimatedStartTime(Node n) throws TimeOutOfBoundsException, TimeEstimationException, TimeEstimationValueException {
        Interval start = null;

        if (n instanceof Condition) {
            Condition c = (Condition) n;
            Collection<SONConnection> cons = net.getInputScenarioPNConnections(c, scenario);
            //p is initial
            if (cons.isEmpty()) {
                start = getEstimatedStartTime(n);
                //set estimated start time
                if (start != null) {
                    c.setStartTime(start);
                    throw new TimeEstimationValueException("Estimated start time = " + start.toString() + ", from " + intervals(resultTimeAndDuration));
                }
            } else {
                SONConnection con = null;
                if (cons.size() == 1) {
                    con = cons.iterator().next();
                }
                if (con == null) throw new RuntimeException(
                        "input connection != 1" + net.getNodeReference(n));
                //set estimated start time
                if (!con.getTime().isSpecified()) {
                    start = getEstimatedStartTime(n);
                    if (start != null) {
                        c.setStartTime(start);
                        throw new TimeEstimationValueException("Estimated start time = " + start.toString() + ", from " + intervals(resultTimeAndDuration));
                    }
                    //set start time from connection
                } else {
                    c.setStartTime(con.getTime());
                    throw new TimeEstimationValueException("Start time = " + con.getTime().toString());
                }
            }
        } else if (n instanceof TransitionNode) {
            TransitionNode t = (TransitionNode) n;
            Collection<SONConnection> cons2 = net.getInputScenarioPNConnections(t, scenario);

            Collection<SONConnection> specifiedCons = new ArrayList<>();
            for (SONConnection con : cons2) {
                if (con.getTime().isSpecified()) {
                    specifiedCons.add(con);
                }
            }

            //some or all of the connections have specified values
            if (!specifiedCons.isEmpty()) {
                if (concurConsistency(specifiedCons)) {
                    start =  specifiedCons.iterator().next().getTime();
                    if (start != null) {
                        t.setStartTime(start);
                        throw new TimeEstimationValueException("Start time = " + start.toString());
                    }
                } else {
                    throw new TimeEstimationException(
                            "end times are concurrently inconsistency.");
                }
                //none of the connections has specified value.
            } else {
                start = getEstimatedStartTime(n);
                if (start != null) {
                    t.setStartTime(start);
                    throw new TimeEstimationValueException("Estimated start time = " + start.toString() + ", from " + intervals(resultTimeAndDuration));
                }
            }
        } else if (n instanceof ChannelPlace) {
            ChannelPlace cp = (ChannelPlace) n;
            start = getEstimatedStartTime(n);
            if (start != null) {
                cp.setStartTime(start);
                throw new TimeEstimationValueException("Estimated start time = " + start.toString() + ", from " + intervals(resultTimeAndDuration));
            }
        }
    }

    private boolean concurConsistency(Collection<SONConnection> cons) {
        SONConnection con = cons.iterator().next();
        Interval time = con.getTime();
        for (SONConnection con1 : cons) {
            Interval time1 = con1.getTime();
            if (!time.equals(time1)) {
                return false;
            }
        }
        return true;
    }

    protected Interval getEstimatedStartTime(Node n) throws TimeEstimationException, TimeOutOfBoundsException {
        Interval result = new Interval();

        LinkedList<Time> visited = new LinkedList<>();
        visited.add((Time) n);

        if (scenario != null) {
            backwardDFS(visited, scenario.getNodes(net));
        }

        Collection<Interval> possibleTimes = new ArrayList<>();
        for (Interval[] interval : resultTimeAndDuration) {
            possibleTimes.add(granularity.plusTD(interval[0], interval[1]));
        }
        if (!possibleTimes.isEmpty()) {
            result = Interval.getOverlapping(possibleTimes);
        }

        if (result != null) {
            clearSets();
            if (!result.isSpecified()) {
                throw new TimeEstimationException(
                        "cannot find causally time value (backward).");
            } else {
                return result;
            }
        } else {
            clearSets();
            throw new TimeEstimationException("intervals" +
                    intervals(resultTimeAndDuration) + "are not consistent (backward).");
        }
    }

    protected Interval getEstimatedEndTime(Node n) throws TimeEstimationException, TimeOutOfBoundsException {
        Interval result = new Interval();

        LinkedList<Time> visited = new LinkedList<>();
        visited.add((Time) n);

        if (scenario != null) {
            forwardDFS(visited, scenario.getNodes(net));
        }

        Collection<Interval> possibleTimes = new ArrayList<>();

        for (Interval[] interval : resultTimeAndDuration) {
            possibleTimes.add(granularity.subtractTD(interval[0], interval[1]));
        }

        if (!possibleTimes.isEmpty()) {
            result = Interval.getOverlapping(possibleTimes);
        }

        if (result != null) {
            clearSets();
            if (!result.isSpecified()) {
                throw new TimeEstimationException(
                        "cannot find causally time value (forward).");
            } else {
                return result;
            }
        } else {
            clearSets();
            throw new TimeEstimationException(
                    "intervals" +
                    intervals(resultTimeAndDuration) + "are not consistent (forward).");
        }

    }

    private void forwardDFS(LinkedList<Time> visited, Collection<Node> nodes)  {
        LinkedList<Time> neighbours = getCausalPostset(visited.getLast(), nodes);

        if (visited.getLast().getEndTime().isSpecified()) {
            Interval[] result = new Interval[2];
            result[0] = visited.getLast().getEndTime();
            result[1] = durationAccumulator1(visited);
            resultTimeAndDuration.add(result);

            Path path = new Path();
            path.addAll(visited);
            fwdPaths.add(path);
        }

        // examine post nodes
        for (Time node : neighbours) {
            SONConnection con = net.getSONConnection(visited.getLast(), node);
            if (visited.contains(node)) {
                continue;
            }
            if (visited.getLast().getEndTime().isSpecified()) {
                break;
            } else if (con != null && con.getTime().isSpecified()) {
                Interval[] result = new Interval[2];
                result[0] = con.getTime();
                result[1] = durationAccumulator1(visited);
                resultTimeAndDuration.add(result);

                Path path = new Path();
                visited.addLast(node);
                path.addAll(visited);
                visited.removeLast();
                fwdPaths.add(path);
            }
        }
        // in depth-first, recursion needs to come after visiting post nodes
        for (Time node : neighbours) {
            SONConnection con = net.getSONConnection(visited.getLast(), node);
            if (visited.contains(node) || visited.getLast().getEndTime().isSpecified()) {
                continue;
            } else if (con != null && con.getTime().isSpecified()) {
                continue;
            }
            visited.addLast(node);
            forwardDFS(visited, nodes);
            visited.removeLast();
        }
    }

    private void backwardDFS(LinkedList<Time> visited, Collection<Node> nodes) {
        LinkedList<Time> neighbours = getCausalPreset(visited.getLast(), nodes);

        if (visited.getLast().getStartTime().isSpecified()) {
            Interval[] result = new Interval[2];
            result[0] = visited.getLast().getStartTime();
            result[1] = durationAccumulator1(visited);
            resultTimeAndDuration.add(result);

            Path path = new Path();
            path.addAll(visited);
            bwdPaths.add(path);
        }

        // examine post nodes
        for (Time node : neighbours) {
            SONConnection con = net.getSONConnection(node, visited.getLast());
            if (visited.contains(node)) {
                continue;
            }
            if (visited.getLast().getStartTime().isSpecified()) {
                break;
            } else if (con != null && con.getTime().isSpecified()) {
                Interval[] result = new Interval[2];
                result[0] = con.getTime();
                result[1] = durationAccumulator1(visited);
                resultTimeAndDuration.add(result);

                Path path = new Path();
                visited.addLast(node);
                path.addAll(visited);
                visited.removeLast();
                bwdPaths.add(path);
            }
        }
        // in depth-first, recursion needs to come after visiting post nodes
        for (Time node : neighbours) {
            SONConnection con = net.getSONConnection(node, visited.getLast());
            if (visited.contains(node) || visited.getLast().getStartTime().isSpecified()) {
                continue;
            } else if (con != null && con.getTime().isSpecified()) {
                continue;
            }
            visited.addLast(node);
            backwardDFS(visited, nodes);
            visited.removeLast();

        }
    }

    private Interval durationAccumulator1(LinkedList<Time> visited) {
        Interval result = new Interval(0000, 0000);
        Time first = visited.getFirst();
        for (Time time : visited) {
            if (time != first) {
                if (time.getDuration().isSpecified()) {
                    result = result.add(time.getDuration());
                } else {
                    result = result.add(defaultDuration);
                }
            }
        }
        return result;
    }

    protected LinkedList<Time> getCausalPreset(Time n, Collection<Node> nodes) {
        LinkedList<Time> preSet = new LinkedList<>();
        LinkedList<Time> result = new LinkedList<>();

        for (TransitionNode[] pre : before) {
            if (pre[1] == n) {
                preSet.add(pre[0]);
            }
        }

        for (Node node : getPrePNSet(n)) {
            if (node instanceof Time) {
                preSet.add((Time) node);
            }
        }

        if (n instanceof TransitionNode) {
            for (SONConnection con : net.getSONConnections(n)) {
                if (con.getSemantics() == Semantics.SYNCLINE) {
                    if (con.getFirst() == n) {
                        preSet.add((Time) con.getSecond());
                    } else {
                        preSet.add((Time) con.getFirst());
                    }
                } else if (con.getSemantics() == Semantics.ASYNLINE && con.getSecond() == n) {
                    preSet.add((Time) con.getFirst());
                }
            }
        } else if (n instanceof ChannelPlace) {
            Node input = net.getPreset(n).iterator().next();
            preSet.add((Time) input);
            Collection<Semantics> semantics = net.getSONConnectionTypes(n);
            if (semantics.iterator().next() == Semantics.SYNCLINE) {
                Node output = net.getPostset(n).iterator().next();
                preSet.add((Time) output);
            }
        }

        for (Time node : preSet) {
            if (nodes.contains(node)) {
                result.add(node);
            }
        }

        return result;
    }

    protected LinkedList<Time> getCausalPostset(Time n, Collection<Node> nodes) {
        LinkedList<Time> postSet = new LinkedList<>();
        LinkedList<Time> result = new LinkedList<>();

        for (TransitionNode[] post : before) {
            if (post[0] == n) {
                postSet.add(post[1]);
            }
        }

        for (Node node :getPostPNSet(n)) {
            if (node instanceof Time) {
                postSet.add((Time) node);
            }
        }

        if (n instanceof TransitionNode) {
            for (SONConnection con : net.getSONConnections(n)) {
                if (con.getSemantics() == Semantics.SYNCLINE) {
                    if (con.getFirst() == n) {
                        postSet.add((Time) con.getSecond());
                    } else {
                        postSet.add((Time) con.getFirst());
                    }
                } else if (con.getSemantics() == Semantics.ASYNLINE && con.getFirst() == n) {
                    postSet.add((Time) con.getSecond());
                }
            }

        } else if (n instanceof ChannelPlace) {
            Node output = net.getPostset(n).iterator().next();
            postSet.add((Time) output);
            Collection<Semantics> semantics = net.getSONConnectionTypes(n);
            if (semantics.iterator().next() == Semantics.SYNCLINE) {
                Node input = net.getPreset(n).iterator().next();
                postSet.add((Time) input);
            }
        }

        for (Time node : postSet) {
            if (nodes.contains(node)) {
                result.add(node);
            }
        }

        return result;
    }

    public String intervals(Collection<Interval[]> intervals) {
        ArrayList<String> strs = new ArrayList<>();
        for (Interval[] interval : intervals) {
            strs.add(interval[0].toString());
        }
        return "(" + strs.toString() + ")";
    }

    public Collection<Interval[]> getResultTimeAndDuration() {
        return resultTimeAndDuration;
    }

    public void clearSets() {
        resultTimeAndDuration.clear();
    }
}
