package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import org.workcraft.dom.Node;
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
	 * get phases collection for a given abstract condition
	 */
	public Collection<Phase> getPhases(Condition c){
		Collection<Phase> result = new ArrayList<Phase>();

		for(ONGroup group : getBhvGroups(net.getGroups())){
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

			Phase phase = new Phase();
			for(Node node : dfs(nodes, nodes)){
				if(node instanceof Condition)
					phase.add((Condition)node);
			}
			result.add(phase);
		}
		return result;
	}

	private Collection<Node> dfs (Collection<Node> s, Collection<Node> v){
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

	/**
	 * get the phase collection for all abstract conditions.
	 */
	public Map<Condition, Collection<Phase>> getAllPhases(){
		Map<Condition, Collection<Phase>> result = new  HashMap<Condition, Collection<Phase>>();
		Collection<ONGroup> abstractGroups =getAbstractGroups(net.getGroups());

		for(ONGroup group : abstractGroups){
			for(Condition c : group.getConditions())
				result.put(c, getPhases(c));
		}
		return result;
	}

	private Collection<Condition> Max(Node node){
		Collection<Condition> result = new HashSet<Condition>();

		if(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE)){
			result.addAll(getPostBhvSet((Condition)node));
		}
		else{

			Collection<Node> postPN = getPostPNSet(node);

			if(!postPN.isEmpty()){
				for(Node post : postPN)
					result.addAll(Max(post));
			}

		}
		return result;
	}

	private Collection<Condition> Min(Node node){
		Collection<Condition> result = new HashSet<Condition>();

		if(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE)){
			result.addAll(getPostBhvSet((Condition)node));
		}
		else{
			Collection<Node> prePN = getPrePNSet(node);

			if(!prePN.isEmpty()){
				for(Node pre : prePN)
					result.addAll(Min(pre));
			}
		}
		return result;
	}

	/**
	 * get the set of corresponding abstract conditions for a given node
	 */
	public Collection<Condition> getAbstractConditions(Node node){
		Collection<Condition> result = new HashSet<Condition>();

		if(isAbstractCondition(node)){
			result.add((Condition)node);
			return result;
		}

		Collection<Condition> min = Min(node);
		Collection<Condition> max = Max(node);
		for(Condition c : max){
			if(min.contains(c))
				result.add(c);
		}
		return result;
	}

	/**
	 * return true if the given node is in abstract group.
	 */
	public boolean isAbstractCondition(Node node){
		if((node instanceof Condition)
				&& !(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE))
				&&(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE)))
			return true;

		return false;
	}

	/**
	 * get behavioral group for a set of phase bounds
	 */
	public ONGroup getBhvGroup(Collection<Condition> phase_bound){
		Collection<ONGroup> groups = new HashSet<ONGroup>();
		for(ONGroup group : net.getGroups())
			if(!getCommonElements(group.getComponents(), phase_bound).isEmpty())
				groups.add(group);

		return groups.iterator().next();
	}

	/**
	 * get corresponding behavioral groups for a given abstract condition;
	 * each abstract condition must map to a phase.
	 *
	 */
	public Collection<ONGroup> getBhvGroups(Condition abs_condition){
		Collection<ONGroup> result = new HashSet<ONGroup>();

		for(SONConnection con : net.getInputSONConnections(abs_condition)){
			if(con.getSemantics() == Semantics.BHVLINE)
				for(ONGroup group : net.getGroups())
					if(group.getConditions().contains(con.getFirst()))
						result.add(group);
		}
		return result;
	}

	/**
	 * get corresponding abstract groups for a given behavioral condition
	 */
	public Collection<ONGroup> getAbstractGroups(Condition bhv_condition){
		Collection<ONGroup> result = new HashSet<ONGroup>();
		Collection<Condition> absConditions = getAbstractConditions(bhv_condition);

		for(ONGroup group : net.getGroups()){
			for(Condition c : absConditions)
				if(group.getComponents().contains(c))
					result.add(group);
		}
		return result;
	}


	/**
	 * get behavioural groups for a given group set.
	 */
	public Collection<ONGroup> getBhvGroups(Collection<ONGroup> groups){
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
	 * get abstract groups for a given group set.
	 */
	public Collection<ONGroup> getAbstractGroups(Collection<ONGroup> groups){
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
	 * get minimal phase of a given phase
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
	 * get maximal phase of a given phase
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
	 * return true if a transitionNode is in abstract group
	 */
	public boolean isAbstractEvent(TransitionNode n){
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
	 * get before(e) relation for a given abstract transition node
	 */
	public Collection<TransitionNode[]> before(TransitionNode e){
		Collection<TransitionNode[]> result = new ArrayList<TransitionNode[]>();

		Collection<Condition> PRE = getPREset(e);
		Collection<Condition> POST = getPOSTset(e);

		//get Pre(e)
		for(Condition c : PRE){
			//get phase collection for each Pre(e)
			Collection<Phase> prePhases = getPhases(c);
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
			Collection<Phase> postPhases = getPhases(c);
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

}
