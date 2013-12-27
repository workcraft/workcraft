package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;

public class ErrorTracingAlg extends SimulationAlg{

	private SONModel net;
	private RelationAlg relation;

	public ErrorTracingAlg(SONModel net){
		super(net);
		this.net = net;
		relation =  new RelationAlg(net);

	}

	//Forward error tracing
	public void setErrNum (Collection<Event> runList, Collection<ArrayList<Node>> syncSet, boolean isBhv){
		while(true){
			boolean b = false;
			Collection<Event> removeList = new ArrayList<Event>();
			for(Event e : runList){
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
			Collection<Event> removeList = new ArrayList<Event>();

			for(Event e :runList){
				for(ArrayList<Node> cycle : syncSet){
					if(cycle.contains(e) && !removeList.contains(e)){
						Collection<Event> runList2 = new ArrayList<Event>();
						Collection<Event> eventCycle = new ArrayList<Event>();

						runList2.addAll(runList);
						runList2.removeAll(cycle);
						boolean hasPreAsyn = false;
						for(Node n: cycle){
							if(n instanceof Event){
								eventCycle.add((Event)n);
								if(!this.getPreAsynEvents((Event)n).isEmpty()
										&& this.hasCommonElements(runList2, this.getPreAsynEvents((Event)n)))
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


	private boolean hasCommonElements(Collection<Event> cycle1, Collection<Event> cycle2){
		for(Node n : cycle1)
			if(cycle2.contains(n))
				return true;
		for(Node n : cycle2)
			if(cycle1.contains(n))
				return true;
		return false;
	}

	private void setAsyncErrNum(Event e, boolean isBhv){
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
					for(Condition min : relation.getMinimalPhase(relation.getPhase((Condition)post))){
						((Condition) min).setErrors(((Condition) min).getErrors()+((Condition)post).getErrors());
					}
			}
			if(post instanceof ChannelPlace){
				((ChannelPlace)post).setErrors(err);
			}
		}
	}

	private void setSyncErrNum(Collection<Event> syncEvents, boolean isBhv){
		int err = 0;
		for(Event e : syncEvents){
			if(((Event)e).isFaulty())
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
			if(e instanceof Event){
				for(Node post: net.getPostset(e)){
					if(post instanceof Condition){
						((Condition)post).setErrors(err);
						//set err number from high level states
						if(!isBhv)
							for(Condition min : relation.getMinimalPhase(relation.getPhase((Condition)post))){
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
	public void setReverseErrNum (Collection<Event> runList, Collection<ArrayList<Node>> syncSet, boolean isBhv){
		while(true){
			boolean b = false;
			Collection<Event> removeList = new ArrayList<Event>();
			for(Event e : runList){
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
			Collection<Event> removeList = new ArrayList<Event>();

			for(Event e :runList){
				for(ArrayList<Node> cycle : syncSet){
					if(cycle.contains(e)  && !removeList.contains(e)){
						Collection<Event> runList2 = new ArrayList<Event>();
						Collection<Event> eventCycle = new ArrayList<Event>();

						runList2.addAll(runList);
						runList2.removeAll(cycle);
						boolean hasPostAsyn = false;
						for(Node n: cycle){
							if(n instanceof Event){
								eventCycle.add((Event)n);
								if(!this.getPostAsynEvents((Event)n).isEmpty()
										&& this.hasCommonElements(runList2, this.getPostAsynEvents((Event)n)))
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

	private void setReverseAsyncErrNum(Event e, boolean isBhv){
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
					for(Condition min : relation.getMinimalPhase(relation.getPhase((Condition)post))){
						((Condition) min).setErrors(((Condition) min).getErrors() - ((Condition)post).getErrors());
					}
				((Condition)post).setErrors(((Condition)post).getErrors() - err);
			}
			if(post instanceof ChannelPlace){
				((ChannelPlace)post).setErrors(((ChannelPlace)post).getErrors() - err);
			}
		}
	}

	private void setReverseSyncErrNum(Collection<Event> syncEvents, boolean isBhv){
		int err = 0;
		for(Event e : syncEvents){
			if(((Event)e).isFaulty())
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
			if(e instanceof Event){
				for(Node post: net.getPostset(e)){
					if(post instanceof Condition){
						//set err number from high level states
						if(!isBhv)
							for(Condition min : relation.getMinimalPhase(relation.getPhase((Condition)post))){
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
