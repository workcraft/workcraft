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

public class SimulationAlg extends RelationAlgorithm {

	private SON net;
	private BSONAlg bsonAlg;

	private Collection<TransitionNode> syncEventSet = new HashSet<TransitionNode>();
	private Collection<Node> checkedEvents = new HashSet<Node>();

	private Collection<Node> minFire = new HashSet<Node>();
	private Collection<Node> postEventSet = new HashSet<Node>();

	private Collection<ONGroup> abstractGroups;
	private Collection<ONGroup> bhvGroups;

	public SimulationAlg(SON net){
		super(net);
		this.net = net;
		bsonAlg = new BSONAlg(net);

		abstractGroups = bsonAlg.getAbstractGroups(net.getGroups());
		bhvGroups = bsonAlg.getBhvGroups(net.getGroups());
	}

	private void getMinFireSet (TransitionNode e, Collection<Path> sync, Collection<TransitionNode> enabledEvents){

		HashSet<Node> syncEvents = new HashSet<Node>();

		for(Path cycle : sync){
			if(cycle.contains(e))
				syncEvents.addAll(cycle);
		}

		if(!syncEvents.isEmpty()){
			for(Node n : syncEvents){
				if(enabledEvents.contains(n) && !minFire.contains(n)){
					minFire.add(n);

					for(TransitionNode pre : this.getPreAsynEvents((TransitionNode)n)){
						if(!syncEvents.contains(pre) && enabledEvents.contains(pre))
							getMinFireSet((TransitionNode)n, sync, enabledEvents);
					}
				}
				if(!enabledEvents.contains(n))
					throw new RuntimeException("algorithm error: has unenabled event in sync cycle  "+net.getName(n));
			}
		}

		if(!this.getPreAsynEvents(e).isEmpty() && enabledEvents.contains(e)){
			if(!minFire.contains(e))
				minFire.add(e);

			for(TransitionNode n : this.getPreAsynEvents(e))
				if(!minFire.contains(n) && enabledEvents.contains(n)){
					minFire.add(n);
					getMinFireSet((TransitionNode)n, sync, enabledEvents);
				}
		}
		else
			minFire.add(e);
	}

	/**
	 * return minimal execution set of a given node.
	 * This may contain other nodes which have synchronous with the target node.
	 */
	public List<TransitionNode> getMinFires (TransitionNode e, Collection<Path> sync, Collection<TransitionNode> enabledEvents){
		List<TransitionNode> result = new ArrayList<TransitionNode>();

		getMinFireSet(e, sync, enabledEvents);

		for(Node n : this.minFire)
			if(n instanceof TransitionNode)
				result.add((TransitionNode)n);;

		return result;
	}


	private void getMaxFireSet(TransitionNode e, Collection<Path> sync, Collection<TransitionNode> enabledEvents){

		Collection<Node> syncEvents = new HashSet<Node>();

		for(Path cycle : sync){
			if(cycle.contains(e))
				syncEvents.addAll(cycle);
		}

		if(!syncEvents.isEmpty()){
			for(Node n : syncEvents){
				if(enabledEvents.contains(n) && !postEventSet.contains(n)){
					postEventSet.add(n);

					for(TransitionNode post : this.getPostAsynEvents((TransitionNode)n)){
						if(!syncEvents.contains(post) && enabledEvents.contains(post))
							getMaxFireSet((TransitionNode)n, sync, enabledEvents);
					}

				}
				if(!enabledEvents.contains(n))
					throw new RuntimeException("algorithm error: has unenable event in sync cycle");
			}
		}

		if(!this.getPostAsynEvents(e).isEmpty() && enabledEvents.contains(e)){
			if(!postEventSet.contains(e))
				postEventSet.add(e);

			for(TransitionNode n : this.getPostAsynEvents(e))
				if(!postEventSet.contains(n) && enabledEvents.contains(n)){
					postEventSet.add(n);
					getMaxFireSet((TransitionNode)n, sync, enabledEvents);
				}
		}
		else
			postEventSet.add(e);
	}

	public List<TransitionNode> getMaxFires (TransitionNode e, Collection<Path> sync, Collection<TransitionNode> enabledEvents){
		List<TransitionNode> result = new ArrayList<TransitionNode>();
		getMaxFireSet(e, sync, enabledEvents);

		for(Node n : this.postEventSet)
			if(n instanceof TransitionNode)
				result.add((TransitionNode)n);

		return result;
	}

	/**
	 * clear all set.
	 */
	public void clearAll(){
			syncEventSet.clear();
			checkedEvents.clear();

			minFire.clear();
			postEventSet.clear();
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

		this.clearAll();
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
		return 	merging(filter);
	}

	private List<Path> merging (List<Path> cycles){
		List<Path> result = new ArrayList<Path>();

		while (cycles.size() > 0){
			Path first = cycles.get(0);
			List<Path> rest = cycles;
			rest.remove(0);

			int i = -1;
			while (first.size() > i){
				i = first.size();

				List<Path> rest2 = new ArrayList<Path>();
				for(Path path : rest){
					if(hasCommonElements(first, path)){
						first.addAll(path);
					}
					else{
						rest2.add(path);
					}
				}
				rest = rest2;
			}

			HashSet<Node> filter = new HashSet<Node>();
			for(Node node : first){
				filter.add(node);
			}

			Path subResult = new Path();
			subResult.addAll(filter);
			result.add(subResult);
			cycles = rest;
		}
		return result;
	}



	private boolean hasCommonElements(Collection<Node> cycle1, Collection<Node> cycle2){
		for(Node n : cycle1)
			if(cycle2.contains(n))
				return true;
		for(Node n : cycle2)
			if(cycle1.contains(n))
				return true;
		return false;
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
		HashSet<Node> syncEvents = new HashSet<Node>();

		for(Path cycle : sync){
			if(cycle.contains(e))
				syncEvents.addAll(cycle);
		}

		if(syncEvents.contains(e)){
			checkedEvents.addAll(syncEvents);
			for(Node n : syncEvents)
				if(n instanceof TransitionNode){
					if(!this.isPNEnabled((TransitionNode)n) || !this.isBhvEnabled((TransitionNode)n, phases))
							return false;
					for(TransitionNode pre : this.getPreAsynEvents((TransitionNode)n)){
						if(!syncEvents.contains(pre)){
							if(!this.isAsynEnabled((TransitionNode)n, sync, phases) || !this.isBhvEnabled((TransitionNode)n, phases))
								return false;
						}
					}
				}
			}

		return true;
	}

	private boolean isAsynEnabled(TransitionNode e, Collection<Path> sync, Map<Condition, Phase> phases){

		for (Node n : net.getPreset(e)){
			if(n instanceof ChannelPlace)
				if (((ChannelPlace)n).isMarked() == false)
					for(Node node : net.getPreset(n)){
						if(node instanceof TransitionNode && !checkedEvents.contains(node)){
							if(!this.isPNEnabled((TransitionNode)node) ||!this.isSyncEnabled((TransitionNode)node, sync, phases)
									||!this.isAsynEnabled((TransitionNode)node, sync, phases) ||!this.isBhvEnabled((TransitionNode)node, phases))
								return false;
						}
				}
		}
		return true;
	}

	private boolean isBhvEnabled(TransitionNode e, Map<Condition, Phase> phases){
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
					if(c.isMarked())
						if((!phases.get(c).containsAll(getPrePNSet(e)) && phases.get(c).containsAll(getPostPNSet(e)))||
								(!phases.get(c).containsAll(getPostPNSet(e)) && phases.get(c).containsAll(getPrePNSet(e))))
							return false;
					if(!c.isMarked())
						if(phases.get(c).containsAll(getPostPNSet(e)) && phases.get(c).containsAll(getPrePNSet(e)))
							return false;
					}
				}
			}
		return true;
	}

	final public boolean isEnabled (TransitionNode e, Collection<Path> sync, Map<Condition, Phase> phases){
		checkedEvents.clear();
		if(isPNEnabled(e) && isSyncEnabled(e, sync, phases) && this.isAsynEnabled(e, sync, phases) && isBhvEnabled(e, phases)){
			return true;
		}
		return false;
	}

	public void fire(Collection<TransitionNode> runList){
		for(TransitionNode e : runList){
			for (SONConnection c : net.getSONConnections(e)) {
				if (c.getSemantics() == Semantics.PNLINE && e==c.getFirst()) {
					Condition to = (Condition)c.getSecond();
					if(to.isMarked())
						throw new RuntimeException("Token setting error: the number of token in "+net.getName(to) + " > 1");
					to.setMarked(true);
				}
				if (c.getSemantics() == Semantics.PNLINE && e==c.getSecond()) {
					Condition from = (Condition)c.getFirst();
					from.setMarked(false);

				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getFirst()){
						ChannelPlace to = (ChannelPlace)c.getSecond();
						if(runList.containsAll(net.getPostset(to)) && runList.containsAll(net.getPreset(to)))
							to.setMarked(((ChannelPlace)to).isMarked());
						else{
							if(to.isMarked())
								throw new RuntimeException("Token setting error: the number of token in "+net.getName(to) + " > 1");
							to.setMarked(true);
						}
				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getSecond()){
						ChannelPlace from = (ChannelPlace)c.getFirst();
						if(runList.containsAll(net.getPostset(from)) && runList.containsAll(net.getPreset(from)))
							from.setMarked(((ChannelPlace)from).isMarked());
						else
							from.setMarked(!((ChannelPlace)from).isMarked());
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
					boolean isFinal=true;
					for(Condition fin : preMax)
							if(!isFinal(fin))
								isFinal=false;
					if(isFinal){
						for(Condition fin : preMax){
							//structure such that condition fin has more than one high-level states
							int tokens = 0;
							for(Node post : net.getPostset(fin)){
								if(post instanceof Condition && net.getSONConnectionType(post, fin) == Semantics.BHVLINE)
									if(((Condition)post).isMarked())
										tokens++;
							}
							//if preMax has token and there is no high-level states has token, then token -> false;
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
							//structure such that condition ini has more than one high-level states
							int tokens = 0;
							int size = 0;
							for(Node post : net.getPostset(ini)){
								if(post instanceof Condition && net.getSONConnectionType(post, ini) == Semantics.BHVLINE){
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
		checkedEvents.clear();
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
		HashSet<Node> syncEvents = new HashSet<Node>();

		for(Path cycle : sync){
			if(cycle.contains(e))
				syncEvents.addAll(cycle);
		}

		if(syncEvents.contains(e)){
			checkedEvents.addAll(syncEvents);
			for(Node n : syncEvents)
				if(n instanceof TransitionNode){
					if(!this.isPNUnEnabled((TransitionNode)n) || !this.isBhvUnEnabled((TransitionNode)n, phases))
							return false;
					for(Node post : this.getPostAsynEvents((TransitionNode)n)){
						if(post instanceof TransitionNode && !syncEvents.contains(post)){
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
						if(node instanceof TransitionNode && !checkedEvents.contains(node)){
							if(!this.isPNUnEnabled((TransitionNode)node) ||!this.isSyncUnEnabled((TransitionNode)node, sync, phases)
									||!this.isAsynUnEnabled((TransitionNode)node, sync, phases) ||!this.isBhvUnEnabled((TransitionNode)node, phases))
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
						Phase phase = bsonAlg.getPhase((Condition)pre);
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
						if((!phases.get(c).containsAll(getPrePNSet(e)) && phases.get(c).containsAll(getPostPNSet(e)))||
								(!phases.get(c).containsAll(getPostPNSet(e)) && phases.get(c).containsAll(getPrePNSet(e))))
							return false;
					if(!c.isMarked())
						if(phases.get(c).containsAll(getPostPNSet(e)) && phases.get(c).containsAll(getPrePNSet(e)))
							return false;
					}
				}
			}
		return true;
	}

	public void unFire(Collection<TransitionNode> events){
		for(TransitionNode e : events){
			for (SONConnection c : net.getSONConnections(e)) {
				if (c.getSemantics() == Semantics.PNLINE && e==c.getSecond()) {
					Condition to = (Condition)c.getFirst();
					to.setMarked(true);
				}
				if (c.getSemantics() == Semantics.PNLINE && e==c.getFirst()) {
					Condition from = (Condition)c.getSecond();
					from.setMarked(false);
				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getSecond()){
						ChannelPlace to = (ChannelPlace)c.getFirst();
						if(events.containsAll(net.getPreset(to)) && events.containsAll(net.getPostset(to)))
							to.setMarked(((ChannelPlace)to).isMarked());
						else
							to.setMarked(!((ChannelPlace)to).isMarked());
				}
				if (c.getSemantics() == Semantics.ASYNLINE && e==c.getFirst()){
						ChannelPlace from = (ChannelPlace)c.getSecond();
						if(events.containsAll(net.getPreset(from)) && events.containsAll(net.getPostset(from)))
							from.setMarked(((ChannelPlace)from).isMarked());
						else
							from.setMarked(!((ChannelPlace)from).isMarked());
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
