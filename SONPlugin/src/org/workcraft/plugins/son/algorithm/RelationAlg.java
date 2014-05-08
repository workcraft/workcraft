package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;

public class RelationAlg{

	private SONModel net;

	public RelationAlg(SONModel net) {
		this.net = net;
	}

	//Occurrence net relation

	public boolean hasPostConflictEvents(Node c){
		if (c instanceof Condition){
			if(net.getPostset(c).size() > 1){
				int polyTypeCount = 0;
				for (SONConnection con : net.getOutputSONConnections(c)){
					if (con.getType() == "POLYLINE")
						polyTypeCount++;
				}
				if(polyTypeCount > 1){
					int n = 0;
						for (Node post : net.getPostset(c))
							if (post instanceof Event)
								n++;
							if (n > 1)
								return true;
				}
			}
		}
		return false;
	}

	public boolean hasPreConflictEvents(Node c){
		if (c instanceof Condition){
			if(net.getPreset(c).size() > 1){
				int polyTypeCount = 0;
				for (SONConnection con : net.getInputSONConnections(c)){
					if (con.getType() == "POLYLINE")
						polyTypeCount++;
				}
				if(polyTypeCount > 1){
					int n = 0;
					for (Node pre : net.getPreset(c))
						if (pre instanceof Event)
								n++;
						if (n > 1)
							return true;
					}
				}
		}
	return false;
	}

	public boolean isInitial(Node n){
		boolean conType = true;

		if(net.getPreset(n).size() == 0)
			return true;
		else{
			if (n instanceof Condition){
				for(SONConnection con : net.getInputSONConnections(n)){
					if (con.getType() == "POLYLINE")
						conType = false;
				}
				if(conType)
					return true;
			}
		}

		return false;
	}

	public boolean isFinal(Node n){
		boolean conType = true;

		if(net.getPostset(n).size() == 0)
			return true;
		else{
			if (n instanceof Condition){
				for(SONConnection con : net.getOutputSONConnections(n)){
					if (con.getType() == "POLYLINE")
						conType = false;
				}
				if(conType)
					return true;
			}
		}

		return false;
	}

	public boolean hasInitial(Collection<Node> nodes){
		boolean result = false;

		for(Node node : nodes)
			if (isInitial(node))
				result = true;
		return result;
	}

	public boolean hasFinal(Collection<Node> nodes){
		boolean result = false;

		for(Node node : nodes)
			if (isFinal(node))
				result = true;
		return result;
	}

	public Collection<Node> getInitial(Collection<Node> nodes){
		ArrayList<Node> result =  new ArrayList<Node>();
		for (Node node : nodes)
			if (isInitial(node))
				result.add(node);
		return result;
	}

	public Collection<Node> getFinal(Collection<Node> nodes){
		ArrayList<Node> result =  new ArrayList<Node>();
		for (Node node : nodes)
			if (isFinal(node))
				result.add(node);
		return result;
	}

	//Communication SON relation

	public Collection<ChannelPlace> getRelatedChannelPlace(Collection<ONGroup> groups){
		HashSet<ChannelPlace> result = new HashSet<ChannelPlace>();

		for(ChannelPlace cPlace : net.getChannelPlace())
			for (ONGroup group : groups){
				for (Node node : net.getPostset(cPlace)){
					if (group.contains(node))
						result.add(cPlace);
				}
				for (Node node : net.getPreset(cPlace)){
					if (group.contains(node))
						result.add(cPlace);
				}
			}

		return result;
	}


	public Collection<Condition> getPrePNCondition(Condition c){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Node n : net.getPreset(c))
			if(n instanceof Event && net.getSONConnectionType(c, n) == "POLYLINE")
				for(Node n2 : net.getPreset(n))
					if(n2 instanceof Condition && net.getSONConnectionType(n, n2)== "POLYLINE")
						result.add((Condition)n2);

		return result;
	}

	public Collection<Condition> getPostPNCondition(Condition c){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Node n : net.getPostset(c))
			if(n instanceof Event && net.getSONConnectionType(c, n)=="POLYLINE")
				for(Node n2 : net.getPostset(n))
					if(n2 instanceof Condition && net.getSONConnectionType(n, n2) == "POLYLINE")
						result.add((Condition)n2);

		return result;
	}

	public Collection<Condition> getPREset(Event e){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Node n : net.getPreset(e)){
			if(n instanceof Condition)
				result.add((Condition)n);
			if(n instanceof ChannelPlace && net.getSONConnectionType(e, n).contains("ASYNLINE"))
				for(Node pre : net.getPreset(n))
					for(Node preCondition : net.getPreset(pre))
						if(preCondition instanceof Condition)
							result.add((Condition)preCondition);
			if(n instanceof ChannelPlace && net.getSONConnectionType(e, n).contains("SYNCLINE"))
				for(Node pre : net.getPreset(n))
					for(Node preCondition : net.getPreset(pre))
						if(preCondition instanceof Condition)
							result.add((Condition)preCondition);
		}

		for(Node n : net.getPostset(e)){
			if(n instanceof ChannelPlace && net.getSONConnectionType(e, n).contains("SYNCLINE"))
				for(Node post : net.getPostset(n))
					for(Node preCondition : net.getPreset(post))
						if(preCondition instanceof Condition)
							result.add((Condition)preCondition);
		}

		return result;
	}

	public Collection<Condition> getPOSTset(Event e){
		Collection<Condition> result = new ArrayList<Condition>();

		for(Node n : net.getPostset(e)){
			if(n instanceof Condition)
				result.add((Condition)n);
			if(n instanceof ChannelPlace && net.getSONConnectionType(e, n).contains("ASYNLINE"))
				for(Node post : net.getPostset(n))
					for(Node postCondition : net.getPostset(post))
						if(postCondition instanceof Condition)
							result.add((Condition)postCondition);
			if(n instanceof ChannelPlace && net.getSONConnectionType(e, n).contains("SYNCLINE"))
				for(Node post : net.getPostset(n))
					for(Node postCondition : net.getPostset(post))
						if(postCondition instanceof Condition)
							result.add((Condition)postCondition);
		}

		for(Node n : net.getPreset(e)){
			if(n instanceof ChannelPlace && net.getSONConnectionType(e, n).contains("SYNCLINE"))
				for(Node pre : net.getPreset(n))
					for(Node postCondition : net.getPostset(pre))
						if(postCondition instanceof Condition)
							result.add((Condition)postCondition);
		}

		return result;
	}

	public Collection<Node> getPrePNSet(Node node){
		Collection<Node> result = new ArrayList<Node>();
		for(Node n : net.getPreset(node)){
			if(net.getSONConnectionType(node, n)=="POLYLINE" )
			result.add(n);
		}
		return result;
	}

	public Collection<Node> getPostPNSet(Node node){
		Collection<Node> result = new ArrayList<Node>();
		for(Node n : net.getPostset(node)){
			if(net.getSONConnectionType(node, n) =="POLYLINE")
			result.add(n);
		}
		return result;
	}
}
