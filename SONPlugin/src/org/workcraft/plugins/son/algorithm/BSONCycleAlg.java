package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;

public class BSONCycleAlg extends ONCycleAlg{

	private SON net;
	protected BSONAlg bsonAlg;
	private Map<Condition, Collection<Phase>> phases;

	public BSONCycleAlg(SON net, Map<Condition, Collection<Phase>> phases){
		super(net);
		this.net = net;
		this.phases = phases;
		bsonAlg =new BSONAlg(net);
	}

	/**
	 * create Integer Graph for a nodes set
	 * Synchronous communication would be treated as an undirected line.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected List<Integer>[] createGraph(List<Node> nodes){
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

		//get upper-level transition nodes.
		Collection<ONGroup> upperGroups = bsonAlg.getUpperGroups(net.getGroups());
		Collection<TransitionNode> upperT = new ArrayList<TransitionNode>();
		for(ONGroup group : upperGroups)
			upperT.addAll(group.getTransitionNodes());

		for(int i = 0; i < nodes.size(); i++){
			//add before relation
            Node n = nodes.get(i);
            if(upperT.contains(n)){
                for(TransitionNode[] before : bsonAlg.before((TransitionNode)n, phases)){
                	TransitionNode c0 = before[0];
                	TransitionNode c1 = before[1];
                    int index = nodeIndex.get(c0);
                    if(result[index] == null){
                            result[index] = new ArrayList<Integer>();
                    }
                    result[index].add(nodeIndex.get(c1));
                }
            }
        }
		return result;
	}

	@Override
	public Collection<Path> cycleTask (Collection<? extends Node> nodes){
		//remove all paths which do not involve before(e) relation.
		 return cycleFliter(super.cycleTask(nodes));
	}

	@Override
	protected Collection<Path> cycleFliter(Collection<Path> paths){
		List<Path> delList = new ArrayList<Path>();

		for(Path cycle : paths){
			int upper = 0;
			int lower = 0;

			for(Node n : cycle){
				if(n instanceof ChannelPlace)
					continue;
				else if(bsonAlg.isUpperNode(n))
					upper++;
				else
					lower++;
			}
			//all cycle nodes are in the same level
			if(upper==0 || lower==0)
				delList.add(cycle);
		}
		paths.removeAll(delList);
		return paths;
	}

}
