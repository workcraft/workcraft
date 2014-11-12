package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;

public class BSONCycleAlg extends ONCycleAlg{

	private SON net;
	private BSONAlg bsonAlg;

	public BSONCycleAlg(SON net){
		super(net);
		this.net = net;
		bsonAlg =new BSONAlg(net);
	}

//	@Override
//	public List<Node[]> createAdj(Collection<Node> nodes){
//
//		List<Node[]> result = new ArrayList<Node[]>();
//
//		for (Node n: nodes){
//			for (Node next: net.getPostset(n))
//				if(nodes.contains(next) && net.getSONConnectionType(n, next) != Semantics.BHVLINE){
//					Node[] adjoin = new Node[2];
//					adjoin[0] = n;
//					adjoin[1] = next;
//					result.add(adjoin);
//
//					if(net.getSONConnectionType(n, next) == Semantics.SYNCLINE){
//						Node[] reAdjoin = new Node[2];
//						reAdjoin[0] = next;
//						reAdjoin[1] = n;
//						if(!result.contains(reAdjoin))
//							result.add(reAdjoin);
//
//				}
//			}
//			if(n instanceof TransitionNode)
//				result.addAll(bsonAlg.before((TransitionNode)n));
//		}
//		return result;
//	}

	/**
	 * create Integer Graph for a nodes set
	 * Synchronous communication would be treated as an undirected line.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Integer>[] createGraph(List<Node> nodes){
		List<Integer>[] result = new List[nodes.size()];

		LinkedHashMap<Node, Integer> nodeIndex = new LinkedHashMap<Node, Integer>();
		for(int i = 0; i < nodes.size(); i++){
			nodeIndex.put(nodes.get(i), i);
		}

		if(nodes.size() == nodeIndex.size()){
			for(int i = 0; i < nodes.size(); i++){
				int index = nodeIndex.get(nodes.get(i));

				if(result[index] == null){
					result[index] = new ArrayList<Integer>();
				}

				for(Node post: net.getPostset(nodes.get(index))){
					if(nodes.contains(post) && net.getSONConnectionType(nodes.get(index), post) != Semantics.BHVLINE){
						result[index].add(nodeIndex.get(post));

						//reverse direction for synchronous connection
						if(net.getSONConnectionType(nodes.get(index), post) == Semantics.SYNCLINE){
							int index2 = nodeIndex.get(post);
							if(result[index2] == null){
								result[index2] = new ArrayList<Integer>();
							}
							result[index2].add(index);
						}
					}
				}
			}
		}else{
			throw new RuntimeException("fail to create graph, input size is not equal to nodeIndex size");
		}

		for(int i = 0; i < nodes.size(); i++){
			//add before relation
			if(nodes.get(i) instanceof TransitionNode){
				for(Condition[] before : bsonAlg.before((TransitionNode)nodes.get(i))){
					result[nodeIndex.get(before[0])].add(nodeIndex.get(before[1]));
				}
			}
		}
		return result;
	}

	@Override
	public Collection<Path> cycleTask (Collection<Node> nodes){
		 return cyclePathFliter(super.cycleTask(nodes));
	}

	private Collection<Path> cyclePathFliter(Collection<Path> paths){
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
		return paths;
	}

}
