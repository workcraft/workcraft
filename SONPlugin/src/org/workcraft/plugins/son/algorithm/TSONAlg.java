package org.workcraft.plugins.son.algorithm;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Block;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.elements.Event;

public class TSONAlg extends RelationAlgorithm{

	private SONModel net;

	public TSONAlg(SONModel net) {
		super(net);
		this.net = net;
	}

	/**
	 * get all inputs of a given block without concerning connection types
	 */
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

	/**
	 * get all outputs of a given block without concerning connection types
	 */
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

	/**
	 * get petri net-based inputs of a given block
	 */
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

	/**
	 * get petri net-based outputs of a given block
	 */
	public Collection<Node> getBlockPNOutputs(Block block){
		Collection<Node> result = new HashSet<Node>();
		Collection<Node> components = block.getComponents();

		for(Node node:components){
			for(Node post : net.getPostset(node)){
				if(!components.contains(post) && net.getSONConnectionType(node, post) == "POLYLINE")
					result.add(post);
			}
		}

		return result;
	}

	/**
	 * get cson-based inputs of a given block
	 */
	public Collection<Event> getBlockASynInputs(Block block){
		Collection<Event> result = new HashSet<Event>();
		Collection<Node> components = block.getComponents();

		for(Node node:components){
			if(node instanceof Event)
				for(Event e : this.getPreASynEvents((Event)node)){
					if(!components.contains(e))
						result.add(e);
				}
		}
		return result;
	}

	/**
	 * get cson-based outputs of a given block
	 */
	public Collection<Event> getBlockASynOutputs(Block block){
		Collection<Event> result = new HashSet<Event>();
		Collection<Node> components = block.getComponents();

		for(Node node:components){
			if(node instanceof Event)
				for(Event e : this.getPostASynEvents((Event)node)){
					if(!components.contains(e))
						result.add(e);
				}
		}
		return result;
	}

	public boolean isInCollapsedBlock(Node node){
		for(Block block : net.getBlocks())
			if(block.getComponents().contains(node) && block.getIsCollapsed())
				return true;
		return false;
	}

	/**
	 * check if a given input causally precede all outputs
	 */
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
