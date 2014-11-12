package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;

public class RelationAlgorithm{

	private SON net;

	public RelationAlgorithm(SON net) {
		this.net = net;
	}

	/**
	 * check if a given condition has more than one input events
	 */
	public boolean hasPostConflictEvents(Node c){
		if (c instanceof Condition){
			if(net.getPostset(c).size() > 1){
				int count = 0;
				for (SONConnection con : net.getOutputSONConnections(c)){
					if (con.getSemantics() == Semantics.PNLINE)
						count++;
				}
				if(count > 1){
					int n = 0;
						for (Node post : net.getPostset(c))
							if (post instanceof TransitionNode)
								n++;
							if (n > 1)
								return true;
				}
			}
		}
		return false;
	}

	/**
	 * check if a given condition has more than one output events
	 */
	public boolean hasPreConflictEvents(Node c){
		if (c instanceof Condition){
			if(net.getPreset(c).size() > 1){
				int count = 0;
				for (SONConnection con : net.getInputSONConnections(c)){
					if (con.getSemantics() == Semantics.PNLINE)
						count++;
				}
				if(count > 1){
					int n = 0;
					for (Node pre : net.getPreset(c))
						if (pre instanceof TransitionNode)
								n++;
						if (n > 1)
							return true;
					}
				}
		}
	return false;
	}

	/**
	 * check if a given node is initial state i.e., empty preset
	 */
	public boolean isInitial(Node n){
		boolean conType = true;

		if(net.getPreset(n).size() == 0)
			return true;
		else{
			if (n instanceof Condition){
				for(SONConnection con : net.getInputSONConnections(n)){
					if (con.getSemantics() == Semantics.PNLINE)
						conType = false;
				}
				if(conType)
					return true;
			}
		}

		return false;
	}

	/**
	 * check if a given node is final state i.e., empty postset
	 */
	public boolean isFinal(Node n){
		boolean conType = true;

		if(net.getPostset(n).size() == 0)
			return true;
		else{
			if (n instanceof Condition){
				for(SONConnection con : net.getOutputSONConnections(n)){
					if (con.getSemantics() == Semantics.PNLINE)
						conType = false;
				}
				if(conType)
					return true;
			}
		}

		return false;
	}

	/**
	 * check if a given set of nodes contains initial states.
	 */
	public boolean hasInitial(Collection<Node> nodes){
		boolean result = false;

		for(Node node : nodes)
			if (isInitial(node))
				result = true;
		return result;
	}

	/**
	 * check if a given set of nodes contains final states.
	 */
	public boolean hasFinal(Collection<Node> nodes){
		boolean result = false;

		for(Node node : nodes)
			if (isFinal(node))
				result = true;
		return result;
	}

	/**
	 * get all initial states of a given node set
	 */
	public Collection<Node> getInitial(Collection<Node> nodes){
		ArrayList<Node> result =  new ArrayList<Node>();
		for (Node node : nodes)
			if (isInitial(node))
				result.add(node);
		return result;
	}

	/**
	 * get all final states of a given node set
	 */
	public Collection<Node> getFinal(Collection<Node> nodes){
		ArrayList<Node> result =  new ArrayList<Node>();
		for (Node node : nodes)
			if (isFinal(node))
				result.add(node);
		return result;
	}

	/**
	 * get all connected channel places for a set of groups
	 */
	public Collection<ChannelPlace> getRelatedChannelPlace(Collection<ONGroup> groups){
		HashSet<ChannelPlace> result = new HashSet<ChannelPlace>();

		for(ChannelPlace cPlace : net.getChannelPlaces())
			for (ONGroup group : groups){
				for (Node node : net.getPostset(cPlace)){
					if (group.contains(node))
						result.add(cPlace);
				}
				for (Node node : net.getPreset(cPlace)){
					if (group.contains(node))
						result.add(cPlace);
				}
			}

		return result;
	}

	/**
	 * get all PN-based(petri net) pre-conditions of a given condition
	 */
	public Collection<Condition> getPrePNCondition(Condition c){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Node pre : net.getPreset(c))
			if(pre instanceof TransitionNode)
				if(net.getSONConnectionType(c, pre) == Semantics.PNLINE)
					for(Node n2 : net.getPreset(pre))
						if((n2 instanceof Condition) && net.getSONConnectionType(pre, n2)== Semantics.PNLINE)
							result.add((Condition)n2);

		return result;
	}

	/**
	 * get all PN-based post-conditions of a given condition
	 */
	public Collection<Condition> getPostPNCondition(Condition c){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Node post : net.getPostset(c))
			if(post instanceof TransitionNode)
				if(net.getSONConnectionType(c, post)== Semantics.PNLINE)
					for(Node n2 : net.getPostset(post))
						if((n2 instanceof Condition) && net.getSONConnectionType(post, n2) == Semantics.PNLINE)
							result.add((Condition)n2);

		return result;
	}

	/**
	 * get all asynchronous (Communication-SON) pre-events of a given event node
	 */
	public Collection<TransitionNode> getPreAsynEvents (TransitionNode e){
		Collection<TransitionNode> result = new ArrayList<TransitionNode>();
		for(Node node : net.getPreset(e)){
			if((node instanceof ChannelPlace) && net.getSONConnectionType(node, e) == Semantics.ASYNLINE){
				TransitionNode node2 = (TransitionNode)net.getPreset(node).iterator().next();
				result.add(node2);
			}
		}
		return result;
	}

	/**
	 * get all asynchronous (Communication-SON) post-events of a given event node
	 */
	public Collection<TransitionNode> getPostAsynEvents (TransitionNode e){
		Collection<TransitionNode> result = new ArrayList<TransitionNode>();
		for(Node node : net.getPostset(e) )
			if((node instanceof ChannelPlace) && net.getSONConnectionType(node, e) == Semantics.ASYNLINE){
				TransitionNode node2 = (TransitionNode)net.getPostset(node).iterator().next();
				result.add(node2);
				}
		return result;
	}

	/**
	 * get all asynchronous and synchronous (Communication-SON) pre-event of a given event or collapsed block
	 */
	public Collection<TransitionNode> getPreASynEvents(TransitionNode node){
		Collection<TransitionNode> result = new ArrayList<TransitionNode>();

		for(Node pre : net.getPreset(node)){
			if(pre instanceof ChannelPlace){
				TransitionNode pre2 = (TransitionNode)net.getPreset(pre).iterator().next();
				result.add(pre2);
			}
		}
		for(Node post : net.getPostset(node)){
			if((post instanceof ChannelPlace) && net.getSONConnectionType(post, node) == Semantics.SYNCLINE){
				TransitionNode post2 = (TransitionNode)net.getPostset(post).iterator().next();
				result.add(post2);
			}
		}

		return result;
	}

	/**
	 * get all asynchronous and synchronous(Communication-SON) post-event of a given event or block
	 */
	public Collection<TransitionNode> getPostASynEvents(TransitionNode node){
		Collection<TransitionNode> result = new ArrayList<TransitionNode>();

		for(Node post : net.getPostset(node)){
			if(post instanceof ChannelPlace){
				TransitionNode post2 = (TransitionNode)net.getPostset(post).iterator().next();
				result.add(post2);
			}
		}
		for(Node pre : net.getPreset(node)){
			if(pre instanceof ChannelPlace && net.getSONConnectionType(pre, node) == Semantics.SYNCLINE){
				TransitionNode pre2 = (TransitionNode)net.getPreset(pre).iterator().next();
				result.add(pre2);
			}
		}

		return result;
	}

	/**
	 * get all PRE-conditions (PN and CSON-based) of a given event or block.
	 */
	public Collection<Condition> getPREset(TransitionNode e){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Node n : net.getPreset(e)){
			if(n instanceof Condition)
				result.add((Condition)n);
			if(n instanceof ChannelPlace){
				Node pre = (Node)net.getPreset(n).iterator().next();
				for(Node preCondition : net.getPreset(pre))
					if(preCondition instanceof Condition)
						result.add((Condition)preCondition);
			}
		}

		for(Node n : net.getPostset(e)){
			if(n instanceof ChannelPlace && net.getSONConnectionType(e, n) == Semantics.SYNCLINE){
				Node post = (Node)net.getPostset(n).iterator().next();
				for(Node preCondition : net.getPreset(post))
					if(preCondition instanceof Condition)
						result.add((Condition)preCondition);
			}
		}

		return result;
	}

	/**
	 * get all POST-conditions (PN and CSON-based) of a given event or block.
	 */
	public Collection<Condition> getPOSTset(TransitionNode e){
		Collection<Condition> result = new ArrayList<Condition>();

		for(Node n : net.getPostset(e)){
			if(n instanceof Condition)
				result.add((Condition)n);
			if(n instanceof ChannelPlace){
				Node post = (Node)net.getPostset(n).iterator().next();
				for(Node postCondition : net.getPostset(post))
					if(postCondition instanceof Condition)
						result.add((Condition)postCondition);
			}
		}

		for(Node n : net.getPreset(e)){
			if((n instanceof ChannelPlace) && net.getSONConnectionType(e, n) == Semantics.SYNCLINE){
				Node pre = (Node)net.getPreset(n).iterator().next();
				for(Node postCondition : net.getPostset(pre))
					if(postCondition instanceof Condition)
						result.add((Condition)postCondition);
			}
		}

		return result;
	}

	/**
	 * get all PN-based preset of a given node.
	 */
	public Collection<Node> getPrePNSet(Node node){
		Collection<Node> result = new ArrayList<Node>();
		for(Node n : net.getPreset(node)){
			if(net.getSONConnectionType(node, n) == Semantics.PNLINE)
				result.add(n);
		}
		return result;
	}

	/**
	 * get all PN-based postset of a given node.
	 */
	public Collection<Node> getPostPNSet(Node node){
		Collection<Node> result = new ArrayList<Node>();
		for(Node n : net.getPostset(node)){
			if(net.getSONConnectionType(node, n) == Semantics.PNLINE)
				result.add(n);
		}
		return result;
	}

	/**
	 * get all Bhv-based postset of a given condition.
	 */
	public Collection<Condition> getPostBhvSet(Condition c){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Node n : net.getPostset(c)){
			if(net.getSONConnectionType(c, n) == Semantics.BHVLINE)
				result.add((Condition)n);
		}
		return result;
	}

	/**
	 * get all Bhv-based preset of a given condition.
	 */
	public Collection<Condition> getPreBhvSet(Condition c){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Node n : net.getPreset(c)){
			if(net.getSONConnectionType(c, n) == Semantics.BHVLINE)
				result.add((Condition)n);
		}
		return result;
	}
}
