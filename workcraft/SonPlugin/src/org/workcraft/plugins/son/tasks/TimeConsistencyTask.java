package org.workcraft.plugins.son.tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.TimeConsistencySettings;
import org.workcraft.plugins.son.algorithm.ConsistencyAlg;
import org.workcraft.plugins.son.algorithm.DFSEstimationAlg;
import org.workcraft.plugins.son.algorithm.TimeAlg;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.exception.AlternativeStructureException;
import org.workcraft.plugins.son.exception.TimeEstimationException;
import org.workcraft.plugins.son.exception.TimeOutOfBoundsException;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class TimeConsistencyTask implements Task<VerificationResult> {

    private final SON net;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private ConsistencyAlg consistencyAlg;
    private DFSEstimationAlg estimationAlg;
    private final TimeConsistencySettings settings;

    private static final Color causalColor = new Color(225, 90, 70);
    private static final Color inconsistencyColor = new Color(250, 210, 80);

    private int totalErrNum = 0;

    public TimeConsistencyTask(WorkspaceEntry we, TimeConsistencySettings settings) {
        this.settings = settings;
        net = WorkspaceUtils.getAs(we, SON.class);
        initialise();
    }

    @Override
    public Result<? extends VerificationResult> run(ProgressMonitor<? super VerificationResult> monitor) {

        Collection<Node> checkList = new ArrayList<>();
        Map<Node, Boolean[]> timeInfoMap = new HashMap<>();

        Collection<Node> specifyNodes = new ArrayList<>();
        Collection<Node> unspecifyNodes = new ArrayList<>();
        Collection<Node> partialNodes = new ArrayList<>();
        Map<Node, Boolean[]> partialNodesMap = new HashMap<>();

        Collection<Node> outOfBoundNodes = new ArrayList<>();
        Collection<Node> inconsistencyNodes = new ArrayList<>();
        Collection<Node> causalInconsistencyNodes = new ArrayList<>();

        infoMsg("-------------------------Time Consistency Checking Result-------------------------");
        if (settings.getTabIndex() == 0) {
            infoMsg("Initialising selected scenario...");
            checkList = consistencyAlg.getScenario().getNodes(net);
            infoMsg("Nodes = " + checkList.size() + "\n");

        } else if (settings.getTabIndex() == 1) {
            // node info
            infoMsg("Initialising selected components...");
            checkList = settings.getSeletedNodes();
            infoMsg("Selected nodes = " + checkList.size() + "\n");
        }

        // create partial node map
        // s[0] = start
        // s[1] = duration
        // s[2] = end
        infoMsg("Create time information map...");
        timeInfoMap = createTimeInfoMap(consistencyAlg.getScenario().getNodes(net));

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

        // specifiedNodes: place nodes and transition nodes with specified
        // start,
        // duration and end; channel place with specified start and end.
        // unspecifiedNodes: nodes with unspecified start and end
        // partialNodes: start or end is unspecified
        infoMsg("--------------------------------------------------");
        infoMsg("Running time information checking task...");
        for (Node node : checkList) {

            Time t = (Time) node;
            if (!t.getStartTime().isSpecified() && !t.getEndTime().isSpecified()) {
                unspecifyNodes.add(t);
            } else if (t.getStartTime().isSpecified() && t.getEndTime().isSpecified()) {
                if (t instanceof ChannelPlace) {
                    specifyNodes.add(t);
                } else {
                    if (t.getDuration().isSpecified()) {
                        specifyNodes.add(t);
                    } else {
                        partialNodes.add(node);
                    }
                }
            } else {
                partialNodes.add(node);
            }

        }

        infoMsg("Node with complete time information： " + specifyNodes.size());
        infoMsg("Node with partial time information： " + partialNodes.size());
        infoMsg("Node with empty time information： " + unspecifyNodes.size());

        if ((settings.getTabIndex() == 0) && settings.isCausalConsistency()) {
            infoMsg("--------------------------------------------------");
            infoMsg("Set estimated time interval for nodes with partial time infomation...");
            infoMsg("Default duration = " + settings.getDefaultDuration().toString());
            infoMsg("Create partial time information map...");
            partialNodesMap = createTimeInfoMap(partialNodes);

            Map<Node, ArrayList<String>> estimationResult = timeEstimationTask(partialNodesMap);
            for (Node node : estimationResult.keySet()) {
                Time n = (Time) node;
                infoMsg("Node:" + net.getNodeReference(node));
                String value = "";
                Boolean[] s = partialNodesMap.get(node);
                if (!s[0] && n.getStartTime().isSpecified()) {
                    value = "Estimated start = " + ((Time) node).getStartTime() + " ";
                }
                if (!s[1] && n.getDuration().isSpecified()) {
                    value += "Estimated duration = " + ((Time) node).getDuration() + " ";
                }
                if (!s[2] && n.getEndTime().isSpecified()) {
                    value += "Estimated finish = " + ((Time) node).getEndTime();
                }
                if (!value.isEmpty()) {
                    infoMsg("-" + value);
                }

                ArrayList<String> err = estimationResult.get(node);
                if (!err.isEmpty()) {
                    for (String str : err) {
                        infoMsg("-" + str);
                    }
                } else {
                    specifyNodes.add(node);
                }
            }
        }

        infoMsg("Node with complete time information： " + specifyNodes.size());

        infoMsg("--------------------------------------------------");
        infoMsg("Running ON consistency checking task...");

        Map<Node, ArrayList<String>> onResult = consistencyAlg.onConsistency(specifyNodes);

        for (Node node : onResult.keySet()) {
            infoMsg("Node:" + net.getNodeReference(node));
            Time n = (Time) node;
            infoMsg("-start=" + (n.getStartTime()) + " finish=" + (n.getEndTime()) + " duration=" + (n.getDuration()));

            ArrayList<String> err = onResult.get(node);
            for (String str : err) {
                errMsg(str);
                totalErrNum++;
            }
            if (!err.isEmpty()) {
                inconsistencyNodes.add(node);
            }
        }

        infoMsg("--------------------------------------------------");
        infoMsg("Running CSON consistency checking task...");

        Map<Node, ArrayList<String>> csonResult = consistencyAlg.csonConsistency(specifyNodes);

        for (Node node : csonResult.keySet()) {
            infoMsg("Node:" + net.getNodeReference(node));
            Time n = (Time) node;
            infoMsg("-start=" + (n.getStartTime()) + " finish=" + (n.getEndTime()) + " duration=" + (n.getDuration()));

            ArrayList<String> err = csonResult.get(node);
            for (String str : err) {
                errMsg(str);
                totalErrNum++;
            }
            if (!err.isEmpty()) {
                inconsistencyNodes.add(node);
            }
        }
        infoMsg("--------------------------------------------------");
        infoMsg("Running BSON consistency checking task...");

        Map<HashSet<Node>, ArrayList<String>> bsonResult = consistencyAlg.bsonConsistency(specifyNodes);
        for (HashSet<Node> nodes : bsonResult.keySet()) {
            infoMsg("Behavioural causal nodes:" + net.toString(nodes));

            ArrayList<String> err = bsonResult.get(nodes);
            for (String str : err) {
                errMsg(str);
                totalErrNum++;
            }
            if (!err.isEmpty()) {
                inconsistencyNodes.addAll(nodes);
            }
        }

        inconsistencyHighlight(settings.getInconsistencyHighlight(), inconsistencyNodes);
        unspecifyHighlight(settings.getUnspecifyHighlight(), unspecifyNodes);
        causalHighlight(settings.isCausalHighlight(), causalInconsistencyNodes);

        complete(timeInfoMap);
        logger.info("\n\nVerification-Result : " + totalErrNum + " Error(s).");

        return new Result<>(Outcome.SUCCESS);
    }

    private Map<Node, Boolean[]> createTimeInfoMap(Collection<Node> nodes) {
        Map<Node, Boolean[]> result = new HashMap<>();
        for (Node node : nodes) {
            Time t = (Time) node;

            Boolean[] s = new Boolean[3];
            if (t.getStartTime().isSpecified()) {
                s[0] = true;
            } else {
                s[0] = false;
            }
            if (t.getDuration().isSpecified()) {
                s[1] = true;
            } else {
                s[1] = false;
            }
            if (t.getEndTime().isSpecified()) {
                s[2] = true;
            } else {
                s[2] = false;
            }
            result.put(node, s);
        }
        return result;
    }

    private Map<Node, ArrayList<String>> timeEstimationTask(Map<Node, Boolean[]> map) {
        Map<Node, ArrayList<String>> result = new HashMap<>();

        for (Node node : map.keySet()) {
            Boolean[] s = map.get(node);
            ArrayList<String> subStr = new ArrayList<>();
            if (!s[0]) {
                try {
                    estimationAlg.estimateStartTime(node);
                } catch (TimeOutOfBoundsException | TimeEstimationException e1) {
                    subStr.add(e1.getMessage());
                }
            }

            if (!s[1]) {
                ((Time) node).setDuration(settings.getDefaultDuration());
            }

            if (!s[2]) {
                try {
                    estimationAlg.estimateEndTime(node);
                } catch (TimeOutOfBoundsException | TimeEstimationException e1) {
                    subStr.add(e1.getMessage());
                }
            }

            result.put(node, subStr);
        }

        return result;
    }

    public void initialise() {
        try {
            consistencyAlg = new ConsistencyAlg(net, settings.getDefaultDuration(), settings.getGranularity(),
                    settings.getSeletedScenario());
            estimationAlg = new DFSEstimationAlg(net, settings.getDefaultDuration(), settings.getGranularity(),
                    settings.getSeletedScenario());
        } catch (AlternativeStructureException e) {
            errMsg(e.getMessage());
            return;
        }

        consistencyAlg.prepare();

        TimeAlg.removeProperties(net);
        TimeAlg.setProperties(net);
    }

    protected void complete(Map<Node, Boolean[]> map) {
        if (!SONSettings.getTimeVisibility()) {
            TimeAlg.removeProperties(net);
        }

        for (Node node : map.keySet()) {
            if (!map.get(node)[1]) {
                ((Time) node).setDuration(new Interval());
            }
        }

        consistencyAlg.complete();
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
        logger.info("[ERROR] " + msg);
    }
}
