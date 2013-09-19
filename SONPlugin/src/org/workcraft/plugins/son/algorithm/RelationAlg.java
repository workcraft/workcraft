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

	//Behavioral SON

	public Collection<SONConnection> getRelatedBhvLine(Collection<ONGroup> groups){
		HashSet<SONConnection> result = new HashSet<SONConnection>();

		for(SONConnection con : net.getSONConnections()){
			if (con.getType() == "BHVLINE")
				for(ONGroup group : groups){
					if(group.contains(con.getFirst())){
						for (ONGroup nextGroup : groups){
							if(nextGroup.contains(con.getSecond()))
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

	public Collection<Condition> getPhase(Condition c){
		Collection<Condition> result = new HashSet<Condition>();
		Collection<Condition> connectedNodes = new ArrayList<Condition>();
		Collection<ArrayList<Node>> paths = null;
		ONPathAlg alg = new ONPathAlg(net);

		for(SONConnection con : net.getSONConnections()){
			if(con.getSecond()==c && con.getType()=="BHVLINE")
				connectedNodes.add((Condition)con.getFirst());
		}
		if (connectedNodes.isEmpty())
			return result;
		else{
			for(Node start : connectedNodes)
				for(Node end : connectedNodes)
						alg.getAllPath(start, end, alg.createAdj(this.getBhvGroup(connectedNodes).getComponents()));
			paths = alg.getPathSet();

			for(ArrayList<Node> path : paths){
				for(Node n : path)
					if(n instanceof Condition)
						result.add((Condition)n);
			}
		}
		return result;
	}

	public ONGroup getBhvGroup(Collection<Condition> phase){
		for(ONGroup group : net.getGroups())
			if(group.getConditions().containsAll(phase))
				return group;
		return null;
	}

	public Collection<Condition> getPrePNCondition(Condition c){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Node n : net.getPreset(c))
			if(n instanceof Event && net.getSONConnectionTypes(c, n).size()==1 && net.getSONConnectionTypes(c, n).contains("POLYLINE"))
				for(Node n2 : net.getPreset(n))
					if(n2 instanceof Condition && net.getSONConnectionTypes(n, n2).size()==1 && net.getSONConnectionTypes(n, n2).contains("POLYLINE"))
						result.add((Condition)n2);

		return result;
	}

	public Collection<Condition> getMinialPhase(Collection<Condition> phase){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Condition c : phase){
			boolean isMinimal = true;
			for(Condition pre : this.getPrePNCondition(c))
				if(phase.contains(pre))
					isMinimal = false;
			if(isMinimal)
				result.add(c);
		}
		return result;
	}

	public Collection<Condition> getPostPNCondition(Condition c){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Node n : net.getPostset(c))
			if(n instanceof Event && net.getSONConnectionTypes(c, n).size()==1 && net.getSONConnectionTypes(c, n).contains("POLYLINE"))
				for(Node n2 : net.getPostset(n))
					if(n2 instanceof Condition && net.getSONConnectionTypes(n, n2).size()==1 && net.getSONConnectionTypes(n, n2).contains("POLYLINE"))
						result.add((Condition)n2);

		return result;
	}

	public Collection<Condition> getMaximalPhase(Collection<Condition> phase){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Condition c : phase){
			boolean isMaximal = true;
			for(Condition pre : this.getPostPNCondition(c))
				if(phase.contains(pre))
					isMaximal = false;
			if(isMaximal)
				result.add(c);
		}
		return result;
	}

	private Collection<Condition> getPREset(Event e){
		Collection<Condition> result = new ArrayList<Condition>();
		for(Node n : net.getPreset(e)){
			if(n instanceof Condition)
				result.add((Condition)n);
			if(n instanceof ChannelPlace && net.getSONConnectionTypes(e, n).contains("ASYNLINE"))
				for(Node pre : net.getPreset(n))
					for(Node preCondition : net.getPreset(pre))
						if(preCondition instanceof Condition)
							result.add((Condition)preCondition);
			if(n instanceof ChannelPlace && net.getSONConnectionTypes(e, n).contains("SYNCLINE"))
				for(Node pre : net.getPreset(n))
					for(Node preCondition : net.getPreset(pre))
						if(preCondition instanceof Condition)
							result.add((Condition)preCondition);
		}

		for(Node n : net.getPostset(e)){
			if(n instanceof ChannelPlace && net.getSONConnectionTypes(e, n).contains("SYNCLINE"))
				for(Node post : net.getPostset(n))
					for(Node preCondition : net.getPreset(post))
						if(preCondition instanceof Condition)
							result.add((Condition)preCondition);
		}

		return result;
	}

	private Collection<Condition> getPOSTset(Event e){
		Collection<Condition> result = new ArrayList<Condition>();

		for(Node n : net.getPostset(e)){
			if(n instanceof Condition)
				result.add((Condition)n);
			if(n instanceof ChannelPlace && net.getSONConnectionTypes(e, n).contains("ASYNLINE"))
				for(Node post : net.getPostset(n))
					for(Node postCondition : net.getPostset(post))
						if(postCondition instanceof Condition)
							result.add((Condition)postCondition);
			if(n instanceof ChannelPlace && net.getSONConnectionTypes(e, n).contains("SYNCLINE"))
				for(Node post : net.getPostset(n))
					for(Node postCondition : net.getPostset(post))
						if(postCondition instanceof Condition)
							result.add((Condition)postCondition);
		}

		for(Node n : net.getPreset(e)){
			if(n instanceof ChannelPlace && net.getSONConnectionTypes(e, n).contains("SYNCLINE"))
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
			if(net.getSONConnectionTypes(node, n).size()==1 && net.getSONConnectionTypes(node, n).contains("POLYLINE") )
			result.add(n);
		}
		return result;
	}

	public Collection<Node> getPostPNSet(Node node){
		Collection<Node> result = new ArrayList<Node>();
		for(Node n : net.getPostset(node)){
			if(net.getSONConnectionTypes(node, n).size()==1 && net.getSONConnectionTypes(node, n).contains("POLYLINE") )
			result.add(n);
		}
		return result;
	}

	public Collection<Condition[]> before(Event e){
		Collection<Condition[]> result = new ArrayList<Condition[]>();
		Condition[] pre = new Condition[1];
		Condition[] post = new Condition[1];

		if(getPostPNSet(e).size()!=1 || getPrePNSet(e).size()!=1){
			//System.out.println("size > 1");
			return result;
		}
		for(Node node : getPrePNSet(e))
			pre[0] = (Condition)node;
		for(Node node : getPostPNSet(e))
			post[0] = (Condition)node;

		Collection<Condition> phaseI = getPhase(pre[0]);
		Collection<Condition> phaseI2 = getPhase(post[0]);
		if(phaseI.isEmpty() && phaseI2.isEmpty()){
			//System.out.println(net.getNodeLabel(e)+"  is empty");
			return result;
		}

		Collection<Condition> maxI = getMaximalPhase(phaseI);
		Collection<Condition> minI2 = getMinialPhase(phaseI2);
		Collection<Condition> PRE = this.getPREset(e);
		Collection<Condition> POST = this.getPOSTset(e);
		if(!maxI.containsAll(minI2)){
			for(Condition c0 : maxI){
				for(Condition c1 : minI2){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}

			for(Condition c0 : PRE){
				for(Condition c1 : minI2){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}

			for(Condition c0 : maxI){
				for(Condition c1 : POST){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}
		}
		if(maxI.containsAll(minI2)){
			Collection<Node> preMaxI = new HashSet<Node>();
			Collection<Node> postMinI2 = new HashSet<Node>();
			for(Node n: maxI)
				preMaxI.addAll(this.getPrePNSet(n));
			for(Node n: minI2)
				postMinI2.addAll(this.getPostPNSet(n));

			Collection<Condition> PREpreMaxI = new HashSet<Condition>();
			Collection<Condition> POSTpostMinI2 = new HashSet<Condition>();

			for(Node n : preMaxI){
				if(n instanceof Event)
				PREpreMaxI.addAll(this.getPREset((Event)n));}
			for(Node n : postMinI2){
				if(n instanceof Event)
				POSTpostMinI2.addAll(this.getPOSTset((Event)n));}

			for(Condition c0 : PREpreMaxI){
				for(Condition c1 : POSTpostMinI2){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}

			for(Condition c0 : PRE){
				for(Condition c1 : POSTpostMinI2){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}

			for(Condition c0 : PREpreMaxI){
				for(Condition c1 : POST){
					Condition[] subResult = new Condition[2];
					subResult[0]=c0;
					subResult[1]=c1;
					result.add(subResult);
				}
			}

		}
		return result;
	}
}
