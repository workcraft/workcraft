package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.workcraft.dom.Node;

public class PathAlgorithm{

	private static Collection<Node> history = new Path();
	private static Collection<Path> pathResult =new  HashSet<Path>();
	private static Collection<Path> cycleResult = new HashSet<Path>();

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

	private static void clear(){
		history.clear();
		pathResult.clear();
		cycleResult.clear();
	}

	public static Collection<Path> getPaths(Node s, Node v, List<Node[]> adj){
		clear();
		DFS(s, v, adj);
		return pathResult;
	}

	public static Collection<Path> getCycles(Node s, Node v, List<Node[]> adj){
		clear();
		DFS(s, v, adj);
		return cycleResult;
	}

	public static  List<Path> merging (List<Path> cycles){
		List<Path> result = new ArrayList<Path>();

		while (cycles.size() > 0){
			Path first = cycles.get(0);
			List<Path> rest = cycles;
			rest.remove(0);

			int i = -1;
			while (first.size() > i){
				i = first.size();

				List<Path> rest2 = new ArrayList<Path>();
				for(Path path : rest){
					if(hasCommonElements(first, path)){
						first.addAll(path);
					}
					else{
						rest2.add(path);
					}
				}
				rest = rest2;
			}

			HashSet<Node> filter = new HashSet<Node>();
			for(Node node : first){
				filter.add(node);
			}

			Path subResult = new Path();
			subResult.addAll(filter);
			result.add(subResult);
			cycles = rest;
		}
		return result;
	}

	private static boolean hasCommonElements(Collection<Node> cycle1, Collection<Node> cycle2){
		for(Node n : cycle1)
			if(cycle2.contains(n))
				return true;
		for(Node n : cycle2)
			if(cycle1.contains(n))
				return true;
		return false;
	}
}

