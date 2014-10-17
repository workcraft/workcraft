package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.TransitionNode;

public class BSONPathAlg extends ONPathAlg{

	private SON net;
	private BSONAlg bsonAlg;

	public BSONPathAlg(SON net){
		super(net);
		this.net = net;
		bsonAlg =new BSONAlg(net);
	}

	@Override
	public List<Node[]> createAdj(Collection<Node> nodes){

		List<Node[]> result = new ArrayList<Node[]>();

		for (Node n: nodes){
			for (Node next: net.getPostset(n))
				if(nodes.contains(next) && net.getSONConnectionType(n, next) != Semantics.BHVLINE){
					Node[] adjoin = new Node[2];
					adjoin[0] = n;
					adjoin[1] = next;
					result.add(adjoin);

					if(net.getSONConnectionType(n, next) == Semantics.SYNCLINE){
						Node[] reAdjoin = new Node[2];
						reAdjoin[0] = next;
						reAdjoin[1] = n;
						if(!result.contains(reAdjoin))
							result.add(reAdjoin);

				}
			}
			if(n instanceof TransitionNode)
				result.addAll(bsonAlg.before((TransitionNode)n));
		}
		return result;
	}

	@Override
	public Collection<Path> cycleTask (Collection<Node> nodes){
		List<Path> result = new ArrayList<Path>();
		for(Node start : relationAlg.getInitial(nodes))
			for(Node end : relationAlg.getFinal(nodes))
				result.addAll(PathAlgorithm.getCycles(start, end, createAdj(nodes)));

		 return cyclePathFilter(result);
	}

	private Collection<Path> cyclePathFilter(List<Path> paths){
		List<Path> delList = new ArrayList<Path>();
		for(Path cycle : paths){
			int outputBhvLine = 0;
			int inputBhvLine = 0;
			if(!net.getSONConnectionTypes(cycle).contains(Semantics.PNLINE))
				delList.add(cycle);
			for(Node n : cycle){
				if(net.getOutputSONConnections(n).contains(Semantics.BHVLINE))
					outputBhvLine ++;
				if(net.getInputSONConnections(n).contains(Semantics.BHVLINE))
					inputBhvLine ++;
			if(inputBhvLine==0 || outputBhvLine==0)
				delList.add(cycle);
			}
		}
		paths.removeAll(delList);
		return PathAlgorithm.merging(paths);
	}

}
