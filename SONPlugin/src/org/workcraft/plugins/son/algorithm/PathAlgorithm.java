package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;

public class PathAlgorithm{

	private SON net;

	public PathAlgorithm(SON net) {
		this.net = net;
	}

	private static Collection<Node> history = new Path();
	private static Collection<Path> pathResult =new  HashSet<Path>();

	/**
	 * create adjacency matrix
	 */
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


	private static void DFS(Node s, Node v, List<Node[]> adj){
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
					DFS((Node)adj.get(i)[1], v, adj);
				}
			}
		}
		history.remove(s);
	}

	private static void clear(){
		history.clear();
		pathResult.clear();
	}

	public static Collection<Path> getPaths(Node s, Node v, List<Node[]> adj){
		clear();
		DFS(s, v, adj);
		return pathResult;
	}

	public Collection<Node> traverse (Collection<Node> s, Collection<Node> v){
		Collection<Node> result = new ArrayList<Node>();
        Stack<Node> stack = new Stack<Node>();

		for(Node s1 : s){
			for(Node v1 : v){
				stack.push(s1);
	            while(!stack.empty()){
            		s1 = stack.pop();
	            	if(!result.contains(s1)){
	            		result.add(s1);
	            	}

	            	if(s1 == v1){
	            		continue;
	            	}

	    			for (Node next: net.getPostset(s1)){
	    				if(!result.contains(next)){
	    					stack.push(next);
	    				}
	    			}
	            }
			}
		}
		return result;
	}
}

