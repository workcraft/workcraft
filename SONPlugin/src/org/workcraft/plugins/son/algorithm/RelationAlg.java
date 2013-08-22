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

	public boolean isConflict(Node c){
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
			for(SONConnection con : net.getInputSONConnections(n)){
				if (con.getType() == "POLYLINE")
					conType = false;
			}
			if(conType)
				return true;
		}

		return false;
	}

	public boolean isFinal(Node n){
		boolean conType = true;

		if(net.getPostset(n).size() == 0)
			return true;
		else{
			for(SONConnection con : net.getOutputSONConnections(n)){
				if (con.getType() == "POLYLINE")
					conType = false;
			}
			if(conType)
				return true;
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

	//Behavioral SON

	public Collection<SONConnection> getRelatedBhvLine(Collection<ONGroup> groups){
		HashSet<SONConnection> result = new HashSet<SONConnection>();

		for(SONConnection con : net.getSONConnections()){
			if (con.getType() == "BHVLINE")
				for(ONGroup group : groups){
					if(group.contains(con.getFirst())){
						for (ONGroup subGroup : groups){
							if(subGroup.contains(con.getSecond()))
								result.add(con);
					}
				}
			}
		}
		return result;
	}

	public boolean isLineLikeGroup(ONGroup group){
		for(Node node : group.getComponents()){
			if(net.getPostset(node).size() > 1 && group.containsAll(net.getPostset(node)))
				return false;
			if(net.getPreset(node).size() > 1 && group.containsAll(net.getPreset(node)))
				return false;
		}
		return true;
	}

}
