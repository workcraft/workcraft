package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.TransitionNode;

public class CSONCycleAlg extends ONCycleAlg{

	private SON net;

	public CSONCycleAlg(SON net) {
		super(net);
		this.net = net;
	}

//	@Override
//	public List<Node[]> createAdj(Collection<Node> nodes){
//
//		List<Node[]> result = new ArrayList<Node[]>();
//
//		for (Node n: nodes){
//			for (Node next: net.getPostset(n))
//				if(nodes.contains(next)){
//					Node[] adjoin = new Node[2];
//					adjoin[0] = n;
//					adjoin[1] = next;
//					if(!result.contains(adjoin))
//						result.add(adjoin);
//
//					if(net.getSONConnectionType(n, next) == Semantics.SYNCLINE){
//						Node[] reAdjoin = new Node[2];
//						reAdjoin[0] = next;
//						reAdjoin[1] = n;
//						if(!result.contains(reAdjoin))
//							result.add(reAdjoin);
//				}
//			}
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
					if(nodes.contains(post)){
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

		return result;
	}


//	@SuppressWarnings("unchecked")
//	public List<Integer>[] createSyncGraph(List<Node> nodes){
//		List<Integer>[] result = new List[nodes.size()];
//
//		LinkedHashMap<Node, Integer> nodeIndex = new LinkedHashMap<Node, Integer>();
//		for(int i = 0; i < nodes.size(); i++){
//			nodeIndex.put(nodes.get(i), i);
//		}
//
//		if(nodes.size() == nodeIndex.size()){
//			for(int i = 0; i < nodes.size(); i++){
//				int index = nodeIndex.get(nodes.get(i));
//
//				if(result[index] == null){
//					result[index] = new ArrayList<Integer>();
//				}
//				for(Node post: net.getPostset(nodes.get(index))){
//					if(((post instanceof ChannelPlace) && net.getSONConnectionType(post, nodes.get(index)) == Semantics.ASYNLINE)){
//						for(Node t : net.getPostset(post))
//							result[index].add(nodeIndex.get(t));
//				}
//					if((post instanceof ChannelPlace) && (net.getSONConnectionType(nodes.get(index), post) == Semantics.SYNCLINE)){
//						for(Node t : net.getPostset(post)){
//							result[index].add(nodeIndex.get(t));
//
//							int index2 = nodeIndex.get(t);
//							if(result[index2] == null){
//								result[index2] = new ArrayList<Integer>();
//							}
//							result[index2].add(index);
//						}
//					}
//				}
//			}
//		}else{
//			throw new RuntimeException("fail to create graph, input size is not equal to nodeIndex size");
//		}
//
//		return result;
//	}

	/**
	 * get synchronous cycle for a set of transition and channel place nodes.
	 */
	public Collection<Path> syncCycleTask(Collection<? extends Node> nodes){
		return syncCycleFliter(super.cycleTask(nodes));
	}

	//Flit synchronous cycles
	private Collection<Path> syncCycleFliter(Collection<Path> paths){
		List<Path> result = new ArrayList<Path>();
		for (Path path : paths){
			Path sub = new Path();
			for (Node node : path){
				if(node instanceof TransitionNode)
					sub.add(node);
			}
			result.add(sub);
		}
		return result;
	}

	@Override
	public Collection<Path> cycleTask (Collection<? extends Node> nodes){
		 return cycleFliter(super.cycleTask(nodes));
	}

	//Flit synchronous cycles
	private Collection<Path> cycleFliter(Collection<Path> paths){
		List<Path> delList = new ArrayList<Path>();
		for (Path path : paths){
			if(!net.getSONConnectionTypes(path).contains(Semantics.PNLINE))
				delList.add(path);
			if(!net.getSONConnectionTypes(path).contains(Semantics.SYNCLINE)
					&& !net.getSONConnectionTypes(path).contains(Semantics.ASYNLINE))
				delList.add(path);
		}
		paths.removeAll(delList);

		return paths;
	}

}
