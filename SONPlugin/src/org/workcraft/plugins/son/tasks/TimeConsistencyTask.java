package org.workcraft.plugins.son.tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.TimeConsistencySettings;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.EstimationAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.algorithm.ConsistencyAlg;
import org.workcraft.plugins.son.algorithm.EnhancedEstimationAlg;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.AlternativeStructureException;
import org.workcraft.plugins.son.exception.InvalidStructureException;
import org.workcraft.plugins.son.exception.TimeEstimationException;
import org.workcraft.plugins.son.exception.TimeEstimationValueException;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.Phase;
import org.workcraft.plugins.son.util.ScenarioRef;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeConsistencyTask implements Task<VerificationResult> {

    private SON net;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private ConsistencyAlg consistencyAlg;
    private EnhancedEstimationAlg estimationAlg;
    private RelationAlgorithm relationAlg;
    private BSONAlg bsonAlg;
    private TimeConsistencySettings settings;
    private Collection<Condition> lowerConditions;

    private Collection<ChannelPlace> syncCPs;
    private Map<Condition, Collection<Phase>> phases;

    private Collection<Time> estimatedIniNodes = new ArrayList<>();
    private Collection<Time> estimatedFinalNodes = new ArrayList<>();
    private Collection<Time> estimatedDurNodes = new ArrayList<>();

    private Color causalColor = new Color(225, 90, 70);
    private Color inconsistencyColor = new Color(250, 210, 80);

    private int totalErrNum = 0;

    public TimeConsistencyTask(WorkspaceEntry we, TimeConsistencySettings settings) {
        this.settings = settings;
        net = (SON) we.getModelEntry().getMathModel();
        initialise();
    }

    @Override
    public Result<? extends VerificationResult> run(
            ProgressMonitor<? super VerificationResult> monitor) {

        Collection<Node> checkList = new ArrayList<>();

        Collection<Node> unspecifyNodes = new ArrayList<>();
        Collection<Node> unspecifyPartialNodes = new ArrayList<>();

        Collection<Node> outOfBoundNodes = new ArrayList<>();
        Collection<Node> inconsistencyNodes = new ArrayList<>();
        Collection<Node> causalInconsistencyNodes = new ArrayList<>();

        infoMsg("-------------------------Time Consistency Checking Result-------------------------");
        if (settings.getTabIndex() == 0) {
            checkList = new ArrayList<Node>();
            Collection<ONGroup> groups = settings.getSelectedGroups();

            //group info
            infoMsg("Initialising selected groups and components...");

            for (ONGroup group : groups) {
                checkList.addAll(group.getComponents());
            }

            infoMsg("Selected Groups : " +  net.toString(groups));

            ArrayList<ChannelPlace> relatedCPlaces = new ArrayList<>();
            relatedCPlaces.addAll(relationAlg.getRelatedChannelPlace(groups));
            checkList.addAll(relatedCPlaces);

            infoMsg("Channel Places = " + relatedCPlaces.size() + "\n");

        } else if (settings.getTabIndex() == 1) {
            infoMsg("Initialising selected scenario...");
            if (settings.getSeletedScenario() != null) {
                checkList = settings.getSeletedScenario().getNodes(net);
            }
            infoMsg("Nodes = " + checkList.size() + "\n");

        } else if (settings.getTabIndex() == 2) {
            //node info
            infoMsg("Initialising selected components...");
            checkList = settings.getSeletedNodes();
            infoMsg("Selected nodes = " + checkList.size() + "\n");

        }

        if (settings.getGranularity() == Granularity.YEAR_YEAR) {
            infoMsg("Time granularity: T:year  D:year");
        } else if (settings.getGranularity() == Granularity.HOUR_MINS) {
            infoMsg("Time granularity: T:24 hour clock  D:minutes");
            infoMsg("Running time granularity checking task...");
            for (Node node : checkList) {
                ArrayList<String> result = consistencyAlg.granularityHourMinsTask(node);
                if (!result.isEmpty()) {
                    outOfBoundNodes.add(node);
                    infoMsg("Node:" + net.getNodeReference(node));
                    for (String str : result) {
                        totalErrNum++;
                        errMsg(str);
                    }
                }
            }
        }

        infoMsg("Remove invalid time granularity nodes from checking list...");
        checkList.removeAll(outOfBoundNodes);

        infoMsg("--------------------------------------------------");
        infoMsg("Running unspecified value checking task...");
        for (Node node : checkList) {

            String str = "";
            boolean startResult;
            boolean endResult;
            boolean durResult;
            String cpResult;

            if (settings.getTabIndex() == 1) {
                ScenarioRef s = settings.getSeletedScenario();

                startResult =  consistencyAlg.hasSpecifiedStart(node, s);
                endResult =  consistencyAlg.hasSpecifiedEnd(node, s);
                if (syncCPs.contains(node)) {
                    cpResult = consistencyAlg.hasSpecifiedCP(node, true, s);
                    durResult =  consistencyAlg.hasSpecifiedDur(node, true, s);
                } else {
                    cpResult = consistencyAlg.hasSpecifiedCP(node, false, s);
                    durResult =  consistencyAlg.hasSpecifiedDur(node, false, s);
                }
            } else {
                startResult =  consistencyAlg.hasSpecifiedStart(node, null);
                endResult =  consistencyAlg.hasSpecifiedEnd(node, null);
                if (syncCPs.contains(node)) {
                    cpResult = consistencyAlg.hasSpecifiedCP(node, true, null);
                    durResult =  consistencyAlg.hasSpecifiedDur(node, true, null);
                } else {
                    cpResult = consistencyAlg.hasSpecifiedCP(node, false, null);
                    durResult =  consistencyAlg.hasSpecifiedDur(node, false, null);
                }
            }

            str = startResult ? "" : "start() ";
            str = str + (endResult ? "" : "end() ");
            str = str + (durResult ? "" : "duration() ");
            str = str + (cpResult.equals("") ? "" : cpResult);

            if (!str.equals("")) {
                unspecifyNodes.add(node);
            }
            if ((str.contains("start") && !str.contains("end"))
                    || (!str.contains("start") && str.contains("end"))) {
                unspecifyPartialNodes.add(node);
            }
            //channel place will do causal checking iff
            //there is at least one specified value in its connected events
            if ((node instanceof ChannelPlace) && str.contains("partial")) {
                unspecifyPartialNodes.add(node);
            }

            if (settings.getTabIndex() == 1 && settings.isCausalConsistency()) {
                if ((str.contains("start") && str.contains("end"))
                        || str.contains("events") && (node instanceof ChannelPlace)) {
                    infoMsg("Node:" + net.getNodeReference(node));
                    infoMsg("-Unspecified time value: " + str);
                }
            } else {
                if (!str.equals("")) {
                    infoMsg("Node:" + net.getNodeReference(node));
                    infoMsg("-Unspecified time value: " + str);
                }
            }

        }

        infoMsg("Remove unspecified nodes from consistency checking list...");
        checkList.removeAll(unspecifyNodes);

        infoMsg("--------------------------------------------------");
        infoMsg("Running time consistency checking task...");
        Map<Node, ArrayList<String>> consistencyResult;

        if (settings.getTabIndex() == 1) {
            consistencyResult = timeConsistencyTask(checkList, settings.getSeletedScenario(), false);
        } else {
            consistencyResult = timeConsistencyTask(checkList, null, false);
        }

        for (Node node : consistencyResult.keySet()) {
            infoMsg("Node:" + net.getNodeReference(node));
            Time n = (Time) node;
            infoMsg("-start=" + (n.getStartTime()) + " end=" + (n.getEndTime()) + " duration=" + (n.getDuration()));

            ArrayList<String> strs = consistencyResult.get(node);
            if (!strs.isEmpty()) {
                for (String str : strs) {
                    inconsistencyNodes.add(node);
                    errMsg(str);
                    totalErrNum++;
                }
                consistencyAlg.setDefaultTime(node);
            }
        }

        if ((settings.getTabIndex() == 1) && settings.isCausalConsistency()) {
            infoMsg("--------------------------------------------------");
            infoMsg("Assign estimated time for nodes with partial time infomation...");
            infoMsg("Default duration = " + settings.getDefaultDuration().toString());

            for (Node node : unspecifyPartialNodes) {
                infoMsg("Node:" + net.getNodeReference(node));
                ArrayList<String> estimationResult = timeEstimationTask((Time) node);
                if (!estimationResult.isEmpty()) {
                    for (String str : estimationResult) {
                        infoMsg("-" + str);
                    }
                }
            }
            infoMsg("--------------------------------------------------");
            infoMsg("Running causal consistency checking task...");
            infoMsg("Default duration = " + settings.getDefaultDuration().toString());
            Map<Node, ArrayList<String>> causalResult;

            if (settings.getTabIndex() == 1) {
                causalResult = timeConsistencyTask(unspecifyPartialNodes, settings.getSeletedScenario(), false);
            } else {
                causalResult = timeConsistencyTask(unspecifyPartialNodes, null, false);
            }

            for (Node node : causalResult.keySet()) {
                infoMsg("Node:" + net.getNodeReference(node));
                Time n = (Time) node;
                infoMsg("-start=" + (n.getStartTime()) + " end=" + (n.getEndTime()) + " duration=" + (n.getDuration()));

                ArrayList<String> strs = causalResult.get(node);
                if (!strs.isEmpty()) {
                    causalInconsistencyNodes.add(node);
                    for (String str : strs) {
                        errMsg(str);
                        totalErrNum++;
                    }
                    consistencyAlg.setDefaultTime(node);
                }
            }
        }

        inconsistencyHighlight(settings.getInconsistencyHighlight(), inconsistencyNodes);
        unspecifyHighlight(settings.getUnspecifyHighlight(), unspecifyNodes);
        causalHighlight(settings.isCausalHighlight(), causalInconsistencyNodes);

        removeAssignedProperties();

        logger.info("\n\nVerification-Result : " + totalErrNum + " Error(s).");
        if (!SONSettings.getTimeVisibility()) {
            consistencyAlg.removeProperties();
        }
        return new Result<VerificationResult>(Outcome.FINISHED);
    }

    private Map<Node, ArrayList<String>> timeConsistencyTask(Collection<Node> nodes, ScenarioRef s, boolean isEstimated) {
        Map<Node, ArrayList<String>> result = new HashMap<>();

        Granularity g = settings.getGranularity();

        //ON consistency checking
        for (Node n : nodes) {
            Time node = (Time) n;
            //add node to map
            result.put(node, new ArrayList<String>());

            if ((node instanceof Condition) || (node instanceof TransitionNode)) {
                if (!isEstimated) {
                    try {
                        result.get(node).addAll(consistencyAlg.onConsistency(node, s, g));
                    } catch (InvalidStructureException e) {
                        e.printStackTrace();
                    }
                } else {
                    result.get(node).addAll(consistencyAlg.nodeConsistency(node, node.getStartTime(), node.getEndTime(), node.getDuration(), g));
                }
            }
        }

        //CSON and BSON consistency checking
        for (Node n: nodes) {
            if (n instanceof ChannelPlace) {
                ChannelPlace cp = (ChannelPlace) n;
                try {
                    result.get(cp).addAll(consistencyAlg.csonConsistency(cp, syncCPs, g));
                } catch (InvalidStructureException e) {
                    e.printStackTrace();
                }
            } else if (n instanceof TransitionNode) {
                result.get(n).addAll(consistencyAlg.bsonConsistency((TransitionNode) n, phases, s));
            } else if (n instanceof Condition) {
                Condition c = (Condition) n;
                if (lowerConditions.contains(c) && c.isInitial()) {
                    result.get(n).addAll(consistencyAlg.bsonConsistency2(c, s));;
                }
                if (lowerConditions.contains(c) && c.isFinal()) {
                    result.get(n).addAll(consistencyAlg.bsonConsistency3(c, s));
                }
            }
        }
        return result;
    }

    protected ArrayList<String> timeEstimationTask(Time t) {
        ArrayList<String> result = new ArrayList<>();

//        //set default duration
//        try {
//            if (!t.getStartTime().isSpecified()) {
//                estimationAlg.setEstimatedStartTime((Node) t);
//                if (relationAlg.isInitial(t)) {
//                    estimatedIniNodes.add(t);
//                }
//            }
//        } catch (TimeOutOfBoundsException e1) {
//            result.add(e1.getMessage());
//        } catch (TimeEstimationException e1) {
//            result.add(e1.getMessage());
//        } catch (TimeEstimationValueException e1) {
//            result.add(e1.getMessage());
//        }
        try {
            if (!t.getEndTime().isSpecified()) {
                estimationAlg.estimateFinish(t);
            }
        } catch (TimeOutOfBoundsException e) {
            result.add(e.getMessage());
        } catch (AlternativeStructureException e1) {
            errMsg(e1.getMessage());
        } catch (TimeEstimationException e1) {
            errMsg(e1.getMessage());
        }

        return result;
    }

    protected void initialise() {
        consistencyAlg = new ConsistencyAlg(net);
        consistencyAlg.removeProperties();
        consistencyAlg.setProperties();

        estimationAlg = new EnhancedEstimationAlg(net, settings.getDefaultDuration(), settings.getGranularity(), settings.getSeletedScenario(), false);

        syncCPs = getSyncCPs();

        relationAlg = new RelationAlgorithm(net);
        bsonAlg = new BSONAlg(net);
        phases = bsonAlg.getAllPhases();
        lowerConditions = getLowerConditions();
    }

    protected void removeAssignedProperties() {
        //remove assigned time properties
        Interval interval = new Interval();
        for (Node node: net.getComponents()) {
            consistencyAlg.setDefaultTime(node);
        }
        for (Time node : estimatedIniNodes) {
            node.setStartTime(interval);
        }
        for (Time node : estimatedFinalNodes) {
            node.setEndTime(interval);
        }
        for (Time node : estimatedDurNodes) {
            node.setDuration(interval);
        }
    }

    private Collection<ChannelPlace> getSyncCPs() {
        Collection<ChannelPlace> result = new HashSet<>();
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

    private Collection<Condition> getLowerConditions() {
        Collection<Condition> result = new ArrayList<>();
        Collection<ONGroup> lowerGroups = bsonAlg.getLowerGroups(net.getGroups());

        for (ONGroup group : lowerGroups) {
            result.addAll(group.getConditions());
        }
        return result;
    }

    private void inconsistencyHighlight(boolean b, Collection<Node> nodes) {
        if (b) {
            for (Node node : nodes) {
                net.setForegroundColor(node, inconsistencyColor);
            }
        }
    }

    private void unspecifyHighlight(boolean b, Collection<Node> nodes) {
        if (b) {
            for (Node node : nodes) {
                net.setForegroundColor(node, new Color(204, 204, 255));
            }
        }
    }

    private void causalHighlight(boolean b, Collection<Node> nodes) {
        if (b) {
            for (Node node : nodes) {
                net.setForegroundColor(node, causalColor);
            }
        }
    }

    public void infoMsg(String msg) {
        logger.info(msg);
    }

    public void errMsg(String msg) {
        logger.info("-ERR: " + msg);
    }
}
