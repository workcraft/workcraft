package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;

public class PathAlgorithm{

	protected SON net;
	protected RelationAlgorithm relationAlg;

	private Collection<Node> history;
	protected Collection<Path> pathResult;
	protected Collection<Path> cycleResult;


	public PathAlgorithm(SON net){
		this.net = net;
		relationAlg = new RelationAlgorithm(net);
		history = new Path();
		pathResult =new  HashSet<Path>();
		cycleResult = new HashSet<Path>();
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

	public static Collection<Path> getCycles(Node s, Node v, List<Node[]> adj, Path history, Collection<Path> result){
		history.add(s);
		for (int i=0; i< adj.size(); i++){
			if (((Node)adj.get(i)[0]).equals(s)){
				if(!history.contains((Node)adj.get(i)[1])){
					getCycles((Node)adj.get(i)[1], v, adj, history, result);
				}
				else {
					Path cycle=new Path();
					cycle.addAll(history);
					int n=cycle.indexOf(((Node)adj.get(i)[1]));
					for (int m = 0; m < n; m++ ){
						cycle.remove(0);
					}
					result.add(cycle);
				}
			}
		}
		history.remove(s);
		return result;
	}

	public void getAllPath(Node s, Node v, List<Node[]> adj){

		history.add(s);

		if(s == v){
			Path path = new Path();
			path.add(s);
			pathResult.add(path);
		}

		for (int i=0; i< adj.size(); i++){
			if (((Node)adj.get(i)[0]).equals(s)){
				if(((Node)adj.get(i)[1]).equals(v)){
					Path path= new Path();

					path.addAll(history);
					path.add(v);
					pathResult.add(path);
					continue;
				}
				else if(!history.contains((Node)adj.get(i)[1])){
					getAllPath((Node)adj.get(i)[1], v, adj);
				}
				else {
					Path cycle=new Path();

						cycle.addAll(history);
						int n=cycle.indexOf(((Node)adj.get(i)[1]));
						for (int m = 0; m < n; m++ ){
							cycle.remove(0);
						}
						cycleResult.add(cycle);
				}
			}
		}
		history.remove(s);
	}

	public Collection<Path> cycleTask (Collection<Node> nodes){
		this.clearAll();
		for(Node start : relationAlg.getInitial(nodes))
			for(Node end : relationAlg.getFinal(nodes))
				getAllPath(start, end, createAdj(nodes));

		 return cycleResult;
	}

	public Collection<Path> pathTask (Collection<Node> nodes){
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

	public Collection<Path> getPathSet(){
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

	public Collection<Path> backwardCycleTask (Collection<Node> nodes){

		this.clearAll();
		for(Node start : relationAlg.getFinal(nodes))
			for(Node end : relationAlg.getInitial(nodes))
				getAllPath(start, end, createBackwardAdj(nodes));

		 return cycleResult;
	}

	public Collection<Path> backwardPathTask (Collection<Node> nodes){
		this.clearAll();
		for(Node start : relationAlg.getFinal(nodes))
			for(Node end : relationAlg.getInitial(nodes))
				getAllPath(start, end, createBackwardAdj(nodes));

		 return pathResult;
	}
}

