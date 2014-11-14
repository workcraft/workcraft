package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
	 * get all related behavoural connections in a given set of groups.
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
	 * check if a given group is line like. i.e., post/pre set of every node < 1.
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
	 * get phase of a given (high-level) condition
	 */
	public Phase getPhase(Condition c){
		Phase result = new Phase();
		Collection<Condition> connectedNodes = new ArrayList<Condition>();
		Collection<Path> paths = new ArrayList<Path>();
		ONCycleAlg alg = new ONCycleAlg(net);

		for(SONConnection con : net.getSONConnections()){
			if(con.getSecond()==c && con.getSemantics()==Semantics.BHVLINE)
				connectedNodes.add((Condition)con.getFirst());
		}
		if (connectedNodes.isEmpty())
			return result;
		else{
			for(Node start : connectedNodes)
				for(Node end : connectedNodes)
					paths.addAll(PathAlgorithm.getPaths(start, end, alg.createAdj(getBhvGroup(connectedNodes).getComponents())));

			for(Path path : paths){
				for(Node n : path)
					if(n instanceof Condition)
						result.add((Condition)n);
			}
		}
		return result;
	}

	/**
	 * get the phases of every condition of an abstract group
	 */
	public Map<Condition, Phase> getPhases(ONGroup abstractGroup){
		Map<Condition, Phase> result = new HashMap<Condition, Phase>();
		for(Condition c : abstractGroup.getConditions())
			result.put(c, getPhase(c));

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
	 * get abstract conditions for a node
	 */
	public Collection<Condition> getAbstractConditions(Node node){
		Collection<Condition> result = new HashSet<Condition>();
		Collection<Condition> min = Min(node);
		Collection<Condition> max = Max(node);
		for(Condition c : max){
			if(min.contains(c))
				result.add(c);
		}
		return result;
	}

	/**
	 * get behavioral group of a set of conditions (phase inputs or outputs)
	 */
	public ONGroup getBhvGroup(Collection<Condition> conditions){
		for(ONGroup group : net.getGroups())
			if(group.getConditions().containsAll(conditions))
				return group;
		return null;
	}

	/**
	 * get corresponding behavioral groups for a given abstract condition
	 */
	public Collection<ONGroup> getBhvGroups(Condition c){
		Collection<ONGroup> result = new HashSet<ONGroup>();
		for(SONConnection con : net.getInputSONConnections(c))
			if(con.getSemantics() == Semantics.BHVLINE)
				for(ONGroup group : net.getGroups())
					if(group.getConditions().contains(con.getFirst()))
						result.add(group);
		return result;
	}

	/**
	 * get corresponding abstract Groups for a given final behavioral condition
	 */
	public Collection<ONGroup> getAbstractGroups(Condition fin){
		Collection<ONGroup> result = new HashSet<ONGroup>();
		for(SONConnection con : net.getOutputSONConnections(fin))
			if(con.getSemantics() == Semantics.BHVLINE)
				for(ONGroup group : net.getGroups())
					if(group.getConditions().contains(con.getSecond()))
						result.add(group);
		return result;
	}


	/**
	 * Flit behavioral groups in a group set .
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
	 * Flit abstract groups in a group set.
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

	//if a transitionNode is in abstract group
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

	public Collection<Condition[]> before(TransitionNode e){
		Collection<Condition[]> result = new ArrayList<Condition[]>();
		Condition[] pre = new Condition[1];
		Condition[] post = new Condition[1];

		if(this.getPostPNSet(e).size()!=1 || getPrePNSet(e).size()!=1){
			//System.out.println("size > 1");
			return result;
		}
		for(Node node : this.getPrePNSet(e))
			pre[0] = (Condition)node;
		for(Node node : this.getPostPNSet(e))
			post[0] = (Condition)node;

		Phase phaseI = getPhase(pre[0]);
		Phase phaseI2 = getPhase(post[0]);
		if(phaseI.isEmpty() && phaseI2.isEmpty()){
			//System.out.println(net.getNodeLabel(e)+"  is empty");
			return result;
		}

		Collection<Condition> maxI = getMaximalPhase(phaseI);
		Collection<Condition> minI2 = getMinimalPhase(phaseI2);
		Collection<Condition> PRE = getPREset(e);
		Collection<Condition> POST = getPOSTset(e);
		if(!maxI.containsAll(minI2)){
			for(Condition c0 : maxI){
				for(Condition c1 : minI2){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}

			for(Condition c0 : PRE){
				for(Condition c1 : minI2){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}

			for(Condition c0 : maxI){
				for(Condition c1 : POST){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}
		}
		if(maxI.containsAll(minI2)){
			Collection<Node> preMaxI = new HashSet<Node>();
			Collection<Node> postMinI2 = new HashSet<Node>();
			for(Node n: maxI)
				preMaxI.addAll(getPrePNSet(n));
			for(Node n: minI2)
				postMinI2.addAll(getPostPNSet(n));

			Collection<Condition> PREpreMaxI = new HashSet<Condition>();
			Collection<Condition> POSTpostMinI2 = new HashSet<Condition>();

			for(Node n : preMaxI){
				if(n instanceof TransitionNode)
				PREpreMaxI.addAll(getPREset((TransitionNode)n));}
			for(Node n : postMinI2){
				if(n instanceof TransitionNode)
				POSTpostMinI2.addAll(getPOSTset((TransitionNode)n));}

			for(Condition c0 : PREpreMaxI){
				for(Condition c1 : POSTpostMinI2){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}

			for(Condition c0 : PRE){
				for(Condition c1 : POSTpostMinI2){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}

			for(Condition c0 : PREpreMaxI){
				for(Condition c1 : POST){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}

		}
		return result;
	}

}
