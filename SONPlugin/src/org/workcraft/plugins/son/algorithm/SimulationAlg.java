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

	public SimulationAlg(SON net){
		super(net);
		this.net = net;
		bsonAlg = new BSONAlg(net);

		abstractGroups = bsonAlg.getAbstractGroups(net.getGroups());
		bhvGroups = bsonAlg.getBhvGroups(net.getGroups());
	}

	private Collection<Node> getMinFireSet (TransitionNode e, Collection<Path> sync, Collection<TransitionNode> enabledEvents){
		Collection<Node> result = new HashSet<Node>();
		HashSet<Node> syncEvents = new HashSet<Node>();
		//get related synchronous events
		for(Path cycle : sync){
			if(cycle.contains(e))
				syncEvents.addAll(cycle);
		}

		if(!syncEvents.isEmpty()){
			for(Node n : syncEvents){
				if(enabledEvents.contains(n) && !result.contains(n)){
					result.add(n);

					for(TransitionNode pre : getPreAsynEvents((TransitionNode)n)){
						if(!syncEvents.contains(pre) && enabledEvents.contains(pre))
							result.addAll(getMinFireSet((TransitionNode)n, sync, enabledEvents));
					}
				}
				if(!enabledEvents.contains(n))
					throw new RuntimeException("algorithm error: has unenabled event in sync cycle  "+net.getName(n));
			}
		}

		if(!this.getPreAsynEvents(e).isEmpty() && enabledEvents.contains(e)){
			if(!result.contains(e))
				result.add(e);

			for(TransitionNode n : this.getPreAsynEvents(e))
				if(!result.contains(n) && enabledEvents.contains(n)){
					result.add(n);
					result.addAll(getMinFireSet((TransitionNode)n, sync, enabledEvents));
				}
		}
		else
			result.add(e);

		return result;
	}

	/**
	 * return minimal execution set of a given node.
	 * contain other nodes which have synchronous with the clicked node.
	 */
	public List<TransitionNode> getMinFires (TransitionNode e, Collection<Path> sync, Collection<TransitionNode> enabledEvents){
		List<TransitionNode> result = new ArrayList<TransitionNode>();

		for(Node n : getMinFireSet(e, sync, enabledEvents))
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
						if(!syncEvents.contains(post) && enabledEvents.contains(post))
							result.addAll(getMaxFireSet((TransitionNode)n, sync, enabledEvents));
					}

				}
				if(!enabledEvents.contains(n))
					throw new RuntimeException("algorithm error: has unenable event in sync cycle");
			}
		}

		if(!this.getPostAsynEvents(e).isEmpty() && enabledEvents.contains(e)){
			if(!result.contains(e))
				result.add(e);

			for(TransitionNode n : this.getPostAsynEvents(e))
				if(!result.contains(n) && enabledEvents.contains(n)){
					result.add(n);
					result.addAll(getMaxFireSet((TransitionNode)n, sync, enabledEvents));
				}
		}
		else
			result.add(e);

		return result;
	}

	public List<TransitionNode> getMaxFires (TransitionNode e, Collection<Path> sync, Collection<TransitionNode> enabledEvents){
		List<TransitionNode> result = new ArrayList<TransitionNode>();

		for(Node n : getMaxFireSet(e, sync, enabledEvents))
			if(n instanceof TransitionNode)
				result.add((TransitionNode)n);

		return result;
	}

	private List<Node[]> createAdj(Collection<Node> nodes){

		List<Node[]> result = new ArrayList<Node[]>();

		for (Node n: nodes){
			for (Node next: net.getPostset(n)){
				if((next instanceof ChannelPlace) &&
						net.getSONConnectionType(next, n) == Semantics.ASYNLINE){
					Node[] adjoin = new Node[2];
					for(Node n2 : net.getPostset(next))
						if(n2 instanceof TransitionNode){
							adjoin[0] = n;
							adjoin[1] = n2;
							result.add(adjoin);
						}
				}

				if((next instanceof ChannelPlace) &&
						net.getSONConnectionType(next, n) == Semantics.SYNCLINE){
					Node[] adjoin = new Node[2];
					Node[] reAdjoin = new Node[2];
					for(Node n2 : net.getPostset(next))
						if(n2 instanceof TransitionNode){
							adjoin[0] = n;
							adjoin[1] = n2;
							reAdjoin[0] = n2;
							reAdjoin[1] = n;
							result.add(adjoin);
							result.add(reAdjoin);
						}
				}

				if((next instanceof TransitionNode) || (next instanceof Condition)){
					Node[] adjoin = new Node[2];
					adjoin[0] = n;
					adjoin[1] = next;
					result.add(adjoin);
					}
				}
		}
		return result;
	}

	/**
	 * get synchronous cycle for a set of node.
	 */
	public Collection<Path> getSyncCycles(Collection<Node> nodes){
		Collection<Path> cycles = new ArrayList<Path>();
		List<Path> filter = new ArrayList<Path>();

		//get all cycle set
		for(Node s : getInitial(nodes))
			for(Node v : getFinal(nodes))
				cycles.addAll(PathAlgorithm.getCycles(s, v, createAdj(nodes)));
		//get synchronous cycle set
		if(!cycles.isEmpty()){
			for(Path path : cycles){
				boolean hasCondition = false;
				for(Node n : path)
					if(n instanceof Condition){
						hasCondition = true;
						continue;
					}
				if(!hasCondition)
					filter.add(path);
			}
		}
		//get longest synchronous cycle set
		return 	PathAlgorithm.merging(filter);
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

	private boolean isSyncEnabled(TransitionNode e, Collection<Node> relatedSync, Map<Condition, Phase> phases){

		for(Node n : relatedSync)
			if(n instanceof TransitionNode){
				if(!this.isPNEnabled((TransitionNode)n) || !this.isBhvEnabled((TransitionNode)n, phases))
						return false;
				for(TransitionNode pre : this.getPreAsynEvents((TransitionNode)n)){
					if(!relatedSync.contains(pre)){
						if(!this.isAsynEnabled((TransitionNode)n, relatedSync, phases)
								|| !this.isBhvEnabled((TransitionNode)n, phases))
							return false;
					}
				}
			}


		return true;
	}

	private boolean isAsynEnabled(TransitionNode e, Collection<Node> relatedSync, Map<Condition, Phase> phases){
		//get pre-channel place q of e
		for (Node n : net.getPreset(e)){
			if(n instanceof ChannelPlace)
				//if q is un-marked (synchronous semantic)
				if (((ChannelPlace)n).isMarked() == false)
					//get the input transition node of q, if it is unenabled then e is unenabled
					for(Node node : net.getPreset(n)){
						if(node instanceof TransitionNode && !relatedSync.contains(node)){
							if(!this.isPNEnabled((TransitionNode)node)
									||!this.isSyncEnabled((TransitionNode)node, relatedSync, phases)
									||!this.isAsynEnabled((TransitionNode)node, relatedSync, phases)
									||!this.isBhvEnabled((TransitionNode)node, phases))
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
		Collection<Node> relatedSync = new HashSet<Node>();
		//get nodes which have synchronous relation with e
		for(Path cycle : sync){
			if(cycle.contains(e))
				relatedSync.addAll(cycle);
		}

		if(isPNEnabled(e) && isBhvEnabled(e, phases) && isSyncEnabled(e, relatedSync, phases)
				&& this.isAsynEnabled(e, relatedSync, phases)){
			return true;
		}
		return false;
	}

	public void fire(Collection<TransitionNode> runList) throws InvalidStructureException{
		for(TransitionNode e : runList){
			for (SONConnection c : net.getSONConnections(e)) {
				if (c.getSemantics() == Semantics.PNLINE && e==c.getFirst()) {
					Condition to = (Condition)c.getSecond();
					if(to.isMarked())
						throw new InvalidStructureException("Token amount > 1: "+net.getName(to));
					to.setMarked(true);
				}
				if (c.getSemantics() == Semantics.PNLINE && e==c.getSecond()) {
					Condition from = (Condition)c.getFirst();
					if(!from.isMarked())
						throw new InvalidStructureException("Token amount = 0: "+net.getName(from));
					from.setMarked(false);
				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getFirst()){
						ChannelPlace to = (ChannelPlace)c.getSecond();
						if(runList.containsAll(net.getPostset(to)) && runList.containsAll(net.getPreset(to)))
							to.setMarked(((ChannelPlace)to).isMarked());
						else{
							if(to.isMarked())
								throw new InvalidStructureException("Token amount > 1: "+net.getName(to));
							to.setMarked(true);
						}
				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getSecond()){
						ChannelPlace from = (ChannelPlace)c.getFirst();
						if(runList.containsAll(net.getPostset(from)) && runList.containsAll(net.getPreset(from)))
							from.setMarked(((ChannelPlace)from).isMarked());
						else
							if(!from.isMarked())
								throw new InvalidStructureException("Token amount = 0: "+net.getName(from));
							from.setMarked(false);
				}
			}

		for(ONGroup group : abstractGroups){
			if(group.getEvents().contains(e)){
				Collection<Condition> preMax = new HashSet<Condition>();
				Collection<Condition> postMin = new HashSet<Condition>();
				for(Node pre : getPrePNSet(e))
					preMax.addAll(bsonAlg.getMaximalPhase(bsonAlg.getPhase((Condition)pre)));
				for(Node post : getPostPNSet(e))
					postMin.addAll(bsonAlg.getMinimalPhase(bsonAlg.getPhase((Condition)post)));
				//disjoint phases
				if(!preMax.containsAll(postMin)){
					boolean isFinal=true;
					for(Condition fin : preMax)
							if(!isFinal(fin))
								isFinal=false;
					if(isFinal){
						//structure that condition fin has more than one abstract conditions
						for(Condition fin : preMax){
							//amount of marked abstract conditions
							int tokens = 0;
							for(Node post : net.getPostset(fin)){
								if((post instanceof Condition)
										&& net.getSONConnectionType(post, fin) == Semantics.BHVLINE)
									if(((Condition)post).isMarked())
										tokens++;
							}
							//if preMax has token and there exist an empty abstract condition, then token -> false;
							if(fin.isMarked() && tokens == 0)
								fin.setMarked(false);
						}
					}
					boolean isIni = true;
					for(Condition init : postMin)
						if(!isInitial(init))
							isIni=false;
					if(isIni)
						for(Condition ini : postMin){
							//structure that condition ini has more than one abstract conditions
							int tokens = 0;
							int size = 0;
							for(Node post : net.getPostset(ini)){
								if((post instanceof Condition)
										&& net.getSONConnectionType(post, ini) == Semantics.BHVLINE){
									size++;
									if(((Condition)post).isMarked())
										tokens++;
									}
							}

							if(!ini.isMarked() && tokens == size)
								ini.setMarked(true);
							//	JOptionPane.showMessageDialog(null, "Token setting error: token in "+net.getName(ini) + " is true", "Error", JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			}
		}
	}


	//reverse simulation

	final public boolean isUnfireEnabled (TransitionNode e, Collection<Path> sync, Map<Condition, Phase> phases) {

		HashSet<Node> relatedSync = new HashSet<Node>();

		for(Path cycle : sync){
			if(cycle.contains(e))
				relatedSync.addAll(cycle);
		}

		if(isPNUnEnabled(e) && isBhvUnEnabled(e, phases) && isSyncUnEnabled(e, relatedSync, phases)
				&& this.isAsynUnEnabled(e, relatedSync, phases))
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

	private boolean isSyncUnEnabled(TransitionNode e, Collection<Node> relatedSync, Map<Condition, Phase> phases){

		for(Node n : relatedSync)
			if(n instanceof TransitionNode){
				if(!this.isPNUnEnabled((TransitionNode)n) || !this.isBhvUnEnabled((TransitionNode)n, phases))
						return false;
				for(Node post : this.getPostAsynEvents((TransitionNode)n)){
					if(post instanceof TransitionNode && !relatedSync.contains(post)){
						if(!this.isAsynUnEnabled((TransitionNode)n, relatedSync, phases)
								||!this.isBhvUnEnabled((TransitionNode)n, phases))
							return false;
					}
				}
			}

		return true;
	}

	private boolean isAsynUnEnabled(TransitionNode e, Collection<Node> relatedSync, Map<Condition, Phase> phases){

		for (Node n : net.getPostset(e)){
			if(n instanceof ChannelPlace)
				if (((ChannelPlace)n).isMarked() == false)
					for(Node node : net.getPostset(n)){
						if(node instanceof TransitionNode && !relatedSync.contains(node)){
							if(!this.isPNUnEnabled((TransitionNode)node)
									||!this.isSyncUnEnabled((TransitionNode)node, relatedSync, phases)
									||!this.isAsynUnEnabled((TransitionNode)node, relatedSync, phases)
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

	public void unFire(Collection<TransitionNode> events) throws InvalidStructureException{
		for(TransitionNode e : events){
			for (SONConnection c : net.getSONConnections(e)) {
				if (c.getSemantics() == Semantics.PNLINE && e==c.getSecond()) {
					Condition to = (Condition)c.getFirst();
					if(to.isMarked())
						throw new InvalidStructureException("Token amount > 1: "+net.getName(to));
					to.setMarked(true);
				}
				if (c.getSemantics() == Semantics.PNLINE && e==c.getFirst()) {
					Condition from = (Condition)c.getSecond();
					if(!from.isMarked())
						throw new InvalidStructureException("Token amount = 0: "+net.getName(from));
					from.setMarked(false);
				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getSecond()){
					ChannelPlace to = (ChannelPlace)c.getFirst();
					if(events.containsAll(net.getPreset(to)) && events.containsAll(net.getPostset(to)))
						to.setMarked(((ChannelPlace)to).isMarked());
					else
						if(to.isMarked())
							throw new InvalidStructureException("Token amount > 1: "+net.getName(to));
						to.setMarked(!to.isMarked());
				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getFirst()){
					ChannelPlace from = (ChannelPlace)c.getSecond();
					if(events.containsAll(net.getPreset(from)) && events.containsAll(net.getPostset(from)))
						from.setMarked(((ChannelPlace)from).isMarked());
					else
						if(!from.isMarked())
							throw new InvalidStructureException("Token amount = 0: "+net.getName(from));
						from.setMarked(!from.isMarked());
				}
			}

			for(ONGroup group : abstractGroups){
				if(group.getEvents().contains(e)){
					Phase preMax = new Phase();
					Phase postMin = new Phase();
					for(Node pre : getPrePNSet(e))
						preMax.addAll( bsonAlg.getMaximalPhase(bsonAlg.getPhase((Condition)pre)));
					for(Node post : getPostPNSet(e))
						postMin.addAll(bsonAlg.getMinimalPhase(bsonAlg.getPhase((Condition)post)));

					if(!preMax.containsAll(postMin)){
						boolean isInitial=true;
						for(Condition ini : postMin)
								if(!isInitial(ini))
									isInitial=false;
						if(isInitial){
								for(Condition ini : postMin){
									//structure such that condition fin has more than one high-level states
									int tokens = 0;
									for(Node post : net.getPostset(ini)){
										if(post instanceof Condition && net.getSONConnectionType(post, ini) == Semantics.BHVLINE)
											if(((Condition)post).isMarked())
												tokens++;
									}
									//if preMax has token and there is no high-level states has token, then token -> false;
									if(ini.isMarked() && tokens == 0)
										ini.setMarked(false);
								}
							}

						boolean isFinal = true;
						for(Condition fin : preMax)
								if(!isFinal(fin))
									isFinal=false;
						if(isFinal)
							for(Condition fin : preMax){
								//structure such that condition ini has more than one high-level states
								int tokens = 0;
								int size = 0;
								for(Node post : net.getPostset(fin)){
									if(post instanceof Condition && net.getSONConnectionType(post, fin)== Semantics.BHVLINE){
										size++;
										if(((Condition)post).isMarked())
											tokens++;
										}
								}

								if(!fin.isMarked() && tokens == size)
									fin.setMarked(true);
								//	JOptionPane.showMessageDialog(null, "Token setting error: token in "+net.getName(ini) + " is true", "Error", JOptionPane.WARNING_MESSAGE);
							}
					}
				}
			}
		}
	}

	//others

	public Collection<ONGroup> getAbstractGroups(){
		return this.abstractGroups;
	}
}
