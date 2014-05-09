package org.workcraft.plugins.son.algorithm;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Block;
import org.workcraft.plugins.son.SONModel;

public class TSONAlg {

	private SONModel net;

	public TSONAlg(SONModel net) {
		this.net = net;
	}

	//return unchecked block inputs
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

	//return unchecked block outputs
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
