package org.workcraft.plugins.son.algorithm;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Block;
import org.workcraft.plugins.son.SONModel;

public class TSONAlg extends RelationAlgorithm{

	private SONModel net;

	public TSONAlg(SONModel net) {
		super(net);
		this.net = net;
	}

	//return block inputs
	public Collection<Node> getBlockInputs(Block block){
		Collection<Node> result = new HashSet<Node>();
		Collection<Node> components = block.getComponents();

		for(Node node:components){
			for(Node pre : net.getPreset(node)){
				if(!components.contains(pre))
					result.add(pre);
			}
		}

		return result;
	}

	//return block inputs w.r.t. petri net
	public Collection<Node> getBlockPNInputs(Block block){
		Collection<Node> result = new HashSet<Node>();
		Collection<Node> components = block.getComponents();

		for(Node node:components){
			for(Node pre : net.getPreset(node)){
				if(!components.contains(pre) && net.getSONConnectionType(node, pre) == "POLYLINE")
					result.add(pre);
			}
		}

		return result;
	}

	//return block inputs w.r.t. communication son
	public Collection<Node> getBlockASynInputs(Block block){
		Collection<Node> result = new HashSet<Node>();
		Collection<Node> components = block.getComponents();

		for(Node node:components){
			for(Node pre : net.getPreset(node)){
				if(!components.contains(pre) && net.getSONConnectionType(node, pre) == "POLYLINE")
					result.add(pre);
			}
		}

		return result;
	}

	//return block outputs
	public Collection<Node> getBlockOutputs(Block block){
		Collection<Node> result = new HashSet<Node>();
		Collection<Node> components = block.getComponents();

		for(Node node: components){
			for(Node post : net.getPostset(node)){
				if(!components.contains(post))
					result.add(post);
			}
		}
		return result;
	}

	public boolean isCausallyPrecede (Node input, Collection<Node> outputs){
		for(Node post : net.getPostset(input)){
			if(outputs.contains(post))
				outputs.remove(post);
			else
				isCausallyPrecede(post, outputs);
		}
		if(outputs.isEmpty())
			return true;
		else
			return false;
	}

}
