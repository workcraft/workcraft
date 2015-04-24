package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
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
import org.workcraft.plugins.son.exception.InvalidStructureException;

public class SimulationAlg extends RelationAlgorithm {

	private SON net;
	private BSONAlg bsonAlg;

	private Collection<ONGroup> upperGroups;
	private Collection<ONGroup> lowerGroups;


	public SimulationAlg(SON net){
		super(net);
		this.net = net;
		bsonAlg = new BSONAlg(net);

		upperGroups = bsonAlg.getAbstractGroups(net.getGroups());
		lowerGroups = bsonAlg.getBhvGroups(net.getGroups());
	}

    public List<TransitionNode> getMinFire(TransitionNode e, Collection<Path> sync, Collection<TransitionNode> fireList, boolean isRev){
        List<TransitionNode> result = null;
        if(!isRev){
        	result = getForwordMinFire(e, sync, fireList);
        }else{
        	result = getRevMinFire(e, sync, fireList);
        }

        return result;
    }

	/**
	 * return minimal execution set for a given node.
	 * contain other nodes which have synchronous and PRE- relation with the selected one.
	 */
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

	/**
	 * return maximal execution set for a given node.
	 * contain other nodes which have synchronous and POST- relation with the selected one.
	 */
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
				for(Condition c : bsonAlg.getAbstractConditions(e))
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
//	                            	e2 = (TransitionNode)pre2;
//	                            	stack.push(e2);
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

	private boolean isONRevEnabled (TransitionNode e) {
		if(net.getPostset	(e).isEmpty())
			return false;

		for (Node n : net.getPostset(e)){
			if(n instanceof Condition)
				if (!((Condition)n).isMarked())
					return false;
			}

		return true;
	}

	private boolean isBSONRevEnabled(TransitionNode e, Map<Condition, Collection<Phase>> phases){
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
				for(Condition c : bsonAlg.getAbstractConditions(e))
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
			if(isONRevEnabled(e) && isBSONRevEnabled(e, phases)){
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

	public void setMarking(Collection<TransitionNode> step, Map<Condition, Collection<Phase>> phases, boolean isRev) throws InvalidStructureException{
		if(!isRev)
			fire(step, phases);
		else
			revFire(step, phases);
	}

	/**
	 * token setting after fire.
	 */
	private void fire(Collection<TransitionNode> step, Map<Condition, Collection<Phase>> phases) throws InvalidStructureException{
		//rough checking
		for(TransitionNode e : step){
			for(Node post : net.getPostset(e)){
				if(post instanceof PlaceNode)
					if(((PlaceNode)post).isMarked())
						throw new InvalidStructureException("Token amount > 1: "+net.getNodeReference(post));
			}
		}

		for(TransitionNode e : step){
			for(Node pre : net.getPreset(e)){
				//if e is upper event, remove marking for every maximal phase of pre{e}.
				if(bsonAlg.isAbstractCondition(pre)){
					Condition c = (Condition)pre;
					Collection<Condition> maxSet = bsonAlg.getMaximalPhase(phases.get(c));
					for(Condition c2 : maxSet)
						c2.setMarked(false);
				}
			}
			for(Node post : net.getPostset(e)){
				//set marking for each post node of step U
				if((post instanceof PlaceNode) && net.getSONConnectionType(e, post) != Semantics.SYNCLINE)
					((PlaceNode)post).setMarked(true);
				//if e is upper event, set marking for every minimal phase of post{e}.
				if(bsonAlg.isAbstractCondition(post)){
					Condition c = (Condition)post;
					Collection<Condition> minSet = bsonAlg.getMinimalPhase(phases.get(c));
					for(Condition c2 : minSet)
						c2.setMarked(true);
				}
			}
		}

		for(TransitionNode e : step){
			//remove marking for each post node of step U
			for(Node pre : net.getPreset(e)){
				if((pre instanceof PlaceNode) && net.getSONConnectionType(e, pre) != Semantics.SYNCLINE)
					((PlaceNode)pre).setMarked(false);
			}
		}
	}


	/**
	 * token setting after reverse fire.
	 */
	private void revFire(Collection<TransitionNode> step, Map<Condition, Collection<Phase>> phases) throws InvalidStructureException{
		//rough checking
		for(TransitionNode e : step){
			for(Node pre : net.getPreset(e)){
				if(pre instanceof PlaceNode)
					if(((PlaceNode)pre).isMarked())
						throw new InvalidStructureException("Token amount > 1: "+net.getNodeReference(pre));
			}
		}

		for(TransitionNode e : step){
			for(Node post : net.getPostset(e)){
				//if e is upper event, remove marking for every minimal phase of pre{e}.
				if(bsonAlg.isAbstractCondition(post)){
					Condition c = (Condition)post;
					Collection<Condition> minSet = bsonAlg.getMinimalPhase(phases.get(c));
					for(Condition c2 : minSet)
						c2.setMarked(false);
				}
			}
			for(Node pre : net.getPreset(e)){
				//set marking for each post node of step U
				if((pre instanceof PlaceNode) && net.getSONConnectionType(e, pre) != Semantics.SYNCLINE)
					((PlaceNode)pre).setMarked(true);
				//if e is upper event, set marking for every minimal phase of post{e}.
				if(bsonAlg.isAbstractCondition(pre)){
					Condition c = (Condition)pre;
					Collection<Condition> maxSet = bsonAlg.getMaximalPhase(phases.get(c));
					for(Condition c2 : maxSet)
						c2.setMarked(true);
				}
			}
		}

		for(TransitionNode e : step){
			//remove marking for each post node of step U
			for(Node post : net.getPostset(e)){
				if((post instanceof PlaceNode) && net.getSONConnectionType(e, post) != Semantics.SYNCLINE)
					((PlaceNode)post).setMarked(false);
			}
		}
	}
}
