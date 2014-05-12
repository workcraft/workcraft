package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SONModel;

public class CSONPathAlg extends PathAlgorithm{

	private SONModel net;

	public CSONPathAlg(SONModel net) {
		super(net);
		this.net = net;
	}

	@Override
	public List<Node[]> createAdj(Collection<Node> nodes){

		List<Node[]> result = new ArrayList<Node[]>();

		for (Node n: nodes){
			for (Node next: net.getPostset(n))
				if(nodes.contains(next)){
					Node[] adjoin = new Node[2];
					adjoin[0] = n;
					adjoin[1] = next;
					result.add(adjoin);

					if(net.getSONConnectionType(n, next) == "SYNCLINE"){
						Node[] reAdjoin = new Node[2];
						reAdjoin[0] = next;
						reAdjoin[1] = n;
						if(!result.contains(reAdjoin))
							result.add(reAdjoin);
				}
			}
		}
		return result;
	}

	@Override
	public Collection<ArrayList<Node>> cycleTask (Collection<Node> nodes){

		this.clearAll();
		for(Node start : relationAlg.getInitial(nodes))
			for(Node end : relationAlg.getFinal(nodes))
				getAllPath(start, end, createAdj(nodes));

		 return cyclePathFilter(cycleResult);
	}

	private Collection<ArrayList<Node>> cyclePathFilter(Collection<ArrayList<Node>> pathResult){
		List<ArrayList<Node>> delList = new ArrayList<ArrayList<Node>>();
		for (ArrayList<Node> path : pathResult){
			if(!net.getSONConnectionTypes(path).contains("POLYLINE"))
				delList.add(path);
			if(!net.getSONConnectionTypes(path).contains("SYNCLINE") && !net.getSONConnectionTypes(path).contains("ASYNLINE"))
				delList.add(path);
		}
		pathResult.removeAll(delList);

		return pathResult;
	}

}
