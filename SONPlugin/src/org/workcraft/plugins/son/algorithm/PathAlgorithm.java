package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SONModel;

public class PathAlgorithm{

	protected SONModel net;
	protected RelationAlgorithm relationAlg;

	protected Collection<Node> history;
	protected Collection<ArrayList<Node>> pathResult;
	protected Collection<ArrayList<Node>> cycleResult;


	public PathAlgorithm(SONModel net){
		this.net = net;
		relationAlg = new RelationAlgorithm(net);
		history = new ArrayList<Node>();
		pathResult =new  HashSet<ArrayList<Node>>();
		cycleResult = new HashSet<ArrayList<Node>>();
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

	public void getAllPath(Node start, Node end, List<Node[]> adj){

		history.add(start);

		if(start == end){
			ArrayList<Node> path = new ArrayList<Node>();
			path.add(start);
			pathResult.add(path);
		}

		for (int i=0; i< adj.size(); i++){
			if (((Node)adj.get(i)[0]).equals(start)){
				if(((Node)adj.get(i)[1]).equals(end)){
					ArrayList<Node> path= new ArrayList<Node>();

					path.addAll(history);
					path.add(end);
					pathResult.add(path);
					continue;
				}
				if(!history.contains((Node)adj.get(i)[1])){
					getAllPath((Node)adj.get(i)[1], end, adj);
				}
				else {
					ArrayList<Node> cycle=new ArrayList<Node>();

						cycle.addAll(history);
						int n=cycle.indexOf(((Node)adj.get(i)[1]));
						for (int m = 0; m < n; m++ ){
							cycle.remove(0);
						}
						cycleResult.add(cycle);
				}
			}
		}
		history.remove(start);
	}

	public Collection<ArrayList<Node>> cycleTask (Collection<Node> nodes){

		this.clearAll();
		for(Node start : relationAlg.getInitial(nodes))
			for(Node end : relationAlg.getFinal(nodes))
				getAllPath(start, end, createAdj(nodes));

		 return cycleResult;
	}

	public Collection<ArrayList<Node>> pathTask (Collection<Node> nodes){

		this.clearAll();
		for(Node start : relationAlg.getInitial(nodes))
			for(Node end : relationAlg.getFinal(nodes))
				getAllPath(start, end, createAdj(nodes));
		 return pathResult;
	}

	public void clearAll(){
		history.clear();
		cycleResult.clear();
		pathResult.clear();
	}

	public Collection<ArrayList<Node>> getPathSet(){
		return this.pathResult;
	}

	//Backward Traverse
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

	public Collection<ArrayList<Node>> backwardCycleTask (Collection<Node> nodes){

		this.clearAll();
		for(Node start : relationAlg.getFinal(nodes))
			for(Node end : relationAlg.getInitial(nodes))
				getAllPath(start, end, createBackwardAdj(nodes));

		 return cycleResult;
	}

	public Collection<ArrayList<Node>> backwardPathTask (Collection<Node> nodes){
		this.clearAll();
		for(Node start : relationAlg.getFinal(nodes))
			for(Node end : relationAlg.getInitial(nodes))
				getAllPath(start, end, createBackwardAdj(nodes));

		 return pathResult;
	}
}

