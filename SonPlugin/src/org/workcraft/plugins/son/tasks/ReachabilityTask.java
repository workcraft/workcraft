package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.ReachabilityAlg;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.commands.ToolManager;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.tools.SONSimulationTool;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ReachabilityTask implements Task<VerificationResult> {

    private SON net;
    private final WorkspaceEntry we;
    private final BSONAlg bsonAlg;
    private final ReachabilityAlg reachAlg;

    private final Collection<String> markingRefs;
    private Collection<Node> causalPredecessors;
    private final Collection<String> causalPredecessorRefs;

    public ReachabilityTask(WorkspaceEntry we) {
        this.we = we;
        net = WorkspaceUtils.getAs(we, SON.class);

        bsonAlg = new BSONAlg(net);
        reachAlg = new ReachabilityAlg(net);

        markingRefs = new ArrayList<String>();
        causalPredecessors = new HashSet<Node>();
        causalPredecessorRefs = new HashSet<String>();

        if (hasConflict()) {
            JOptionPane.showMessageDialog(Framework.getInstance().getMainWindow(),
                    "Model has alternative behaviours",
                    "Fail to run reachability task", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (PlaceNode node : net.getPlaceNodes()) {
            if (node.isMarked()) {
                markingRefs.add(net.getNodeReference(node));
            }
        }
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

    @Override
    public Result<? extends VerificationResult> run(
            ProgressMonitor<? super VerificationResult> monitor) {

        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();

        if (markingRefs.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Double click on condition/channel place or use property editor"
                    + " to mark some nodes and check the reachability.",
                    "Marking required", JOptionPane.INFORMATION_MESSAGE);
            return new Result<VerificationResult>(Outcome.FINISHED);
        }

        if (reachabilityTask()) {
            net = WorkspaceUtils.getAs(we, SON.class);
            int result = JOptionPane.showConfirmDialog(mainWindow,
                    "The selected marking is REACHABLE from the initial states. \n" +
                    "Select OK to analyze the trace leading to the marking in the simulation tool.",
                    "Reachability task result", JOptionPane.OK_CANCEL_OPTION);
            if (result == 0) {
                Map<PlaceNode, Boolean> finalStates = simulation();
                for (String ref : markingRefs) {
                    Node node = net.getNodeByReference(ref);
                    if (finalStates.get(node) == false) {
                        throw new RuntimeException("Reachability task error, doesn't reach selected marking" + ref);
                    }
                }
                return new Result<VerificationResult>(Outcome.FINISHED);
            }
        } else {
            JOptionPane.showMessageDialog(mainWindow,
                    "The selected marking is UNREACHABLE from the initial states",
                    "Reachability task result", JOptionPane.INFORMATION_MESSAGE);
        }
        return new Result<VerificationResult>(Outcome.FINISHED);
    }

    private Map<PlaceNode, Boolean> simulation() {
        Map<PlaceNode, Boolean> result;
        final Toolbox toolbox = ToolManager.getToolboxPanel(we);
        final SONSimulationTool tool = toolbox.getToolInstance(SONSimulationTool.class);
        toolbox.selectTool(tool);
        result = tool.reachabilitySimulator(tool.getGraphEditor(), causalPredecessorRefs, markingRefs);
        tool.mergeTrace(tool.getGraphEditor());

        return result;
    }

    private boolean reachabilityTask() {
        //Collection<Node> initial = new HashSet<>();
        Collection<Node> sync = new HashSet<>();
        for (Path path : getSyncCycles()) {
            sync.addAll(path);
        }

        //if marking contains a synchronous channel place, it's unreachable.
        for (String ref : markingRefs) {
            Node node = net.getNodeByReference(ref);
            if (node instanceof ChannelPlace) {
                if (sync.contains(node)) {
                    return false;
                }
            }
        }
//        SimulationAlg simuAlg = new SimulationAlg(net);
//        Map<PlaceNode, Boolean> initialMarking = simuAlg.getInitialMarking();

//        for (Node c : initialMarking.keySet()) {
//            if (initialMarking.get(c)) {
//                initial.add(c);
//            }
//        }
        causalPredecessors = new HashSet<Node>();

        //get causalPredecessors for each marking
        for (String ref : markingRefs) {
            Node node = net.getNodeByReference(ref);
            causalPredecessors.addAll(reachAlg.getCausalPredecessors(node));
        }

        Collection<Node> consume = new HashSet<>();

        //get all place nodes which are the input (consumed) of causal predecessors
        for (Node t : causalPredecessors) {
            if (t instanceof TransitionNode) {
                causalPredecessorRefs.add(net.getNodeReference(t));
                for (Node pre : net.getPreset(t)) {
                    consume.add(pre);
                }
            }
        }

        // marking is reachable if
        //1. none of the marked conditions is consumed by causalPredecessors.
        //2. all of corresponding abstract conditions are not consumed by causalPredecessors
        for (String ref : markingRefs) {
            Node node = net.getNodeByReference(ref);
            if (consume.contains(node)) {
                return false;
            }
            Collection<Condition> upper = bsonAlg.getUpperConditions(node);
            if (!upper.isEmpty() && consume.containsAll(upper)) {
                return false;
            }
        }

        return true;
    }

    private Collection<Path> getSyncCycles() {
        HashSet<Node> nodes = new HashSet<>();
        nodes.addAll(net.getTransitionNodes());
        nodes.addAll(net.getChannelPlaces());
        CSONCycleAlg cycleAlg = new CSONCycleAlg(net);

        return cycleAlg.syncCycleTask(nodes);
    }
}
