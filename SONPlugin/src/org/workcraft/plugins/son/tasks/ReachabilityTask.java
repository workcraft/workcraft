package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.CycleAlgorithm;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;

public class ReachabilityTask implements Task<VerificationResult>{

	private SON net;

	public ReachabilityTask(SON net){
		this.net = net;
	}

	@Override
	public Result<? extends VerificationResult> run(
			ProgressMonitor<? super VerificationResult> monitor) {

		Collection<PlaceNode> marking = new ArrayList<PlaceNode>();

		for(PlaceNode node : net.getPlaceNodes())
			if(node.isMarked())
				marking.add(node);

		try {
			if(reachabilityTask(marking))
				System.out.println("CSON reachable");
			else
				System.out.println("CSON unreachable");
		} catch (StackOverflowError e) {
			JOptionPane.showMessageDialog(null,
					"Fail to run reachability anaylsis tool, " +
					"error may due to incorrect structure", "Invalid structure", JOptionPane.WARNING_MESSAGE);
		}

		return new Result<VerificationResult>(Outcome.FINISHED);
	}


	private boolean reachabilityTask(Collection<PlaceNode> marking){
		Collection<Path> sync = getSyncCycles();
		Collection<Node> syncCycles = new HashSet<Node>();
		for(Path path : sync){
			syncCycles.addAll(path);
		}
		//if marking contains a synchronous channel place, it's unreachable.
		for(PlaceNode node : marking){
			if(node instanceof ChannelPlace)
				if(syncCycles.containsAll(net.getPreset(node))
						&& syncCycles.containsAll(net.getPostset(node))){
					return false;
				}
		}

		Collection<Node> causalPredecessors = new HashSet<Node>();

		for(PlaceNode node : marking){
			System.out.println();
			System.out.println("marking = " + net.getNodeReference(node));
			causalPredecessors.addAll(getCausalPredecessors(node, sync));
			for(Node n : causalPredecessors){
				System.out.print(" " + net.getNodeReference(n));
			}
			System.out.println();
		}

		for(PlaceNode node : marking){
			if(causalPredecessors.contains(node))
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
						result.add(t);
						result.addAll(getCausalPredecessors(t, sync));
					}
				}
			}else{
				for(Node pre : relationAlg.getPreset(path)){
					for(TransitionNode t : CausalRelations(pre)){
						result.add(t);
						result.addAll(getCausalPredecessors(t, sync));
					}
				}
			}
		}else{
			for(Node pre : net.getPreset(node)){
				for(TransitionNode t : CausalRelations(pre)){
					result.add(t);
					result.addAll(getCausalPredecessors(t, sync));
				}
			}
		}
		return result;
	}

	private Collection<TransitionNode> CausalRelations(Node pre){
		Collection<Node> causalSet = new HashSet<Node>();
		BSONAlg bsonAlg = new BSONAlg(net);
		RelationAlgorithm relationAlg = new RelationAlgorithm(net);

		//if pre-condition is not max/min phase, add pre-pre-event to the set.
		if((pre instanceof Condition) && !(net.getSONConnectionTypes(pre).contains(Semantics.BHVLINE))){
			causalSet.addAll(net.getPreset(pre));
		}
		if(pre instanceof ChannelPlace){
			causalSet.addAll(net.getPreset(pre));
		}
		//if pre-condition is max/min phase, add pre-event of corresponding abstract condition to
		if((pre instanceof Condition)
				&& (net.getOutputSONConnectionTypes(pre).contains(Semantics.BHVLINE))
				&& (!net.getInputSONConnectionTypes(pre).contains(Semantics.BHVLINE))){
			//get corresponding abstract conditions
			Collection<Condition> absConditions = bsonAlg.getAbstractConditions(pre);
			//for each phase of abstract conditions,
			//if c is the minimum phase (but not maximum) of that condition, add its pre-event to the set.
			for(Condition c : absConditions){
				Phase phase = bsonAlg.getPhase(c);
				if(bsonAlg.getMinimalPhase(phase).contains(c) && !bsonAlg.getMaximalPhase(phase).contains(c))
					causalSet.addAll(relationAlg.getPrePNSet(c));
			}
		}
		//if pre-condition is abstract condition, get is minimum phase first and add its pre-events to the set
		if((pre instanceof Condition) && !(net.getOutputSONConnectionTypes(pre).contains(Semantics.BHVLINE))
				&&(net.getInputSONConnectionTypes(pre).contains(Semantics.BHVLINE))){
			Phase phase = bsonAlg.getPhase((Condition)pre);
			Collection<Condition> max = bsonAlg.getMinimalPhase(phase);
			for(Condition c : max){
				causalSet.addAll(relationAlg.getPrePNSet(c));
			}
		}

		Collection<TransitionNode> result = new HashSet<TransitionNode>();
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
		List<Path> result = new ArrayList<Path>();
		HashSet<Node> nodes = new HashSet<Node>();
		nodes.addAll(net.getTransitionNodes());
		nodes.addAll(net.getChannelPlaces());
		CycleAlgorithm cycleAlg = new CycleAlgorithm();
		CSONCycleAlg alg = new CSONCycleAlg(net);

		List<Node> list = new ArrayList<Node>();
		list.addAll(nodes);

		for(List<Integer> cycleIndex : cycleAlg.getCycles(alg.createGraph(list))){
			if(cycleIndex.size() > 1){
				Path cycle = new Path();
				for(Integer index : cycleIndex){
					cycle.add(list.get(index));
				}
				result.add(cycle);
			}
		}
		return result;
	}
}
