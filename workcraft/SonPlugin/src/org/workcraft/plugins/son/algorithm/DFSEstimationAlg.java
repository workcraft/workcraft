package org.workcraft.plugins.son.algorithm;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.AlternativeStructureException;
import org.workcraft.plugins.son.exception.TimeEstimationException;
import org.workcraft.plugins.son.exception.TimeInconsistencyException;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Before;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;

import java.awt.*;
import java.util.List;
import java.util.*;

public class DFSEstimationAlg extends TimeAlg {

    protected Before before;

    private final List<Interval> possibleTimes;
    private final List<Interval> boundary;
    private final Set<Time> duration;
    private boolean[] b = new boolean[3];

    protected Color color = Color.ORANGE;

    public DFSEstimationAlg(SON net, Interval d, Granularity g, ScenarioRef s) throws AlternativeStructureException {
        super(net, d, g, s);

        BSONAlg bsonAlg = new BSONAlg(net);
        before = bsonAlg.getBeforeList();

        possibleTimes = new ArrayList<>();
        boundary = new ArrayList<>();
        duration = new HashSet<>();
    }

    public void estimateEndTime(Node n) throws TimeOutOfBoundsException, TimeEstimationException {
        if (((Time) n).getEndTime().isSpecified()) {
            return;
        }
        LinkedList<Time> visited = new LinkedList<>();
        visited.add((Time) n);
        forwardDFSDurations(visited);

        Interval result = null;
        if (!possibleTimes.isEmpty()) {
            result = Interval.getOverlapping(possibleTimes);
            possibleTimes.clear();
        } else {
            throw new TimeEstimationException("Cannot find causally time value (forward).");
        }

        String str = intervalsToString(boundary);
        boundary.clear();
        if (result == null) {
            throw new TimeEstimationException("Causal time intervals are not consistent. Forward boundary: " + str);
        }
        b[2] = true;
        ((Time) n).setEndTime(result);
    }

    public void estimateStartTime(Node n) throws TimeOutOfBoundsException, TimeEstimationException {

        if (((Time) n).getStartTime().isSpecified()) {
            return;
        }
        LinkedList<Time> visited = new LinkedList<>();
        visited.add((Time) n);
        backwardDFSDurations(visited);

        Interval result = null;
        if (!possibleTimes.isEmpty()) {
            result = Interval.getOverlapping(possibleTimes);
            possibleTimes.clear();
        } else {
            throw new TimeEstimationException("Cannot find causally time value (backward).");
        }

        String str = intervalsToString(boundary);
        boundary.clear();
        if (result == null) {
            throw new TimeEstimationException("Causal time intervals are not consistent. Backward boundary: " + str);
        }

        b[0] = true;
        ((Time) n).setStartTime(result);
    }

    public void estimatDuration(Node n)
            throws TimeInconsistencyException, TimeOutOfBoundsException, AlternativeStructureException {

        Time t = (Time) n;
        if (!t.getStartTime().isSpecified() || !t.getEndTime().isSpecified()) {
            return;
        }

        Interval preD = ((Time) n).getDuration();
        Interval d = granularity.subtractTT(t.getStartTime(), t.getEndTime());
        d = Interval.getOverlapping(d, preD);

        if (d != null) {
            ((Time) n).setDuration(d);
            b[1] = true;
        } else {
            throw new TimeInconsistencyException("Warning: duration and time intervals are inconsistent.");
        }

        ConsistencyAlg alg = new ConsistencyAlg(net, null, g, scenario);
        if (!alg.nodeConsistency(n).isEmpty()) {
            throw new TimeInconsistencyException("Warning: Estimated start and end time intervals are inconsistent.\n");
        }
    }

    private void forwardDFSDurations(LinkedList<Time> visited) throws TimeOutOfBoundsException {
        for (Time t : getCausalPostset(visited.getLast(), scenario.getNodes(net))) {
            Interval i = null;
            // set default value for any t with unspecified duration.
            if (!t.getDuration().isSpecified()) {
                t.setDuration(defaultDuration);
                duration.add(t);
            }

            if (t.getEndTime().isSpecified()) {
                visited.add(t);
                boundary.add(t.getEndTime());
                i = granularity.subtractTD(t.getEndTime(), accumulatedDurations(visited));
                visited.removeLast();
                possibleTimes.add(i);
            } else if (!visited.contains(t)) {
                visited.add(t);
                forwardDFSDurations(visited);
                visited.removeLast();
            }
        }
    }

    private void backwardDFSDurations(LinkedList<Time> visited) throws TimeOutOfBoundsException {
        for (Time t : getCausalPreset(visited.getLast(), scenario.getNodes(net))) {
            Interval i = null;
            // set default value for any t with unspecified duration.
            if (!t.getDuration().isSpecified()) {
                t.setDuration(defaultDuration);
                duration.add(t);
            }

            if (t.getStartTime().isSpecified()) {
                visited.add(t);
                boundary.add(t.getStartTime());
                i = granularity.plusTD(t.getStartTime(), accumulatedDurations(visited));
                visited.removeLast();
                possibleTimes.add(i);
            } else if (!visited.contains(t)) {
                visited.add(t);
                backwardDFSDurations(visited);
                visited.removeLast();
            }
        }
    }

    // assign specified value from connections to nodes
    @Override
    public void prepare() {
        super.prepare();
        duration.clear();
    }

    // assign estimated time value from nodes to connections
    public void complete(Node n) {

        if (b[0]) {
            Interval s = ((Time) n).getStartTime();
            Collection<SONConnection> inputs = null;
            inputs = net.getInputPNConnections((MathNode) n);

            for (SONConnection con : inputs) {
                con.setTime(s);
                con.setTimeLabelColor(color);
            }
        }

        if (b[1]) {
            if (n instanceof PlaceNode) {
                ((PlaceNode) n).setDurationColor(color);
            }
        }

        if (b[2]) {
            Interval e = ((Time) n).getEndTime();
            Collection<SONConnection> outputs = null;
            outputs = net.getOutputPNConnections((MathNode) n);

            for (SONConnection con : outputs) {
                con.setTime(e);
                con.setTimeLabelColor(color);
            }
        }
        b = new boolean[3];

        for (Time node : duration) {
            node.setDuration(new Interval());
        }

        super.complete();
    }

    private Interval accumulatedDurations(LinkedList<Time> visited) {
        Interval result = new Interval(0, 0);
        Time first = visited.getFirst();
        for (Time time : visited) {
            if (time != first) {
                result = result.add(time.getDuration());
            }
        }
        return result;
    }

    protected LinkedList<Time> getCausalPostset(Time n, Collection<Node> nodes) {
        LinkedList<Time> postset = new LinkedList<>();
        LinkedList<Time> result = new LinkedList<>();

        for (TransitionNode[] post : before) {
            if (post[0] == n) {
                postset.add(post[1]);
            }
        }

        for (Node node : getPostPNSet(n)) {
            if (node instanceof Time) {
                postset.add((Time) node);
            }
        }

        if (n instanceof TransitionNode) {
            postset.addAll(getPostASynEvents((TransitionNode) n));
        }

        for (Time node : postset) {
            if (nodes.contains(node)) {
                result.add(node);
            }
        }

        return result;
    }

    protected LinkedList<Time> getCausalPreset(Time n, Collection<Node> nodes) {
        LinkedList<Time> preset = new LinkedList<>();
        LinkedList<Time> result = new LinkedList<>();

        for (TransitionNode[] pre : before) {
            if (pre[1] == n) {
                preset.add(pre[0]);
            }
        }

        for (Node node : getPrePNSet(n)) {
            if (node instanceof Time) {
                preset.add((Time) node);
            }
        }

        if (n instanceof TransitionNode) {
            preset.addAll(getPreASynEvents((TransitionNode) n));
        }

        for (Time node : preset) {
            if (nodes.contains(node)) {
                result.add(node);
            }
        }

        return result;
    }

    public String intervalsToString(List<Interval> intervals) {
        ArrayList<String> strs = new ArrayList<>();
        for (Interval interval : intervals) {
            strs.add(interval.toString());
        }
        return "(" + strs + ")";
    }

}
