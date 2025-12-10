package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.ASONAlg;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.BSONCycleAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.exception.UnboundedException;
import org.workcraft.plugins.son.util.Marking;
import org.workcraft.plugins.son.util.Phase;

public class BSONStructureTask extends AbstractStructuralVerification {

    private final SON net;

    private final Collection<Node> relationErrors = new ArrayList<>();
    private final Collection<Path> cycleErrors = new ArrayList<>();
    private final Collection<ONGroup> groupErrors = new HashSet<>();

    private final BSONAlg bsonAlg;
    private final BSONCycleAlg bsonCycleAlg;
    private final Map<Condition, Collection<Phase>> allPhases;
    private final ArrayList<String> phaseCutTask = new ArrayList<>();

    private int errNumber = 0;
    private static final int warningNumber = 0;

    public BSONStructureTask(SON net, Map<ONGroup, List<Marking>> allMarkings) {
        super(net);
        this.net = net;

        bsonAlg = new BSONAlg(net);
        if (allMarkings == null) {
            allPhases = bsonAlg.getAllPhases(getReachableMarking());
        } else {
            allPhases = bsonAlg.getAllPhases(allMarkings);
        }
        bsonCycleAlg = new BSONCycleAlg(net, allPhases);
    }

    @Override
    public void task(Collection<ONGroup> groups) {

        infoMsg("-----------------Behavioral-SON Structure Verification-----------------");

        // group info
        infoMsg("Initialising selected groups and components...");
        ArrayList<MathNode> components = new ArrayList<>();

        for (ONGroup group : groups) {
            components.addAll(group.getComponents());
        }

        infoMsg("Selected Groups : " + net.toString(groups));

        if (!net.getSONConnectionTypes(components).contains(Semantics.BHVLINE)) {
            infoMsg("Task terminated: no behavioural abstraction in selected groups.");
            return;
        }

        ArrayList<ChannelPlace> relatedCPlaces = new ArrayList<>();
        relatedCPlaces.addAll(getRelationAlg().getRelatedChannelPlace(groups));
        components.addAll(relatedCPlaces);

        // Upper-level group structure task
        infoMsg("Running model structure and component relation tasks...");
        infoMsg("Running Upper-level ON structure task...");
        groupErrors.addAll(groupTask1(groups));
        if (groupErrors.isEmpty()) {
            infoMsg("Valid upper-level ON structure.");
        } else {
            for (ONGroup group : groupErrors) {
                errMsg("Invalid Upper-level ON structure (not line-like/has both input and output behavioural relations).",
                        group);
            }
        }
        infoMsg("Upper-level ON structure task complete.");

        // a/synchronous relation group task
        infoMsg("Running a/synchronous relation task...");
        Collection<ChannelPlace> task2 = groupTask2(groups);
        relationErrors.addAll(task2);
        if (relationErrors.isEmpty()) {
            infoMsg("Valid a/synchronous relation.");
        } else {
            for (ChannelPlace cPlace : task2) {
                errMsg("Invalid BSON structure " + "(A/Synchronous communication between upper and lower level ONs).",
                        cPlace);
            }
        }
        infoMsg("A/synchronous relation task complete.");

        // phase decomposition task
        infoMsg("Running phase structure task...");
        Collection<ONGroup> upperGroups = getBSONAlg().getUpperGroups(groups);

        Map<Condition, String> phaseResult = phaseMainTask(upperGroups);
        if (!phaseResult.isEmpty()) {
            for (Condition c : phaseResult.keySet()) {
                errMsg(phaseResult.get(c));
                relationErrors.add(c);
            }
        } else if (!phaseCutTask.isEmpty()) {
            for (String str : phaseCutTask) {
                infoMsg(str);
            }
        } else {
            infoMsg("Valid phase structure.");
        }

        infoMsg("Phase checking tasks complete.");

        // BSON cycle task
        infoMsg("Running cycle detection task...");
        cycleErrors.addAll(getBSONCycleAlg().cycleTask(components));

        if (cycleErrors.isEmpty()) {
            infoMsg("Behavioral-SON is cycle free.");
        } else {
            errNumber++;
            errMsg("Model involves BSCON cycle paths = " + cycleErrors.size() + ".");
            int i = 1;
            for (Path cycle : cycleErrors) {
                infoMsg("Cycle " + i + ": " + cycle.toString(net));
                i++;
            }
        }

        infoMsg("Cycle detection task complete.");
        infoMsg("Model strucuture and component relation tasks complete.\n");

        errNumber += relationErrors.size();
        errNumber += groupErrors.size();
    }

    private Collection<ONGroup> groupTask1(Collection<ONGroup> groups) {
        Collection<ONGroup> result = new HashSet<>();

        for (ONGroup group : groups) {
            if (getBSONAlg().isLineLikeGroup(group)) {

                boolean isInput = false;
                boolean isOutput = false;

                for (MathNode node : group.getComponents()) {
                    if (net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE)) {
                        isInput = true;
                    }
                    if (net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE)) {
                        isOutput = true;
                    }
                }

                if (isInput && isOutput) {
                    result.add(group);
                }
            } else {
                for (MathNode node : group.getComponents()) {
                    if (net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE)) {
                        result.add(group);
                    }
                }
            }
        }
        return result;
    }

    // correctness of A/SYN communication between upper and lower level ONs
    private Collection<ChannelPlace> groupTask2(Collection<ONGroup> groups) {
        Collection<ChannelPlace> result = new HashSet<>();
        Collection<ONGroup> upperGroups = getBSONAlg().getUpperGroups(groups);

        for (ChannelPlace cPlace : getRelationAlg().getRelatedChannelPlace(groups)) {
            int inUpperGroup = 0;

            Collection<Node> connectedNodes = new HashSet<>();
            connectedNodes.addAll(net.getPostset(cPlace));
            connectedNodes.addAll(net.getPreset(cPlace));

            for (Node node : connectedNodes) {
                for (ONGroup group : upperGroups) {
                    if (group.getComponents().contains(node)) {
                        inUpperGroup++;
                    }
                }
            }

            if (inUpperGroup < connectedNodes.size() && inUpperGroup != 0) {
                result.add(cPlace);
            }
        }

        return result;
    }

    private Map<Condition, String> phaseMainTask(Collection<ONGroup> upperGroups) {
        Map<Condition, String> result = new HashMap<>();

        for (ONGroup uGroup : upperGroups) {
            result.putAll(phaseTask1(uGroup));
            result.putAll(phaseTask2(uGroup));
            result.putAll(phaseTask3(uGroup));

            result.putAll(bsonAlg.getPhaseCutErr());
        }

        return result;
    }

    // check for upper level condition
    private Map<Condition, String> phaseTask1(ONGroup upperGroup) {
        Map<Condition, String> result = new HashMap<>();

        for (Condition c : upperGroup.getConditions()) {
            String ref = net.getNodeReference(c);
            if (!getBSONAlg().isUpperCondition(c)) {
                result.put(c, "Upper level condition does not has phase: " + ref);
            }
        }
        return result;
    }

    // check for upper level initial/final state
    private Map<Condition, String> phaseTask2(ONGroup upperGroup) {
        Map<Condition, String> result = new HashMap<>();

        for (Condition c : upperGroup.getConditions()) {
            Collection<Phase> phases = getAllPhases().get(c);
            String ref = net.getNodeReference(c);
            // the minimal phases of every initial state of upper group must
            // also be the initial state of lower group
            if (getRelationAlg().isInitial(c)) {
                Collection<Condition> minSet = getBSONAlg().getMinimalPhase(phases);

                for (Condition min : minSet) {
                    if (!getRelationAlg().isInitial(min)) {
                        result.put(c, "The minimal phase of " + ref + " does not reach initial state.");
                        break;
                    }
                }
            }
            // the maximal phases of every final state of upper group must also
            // be the final state of lower group
            if (getRelationAlg().isFinal(c)) {
                Collection<Condition> maxSet = getBSONAlg().getMaximalPhase(getAllPhases().get(c));

                for (Condition max : maxSet) {
                    if (!getRelationAlg().isFinal(max)) {
                        result.put(c, "The maximal phase of " + ref + " does not reach final state.");
                        break;
                    }
                }
            }
        }
        return result;
    }

    // check for joint
    private Map<Condition, String> phaseTask3(ONGroup upperGroup) {
        Map<Condition, String> result = new HashMap<>();

        for (Condition c : upperGroup.getConditions()) {
            Condition pre = null;
            Collection<Phase> phases = getAllPhases().get(c);

            if (!getRelationAlg().getPrePNCondition(c).isEmpty()) {
                pre = getRelationAlg().getPrePNCondition(c).iterator().next();
            }

            if (pre != null) {
                Collection<Phase> prePhases = getAllPhases().get(pre);
                Collection<Condition> preMax = getBSONAlg().getMaximalPhase(prePhases);
                System.out.println("premax+" + net.toString(preMax));

                for (Phase phase : phases) {
                    boolean match = false;
                    Collection<Condition> min = getBSONAlg().getMinimalPhase(phase);
                    System.out.println("min+" + net.toString(min));
                    if (preMax.containsAll(min)) {
                        match = true;
                    }

                    if (!match) {
                        match = true;
                        ONGroup lowGroup = net.getGroup(phase.iterator().next());
                        boolean containFinal = false;

                        if (!min.containsAll(getRelationAlg().getONInitial(lowGroup))) {
                            match = false;
                        }
                        for (ONGroup group : getBSONAlg().getLowerGroups(pre)) {
                            if (preMax.containsAll(getRelationAlg().getONFinal(group))) {
                                containFinal = true;
                                break;
                            }
                        }
                        if (!containFinal) {
                            match = false;
                        }
                    }

                    if (!match) {
                        String ref = net.getNodeReference(c);
                        String ref2 = net.getNodeReference(pre);
                        result.put(c, "Disjoint phases between " + ref + " and " + ref2);
                    }
                }
            }
        }
        return result;
    }

    public Map<ONGroup, List<Marking>> getReachableMarking() {
        Map<ONGroup, List<Marking>> result = new HashMap<>();

        ASONAlg alg = new ASONAlg(net);
        Collection<ONGroup> lowerGroups = bsonAlg.getLowerGroups(net.getGroups());

        for (ONGroup group : lowerGroups) {
            try {
                result.put(group, alg.getReachableMarkings(group));
                break;
            } catch (UnboundedException ignored) {
            }
        }

        return result;
    }

    public boolean equals(Marking m, Collection<Condition> b) {

        if (m.size() != b.size()) {
            return false;
        }

        for (Node node : m) {
            if (!b.contains(node)) {
                return false;
            }
        }

        for (Node node : b) {
            if (!m.contains(node)) {
                return false;
            }
        }

        return true;
    }

    public BSONAlg getBSONAlg() {
        return this.bsonAlg;
    }

    public BSONCycleAlg getBSONCycleAlg() {
        return bsonCycleAlg;
    }

    public Map<Condition, Collection<Phase>> getAllPhases() {
        return allPhases;
    }

    @Override
    public Collection<String> getRelationErrors() {
        return getRelationErrorsSetRefs(relationErrors);
    }

    @Override
    public Collection<ArrayList<String>> getCycleErrors() {
        return getCycleErrorsSetRefs(cycleErrors);
    }

    @Override
    public Collection<String> getGroupErrors() {
        return getGroupErrorsSetRefs(groupErrors);
    }

    @Override
    public int getErrNumber() {
        return this.errNumber;
    }

    @Override
    public int getWarningNumber() {
        return warningNumber;
    }

}
