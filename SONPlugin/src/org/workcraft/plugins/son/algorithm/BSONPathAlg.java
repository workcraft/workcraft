package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.Event;

public class BSONPathAlg extends PathAlgorithm{

	private SONModel net;
	private BSONAlg bsonAlg;

	public BSONPathAlg(SONModel net){
		super(net);
		this.net = net;
		bsonAlg =new BSONAlg(net);
	}

	@Override
	public List<Node[]> createAdj(Collection<Node> nodes){

		List<Node[]> result = new ArrayList<Node[]>();

		for (Node n: nodes){
			for (Node next: net.getPostset(n))
				if(nodes.contains(next) && net.getSONConnectionType(n, next) != "BHVLINE"){
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
			if(n instanceof Event || n instanceof Block)
				result.addAll(bsonAlg.before(n));
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

	private Collection<ArrayList<Node>> cyclePathFilter(Collection<ArrayList<Node>> result){
		List<ArrayList<Node>> delList = new ArrayList<ArrayList<Node>>();
		for(ArrayList<Node> cycle : result){
			int outputBhvLine = 0;
			int inputBhvLine = 0;
			if(!net.getSONConnectionTypes(cycle).contains("POLYLINE"))
				delList.add(cycle);
			for(Node n : cycle){
				if(net.getOutputSONConnections(n).contains("BHVLINE"))
					outputBhvLine ++;
				if(net.getInputSONConnections(n).contains("BHVLINE"))
					inputBhvLine ++;
			if(inputBhvLine==0 || outputBhvLine==0)
				delList.add(cycle);
			}
		}
		result.removeAll(delList);
		return result;
	}

}
