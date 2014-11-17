package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.CycleAlgorithm;
import org.workcraft.plugins.son.algorithm.ONCycleAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;

public class ReachabilityTask implements Task<VerificationResult>{

	private SON net;

	public ReachabilityTask(SON net){
		this.net = net;
	}

	@Override
	public Result<? extends VerificationResult> run(
			ProgressMonitor<? super VerificationResult> monitor) {
		return null;
	}

	private boolean BSONReachable(Collection<PlaceNode> marking){
		Collection<PlaceNode> abstractNodes = new ArrayList<PlaceNode>();
		BSONAlg bsonAlg = new BSONAlg(net);
		//get abstract conditions
		for(PlaceNode node : marking){
			if(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE)
					&& !net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE)){
				abstractNodes.add(node);
				//if there exist at least one marked bhv condition in the phase of node
				if(!hasCommonElements(bsonAlg.getPhase((Condition)node), marking))
					return false;
			}
		}
		//get non-abstract node
		marking.removeAll(abstractNodes);
		//for each node, check if its corresponding abstract conditions are all marked.
		for(PlaceNode node : marking){
			for(Condition con : bsonAlg.getAbstractConditions(node)){
				if (!con.isMarked())
					return false;
			}
		}
		return true;
	}

	private boolean hasCommonElements(Phase set1, Collection<PlaceNode> set2){
		for(Node n : set1)
			if(set2.contains(n))
				return true;
		for(Node n : set2)
			if(set1.contains(n))
				return true;
		return false;
}

	private boolean CSONReachable(Collection<PlaceNode> marking){
		boolean result = true;
		Collection<Node> syncCycles = new HashSet<Node>();
		for(Path path : getSyncCycles()){
			syncCycles.addAll(path);
		}
		//if marking contains a synchronous channel place, then it's unreachable.
		for(PlaceNode node : marking){
			if(node instanceof ChannelPlace)
				if(syncCycles.containsAll(net.getPreset(node))
						&& syncCycles.containsAll(net.getPostset(node))){
					return false;
				}
		}

		Collection<TransitionNode> causalPredecessors = new HashSet<TransitionNode>();

		return result;
	}

	private Collection<Node> causalPredecessors (Node node, Collection<Node> history,  Collection<Path> sync){
		Collection<Node> result = new HashSet<Node>();
		history.add(node);
		Path path = isInSync(node, sync);
		if(path.isEmpty()){
			for(Node pre : net.getPreset(node)){
				result.add(pre);
				result.addAll(causalPredecessors(pre, history, sync));
			}
		}else{

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
