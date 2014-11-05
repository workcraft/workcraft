package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;

public class ONPathAlg{

	private SON net;
	protected RelationAlgorithm relationAlg;

	public ONPathAlg(SON net) {
		this.net = net;
		relationAlg = new RelationAlgorithm(net);
	}

	public List<Node[]> createAdj(Collection<Node> nodes){

		List<Node[]> result = new ArrayList<Node[]>();

		for (Node n: nodes){
			for (Node next: net.getPostset(n))
				if(nodes.contains(next)){
					Node[] adjoin = new Node[2];
					adjoin[0] = n;
					adjoin[1] = next;
					result.add(adjoin);
				}
		}
		return result;
	}

	/**
	 * create a backward adjacency matrix
	 */
	public List<Node[]> createBackwardAdj(Collection<Node> nodes){

		List<Node[]> result = new ArrayList<Node[]>();

		for (Node n: nodes){
			for (Node pre: net.getPreset(n))
				if(nodes.contains(pre)){
				Node[] adjoin = new Node[2];
				adjoin[0] = n;
				adjoin[1] = pre;
				result.add(adjoin);
				}
		}
		return result;
	}

	public Collection<Path> cycleTask (Collection<Node> nodes){
		List<Path> result = new ArrayList<Path>();
		for(Node start : relationAlg.getInitial(nodes))
			for(Node end : relationAlg.getFinal(nodes)){
				result.addAll(PathAlgorithm.getCycles(start, end, createAdj(nodes)));
				result.addAll(PathAlgorithm.getCycles(end, start, createBackwardAdj(nodes)));
			}
		 return PathAlgorithm.merging(result);
	}

	public Collection<Path> pathTask (Collection<Node> nodes){
		List<Path> result = new ArrayList<Path>();
		for(Node start : relationAlg.getInitial(nodes))
			for(Node end : relationAlg.getFinal(nodes)){
				result.addAll(PathAlgorithm.getPaths(start, end, createAdj(nodes)));
			}
		 return result;
	}
}
