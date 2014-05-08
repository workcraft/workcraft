package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Block;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;

public class TSONAlg {

	private SONModel net;
	private ONPathAlg onPathAlg;

	public TSONAlg(SONModel net) {
		this.net = net;
		this.onPathAlg = onPathAlg;
	}

	public Collection<Condition> getBlockInputs(Block block){
		Collection<Condition> result = new HashSet<Condition>();
		for(Node node: block.getComponents()){
			if(node instanceof Event)
				for(Node pre : net.getPreset(node)){
					if(!block.getConditions().contains(pre))
						result.add((Condition)pre);
				}
		}

		return result;
	}

	public Collection<Condition> getBlockOutputs(Block block){
		Collection<Condition> result = new HashSet<Condition>();
		for(Node node: block.getComponents()){
			if(node instanceof Event)
				for(Node post : net.getPostset(node)){
					if(!block.getConditions().contains(post))
						result.add((Condition)post);
				}
		}

		return result;
	}

	public boolean isCausallyPrecede (Node input, Collection<Condition> outputs, Block block){
		if(!block.getComponents().contains(input) && !net.getPreset(input).isEmpty() && !net.getPostset(input).isEmpty())
			if(block.getComponents().containsAll(net.getPreset(input)) && block.getComponents().containsAll(net.getPostset(input))){
				System.out.println("break 1" + net.getName(input));
				return false;
			}

		for(Node post : net.getPostset(input)){
			if(!block.getComponents().contains(post) && !outputs.contains(post)){
				System.out.println("continue  "+ net.getName(post));
				continue;
			}
			else if(outputs.contains(post)){
				System.out.println("remove  "+ net.getName(post));
				System.out.println("output size"+ outputs.size());
				outputs.remove(post);
				System.out.println("after remove output size"+ outputs.size());
			}
			else{
				System.out.println("else+ post" + net.getName(post));
				isCausallyPrecede(post, outputs, block);
				}
		}

		if(outputs.isEmpty())
			return true;
		else
			return false;

	}

}
