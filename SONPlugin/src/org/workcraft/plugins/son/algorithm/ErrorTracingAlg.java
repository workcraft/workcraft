package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;

public class ErrorTracingAlg extends RelationAlgorithm{

	private SON net;
	private BSONAlg bsonAlg;

	public ErrorTracingAlg(SON net){
		super(net);
		bsonAlg = new BSONAlg(net);
		this.net = net;
	}

	//Forward error tracing
	public void setErrNum (Collection<TransitionNode> fireList, Collection<Path> sync, Map<Condition, Collection<Phase>> phases, boolean isBhv){

		while(!fireList.isEmpty()){
			for(TransitionNode e : fireList){
				if(!net.getSONConnectionTypes(e).contains(Semantics.SYNCLINE)){
					//set number from the very first asynchronous event
					if(getPreAsynEvents(e).isEmpty() || !hasCommonElements(fireList, getPreAsynEvents(e))){
						setAsynErrNum(e, phases, isBhv);
						fireList.remove(e);
						break;
					}
				}
			}

			Collection<TransitionNode> removeList = new ArrayList<TransitionNode>();
			for(TransitionNode e :fireList){
				for(Path cycle : sync){
					if(cycle.contains(e)){
						Collection<TransitionNode> fireList2 = new ArrayList<TransitionNode>();
						Collection<TransitionNode> eventCycle = new ArrayList<TransitionNode>();

						fireList2.addAll(fireList);
						fireList2.removeAll(cycle);
						boolean hasPreAsyn = false;
						for(Node n: cycle){
							if(n instanceof TransitionNode){
								eventCycle.add((TransitionNode)n);
								if(!getPreAsynEvents((TransitionNode)n).isEmpty()
										&& hasCommonElements(fireList2, getPreAsynEvents((TransitionNode)n)))
									hasPreAsyn = true;
							}
						}
						if(!hasPreAsyn){
							setSyncErrNum(eventCycle, phases, isBhv);
							removeList.addAll(eventCycle);
							break;
						}
					}
				}
			}
			fireList.removeAll(removeList);
		}
	}


	private boolean hasCommonElements(Collection<TransitionNode> cycle1, Collection<TransitionNode> cycle2){
		for(Node n : cycle1)
			if(cycle2.contains(n))
				return true;
		for(Node n : cycle2)
			if(cycle1.contains(n))
				return true;
		return false;
	}

	private void setAsynErrNum(TransitionNode e, Map<Condition, Collection<Phase>> phases, boolean isLower){
		int err = 0;
		if(e.isFaulty())
			err++;
		//get err number from lower conditions and channel places
		for(Node pre : net.getPreset(e)){
			if(pre instanceof PlaceNode)
				err = err + ((PlaceNode)pre).getErrors();
		}

		for(Node post: net.getPostset(e)){
			if(post instanceof Condition){
				((Condition)post).setErrors(err);
				//set err number for lower condition
				if(!isLower)
					for(Condition min : bsonAlg.getMinimalPhase(phases.get(post))){
						((Condition) min).setErrors(((Condition) min).getErrors() + ((Condition)post).getErrors());
					}
			}
			if(post instanceof ChannelPlace){
				((ChannelPlace)post).setErrors(err);
			}
		}
	}

	private void setSyncErrNum(Collection<TransitionNode> sync, Map<Condition, Collection<Phase>> phases, boolean isLower){
		int err = 0;

		for(TransitionNode e : sync){
			if(((TransitionNode)e).isFaulty())
				err++;
			for(Node pre : net.getPreset(e)){
				if(pre instanceof Condition)
					err = err + ((Condition)pre).getErrors();
				if(pre instanceof ChannelPlace){
					for(Node n : net.getPreset(pre))
						if(!sync.contains(n))
							err = err + ((ChannelPlace)pre).getErrors();
				}
			}
		}

		for(TransitionNode e : sync){
			for(Node post: net.getPostset(e)){
				if(post instanceof Condition){
					((Condition)post).setErrors(err);
					//set err number for upper conditions
					if(!isLower)
						for(Condition min : bsonAlg.getMinimalPhase(phases.get(post))){
							((Condition) min).setErrors(((Condition) min).getErrors() + ((Condition)post).getErrors());
						}
				}

				if(post instanceof ChannelPlace){
					for(Node n : net.getPostset(post))
						if(!sync.contains(n))
							((ChannelPlace)post).setErrors(err);
				}
			}
		}
	}

	//Backward error tracing
	public void setRevErrNum (Collection<TransitionNode> runList, Collection<Path> syncSet, boolean isBhv){
		while(true){
			boolean b = false;
			Collection<TransitionNode> removeList = new ArrayList<TransitionNode>();
			for(TransitionNode e : runList){
				if(!net.getSONConnectionTypes(e).contains(Semantics.SYNCLINE))
					if(this.getPostAsynEvents(e).isEmpty()
							|| !this.hasCommonElements(runList, this.getPostAsynEvents(e))){
						this.setReverseAsyncErrNum(e, isBhv);
						removeList.add(e);
						b = true;;
						break;
					}
			}
			runList.removeAll(removeList);
			if(!b)
				break;
		}

		while(true){
			boolean b = false;
			Collection<TransitionNode> removeList = new ArrayList<TransitionNode>();

			for(TransitionNode e :runList){
				for(Path cycle : syncSet){
					if(cycle.contains(e)  && !removeList.contains(e)){
						Collection<TransitionNode> runList2 = new ArrayList<TransitionNode>();
						Collection<TransitionNode> eventCycle = new ArrayList<TransitionNode>();

						runList2.addAll(runList);
						runList2.removeAll(cycle);
						boolean hasPostAsyn = false;
						for(Node n: cycle){
							if(n instanceof TransitionNode){
								eventCycle.add((TransitionNode)n);
								if(!this.getPostAsynEvents((TransitionNode)n).isEmpty()
										&& this.hasCommonElements(runList2, this.getPostAsynEvents((TransitionNode)n)))
									hasPostAsyn = true;
							}
						}
						if(!hasPostAsyn){
							this.setReverseSyncErrNum(eventCycle, isBhv);
							removeList.addAll(eventCycle);
							b = true;
							break;
						}
					}
				}
			}
			runList.removeAll(removeList);
			if(!b)
				break;
		}

		if(!runList.isEmpty())
			setRevErrNum(runList, syncSet, isBhv);
	}

	private void setReverseAsyncErrNum(TransitionNode e, boolean isBhv){
		int err = 0;
		if(e.isFaulty())
			err++;
		//get err number from low level conditions and channel places
		for(Node pre : net.getPreset(e)){
			if(pre instanceof Condition)
				err = err + ((Condition)pre).getErrors();
			if(pre instanceof ChannelPlace)
				err = err + ((ChannelPlace)pre).getErrors();
		}

		for(Node post: net.getPostset(e)){
			if(post instanceof Condition){
				//set err number to low level states
				if(!isBhv)
					for(Condition min : bsonAlg.getMinimalPhase(bsonAlg.getPhases((Condition)post))){
						((Condition) min).setErrors(((Condition) min).getErrors() - ((Condition)post).getErrors());
					}
				((Condition)post).setErrors(((Condition)post).getErrors() - err);
			}
			if(post instanceof ChannelPlace){
				((ChannelPlace)post).setErrors(((ChannelPlace)post).getErrors() - err);
			}
		}
	}

	private void setReverseSyncErrNum(Collection<TransitionNode> syncEvents, boolean isBhv){
		int err = 0;
		for(TransitionNode e : syncEvents){
			if(((TransitionNode)e).isFaulty())
				err++;
			for(Node pre : net.getPreset(e)){
				if(pre instanceof Condition){
					err = err + ((Condition)pre).getErrors();
				}
				if(pre instanceof ChannelPlace){
					for(Node n : net.getPreset(pre))
						if(!syncEvents.contains(n))
							err = err + ((ChannelPlace)pre).getErrors();
				}
			}
		}

		for(Node e : syncEvents)
			if(e instanceof TransitionNode){
				for(Node post: net.getPostset(e)){
					if(post instanceof Condition){
						//set err number from high level states
						if(!isBhv)
							for(Condition min : bsonAlg.getMinimalPhase(bsonAlg.getPhases((Condition)post))){
								((Condition) min).setErrors(((Condition) min).getErrors() - ((Condition)post).getErrors());
							}
						((Condition)post).setErrors(((Condition)post).getErrors() - err);
					}
					if(post instanceof ChannelPlace){
						for(Node n : net.getPostset(post))
							if(!syncEvents.contains(n))
								((ChannelPlace)post).setErrors(((ChannelPlace)post).getErrors() - err);
					}
				}
			}
	}
}
