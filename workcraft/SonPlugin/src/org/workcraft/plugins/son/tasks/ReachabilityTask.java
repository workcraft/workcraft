package org.workcraft.plugins.son.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.*;
import org.workcraft.plugins.son.commands.ToolManager;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.tools.SONSimulationTool;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class ReachabilityTask implements Task<VerificationResult> {

    private final WorkspaceEntry we;
    private final BSONAlg bsonAlg;
    private final ReachabilityAlg reachAlg;

    private final Collection<String> markingRefs;
    private final Collection<String> causalPredecessorRefs;

    public ReachabilityTask(WorkspaceEntry we) {
        this.we = we;
        final SON net = WorkspaceUtils.getAs(we, SON.class);

        bsonAlg = new BSONAlg(net);
        reachAlg = new ReachabilityAlg(net);

        markingRefs = new ArrayList<>();
        causalPredecessorRefs = new HashSet<>();

        if (hasConflict(net)) {
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

    private boolean hasConflict(final SON net) {
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
            return Result.success();
        }

        final SON net = WorkspaceUtils.getAs(we, SON.class);
        if (reachabilityTask(net)) {
            int result = JOptionPane.showConfirmDialog(mainWindow,
                    "The selected marking is REACHABLE from the initial states. \n" +
                    "Select OK to analyze the trace leading to the marking in the simulation tool.",
                    "Reachability task result", JOptionPane.OK_CANCEL_OPTION);
            if (result == 0) {
                Map<PlaceNode, Boolean> finalStates = simulation();
                for (String ref : markingRefs) {
                    Node node = net.getNodeByReference(ref);
                    if (!finalStates.get(node)) {
                        throw new RuntimeException("Reachability task error, doesn't reach selected marking" + ref);
                    }
                }
                return Result.success();
            }
        } else {
            JOptionPane.showMessageDialog(mainWindow,
                    "The selected marking is UNREACHABLE from the initial states",
                    "Reachability task result", JOptionPane.INFORMATION_MESSAGE);
        }
        return Result.success();
    }

    private Map<PlaceNode, Boolean> simulation() {
        Map<PlaceNode, Boolean> result;
        final Toolbox toolbox = ToolManager.getToolboxPanel(we);
        final SONSimulationTool tool = toolbox.getToolInstance(SONSimulationTool.class);
        toolbox.selectTool(tool);
        final MainWindow mainWindow = Framework.getInstance().getMainWindow();
        final GraphEditor editor = mainWindow.getOrCreateEditor(we);
        result = tool.reachabilitySimulator(editor, causalPredecessorRefs, markingRefs);
        tool.mergeTrace(editor);
        return result;
    }

    private boolean reachabilityTask(final SON net) {
        //Collection<Node> initial = new HashSet<>();
        Collection<Node> sync = new HashSet<>();
        for (Path path : getSyncCycles(net)) {
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
        Collection<Node> causalPredecessors = new HashSet<>();

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
                consume.addAll(net.getPreset((MathNode) t));
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

    private Collection<Path> getSyncCycles(final SON net) {
        HashSet<Node> nodes = new HashSet<>();
        nodes.addAll(net.getTransitionNodes());
        nodes.addAll(net.getChannelPlaces());
        CSONCycleAlg cycleAlg = new CSONCycleAlg(net);

        return cycleAlg.syncCycleTask(nodes);
    }
}
