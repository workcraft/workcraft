package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Before;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;

public class BSONAlg extends RelationAlgorithm{

	private SON net;

	public BSONAlg(SON net) {
		super(net);
		this.net = net;
	}

	/**
	 * get all related behavoural connections for a given set of groups.
	 */
	public Collection<SONConnection> getRelatedBhvLine(Collection<ONGroup> groups){
		HashSet<SONConnection> result = new HashSet<SONConnection>();

		for(SONConnection con : net.getSONConnections()){
			if (con.getSemantics() == Semantics.BHVLINE)
				for(ONGroup group : groups){
					if(group.contains(con.getFirst())){
						for (ONGroup nextGroup : groups){
							if(nextGroup.contains(con.getSecond()))
								result.add(con);
					}
				}
			}
		}
		return result;
	}

	/**
	 * check if a given group is line like. i.e., post/pre set of each node < 1.
	 */
	public boolean isLineLikeGroup(ONGroup group){
		for(Node node : group.getComponents()){
			if(net.getPostset(node).size() > 1 && group.containsAll(net.getPostset(node)))
				return false;
			if(net.getPreset(node).size() > 1 && group.containsAll(net.getPreset(node)))
				return false;
		}
		return true;
	}

	/**
	 * get phases collection for a given upper-level condition
	 */
	public Collection<Phase> getPhases(Condition c){
		Collection<Phase> result = new ArrayList<Phase>();

		for(ONGroup group : getLowerGroups(net.getGroups())){
			//find all nodes pointing to c
			Collection<Node> nodes = new ArrayList<Node>();
			for(Node n : group.getConditions()){
				SONConnection con = null;
				if(n != c){
					con = net.getSONConnection(n, c);
				}
				if(con != null && con.getSemantics()==Semantics.BHVLINE)
					nodes.add(n);
			}
			if(!nodes.isEmpty()){
				Phase phase = new Phase();
				for(Node node : PathAlgorithm.dfs2(nodes, nodes, net)){
					if(node instanceof Condition)
						phase.add((Condition)node);
				}
				result.add(phase);
			}
		}
		return result;
	}

	/**
	 * get the phase collection for all upper-level conditions.
	 */
	public Map<Condition, Collection<Phase>> getAllPhases(){
		Map<Condition, Collection<Phase>> result = new HashMap<Condition, Collection<Phase>>();
		Collection<ONGroup> upperGroups =getUpperGroups(net.getGroups());

		for(ONGroup group : upperGroups){
			for(Condition c : group.getConditions())
				result.put(c, getPhases(c));
		}
		return result;
	}


	private Collection<Condition> forwardSearch(Node node){
		Collection<Condition> result = new HashSet<Condition>();
		Stack<Node> stack = new Stack<Node>();
		Collection<Node> visit = new HashSet<Node>();

		stack.push(node);

		while(!stack.isEmpty()){
			node = stack.pop();
			visit.add(node);

			if(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE)){
				result.addAll(getPostBhvSet((Condition)node));
			}

			Collection<Node> postSet = getPostPNSet(node);
			if(!postSet.isEmpty()){
				for(Node post : postSet){
					if(!visit.contains(post)){
						stack.push(post);
					}
				}
			}
		}
		return result;
	}

	private Collection<Condition> backWardSearch(Node node){
		Collection<Condition> result = new HashSet<Condition>();
		Stack<Node> stack = new Stack<Node>();
		Collection<Node> visit = new HashSet<Node>();

		stack.push(node);

		while(!stack.isEmpty()){
			node = stack.pop();
			visit.add(node);

			if(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE)){
				result.addAll(getPostBhvSet((Condition)node));
			}

			Collection<Node> preSet = getPrePNSet(node);
			if(!preSet.isEmpty()){
				for(Node pre : preSet){
					if(!visit.contains(pre)){
						stack.push(pre);
					}
				}
			}
		}
		return result;
	}

	/**
	 * get the set of corresponding upper-level conditions for a given node
	 */
	public Collection<Condition> getUpperConditions(Node node){
		Collection<Condition> result = new HashSet<Condition>();

		if(isUpperNode(node)){
			if(node instanceof Condition)
				result.add((Condition)node);
			else
				return result;
		}

		Collection<Condition> min = backWardSearch(node);
		Collection<Condition> max = forwardSearch(node);
		for(Condition c : max){
			if(min.contains(c))
				result.add(c);
		}
		return result;
	}

	/**
	 * return true if the given node is an upper-level condition.
	 */
	public boolean isUpperCondition(Node node){
		if((node instanceof Condition)
				&& !(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE))
				&&(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE)))
			return true;

		return false;
	}

	/**
	 * return true if the given node is in upper-level group.
	 */
	public boolean isUpperNode(Node node){
		if(isUpperCondition(node))
			return true;
		for(Node pre : getPrePNSet(node))
			if(isUpperCondition(pre))
				return true;

		return false;
	}

	/**
	 * get lower-level group for a set of phase bounds
	 */
	public ONGroup getLowerGroup(Collection<Condition> phaseBound){
		Collection<ONGroup> groups = new HashSet<ONGroup>();
		for(ONGroup group : net.getGroups())
			if(!getCommonElements(group.getComponents(), phaseBound).isEmpty())
				groups.add(group);

		return groups.iterator().next();
	}

	/**
	 * get all lower-level groups for a given upper-level group
	 */
	public Collection<ONGroup> getLowerGroups(ONGroup upperGroup){
		Collection<ONGroup> result = new HashSet<ONGroup>();

		for(Condition c : upperGroup.getConditions()){
			result.addAll(getLowerGroups(c));
		}

		return result;
	}

	/**
	 * get all lower-level groups for a given upper-level condition;
	 *
	 */
	public Collection<ONGroup> getLowerGroups(Condition upperCondition){
		Collection<ONGroup> result = new HashSet<ONGroup>();

		for(Node pre : getPreBhvSet(upperCondition)){
			result.add(net.getGroup(pre));
		}

		return result;
	}


	/**
	 * get lower-level groups for a given group set.
	 */
	public Collection<ONGroup> getLowerGroups(Collection<ONGroup> groups){
		Collection<ONGroup> result = new HashSet<ONGroup>();
		for(ONGroup group : groups){
			boolean isInput = false;
			boolean isOutput = false;
			for(Node node : group.getComponents()){
				if(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE))
					isInput = true;
				if(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE))
					isOutput = true;
			}
			if(!isInput && isOutput)
				result.add(group);

		}
		return result;
	}


	/**
	 * get upper-level groups for a given group set.
	 */
	public Collection<ONGroup> getUpperGroups(Collection<ONGroup> groups){
		Collection<ONGroup> result = new HashSet<ONGroup>();
		for(ONGroup group : groups){
			boolean isInput = false;
			boolean isOutput = false;
			if(this.isLineLikeGroup(group)){
				for(Node node : group.getComponents()){
					if(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE))
						isInput = true;
					if(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE))
						isOutput = true;
				}
				if(isInput && !isOutput)
					result.add(group);
			}
		}
		return result;
	}

	/**
	 * get minimal phase for a given phase
	 */
	public ArrayList<Condition> getMinimalPhase(Phase phase){
		ArrayList<Condition> result = new ArrayList<Condition>();
		for(Condition c : phase){
			boolean isMinimal = true;
			for(Condition pre : this.getPrePNCondition(c))
				if(phase.contains(pre))
					isMinimal = false;
			if(isMinimal)
				result.add(c);
		}
		return result;
	}

	/**
	 * get minimal phase collection for a set of phase
	 */
	public ArrayList<Condition> getMinimalPhase(Collection<Phase> phases){
		ArrayList<Condition> result = new ArrayList<Condition>();
		for(Phase phase : phases){
			result.addAll(getMinimalPhase(phase));
		}
		return result;
	}

	/**
	 * get maximal phase for a given phase
	 */
	public ArrayList<Condition> getMaximalPhase(Phase phase){
		ArrayList<Condition> result = new ArrayList<Condition>();
		for(Condition c : phase){
			boolean isMaximal = true;
			for(Condition pre : this.getPostPNCondition(c))
				if(phase.contains(pre))
					isMaximal = false;
			if(isMaximal)
				result.add(c);
		}
		return result;
	}

	/**
	 * get maximal phase collection for a set of phase
	 */
	public ArrayList<Condition> getMaximalPhase(Collection<Phase> phases){
		ArrayList<Condition> result = new ArrayList<Condition>();
		for(Phase phase : phases){
			result.addAll(getMaximalPhase(phase));
		}
		return result;
	}

	/**
	 * return true if a transitionNode is in upper-level group
	 */
	public boolean isUpperEvent(TransitionNode n){
		if(getPrePNSet(n).size() == 1){
			Condition c = (Condition)getPrePNSet(n).iterator().next();
			if(net.getInputSONConnectionTypes(c).contains(Semantics.BHVLINE)
					&& !net.getOutputSONConnectionTypes(c).contains(Semantics.BHVLINE)){
				return true;
			}else
				return false;
		}else
			return false;
	}

	/**
	 * get before(e) relation for a given upper-level transition node
	 */
	public Before before(TransitionNode e, Map<Condition, Collection<Phase>> phases){
		Before result = new Before();

		Collection<Condition> PRE = getPREset(e);
		Collection<Condition> POST = getPOSTset(e);

		//get Pre(e)
		for(Condition c : PRE){
			//get phase collection for each Pre(e)
			Collection<Phase> prePhases = null;

			if(phases.containsKey(c)){
				prePhases = phases.get(c);
			}else{
				return result;
			}

			//get maximal phase
			for(Phase phase : prePhases){
				Collection<Condition> max = getMaximalPhase(phase);
				for(Condition c1 : max){
					//get pre(c1)
					Collection<Node> pre = getPrePNSet(c1);
					for(Node e1 : pre){
						if(e1 instanceof TransitionNode){
							TransitionNode[] subResult = new TransitionNode[2];
							subResult[0] = (TransitionNode)e1;
							subResult[1] = (TransitionNode)e;
							result.add(subResult);
						}
					}
				}
			}
		}

		//get Post(e)
		for(Condition c : POST){
			//get phase collection for each Pre(e)
			Collection<Phase> postPhases =  null;
			if(phases.containsKey(c)){
				postPhases = phases.get(c);
			}else{
				return result;
			}

			//get minimal phase
			for(Phase phase : postPhases){
				Collection<Condition> min = getMinimalPhase(phase);
				for(Condition c1 : min){
					//get pre(c1)
					Collection<Node> post = getPostPNSet(c1);
					for(Node e1 : post){
						if(e1 instanceof TransitionNode){
							TransitionNode[] subResult = new TransitionNode[2];
							subResult[0] = (TransitionNode)e;
							subResult[1] = (TransitionNode)e1;
							result.add(subResult);
						}
					}
				}
			}
		}
		return result;
	}

	public Map<TransitionNode, Before> getAllBefore(){
		Map<TransitionNode, Before> result = new HashMap<TransitionNode, Before>();

		Collection<ONGroup> upperGroups = getUpperGroups(net.getGroups());

		for(ONGroup group : upperGroups)
			for(TransitionNode e : group.getTransitionNodes()){
				result.put(e, before(e, getAllPhases()));
		}

		return result;
	}

}
