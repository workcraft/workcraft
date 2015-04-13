package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
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

public class ReachabilityTask implements Task<VerificationResult>{

	private SON net;
	private final WorkspaceEntry we;
	private BSONAlg bsonAlg;

	private Collection<String> markingRefs;
	private Map<Condition, Phase> phases;
	private Collection<TransitionNode> causalPredecessors;
	private Collection<String> causalPredecessorRefs;


	public ReachabilityTask(WorkspaceEntry we){
		this.we = we;

		net = (SON)we.getModelEntry().getMathModel();
		bsonAlg = new BSONAlg(net);
		markingRefs = new ArrayList<String>();
		causalPredecessors = new HashSet<TransitionNode>();
		causalPredecessorRefs = new HashSet<String>();

		for(PlaceNode node : net.getPlaceNodes()){
			if(node.isMarked())
				markingRefs.add(net.getNodeReference(node));
		}

		phases = bsonAlg.getAllPhases();
	}

	@Override
	public Result<? extends VerificationResult> run(
			ProgressMonitor<? super VerificationResult> monitor) {
		if(markingRefs.isEmpty()){
			JOptionPane.showMessageDialog(null,
					"Double click on condition/channel place or use property editor"
					+ " to mark some nodes and check the reachability.",
					"Marking required", JOptionPane.INFORMATION_MESSAGE);
			return new Result<VerificationResult>(Outcome.FINISHED);
		}

		//change connections from block inside to bounding
		VisualSON vnet = (VisualSON)we.getModelEntry().getVisualModel();
		vnet.connectToBlocks(we);

		//cycle detection
		CSONCycleAlg cycleAlg = new CSONCycleAlg(net);
		if(!cycleAlg.cycleTask(net.getComponents()).isEmpty()){
			we.cancelMemento();
			JOptionPane.showMessageDialog(null,
					"Fail to run reachability anaylsis tool, " +
					"error due to cyclic structure", "Invalid structure", JOptionPane.WARNING_MESSAGE);
			return new Result<VerificationResult>(Outcome.FINISHED);
		}

		if(reachabilityTask()){
			we.cancelMemento();
			net = (SON)we.getModelEntry().getMathModel();
			int result = JOptionPane.showConfirmDialog(null,
					"The selected marking is REACHABLE from the initial states. \n" +
					"Select OK to analyze the trace leading to the marking in the simulation tool.",
					"Reachability task result", JOptionPane.OK_CANCEL_OPTION);
			if(result == 0){
				Map<PlaceNode, Boolean> finalStates = simulation();
				for(String ref : markingRefs){
					Node node = net.getNodeByReference(ref);
					if(finalStates.get(node) == false)
						throw new RuntimeException("Reachability task error, doesn't reach selected marking" + ref);
				}
				return new Result<VerificationResult>(Outcome.FINISHED);
			}
		}
		else{
			we.cancelMemento();
			JOptionPane.showMessageDialog(null,
					"The selected marking is UNREACHABLE from initial states",
					"Reachability task result", JOptionPane.INFORMATION_MESSAGE);
		}
		return new Result<VerificationResult>(Outcome.FINISHED);
	}

	private Map<PlaceNode, Boolean> simulation(){
		Map<PlaceNode, Boolean> result;
		final Framework framework = Framework.getInstance();
		final MainWindow mainWindow = framework.getMainWindow();
		GraphEditorPanel currentEditor = mainWindow.getCurrentEditor();
		if(currentEditor == null || currentEditor.getWorkspaceEntry() != we) {
			final List<GraphEditorPanel> editors = mainWindow.getEditors(we);
			if(editors.size()>0) {
				currentEditor = editors.get(0);
				mainWindow.requestFocus(currentEditor);
			} else {
				currentEditor = mainWindow.createEditorWindow(we);
			}
		}
		final ToolboxPanel toolbox = currentEditor.getToolBox();
		final SONSimulationTool tool = toolbox.getToolInstance(SONSimulationTool.class);
		toolbox.selectTool(tool);
		result = tool.ReachabilitySimulator(tool.getGraphEditor(), causalPredecessorRefs, markingRefs);
		tool.mergeTrace(tool.getGraphEditor());

		return result;
	}


	private boolean reachabilityTask(){
		Collection<Path> sync = getSyncCycles();
		Collection<Node> syncCycles = new HashSet<Node>();

		//if marking contains a synchronous channel place, it's unreachable.
		for(String ref : markingRefs){
			Node node = net.getNodeByReference(ref);
			if(node instanceof ChannelPlace)
				if(syncCycles.contains(node)) {
					return false;
				}
		}

		causalPredecessors = new HashSet<TransitionNode>();

		//get CausalPredecessors for each marking
		for(String ref : markingRefs){
			Node node = net.getNodeByReference(ref);
			causalPredecessors.addAll(getCausalPredecessors(node, sync));
		}

		Collection<Node> consume = new HashSet<Node>();

		//get all place nodes which are the input (consumed) of causal predecessors
		for(TransitionNode t : causalPredecessors){
			causalPredecessorRefs.add(net.getNodeReference(t));
			for(Node pre : net.getPreset(t)){
				consume.add(pre);
			}
		}
//		//test
//		for(Node n : causalPredecessors){
//			System.out.print(" " + net.getNodeReference(n));
//			net.setForegroundColor(n, Color.RED);
//		}
//		System.out.println();
//		System.out.println();
//		System.out.println("consume:");
//		for(Node n : consume){
//			System.out.print(" " + net.getNodeReference(n));
//		}
//		System.out.println();

		// marking is reachable if
		//1. none of the marked conditions is consumed by causalPredecessors.
		//2. all of corresponding abstract conditions are not consumed by causalPredecessors
		for(String ref : markingRefs){
			Node node = net.getNodeByReference(ref);
			if(consume.contains(node))
				return false;
			Collection<Condition> c = bsonAlg.getAbstractConditions(node);
			if(!c.isEmpty() && consume.containsAll(c))
				return false;
		}

		return true;
	}

	private Collection<TransitionNode> getCausalPredecessors (Node node, Collection<Path> sync){
		Collection<TransitionNode> result = new HashSet<TransitionNode>();

		RelationAlgorithm relationAlg = new RelationAlgorithm(net);

		Path path = isInSync(node, sync);

		if(node instanceof TransitionNode){
			if(path.isEmpty()){
				for(Node pre : net.getPreset(node)){
					for(TransitionNode t : CausalRelations(pre)){
						if(!result.contains(t)){
							result.add(t);
							result.addAll(getCausalPredecessors(t, sync));
						}
					}
				}
			}else{
				for(Node n : path){
					if(n instanceof TransitionNode && !result.contains(n))
						result.add((TransitionNode)n);
				}
				for(Node pre : relationAlg.getPreset(path)){
					for(TransitionNode t : CausalRelations(pre)){
						if(!result.contains(t)){
							result.add(t);
							result.addAll(getCausalPredecessors(t, sync));
						}
					}
				}
			}
		}
		else{
			for(TransitionNode t : CausalRelations(node)){
				if(!result.contains(t)){
					result.add(t);
					result.addAll(getCausalPredecessors(t, sync));
				}
			}
		}
		return result;
	}

	private List<TransitionNode> CausalRelations(Node pre){
		List<Node> causalSet = new ArrayList<Node>();
		RelationAlgorithm relationAlg = new RelationAlgorithm(net);

		//if condition is not max/min phase, add its pre-event to the set.
		if((pre instanceof Condition) && !(net.getSONConnectionTypes(pre).contains(Semantics.BHVLINE))){
			causalSet.addAll(relationAlg.getPrePNSet(pre));
		}
		//else if pre is channel place, add its pre-event to the set
		else if(pre instanceof ChannelPlace){
			causalSet.addAll(net.getPreset(pre));
		}
		//else if marking contains pre and pre is not initial state (off-line structure), add its PN pre-event to the set
		else if(markingRefs.contains(net.getNodeReference(pre)) && !relationAlg.isInitial(pre)){
			causalSet.addAll(relationAlg.getPrePNSet(pre));
		}
		//else if 'pre' is a bhv condition,
		else if((pre instanceof Condition)
				&& (net.getOutputSONConnectionTypes(pre).contains(Semantics.BHVLINE))
				&& (!net.getInputSONConnectionTypes(pre).contains(Semantics.BHVLINE))){
			//get corresponding abstract conditions
			Collection<Condition> absConditions = bsonAlg.getAbstractConditions(pre);
			//for each phase of abstract conditions,
			for(Condition c : absConditions){
				Phase phase = phases.get(c);
				Collection<Condition> max = bsonAlg.getMaximalPhase(phase);
				Collection<Condition> min = bsonAlg.getMinimalPhase(phase);
				//if pre is in min, add abstract pre-event to the abstract set.
				if (min.contains(pre)){
					causalSet.addAll(relationAlg.getPrePNSet(c));
				}
				//if pre is maximal but not minimal phase, add behavioural pre-event to the behavioural set.
				else if(!min.contains(pre) && max.contains(pre))
					causalSet.addAll(relationAlg.getPrePNSet(pre));
			}
		}
		//if 'pre' is an abstract condition, get minimal and maximal phase,
		//if min == max, add abstract pre-event to the set
		//else add pre-behavioural event of which min!=max
		else if(bsonAlg.isAbstractCondition(pre)){
			Phase phase = phases.get(pre);
			Collection<Condition> min = bsonAlg.getMinimalPhase(phase);
			Collection<Condition> max = bsonAlg.getMaximalPhase(phase);
			if(min.containsAll(max) && max.containsAll(min)){
				causalSet.addAll(relationAlg.getPrePNSet(pre));
			}
			else{
				for(Condition cMax : max){
					if(!min.contains(cMax))
						causalSet.addAll(relationAlg.getPrePNSet(cMax));
				}
			}
		}

		List<TransitionNode> result = new ArrayList<TransitionNode>();
		for(Node node : causalSet){
			if(node instanceof TransitionNode)
				result.add((TransitionNode)node);
		}

		return result;
	}


	private Path isInSync(Node node, Collection<Path> sync){
		Path result = new Path();
		for(Path path : sync)
			if(path.contains(node)){
				result = path;
			}
		return result;
	}

	private Collection<Path> getSyncCycles(){
		HashSet<Node> nodes = new HashSet<Node>();
		nodes.addAll(net.getTransitionNodes());
		nodes.addAll(net.getChannelPlaces());
		CSONCycleAlg cycleAlg = new CSONCycleAlg(net);

		return cycleAlg.syncCycleTask(nodes);
	}
}
