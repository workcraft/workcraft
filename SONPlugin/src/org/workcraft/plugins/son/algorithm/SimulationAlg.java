package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.UnboundedException;

public class SimulationAlg extends RelationAlgorithm {

	private SON net;
	private BSONAlg bsonAlg;

	private Collection<ONGroup> upperGroups;
	private Collection<ONGroup> lowerGroups;


	public SimulationAlg(SON net){
		super(net);
		this.net = net;
		bsonAlg = new BSONAlg(net);

		upperGroups = bsonAlg.getUpperGroups(net.getGroups());
		lowerGroups = bsonAlg.getLowerGroups(net.getGroups());
	}

	//get SON initial marking
	public Map<PlaceNode, Boolean> getInitialMarking(){
		HashMap<PlaceNode, Boolean> result = new HashMap<PlaceNode, Boolean>();
		Collection<ONGroup> upperGroups = bsonAlg.getUpperGroups(net.getGroups());
		Collection<ONGroup> lowerGroups = bsonAlg.getLowerGroups(net.getGroups());

		for(PlaceNode c : net.getPlaceNodes())
			result.put(c, false);

		for(ONGroup group : net.getGroups()){
			if(upperGroups.contains(group))
				for(Condition c : getInitial(group.getConditions())){
					result.put(c, true);
				}
			//an initial state of a lower group is the initial state of SON
			//if all of its upper conditions are the initial states.
			else if(lowerGroups.contains(group)){
				for(Condition c : getInitial(group.getConditions())){
					boolean isInitial = true;
					Collection<Condition> set = bsonAlg.getUpperConditions(c);
					for(Condition c2 : set){
						if(!isInitial(c2)){
							ONGroup group2 = net.getGroup(c2);
							if(!set.containsAll(getInitial(group2.getConditions())))
								isInitial = false;
						}
					}
					if(isInitial)
						result.put(c, true);
				}
			}
			else{
				for(Condition c : getInitial(group.getConditions())){
					result.put(c, true);
				}
			}
		}
		return result;
	}

	//get SON final marking
	public Map<PlaceNode, Boolean> getFinalMarking(){
		HashMap<PlaceNode, Boolean> result = new HashMap<PlaceNode, Boolean>();
		Collection<ONGroup> upperGroups = bsonAlg.getUpperGroups(net.getGroups());
		Collection<ONGroup> lowerGroups = bsonAlg.getLowerGroups(net.getGroups());

		for(PlaceNode c : net.getPlaceNodes())
			result.put(c, false);

		for(ONGroup group : net.getGroups()){
			if(upperGroups.contains(group))
				for(Condition c : getFinal(group.getConditions())){
					result.put(c, true);
				}

			else if(lowerGroups.contains(group)){
				for(Condition c : getFinal(group.getConditions())){
					boolean isFinal = true;
					Collection<Condition> set = bsonAlg.getUpperConditions(c);
					for(Condition c2 : set){
						if(!isInitial(c2)){
							ONGroup group2 = net.getGroup(c2);
							if(!set.containsAll(getFinal(group2.getConditions())))
								isFinal = false;
						}
					}
					if(isFinal)
						result.put(c, true);
				}
			}
			else{
				for(Condition c : getFinal(group.getConditions())){
					result.put(c, true);
				}
			}
		}
		return result;
	}

	/**
	 * return minimal execution set for a given node.
	 * contain other nodes which have synchronous and PRE- relation with the selected one.
	 */
    public List<TransitionNode> getMinFire(TransitionNode e, Collection<Path> sync, Collection<TransitionNode> fireList, boolean isRev){
        List<TransitionNode> result = null;
        if(!isRev){
        	result = getForwordMinFire(e, sync, fireList);
        }else{
        	result = getRevMinFire(e, sync, fireList);
        }

        return result;
    }

    private List<TransitionNode> getForwordMinFire(TransitionNode e, Collection<Path> sync, Collection<TransitionNode> fireList){
        List<TransitionNode> result = new ArrayList<TransitionNode>();
        Collection<TransitionNode> u = new ArrayList<TransitionNode>();
        Stack<TransitionNode> stack = new Stack<TransitionNode>();
        u.addAll(fireList);

        if(e!= null){
            stack.push(e);
            while(!stack.empty()){
	            e = stack.pop();
	            if(!result.contains(e)){
                    result.add(e);
                    u.remove(e);
	            }
	            //event in sync cycle belongs to the result
	            for(Path cycle : sync){
                    if(cycle.contains(e))
                        for(Node e2 : cycle){
                            if(e2 instanceof TransitionNode && u.contains(e2)){
                                u.remove(e2);
                                stack.push((TransitionNode)e2);
                            }
                            else if(!fireList.contains(e2)){
                            	throw new RuntimeException
                            	("algorithm error: unenabled event in sync cycle"+net.getNodeReference(e2));
                            }
                        }
	            }
	            //event which is the preset w.r.t weak causality, of selected event belongs to the result.
	            if(!getPreAsynEvents(e).isEmpty()){
                    for(TransitionNode e3 : getPreAsynEvents(e)){
                        if(u.contains(e3)){
                            u.remove(e3);
                            stack.push((TransitionNode)e3);
                        }
                    }
	            }
            }
        }
        return result;
    }

    private List<TransitionNode> getRevMinFire(TransitionNode e, Collection<Path> sync, Collection<TransitionNode> fireList){
        List<TransitionNode> result = new ArrayList<TransitionNode>();
        Collection<TransitionNode> u = new ArrayList<TransitionNode>();
        Stack<TransitionNode> stack = new Stack<TransitionNode>();
        u.addAll(fireList);

        if(e!= null){
            stack.push(e);
            while(!stack.empty()){
	            e = stack.pop();
	            if(!result.contains(e)){
                    result.add(e);
                    u.remove(e);
	            }

	            for(Path cycle : sync){
                    if(cycle.contains(e))
                        for(Node e2 : cycle){
                            if(e2 instanceof TransitionNode && u.contains(e2)){
                                u.remove(e2);
                                stack.push((TransitionNode)e2);
                            }
                            else if(!fireList.contains(e2)){
                            	throw new RuntimeException
                            	("algorithm error: unenabled event in sync cycle"+net.getNodeReference(e2));
                            }
                        }
	            }
	            if(!getPostAsynEvents(e).isEmpty()){
                    for(TransitionNode e3 : getPostAsynEvents(e)){
                        if(u.contains(e3)){
                            u.remove(e3);
                            stack.push((TransitionNode)e3);
                        }
                    }
	            }
            }
        }
        return result;
    }

	final public List<TransitionNode> getEnabledNodes(Collection<Path> sync, Map<Condition, Collection<Phase>> phases, boolean isRev){
		List<TransitionNode> result = null;
		if(!isRev)
			result = getEnabled(sync, phases);
		else
			result = getRevEnabled(sync, phases);

		return result;
	}


	private boolean isONEnabled (TransitionNode e) {
		if(net.getPreset(e).isEmpty())
			return false;

		for (Node n : net.getPreset(e)){
			if(n instanceof Condition)
				if (!((Condition)n).isMarked())
					return false;
			}

		return true;
	}

	private boolean isBSONEnabled(TransitionNode e, Map<Condition, Collection<Phase>> phases){
		//if e is upper event, e is BSON enabled if every condition in the maximal phases of e is marked
		for(ONGroup group : upperGroups){
			if(group.getComponents().contains(e)){
				for(Node pre : getPrePNSet(e)){
					Condition c = (Condition)pre;
					Collection<Phase> phase = phases.get(c);
					Collection<Condition> max = bsonAlg.getMaximalPhase(phase);
					for(Condition c2 : max)
						if(!c2.isMarked())
							return false;
				}
			return true;
			}
		}

		//if e is lower event, e is BSON enabled if every e's upper condition is marked
		for(ONGroup group : lowerGroups){
			if(group.getComponents().contains(e)){
				for(Condition c : bsonAlg.getUpperConditions(e))
					if(!c.isMarked())
						return false;
			}
		}
		return true;
	}


	private List<TransitionNode> getEnabled(Collection<Path> sync, Map<Condition, Collection<Phase>> phases){
		List<TransitionNode> result = new ArrayList<TransitionNode>();
		Collection<Node> del = new HashSet<Node>();
		Stack<TransitionNode> stack = new Stack<TransitionNode>();

		//ON and BSON enabled
		for(TransitionNode e : net.getTransitionNodes()){
			if(isONEnabled(e) && isBSONEnabled(e, phases)){
				result.add(e);
			}
		}

		//Sync enabled
        for(Path cycle : sync){
    		for(Node n : cycle){
    			if(n instanceof TransitionNode && !result.contains(n)){
					del.addAll(cycle);
					break;
    			}
    		}
        }

        //Aync enabled
		for(TransitionNode e : result){
	        LinkedList<Node> visit = new LinkedList<Node>();
	        stack.push(e);

	        while(!stack.isEmpty()){
	        	e = stack.peek();
	            visit.add(e);

	            TransitionNode e2 = null;
				for(Node pre : net.getPreset(e)){
					if(pre instanceof ChannelPlace){
						if(!((ChannelPlace)pre).isMarked()){
							for(Node pre2 : net.getPreset(pre)){
	            				if(visit.contains(pre2)){
	                                continue;
	            				}
	            				else if(!result.contains(pre2) || del.contains(pre2)){
	                	            visit.add(e2);
	                	            del.addAll(visit);
	                                visit.removeLast();
	                                break;
	            				}
	            				else if(!visit.contains(pre2)){
	                            	e2 = (TransitionNode)pre2;
	                            	stack.push(e2);
	                            }
							}
						}
					}
				}
	        	if(e2 == null){
	    			while(!stack.isEmpty()){
	    				e = stack.peek();
	    				if(!visit.isEmpty() && e==visit.peekLast()){
	    					stack.pop();
	    					visit.removeLast();
	    				}else{
	    					break;
	    				}
	    			}
	    		}
            }
		}

		result.removeAll(del);
		return result;
	}


	//reverse simulation
	private boolean isRevONEnabled (TransitionNode e) {
		if(net.getPostset	(e).isEmpty())
			return false;

		for (Node n : net.getPostset(e)){
			if(n instanceof Condition)
				if (!((Condition)n).isMarked())
					return false;
			}

		return true;
	}

	private boolean isRevBSONEnabled(TransitionNode e, Map<Condition, Collection<Phase>> phases){
		//if e is upper event, e is BSON unfire enabled if every condition in the minimal phases of e is marked
		for(ONGroup group : upperGroups){
			if(group.getComponents().contains(e)){
				for(Node post : getPostPNSet(e)){
					Condition c = (Condition)post;
					Collection<Phase> phase = phases.get(c);
					Collection<Condition> min = bsonAlg.getMinimalPhase(phase);
					for(Condition c2 : min)
						if(!c2.isMarked())
							return false;
				}
			return true;
			}
		}

		//if e is lower event, e is BSON enabled if every e's upper condition is marked
		for(ONGroup group : lowerGroups){
			if(group.getComponents().contains(e)){
				for(Condition c : bsonAlg.getUpperConditions(e))
					if(!c.isMarked())
						return false;
			}
		}
		return true;
	}

	private List<TransitionNode> getRevEnabled(Collection<Path> sync, Map<Condition, Collection<Phase>> phases){
		List<TransitionNode> result = new ArrayList<TransitionNode>();
		Collection<Node> del = new HashSet<Node>();
		Stack<TransitionNode> stack = new Stack<TransitionNode>();

		//ON and BSON enabled
		for(TransitionNode e : net.getTransitionNodes()){
			if(isRevONEnabled(e) && isRevBSONEnabled(e, phases)){
				result.add(e);
			}
		}

		//Sync enabled
        for(Path cycle : sync){
    		for(Node n : cycle){
    			if(n instanceof TransitionNode && !result.contains(n)){
					del.addAll(cycle);
					break;
    			}
    		}
        }

        //Aync enabled
		for(TransitionNode e : result){
	        LinkedList<Node> visit = new LinkedList<Node>();
	        stack.push(e);

	        while(!stack.isEmpty()){
	        	e = stack.peek();
	            visit.add(e);

	            TransitionNode e2 = null;
				for(Node post : net.getPostset(e)){
					if(post instanceof ChannelPlace){
						if(!((ChannelPlace)post).isMarked()){
							for(Node post2 : net.getPostset(post)){
	            				if(visit.contains(post2)){
	                                continue;
	            				}
	            				else if(!result.contains(post2) || del.contains(post2)){
//	                            	e2 = (TransitionNode)pre2;
//	                            	stack.push(e2);
	                	            visit.add(e2);
	                	            del.addAll(visit);
	                                visit.removeLast();
	                                break;
	            				}
	            				else if(!visit.contains(post2)){
	                            	e2 = (TransitionNode)post2;
	                            	stack.push(e2);
	                            }
							}
						}
					}
				}
	        	if(e2 == null){
	    			while(!stack.isEmpty()){
	    				e = stack.peek();
	    				if(!visit.isEmpty() && e==visit.peekLast()){
	    					stack.pop();
	    					visit.removeLast();
	    				}else{
	    					break;
	    				}
	    			}
	    		}
            }
		}

		result.removeAll(del);
		return result;
	}

	public void setMarking(Collection<TransitionNode> step, Map<Condition, Collection<Phase>> phases, boolean isRev) throws UnboundedException {
		if(!isRev)
			fire(step, phases);
		else
			revFire(step, phases);
	}

	/**
	 * token setting after forward fire.
	 * @throws UnboundedException
	 */
	private void fire(Collection<TransitionNode> step, Map<Condition, Collection<Phase>> phases) throws UnboundedException{

		//marking for ON and CSON
		for(TransitionNode e : step){
			for(Node post : net.getPostset(e)){
				if((post instanceof PlaceNode) && net.getSONConnectionType(e, post) != Semantics.SYNCLINE)
					if(((PlaceNode)post).isMarked())
						throw new UnboundedException(net.getNodeReference(post));
					else
						((PlaceNode)post).setMarked(true);
			}
		}

		for(TransitionNode e : step){
			for(Node pre : net.getPreset(e)){
				if((pre instanceof PlaceNode) && net.getSONConnectionType(e, pre) != Semantics.SYNCLINE)
					((PlaceNode)pre).setMarked(false);
			}
		}

		for(TransitionNode e : step){
			//marking for BSON
			for(Node pre : net.getPreset(e)){
				//if e is upper event, remove marking for maximal phase of pre{e}.
				if(bsonAlg.isUpperCondition(pre)){
					Condition c = (Condition)pre;
					Collection<Condition> maxSet = bsonAlg.getMaximalPhase(phases.get(c));
					//backward checking for all upper conditions, if there has no marked condition, remvoe the token
					boolean hasMarking = false;
					for(Condition c2 : maxSet){
						for(Condition c3 : bsonAlg.getUpperConditions(c2)){
							if(c3.isMarked())
								hasMarking = true;
						}
						if(!hasMarking)
							c2.setMarked(false);
					}
				}
			}

			for(Node post : net.getPostset(e)){
				//if e is upper event, set marking for every minimal phase of post{e}.
				if(bsonAlg.isUpperCondition(post)){
					Condition c = (Condition)post;
					Collection<Condition> minSet = bsonAlg.getMinimalPhase(phases.get(c));
					for(Condition c2 : minSet)
						if(!c2.isMarked())
							c2.setMarked(true);
				}
			}
		}
	}

	/**
	 * token setting after reverse fire.
	 * @throws UnboundedException
	 */
	private void revFire(Collection<TransitionNode> step, Map<Condition, Collection<Phase>> phases) throws UnboundedException{

		//marking for ON and CSON
		for(TransitionNode e : step){
			for(Node pre : net.getPreset(e)){
				if((pre instanceof PlaceNode) && net.getSONConnectionType(e, pre) != Semantics.SYNCLINE)
					if(((PlaceNode)pre).isMarked())
						throw new UnboundedException(net.getNodeReference(pre));
					else
						((PlaceNode)pre).setMarked(true);
			}
		}

		for(TransitionNode e : step){
			for(Node post : net.getPostset(e)){
				if((post instanceof PlaceNode) && net.getSONConnectionType(e, post) != Semantics.SYNCLINE)
					((PlaceNode)post).setMarked(false);
			}
		}

		for(TransitionNode e : step){
			//marking for BSON
			for(Node post : net.getPostset(e)){
				//if e is upper event, remove marking for maximal phase of pre{e}.
				if(bsonAlg.isUpperCondition(post)){
					Condition c = (Condition)post;
					Collection<Condition> minSet = bsonAlg.getMinimalPhase(phases.get(c));
					//backward checking for all upper conditions, if there has no marked condition, remvoe the token
					boolean hasMarking = false;
					for(Condition c2 : minSet){
						for(Condition c3 : bsonAlg.getUpperConditions(c2)){
							if(c3.isMarked())
								hasMarking = true;
						}
						if(!hasMarking)
							c2.setMarked(false);
					}
				}
			}

			for(Node pre : net.getPreset(e)){
				//if e is upper event, set marking for every minimal phase of post{e}.
				if(bsonAlg.isUpperCondition(pre)){
					Condition c = (Condition)pre;
					Collection<Condition> maxSet = bsonAlg.getMaximalPhase(phases.get(c));
					for(Condition c2 : maxSet)
						if(!c2.isMarked())
							c2.setMarked(true);
				}
			}
		}
	}
}
