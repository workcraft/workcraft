package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.InvalidStructureException;

public class SimulationAlg extends RelationAlgorithm {

	private SON net;
	private BSONAlg bsonAlg;

	private Collection<ONGroup> abstractGroups;
	private Collection<ONGroup> bhvGroups;

	private Collection<Node> checkedNodes = new HashSet<Node>();

	public SimulationAlg(SON net){
		super(net);
		this.net = net;
		bsonAlg = new BSONAlg(net);

		abstractGroups = bsonAlg.getAbstractGroups(net.getGroups());
		bhvGroups = bsonAlg.getBhvGroups(net.getGroups());
	}

	private Collection<Node> getSyncMinimum (TransitionNode e, Collection<Path> sync, Collection<TransitionNode> enabledEvents){
		Collection<Node> result = new HashSet<Node>();
		HashSet<Node> syncEvents = new HashSet<Node>();
		//get related synchronous events
		for(Path cycle : sync){
			if(cycle.contains(e))
				syncEvents.addAll(cycle);
		}
		//e is in synchronous cycle
		if(!syncEvents.isEmpty()){
			for(Node n : syncEvents){
				//add all related synchronous cycle to result
				if(enabledEvents.contains(n) && !result.contains(n)){
					result.add(n);
					//continue to check the event which is the pre-aysn-event of related synchronous cycle
					for(TransitionNode pre : getPreAsynEvents((TransitionNode)n)){
						if(!syncEvents.contains(pre) && enabledEvents.contains(pre)){
							result.addAll(getSyncMinimum((TransitionNode)pre, sync, enabledEvents));
						}
					}
				}
				if(!enabledEvents.contains(n))
					throw new RuntimeException("algorithm error: has unenabled event in sync cycle  "+net.getNodeReference(n));
			}
		}
		//e is not in synchronous cycle	but has pre asyn event
		else if(!getPreAsynEvents(e).isEmpty() && enabledEvents.contains(e)){
			if(!result.contains(e))
				result.add(e);

			for(TransitionNode n : getPreAsynEvents(e))
				if(!result.contains(n) && enabledEvents.contains(n)){
					result.add(n);
					result.addAll(getSyncMinimum((TransitionNode)n, sync, enabledEvents));
				}
		}
		else{
			result.add(e);
		}
		return result;
	}

	/**
	 * return minimal execution set of a given node.
	 * contain other nodes which have synchronous with the clicked node.
	 */
	public List<TransitionNode> getMinFires (TransitionNode e, Collection<Path> sync, Collection<TransitionNode> enabledEvents){
		List<TransitionNode> result = new ArrayList<TransitionNode>();

		for(Node n : getSyncMinimum(e, sync, enabledEvents))
			if(n instanceof TransitionNode)
				result.add((TransitionNode)n);;

		return result;
	}


	private Collection<Node> getMaxFireSet(TransitionNode e, Collection<Path> sync, Collection<TransitionNode> enabledEvents){
		Collection<Node> result = new HashSet<Node>();
		Collection<Node> syncEvents = new HashSet<Node>();

		for(Path cycle : sync){
			if(cycle.contains(e))
				syncEvents.addAll(cycle);
		}

		if(!syncEvents.isEmpty()){
			for(Node n : syncEvents){
				if(enabledEvents.contains(n) && !result.contains(n)){
					result.add(n);

					for(TransitionNode post : this.getPostAsynEvents((TransitionNode)n)){
						if(!syncEvents.contains(post) && enabledEvents.contains(post)){
							result.add(post);
							result.addAll(getMaxFireSet((TransitionNode)post, sync, enabledEvents));
						}
					}

				}
				if(!enabledEvents.contains(n))
					throw new RuntimeException("algorithm error: has unenable event in sync cycle");
			}
		}
		else if(!getPostAsynEvents(e).isEmpty() && enabledEvents.contains(e)){
			if(!result.contains(e))
				result.add(e);

			for(TransitionNode n : getPostAsynEvents(e))
				if(!result.contains(n) && enabledEvents.contains(n)){
					result.add(n);
					result.addAll(getMaxFireSet((TransitionNode)n, sync, enabledEvents));
				}
		}
		else{
			result.add(e);
		}
		return result;
	}

	public List<TransitionNode> getMaxFires (TransitionNode e, Collection<Path> sync, Collection<TransitionNode> enabledEvents){
		List<TransitionNode> result = new ArrayList<TransitionNode>();

		for(Node n : getMaxFireSet(e, sync, enabledEvents))
			if(n instanceof TransitionNode)
				result.add((TransitionNode)n);

		return result;
	}

	private boolean isPNEnabled (TransitionNode e) {
		// gather number of connections for each pre-place
		for (Node n : net.getPreset(e)){
			if(n instanceof Condition)
				if (!((Condition)n).isMarked())
					return false;
			}
		if(net.getPreset(e).isEmpty())
			return false;

		return true;
	}

	private boolean isSyncEnabled(TransitionNode e, Collection<Path> sync, Map<Condition, Phase> phases){
		Collection<Node> relatedSync = new HashSet<Node>();
		//get nodes which have synchronous relation with e
		for(Path cycle : sync){
			if(cycle.contains(e))
				relatedSync.addAll(cycle);
		}

		if(relatedSync.contains(e)){
			checkedNodes.addAll(relatedSync);
			for(Node n : relatedSync)
				if(n instanceof TransitionNode){
					if(!isPNEnabled((TransitionNode)n) || !isBhvEnabled((TransitionNode)n, phases))
							return false;
					for(TransitionNode pre : getPreAsynEvents((TransitionNode)n)){
						if(!relatedSync.contains(pre)){
							if(!isAsynEnabled((TransitionNode)n, sync, phases)
									|| !isBhvEnabled((TransitionNode)n, phases))
								return false;
						}
					}
				}
			}

		return true;
	}

	private boolean isAsynEnabled(TransitionNode e, Collection<Path> sync, Map<Condition, Phase> phases){
		//get pre-channel place q of e
		for (Node n : net.getPreset(e)){
			if(n instanceof ChannelPlace)
				//if q is un-marked (synchronous semantic)
				if (((ChannelPlace)n).isMarked() == false)
					//get the input transition node of q, if it is unenabled then e is unenabled
					for(Node node : net.getPreset(n)){
						if(node instanceof TransitionNode && !checkedNodes.contains(node)){
							if(!isPNEnabled((TransitionNode)node)
									||!isSyncEnabled((TransitionNode)node, sync, phases)
									||!isAsynEnabled((TransitionNode)node, sync, phases)
									||!isBhvEnabled((TransitionNode)node, phases))
								return false;
						}
				}
		}
		return true;
	}

	private boolean isBhvEnabled(TransitionNode e, Map<Condition, Phase> phases){
		//if e is abstract event, e is enabled if the maximal phase of e's pre-condition is marked
		for(ONGroup group : abstractGroups){
			if(group.getComponents().contains(e)){
				for(Node pre : getPrePNSet(e))
					if(pre instanceof Condition){
						Phase phase = phases.get((Condition)pre);
						for(Condition max : bsonAlg.getMaximalPhase(phase))
							if(!max.isMarked())
								return false;
				}
			return true;
			}
		}

		for(ONGroup group : bhvGroups){
			if(group.getComponents().contains(e)){
				for(Condition c : phases.keySet()){
					//if c is marked
					if(c.isMarked())
						//e is unenbled if
						//1. e belong to c
						//2. either the pre- or post condition of e not belong to c's phase
						if((!phases.get(c).containsAll(getPrePNSet(e))
								&& phases.get(c).containsAll(getPostPNSet(e)))
								|| (!phases.get(c).containsAll(getPostPNSet(e))
										&& phases.get(c).containsAll(getPrePNSet(e)))){
						return false;
						}
					//if c is not marked
					if(!c.isMarked())
						//e is un-enabled if the pre- and post-conditions of e all belong to the phase of c.
						if(phases.get(c).containsAll(getPostPNSet(e))
								&& phases.get(c).containsAll(getPrePNSet(e)))
							return false;
					}
				}
			}
		return true;
	}

	final public boolean isEnabled (TransitionNode e, Collection<Path> sync, Map<Condition, Phase> phases){
		checkedNodes.clear();
		if(isPNEnabled(e)
				&& isSyncEnabled(e, sync, phases)
				&& isAsynEnabled(e, sync, phases)
				&& isBhvEnabled(e, phases)){
			return true;
		}
		return false;
	}

	public void fire(Collection<TransitionNode> fireList) throws InvalidStructureException{

		for(TransitionNode e : fireList){
			for (SONConnection c : net.getSONConnections(e)) {
				if (c.getSemantics() == Semantics.PNLINE && e==c.getFirst()) {
					Condition to = (Condition)c.getSecond();
					if(to.isMarked())
						throw new InvalidStructureException("Token amount > 1: "+net.getNodeReference(to));
					to.setMarked(true);
				}
				if (c.getSemantics() == Semantics.PNLINE && e==c.getSecond()) {
					Condition from = (Condition)c.getFirst();
					if(!from.isMarked())
						throw new InvalidStructureException("Token amount = 0: "+net.getNodeReference(from));
					from.setMarked(false);
				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getFirst()){
						ChannelPlace to = (ChannelPlace)c.getSecond();
						if(fireList.containsAll(net.getPostset(to)) && fireList.containsAll(net.getPreset(to)))
							to.setMarked(((ChannelPlace)to).isMarked());
						else{
							if(to.isMarked())
								throw new InvalidStructureException("Token amount > 1: "+net.getNodeReference(to));
							to.setMarked(true);
						}
				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getSecond()){
						ChannelPlace from = (ChannelPlace)c.getFirst();
						if(fireList.containsAll(net.getPostset(from)) && fireList.containsAll(net.getPreset(from)))
							from.setMarked(((ChannelPlace)from).isMarked());
						else
							if(!from.isMarked())
								throw new InvalidStructureException("Token amount = 0: "+net.getNodeReference(from));
							from.setMarked(false);
				}
			}

			//if e is an abstract events, get preMax - maximal phase of e's input, and postMin - minimal phase of e' output
			Collection<Condition> preMax = new HashSet<Condition>();
			Collection<Condition> postMin = new HashSet<Condition>();
			for(Node pre : getPrePNSet(e)){
				if(bsonAlg.isAbstractCondition(pre))
					preMax.addAll(bsonAlg.getMaximalPhase(bsonAlg.getPhase((Condition)pre)));
			}
			for(Node post : getPostPNSet(e)){
				if(bsonAlg.isAbstractCondition(post))
					postMin.addAll(bsonAlg.getMinimalPhase(bsonAlg.getPhase((Condition)post)));
			}
			//if preMax and postMin are in separate ONs.
			if(!preMax.containsAll(postMin)){
				boolean isFinal=true;
				//if preMax are the final states of an ON
				for(Condition fin : preMax){
					if(!isFinal(fin)){
						isFinal=false;
						break;
					}
				}
				//token in preMax sets to false if none of corresponding abstract conditions is marked
				if(isFinal){
					for(Condition fin : preMax){
						int tokens = 0;
						for(Node c : bsonAlg.getAbstractConditions(fin)){
							if(((Condition)c).isMarked())
								tokens++;
						}
						if(fin.isMarked() && tokens == 0)
							fin.setMarked(false);
					}
				}
				//if postMin are the initial states of another ON
				boolean isInitial = true;
				for(Condition init : postMin){
					if(!isInitial(init)){
						isInitial=false;
						break;
					}
				}
				//token in postMin sets to true if ALL corresponding abstract conditions is marked
				if(isInitial)
					for(Condition ini : postMin){
						int tokens = 0;
						int count = 0;
						for(Node c : bsonAlg.getAbstractConditions(ini)){
							count++;
							if(((Condition)c).isMarked())
								tokens++;
						}
						if(!ini.isMarked() && tokens == count)
							ini.setMarked(true);
				}
			}
		}
	}


	//reverse simulation

	final public boolean isUnfireEnabled (TransitionNode e, Collection<Path> sync, Map<Condition, Phase> phases) {
		checkedNodes.clear();
		if(isPNUnEnabled(e) && isSyncUnEnabled(e, sync, phases) && this.isAsynUnEnabled(e, sync, phases) && isBhvUnEnabled(e, phases))
			return true;
		return false;
	}


	private boolean isPNUnEnabled (TransitionNode e) {
		for (Node n : net.getPostset(e)){
			if(n instanceof Condition)
				if (!((Condition)n).isMarked())
					return false;
			}
		if(net.getPostset(e).isEmpty())
			return false;
		return true;
	}

	private boolean isSyncUnEnabled(TransitionNode e, Collection<Path> sync, Map<Condition, Phase> phases){
		HashSet<Node> relatedSync = new HashSet<Node>();

		for(Path cycle : sync){
			if(cycle.contains(e))
				relatedSync.addAll(cycle);
		}

		if(relatedSync.contains(e)){
			checkedNodes.addAll(relatedSync);
			for(Node n : relatedSync)
				if(n instanceof TransitionNode){
					if(!this.isPNUnEnabled((TransitionNode)n) || !this.isBhvUnEnabled((TransitionNode)n, phases))
							return false;
					for(Node post : this.getPostAsynEvents((TransitionNode)n)){
						if(post instanceof TransitionNode && !relatedSync.contains(post)){
							if(!this.isAsynUnEnabled((TransitionNode)n, sync, phases)||!this.isBhvUnEnabled((TransitionNode)n, phases))
								return false;
						}
					}
				}
			}
		return true;
	}

	private boolean isAsynUnEnabled(TransitionNode e, Collection<Path> sync, Map<Condition, Phase> phases){

		for (Node n : net.getPostset(e)){
			if(n instanceof ChannelPlace)
				if (((ChannelPlace)n).isMarked() == false)
					for(Node node : net.getPostset(n)){
						if(node instanceof TransitionNode && !checkedNodes.contains(node)){
							if(!this.isPNUnEnabled((TransitionNode)node)
									||!this.isSyncUnEnabled((TransitionNode)node, sync, phases)
									||!this.isAsynUnEnabled((TransitionNode)node, sync, phases)
									||!this.isBhvUnEnabled((TransitionNode)node, phases))
								return false;
						}
				}
		}
		return true;
	}

	private boolean isBhvUnEnabled(TransitionNode e, Map<Condition, Phase> phases){
		for(ONGroup group : abstractGroups){
			if(group.getComponents().contains(e)){
				for(Node pre : getPostPNSet(e))
					if(pre instanceof Condition){
						Phase phase = phases.get((Condition)pre);
						for(Condition min : bsonAlg.getMinimalPhase(phase))
							if(!min.isMarked())
								return false;
				}
			return true;
			}
		}

		for(ONGroup group : bhvGroups){
			if(group.getComponents().contains(e)){
				for(Condition c : phases.keySet()){
					if(c.isMarked())
						if((!phases.get(c).containsAll(getPrePNSet(e))
								&& phases.get(c).containsAll(getPostPNSet(e)))
								|| (!phases.get(c).containsAll(getPostPNSet(e))
										&& phases.get(c).containsAll(getPrePNSet(e))))
							return false;
					if(!c.isMarked())
						if(phases.get(c).containsAll(getPostPNSet(e)) && phases.get(c).containsAll(getPrePNSet(e)))
							return false;
					}
				}
			}
		return true;
	}

	public void unFire(Collection<TransitionNode> fireList) throws InvalidStructureException{
		for(TransitionNode e : fireList){
			for (SONConnection c : net.getSONConnections(e)) {
				if (c.getSemantics() == Semantics.PNLINE && e==c.getSecond()) {
					Condition to = (Condition)c.getFirst();
					if(to.isMarked())
						throw new InvalidStructureException("Reverse Token amount > 1: "+net.getNodeReference(to));
					to.setMarked(true);
				}
				if (c.getSemantics() == Semantics.PNLINE && e==c.getFirst()) {
					Condition from = (Condition)c.getSecond();
					if(!from.isMarked())
						throw new InvalidStructureException("Reverse Token amount = 0: "+net.getNodeReference(from));
					from.setMarked(false);
				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getSecond()){
					ChannelPlace to = (ChannelPlace)c.getFirst();
					if(fireList.containsAll(net.getPreset(to)) && fireList.containsAll(net.getPostset(to)))
						to.setMarked(((ChannelPlace)to).isMarked());
					else
						if(to.isMarked())
							throw new InvalidStructureException("Reverse Token amount > 1: "+net.getNodeReference(to));
						to.setMarked(!to.isMarked());
				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getFirst()){
					ChannelPlace from = (ChannelPlace)c.getSecond();
					if(fireList.containsAll(net.getPreset(from)) && fireList.containsAll(net.getPostset(from)))
						from.setMarked(((ChannelPlace)from).isMarked());
					else
						if(!from.isMarked())
							throw new InvalidStructureException("Reverse Token amount = 0: "+net.getNodeReference(from));
						from.setMarked(!from.isMarked());
				}
			}


			//if e is an abstract events, get preMax - maximal phase of e's input, and postMin - minimal phase of e' output
			Collection<Condition> preMax = new HashSet<Condition>();
			Collection<Condition> postMin = new HashSet<Condition>();
			for(Node pre : getPrePNSet(e)){
				if(bsonAlg.isAbstractCondition(pre))
					preMax.addAll(bsonAlg.getMaximalPhase(bsonAlg.getPhase((Condition)pre)));
			}
			for(Node post : getPostPNSet(e)){
				if(bsonAlg.isAbstractCondition(post))
					postMin.addAll(bsonAlg.getMinimalPhase(bsonAlg.getPhase((Condition)post)));
			}
			//if preMax and postMin are in separate ONs.
			if(!preMax.containsAll(postMin)){
				boolean isFinal=true;
				//if preMax are the final states of an ON
				for(Condition fin : preMax){
					if(!isFinal(fin)){
						isFinal=false;
						break;
					}
				}
				//token in preMax sets to true if all of corresponding abstract conditions is marked
				if(isFinal){
					for(Condition fin : preMax){
						int tokens = 0;
						int count = 0;
						for(Node c : bsonAlg.getAbstractConditions(fin)){
							count++;
							if(((Condition)c).isMarked())
								tokens++;
						}
						if(!fin.isMarked() && tokens == count)
							fin.setMarked(true);
					}
				}
				//if postMin are the initial states of another ON
				boolean isInitial = true;
				for(Condition init : postMin){
					if(!isInitial(init)){
						isInitial=false;
						break;
					}
				}
				//token in postMin sets to false if none of corresponding abstract conditions is marked
				if(isInitial)
					for(Condition ini : postMin){
						int tokens = 0;
						for(Node c : bsonAlg.getAbstractConditions(ini)){
							if(((Condition)c).isMarked())
								tokens++;
						}
						if(ini.isMarked() && tokens == 0)
							ini.setMarked(false);
				}
			}
		}
	}
}
