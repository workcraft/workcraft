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

public class BSONCycleAlg extends CSONCycleAlg{

	private SON net;
	private BSONAlg bsonAlg;

	public BSONCycleAlg(SON net){
		super(net);
		this.net = net;
		bsonAlg =new BSONAlg(net);
	}

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
            Node n = nodes.get(i);
            if(n instanceof TransitionNode){
                for(Condition[] before : bsonAlg.before((TransitionNode)n)){
                    Condition c0 = before[0];
                    Condition c1 = before[1];
                    int index = nodeIndex.get(c0);
                    if(result[index] == null){
                            result[index] = new ArrayList<Integer>();
                    }
                    result[index].add(nodeIndex.get(c1));
                }
            }
        }
        System.out.println("Index");
        for(Node key : nodeIndex.keySet()){
                System.out.println(net.getComponentLabel(key) + " " + nodeIndex.get(key) + " " + result[nodeIndex.get(key)].toString());
        }
		return result;
	}

	@Override
	public Collection<Path> cycleTask (Collection<? extends Node> nodes){
		 return cyclePathFliter(super.cycleTask(nodes));
	}

	//if cycle contains before relation
	private Collection<Path> cyclePathFliter(Collection<Path> paths){
		List<Path> delList = new ArrayList<Path>();
		CycleAlgorithm cycleAlg = new CycleAlgorithm();

		for(Path cycle : paths){
			boolean hasCycle = false;
			for(List<Integer> cycleIndex : cycleAlg.getCycles(super.createGraph(cycle))){
				 if(cycleIndex.size() > 1){
					  hasCycle = true;
				 }
			}
			if(!hasCycle)
				delList.add(cycle);
		}
		paths.removeAll(delList);
		return paths;
	}

}
