package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
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

		if(BSONReachable(marking))
			System.out.println("BSON reachable");
		else
			System.out.println("BSON unreachable");

		try {
			if(CSONReachable(marking))
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

	private boolean BSONReachable(Collection<PlaceNode> marking){
		Collection<PlaceNode> absNodes = new HashSet<PlaceNode>();
		BSONAlg bsonAlg = new BSONAlg(net);
		//get abstract conditions
		for(PlaceNode node : marking){
			if(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE)
					&& !net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE)){
				absNodes.add(node);
			}
		}

		//get non-abstract node
		Collection<PlaceNode> bhvNodes = new ArrayList<PlaceNode>();
		bhvNodes.addAll(marking);
		bhvNodes.removeAll(absNodes);

		//get all abstract conditions for marking
		for(PlaceNode node : bhvNodes){
			if(node instanceof Condition){
				Collection<Condition> expectedAbsNodes = bsonAlg.getAbstractConditions((Condition)node);
				absNodes.addAll(expectedAbsNodes);
				marking.addAll(expectedAbsNodes);
			}
		}
		//check if
		if(hasOtherMarkedPlacesInGroup(absNodes, bsonAlg))
			return false;

		return true;
	}

	private boolean hasOtherMarkedPlacesInGroup(Collection<PlaceNode> absNodes, BSONAlg bsonAlg){
		Collection<ONGroup> absGroups = bsonAlg.getAbstractGroups(net.getGroups());

		for(ONGroup group : absGroups){
			int i = 0;
			for(PlaceNode pn : absNodes){
				if(group.contains(pn))
					i++;
			}
			if(i > 1)
				return true;
		}
		return false;
	}

	private boolean CSONReachable(Collection<PlaceNode> marking){
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
			causalPredecessors.addAll(causalPredecessors(node, sync));
		}

		for(PlaceNode node : marking){
			if(causalPredecessors.contains(node))
				return false;
		}

		return true;
	}

	private Collection<PlaceNode> causalPredecessors (Node node, Collection<Path> sync){
		Collection<PlaceNode> result = new HashSet<PlaceNode>();
		RelationAlgorithm relationAlg = new RelationAlgorithm(net);

		Path path = isInSync(node, sync);
		if(path.isEmpty()){
			for(Node pre : net.getPreset(node)){
				if(net.getSONConnectionType(node, pre) != Semantics.BHVLINE){
					if(pre instanceof PlaceNode){
						result.add((PlaceNode)pre);
					}
					result.addAll(causalPredecessors(pre, sync));
				}
			}
		}else{
			for(Node pre : relationAlg.getPreset(path)){
				if(pre instanceof PlaceNode){
					result.add((PlaceNode)pre);
				}
				result.addAll(causalPredecessors(pre, sync));
			}
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
