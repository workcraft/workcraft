package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;

public class SimulationAlg {

	private SONModel net;

	private List<Event> preEventSet = new ArrayList<Event>();
	private List<Event> postEventSet = new ArrayList<Event>();
	//private Collection<Event> history;
	//private Collection<ArrayList<Event>> pathResult;
	//private Collection<ArrayList<Event>> cycleResult;

	public SimulationAlg(SONModel net){
		this.net = net;
	}

	private List<Event> getPreAsynEvents (Event e){
		List<Event> result = new ArrayList<Event>();
		for(Node node : net.getPreset(e))
			if(node instanceof ChannelPlace)
				for(Node node2 : net.getPreset(node))
					if(node2 instanceof Event)
						result.add((Event)node2);
		return result;
	}

	public List<Event> getPreRelate(Event n){
		if(this.isEnabled(n))
			for(Event node : this.getPreAsynEvents(n)){
				if(preEventSet.contains(node))
					return preEventSet;
				if(this.isEnabled(node))
					preEventSet.add(node);
					getPreRelate(node);
			}
		else{
			return preEventSet;
		}
		return preEventSet;
	}

	private List<Event> getPostAsynEvents (Event e){
		List<Event> result = new ArrayList<Event>();
		for(Node node : net.getPostset(e))
			if(node instanceof ChannelPlace)
				for(Node node2 : net.getPostset(node))
					if(node2 instanceof Event)
						result.add((Event)node2);
		return result;
	}

	public List<Event> getPostRelate(Event n){
		if(this.isEnabled(n))
			for(Event node : this.getPostAsynEvents(n)){
				if( postEventSet.contains(node))
					return  postEventSet;
				if(this.isEnabled(node))
					postEventSet.add(node);
					getPostRelate(node);
			}
		else{
			return postEventSet;
		}
		return  postEventSet;
	}

	public List<Event> getPossibleExeEvents(Event n){
		List<Event> result = new ArrayList<Event>();
		Collection<Event> subResult = new HashSet<Event>();
		List<Event> preRelateEvents = this.getPreRelate(n);

		subResult.addAll(this.getPostRelate(n));
		for(Event e : preRelateEvents)
			subResult.addAll(this.getPostRelate(e));

		for(Event e : subResult)
			if(e !=n && !preRelateEvents.contains(e))
			result.add(e);

		return result;
	}
	/*
	private List<Event[]> createAdj(Collection<Event> nodes){

		List<Event[]> result = new ArrayList<Event[]>();

		for (Event n: nodes){
			for (Event next: this.getPostAsynEvents(n))
				if(nodes.contains(next)){
					Event[] adjoin = new Event[2];
					adjoin[0] = n;
					adjoin[1] = next;
					result.add(adjoin);
				}
		}
		return result;
	}

	private void getAllPath(Event start, Event end, List<Event[]> adj){

		history.add(start);

		for (int i=0; i< adj.size(); i++){
			if (((Event)adj.get(i)[0]).equals(start)){
				if(((Event)adj.get(i)[1]).equals(end)){
					ArrayList<Event> path= new ArrayList<Event>();

					path.addAll(history);
					path.add(end);
					pathResult.add(path);
					continue;
				}
				if(!history.contains((Event)adj.get(i)[1])){
					getAllPath((Event)adj.get(i)[1], end, adj);
				}
				else {
					ArrayList<Event> cycle=new ArrayList<Event>();

						cycle.addAll(history);
						int n=cycle.indexOf(((Event)adj.get(i)[1]));
						for (int m = 0; m < n; m++ ){
							cycle.remove(0);
						}
						cycleResult.add(cycle);
				}
			}
		}
		history.remove(start);
	}

	private Collection<ArrayList<Event>> getPostPath(Event start, List<Event> list){
		Collection<ArrayList<Event>> result = new HashSet<ArrayList<Event>>();
		ArrayList<Event> endSet = new ArrayList<Event>();
		ArrayList<Event> eventSet = new ArrayList<Event>();

		List<Event[]> adj = this.createAdj(list);

		for(Event e : list){
			eventSet.add(e);
			if(this.getPostAsynEvents(e).isEmpty()){
				endSet.add(e);
			}
			if(!list.containsAll(this.getPostAsynEvents(e))){
				endSet.add(e);
			}
		}

		for(Event end : endSet){
			getAllPath(start, end, adj);
		}

		if(endSet.isEmpty()){
			result.add(eventSet);
			return result;
		}

		return pathResult;
	}


	public  Collection<ArrayList<Event>> getPostRelate(Event n){
		history = new ArrayList<Event>();
		pathResult =new  HashSet<ArrayList<Event>>();
		cycleResult = new HashSet<ArrayList<Event>>();
		List<Event> postSet = new ArrayList<Event>();
		postSet.add(n);
		postSet.addAll(this.getPostSet(n));
		return this.getPostPath(n, postSet);
	}*/

	public void clearEventSet(){
			preEventSet.clear();
			postEventSet.clear();
			//this.cycleResult.clear();
			//this.history.clear();
			//this.pathResult.clear();
	}

	// enable ,fire
	final public boolean isEnabled (SONModel net, Event e){
		if(isPolyEnabled(e) && isSyncEnabled(e))
			return true;
		return false;
	}

	private boolean isPolyEnabled (Event e) {
		// gather number of connections for each pre-place
		Map<Node, Integer> map = new HashMap<Node, Integer>();
		for (SONConnection c: net.getSONConnections(e)) {
			if (c.getType() == "POLYLINE" && c.getSecond()==e) {
				if (map.containsKey(c.getFirst())) {
					map.put((Condition)c.getFirst(), map.get(c.getFirst())+1);
				} else {
					map.put((Condition)c.getFirst(), 1);
				}
			}
		}
		for (Node n : net.getPreset(e)){
			if(n instanceof Condition)
				if (((Condition)n).getTokens() < map.get((Condition)n))
					return false;
			}
		return true;
	}

	private boolean isSyncEnabled(Event e) throws StackOverflowError{
		Map<Node, Integer> map = new HashMap<Node, Integer>();

		for (SONConnection c: net.getSONConnections(e)) {
			if (c.getType() == "ASYNLINE" && c.getSecond()==e){
				if (map.containsKey(c.getFirst())) {
						map.put((ChannelPlace)c.getFirst(), map.get(c.getFirst())+1);
					} else {
						map.put((ChannelPlace)c.getFirst(), 1);
					}
				}
			}
		try{
		for (Node n : net.getPreset(e)){
			if(n instanceof ChannelPlace)
				if (((ChannelPlace)n).getTokens() < map.get((ChannelPlace)n))
					for(Node node : net.getPreset(n))
						if(node instanceof Event)
							if(!this.isEnabled((Event)node))
								return false;
			}
		}catch (StackOverflowError exception){
			System.err.println("cycle path!!");
		}
		return true;
	}

	final public boolean isEnabled (Event e) {
		return isEnabled (net, e);
	}

	final public boolean isUnfireEnabled (SONModel net, Event e) {
		// gather number of connections for each post-place
		Map<Place, Integer> map = new HashMap<Place, Integer>();

		for (SONConnection c: net.getSONConnections(e)) {
			if (c.getType() == "POLYLINE" && c.getFirst()==e) {
				if (map.containsKey(c.getSecond())) {
					map.put((Condition)c.getSecond(), map.get(c.getSecond())+1);
				} else {
					map.put((Condition)c.getSecond(), 1);
				}
			}
		}

		for (Node n : net.getPostset(e)){
			if(n instanceof Condition)
				if (((Condition)n).getTokens() < map.get((Condition)n))
					return false;
		}
		return true;
	}

	public boolean isUnfireEnabled(Event e) {
		return isUnfireEnabled (net, e);
	}

	public void fire (Event e, String type) {
		if (isEnabled(e))
		{
			for (SONConnection c : net.getSONConnections(e)) {
				if (c.getType() == "POLYLINE" && e==c.getFirst()) {
					if(type=="a" || type=="s"){
					Condition to = (Condition)c.getSecond();
					to.setTokens(((Condition)to).getTokens()+1);
					}
				}
				if (c.getType() == "POLYLINE" && e==c.getSecond()) {
					if(type=="a" || type=="s"){
					Condition from = (Condition)c.getFirst();
					from.setTokens(((Condition)from).getTokens()-1);
					}
				}
				if (c.getType() == "ASYNLINE" && e==c.getFirst()){
					if(type=="a"){
						ChannelPlace to = (ChannelPlace)c.getSecond();
						to.setTokens(((ChannelPlace)to).getTokens()+1);
					}
				}
				if (c.getType() == "ASYNLINE" && e==c.getSecond()){
					if(type=="a"){
						ChannelPlace from = (ChannelPlace)c.getFirst();
						if(from.getTokens() > 0)
							from.setTokens(((ChannelPlace)from).getTokens()-1);
					}
				}
			}
		}
	}

	public void fire(Collection<Event> events, String type){
			for(Event e : events){
				for (SONConnection c : net.getSONConnections(e)) {
					if (c.getType() == "POLYLINE" && e==c.getFirst()) {
						if(type=="a" || type=="s"){
						Condition to = (Condition)c.getSecond();
						to.setTokens(((Condition)to).getTokens()+1);
						}
					}
					if (c.getType() == "POLYLINE" && e==c.getSecond()) {
						if(type=="a" || type=="s"){
						Condition from = (Condition)c.getFirst();
						from.setTokens(((Condition)from).getTokens()-1);
						}
					}
					if (c.getType() == "ASYNLINE" && e==c.getFirst()){
						if(type=="s"){
							ChannelPlace to = (ChannelPlace)c.getSecond();
							if(events.containsAll(net.getPostset(to)) && events.containsAll(net.getPreset(to)))
								to.setTokens(((ChannelPlace)to).getTokens());
							else
								to.setTokens(((ChannelPlace)to).getTokens()+1);
						}
					}
					if (c.getType() == "ASYNLINE" && e==c.getSecond()){
						if(type=="s"){
							ChannelPlace from = (ChannelPlace)c.getFirst();
							if(events.containsAll(net.getPostset(from)) && events.containsAll(net.getPreset(from)))
								from.setTokens(((ChannelPlace)from).getTokens());
							else
								if(from.getTokens() > 0)
								from.setTokens(((ChannelPlace)from).getTokens()-1);
						}
					}
				}
			}
		}

	final public void unFire(Event t) {
		unFire(net, t);
	}

	final public static void unFire(SONModel net, Event e) {
		// the opposite action to fire, no additional checks,
		// the transition given must be correct
		// for the transition to be enabled

		for (SONConnection c : net.getSONConnections(e)) {
			if (c.getType() == "POLYLINE" && e==c.getFirst()) {
				Condition to = (Condition)c.getSecond();
				to.setTokens(((Place)to).getTokens()-1);
			}
			if (c.getType() == "POLYLINE" && e==c.getSecond()) {
				Condition from = (Condition)c.getFirst();
				from.setTokens(((Condition)from).getTokens()+1);
			}
		}
	}

}
