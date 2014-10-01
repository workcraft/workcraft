package org.workcraft.plugins.son.algorithm;

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
					boolean repeat = false;
					for(Path path : cycleResult){
						if(path.containsAll(path));
							repeat = true;
					}
					if(!repeat)
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
}

