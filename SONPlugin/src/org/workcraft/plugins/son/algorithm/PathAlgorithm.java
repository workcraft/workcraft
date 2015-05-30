package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;

public class PathAlgorithm{

	private SON net;
	private static Collection<Path> pathResult =new ArrayList<Path>();

	public PathAlgorithm(SON net) {
		this.net = net;
	}

	//get path between two given nodes. (iteration)
    public Collection<Path> dfs3(Node s, Node v, Collection<Node> nodes){
    	Collection<Path> result =new ArrayList<Path>();
        Stack<Node> stack = new Stack<Node>();
        LinkedList<Node> visit = new LinkedList<Node>();

        stack.push(s);

        while(!stack.isEmpty()){
        	s = stack.peek();
            visit.add(s);

            Node n = null;
        	for(Node post : getPostset(s, nodes)){
                if (visit.contains(post)) {
                    continue;
                }
                else if (post.equals(v)) {
                	n = post;
                	stack.push(post);
                	visit.add(post);
                    Path path = new Path();

                    path.addAll(visit);
                    result.add(path);
                    visit.removeLast();
                    break;
                }
                else if(!visit.contains(post)){
                	n = post;
                	stack.push(n);
                }
        	}
        	if(n == null){
    			while(!stack.isEmpty()){
    				s = stack.peek();
    				if(!visit.isEmpty() && s==visit.peekLast()){
    					stack.pop();
    					visit.removeLast();
    				}else{
    					break;
    				}
    			}
    		}
        }
        return result;
    }

    private void dfs(Collection<Node> nodes , LinkedList<Node> visited, Node v) {
        LinkedList<Node> post = getPostset(visited.getLast(), nodes);

        if (visited.getLast().equals(v)) {
            Path path = new Path();
            path.addAll(visited);
            pathResult.add(path);
        }

        // examine post nodes
        for (Node node : post) {
            if (visited.contains(node)) {
                continue;
            }
            if (node.equals(v)) {
                visited.add(node);
                Path path = new Path();
                path.addAll(visited);
                pathResult.add(path);
                visited.removeLast();
                break;
            }
        }
        // in depth-first, recursion needs to come after visiting post nodes
        for (Node node : post) {
            if (visited.contains(node) || node.equals(v)) {
                continue;
            }
            visited.addLast(node);
            dfs(nodes, visited, v);
            visited.removeLast();

        }
    }

    //get paths between two given nodes. (recursion)
    public Collection<Path> getPaths (Node s, Node v, Collection<Node> nodes){
    	pathResult.clear();
    	LinkedList<Node> visited = new LinkedList<Node>();
    	visited.add(s);
    	dfs(nodes, visited, v);
    	return pathResult;
    }

    private LinkedList<Node> getPostset(Node n, Collection<Node> nodes){
    	LinkedList<Node> list = new LinkedList<Node>();
    	for(Node post : net.getPostset(n))
    		if(nodes.contains(post))
    			list.add(post);
    	return list;
    }
	//get nodes between two given nodes. (iteration)
	public static Collection<Node> dfs2 (Collection<Node> s, Collection<Node> v, SON net){
		Collection<Node> result = new HashSet<Node>();
		RelationAlgorithm relation = new RelationAlgorithm(net);
        Stack<Node> stack = new Stack<Node>();

		for(Node s1 : s){
			Collection<Node> visit = new ArrayList<Node>();
			stack.push(s1);
			visit.add(s1);

            while(!stack.empty()){
        		s1 = stack.peek();

            	if(v.contains(s1)){
            		result.add(s1);
            	}

            	Node post = null;
    			for (Node n: relation.getPostPNSet(s1)){
    				if(result.contains(n)){
    					result.add(s1);
    				}
    				if(!visit.contains(n)){
    					post = n;
    					break;
    				}
    			}

    			if(post != null){
    				visit.add(post);
    				stack.push(post);
    			}else{
    				stack.pop();
    			}
            }
		}
		return result;
	}
}

