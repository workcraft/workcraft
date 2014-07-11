package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.EventNode;

public class ErrorTracingAlg extends RelationAlgorithm{

	private SONModel net;
	private BSONAlg bsonAlg;

	public ErrorTracingAlg(SONModel net){
		super(net);
		bsonAlg = new BSONAlg(net);
		this.net = net;
	}

	//Forward error tracing
	public void setErrNum (Collection<EventNode> runList, Collection<ArrayList<Node>> syncSet, boolean isBhv){
		while(true){
			boolean b = false;
			Collection<EventNode> removeList = new ArrayList<EventNode>();
			for(EventNode e : runList){
				if(!net.getSONConnectionTypes(e).contains("SYNCLINE"))
					if(this.getPreAsynEvents(e).isEmpty() || !this.hasCommonElements(runList, this.getPreAsynEvents(e))){
						this.setAsyncErrNum(e, isBhv);
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
			Collection<EventNode> removeList = new ArrayList<EventNode>();

			for(EventNode e :runList){
				for(ArrayList<Node> cycle : syncSet){
					if(cycle.contains(e) && !removeList.contains(e)){
						Collection<EventNode> runList2 = new ArrayList<EventNode>();
						Collection<EventNode> eventCycle = new ArrayList<EventNode>();

						runList2.addAll(runList);
						runList2.removeAll(cycle);
						boolean hasPreAsyn = false;
						for(Node n: cycle){
							if(n instanceof EventNode){
								eventCycle.add((EventNode)n);
								if(!this.getPreAsynEvents((EventNode)n).isEmpty()
										&& this.hasCommonElements(runList2, this.getPreAsynEvents((EventNode)n)))
									hasPreAsyn = true;
							}
						}
						if(!hasPreAsyn){
							this.setSyncErrNum(eventCycle, isBhv);
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
			setErrNum(runList, syncSet, isBhv);
	}


	private boolean hasCommonElements(Collection<EventNode> cycle1, Collection<EventNode> cycle2){
		for(Node n : cycle1)
			if(cycle2.contains(n))
				return true;
		for(Node n : cycle2)
			if(cycle1.contains(n))
				return true;
		return false;
	}

	private void setAsyncErrNum(EventNode e, boolean isBhv){
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
				((Condition)post).setErrors(err);
				//set err number to low level states
				if(!isBhv)
					for(Condition min : bsonAlg.getMinimalPhase(bsonAlg.getPhase((Condition)post))){
						((Condition) min).setErrors(((Condition) min).getErrors()+((Condition)post).getErrors());
					}
			}
			if(post instanceof ChannelPlace){
				((ChannelPlace)post).setErrors(err);
			}
		}
	}

	private void setSyncErrNum(Collection<EventNode> syncEvents, boolean isBhv){
		int err = 0;
		for(EventNode e : syncEvents){
			if(((EventNode)e).isFaulty())
				err++;
			for(Node pre : net.getPreset(e)){
				if(pre instanceof Condition)
					err = err + ((Condition)pre).getErrors();
				if(pre instanceof ChannelPlace){
					for(Node n : net.getPreset(pre))
						if(!syncEvents.contains(n))
							err = err + ((ChannelPlace)pre).getErrors();
				}
			}
		}

		for(Node e : syncEvents)
			if(e instanceof EventNode){
				for(Node post: net.getPostset(e)){
					if(post instanceof Condition){
						((Condition)post).setErrors(err);
						//set err number from high level states
						if(!isBhv)
							for(Condition min : bsonAlg.getMinimalPhase(bsonAlg.getPhase((Condition)post))){
								((Condition) min).setErrors(((Condition) min).getErrors()+((Condition)post).getErrors());
							}
					}
					if(post instanceof ChannelPlace){
						for(Node n : net.getPostset(post))
							if(!syncEvents.contains(n))
								((ChannelPlace)post).setErrors(err);
					}
				}
			}
	}

	//Backward error tracing
	public void setReverseErrNum (Collection<EventNode> runList, Collection<ArrayList<Node>> syncSet, boolean isBhv){
		while(true){
			boolean b = false;
			Collection<EventNode> removeList = new ArrayList<EventNode>();
			for(EventNode e : runList){
				if(!net.getSONConnectionTypes(e).contains("SYNCLINE"))
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
			Collection<EventNode> removeList = new ArrayList<EventNode>();

			for(EventNode e :runList){
				for(ArrayList<Node> cycle : syncSet){
					if(cycle.contains(e)  && !removeList.contains(e)){
						Collection<EventNode> runList2 = new ArrayList<EventNode>();
						Collection<EventNode> eventCycle = new ArrayList<EventNode>();

						runList2.addAll(runList);
						runList2.removeAll(cycle);
						boolean hasPostAsyn = false;
						for(Node n: cycle){
							if(n instanceof EventNode){
								eventCycle.add((EventNode)n);
								if(!this.getPostAsynEvents((EventNode)n).isEmpty()
										&& this.hasCommonElements(runList2, this.getPostAsynEvents((EventNode)n)))
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
			setReverseErrNum(runList, syncSet, isBhv);
	}

	private void setReverseAsyncErrNum(EventNode e, boolean isBhv){
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
					for(Condition min : bsonAlg.getMinimalPhase(bsonAlg.getPhase((Condition)post))){
						((Condition) min).setErrors(((Condition) min).getErrors() - ((Condition)post).getErrors());
					}
				((Condition)post).setErrors(((Condition)post).getErrors() - err);
			}
			if(post instanceof ChannelPlace){
				((ChannelPlace)post).setErrors(((ChannelPlace)post).getErrors() - err);
			}
		}
	}

	private void setReverseSyncErrNum(Collection<EventNode> syncEvents, boolean isBhv){
		int err = 0;
		for(EventNode e : syncEvents){
			if(((EventNode)e).isFaulty())
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
			if(e instanceof EventNode){
				for(Node post: net.getPostset(e)){
					if(post instanceof Condition){
						//set err number from high level states
						if(!isBhv)
							for(Condition min : bsonAlg.getMinimalPhase(bsonAlg.getPhase((Condition)post))){
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
