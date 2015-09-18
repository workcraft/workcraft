package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Before;
import org.workcraft.plugins.son.Interval;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.ScenarioRef;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.InvalidStructureException;

public class TimeAlg extends RelationAlgorithm{

	private SON net;

	public TimeAlg(SON net) {
		super(net);
		this.net = net;
	}

	private ArrayList<String> nodeConsistency (Node node, Interval start, Interval end, Interval dur){
		ArrayList<String> result = new ArrayList<String>();
		int tsl = start.getMin();
		int tsu = start.getMax();
		int tfl = end.getMin();
		int tfu = end.getMax();
		int dl = dur.getMin();
		int du = dur.getMax();

		//Equation 3
		if(!(tsl <= tfl))
			result.add("Node inconsistency: minStart"
					+resultHelper(node)+" > minEnd"+resultHelper(node)+".");
		if(!(tsu <= tfu))
			result.add("Node inconsistency: maxStart"
					+resultHelper(node)+" > maxEnd"+resultHelper(node)+".");
		//Equation 6
		int lowBound = tsl + dl;
		int upBound = tsu + du;
		Interval i = new Interval(lowBound, upBound);
		if(!i.isOverlapping(end))
			result.add("Node inconsistency: start"
					+resultHelper(node)+" + duration"+resultHelper(node)
					+"is not intersected with end"+resultHelper(node)+".");

		//Equation 7
		int lowBound2 = tfl - du;
		int upBound2 = tfu -dl;
		Interval i2 = new Interval(lowBound2, upBound2);
		if(!i2.isOverlapping(start))
			result.add("Node inconsistency: end"
					+resultHelper(node)+" - duration"+resultHelper(node)
					+"is not intersected with start"+resultHelper(node)+".");

		//Equation 8
		int lowBound3 = Math.max(0, tfl - tsu);
		int upBound3 = tfu -tsl;
		Interval i3 = new Interval(lowBound3, upBound3);
		if(!i3.isOverlapping(dur))
			result.add("Node inconsistency: end"
					+resultHelper(node)+" - start"+resultHelper(node)
					+"is not intersected with duration"+resultHelper(node)+".");
		return result;
	}

	private ArrayList<String> concurConsistency (TransitionNode t){
		ArrayList<String> result = new ArrayList<String>();
		Collection<SONConnection> inputConnections =  net.getInputPNConnections(t);
		Collection<SONConnection> outputConnections = net.getOutputPNConnections(t);

		if(inputConnections.size() > 1){
			SONConnection con = inputConnections.iterator().next();
			Interval time = con.getTime();
			for(SONConnection con1 : inputConnections){
				Interval time1 = con1.getTime();
				if(!time.equals(time1)){
					result.add("Concurrently inconsistency: start"
							+resultHelper(t)+" != start'"+resultHelper(t)+".");
				}
			}
		}

		if(outputConnections.size() > 1){
			SONConnection con = outputConnections.iterator().next();
			Interval time = con.getTime();
			for(SONConnection con1 : outputConnections){
				Interval time1 = con1.getTime();
				if(!time.equals(time1)){
					result.add("Concurrently inconsistency: end"
							+resultHelper(t)+" != end'"+resultHelper(t)+".");
				}
			}
		}
		return result;
	}

	private ArrayList<String> alterConsistency(Condition c, ScenarioRef s){
		ArrayList<String> result = new ArrayList<String>();

		Collection<SONConnection> inputConnections;
		Collection<SONConnection> outputConnections;

		if(s!=null){
			inputConnections = net.getInputScenarioPNConnections(c, s);
			outputConnections = net.getOutputScenarioPNConnections(c, s);
		}else{
			inputConnections =  net.getInputPNConnections(c);
			outputConnections = net.getOutputPNConnections(c);
		}

		boolean isConsisent = false;

		if(c.isInitial() && c.isFinal()){
			if(nodeConsistency(c, c.getStartTime(), c.getEndTime(), c.getDuration()).isEmpty()){
				isConsisent = true;
			}
		}else if(c.isInitial() && !c.isFinal()){
			for(SONConnection con : outputConnections){
				if(nodeConsistency(c, c.getStartTime(), con.getTime(), c.getDuration()).isEmpty()){
					c.setEndTime(con.getTime());
					isConsisent = true;
					break;
				}
			}
		}else if(!c.isInitial() && c.isFinal()){
			for(SONConnection con : inputConnections){
				if(nodeConsistency(c, con.getTime(), c.getEndTime(), c.getDuration()).isEmpty()){
					c.setStartTime(con.getTime());
					isConsisent = true;
					break;
				}
			}
		}else{
			for(SONConnection con : inputConnections){
				for(SONConnection con2 : outputConnections){
					if(nodeConsistency(c, con.getTime(), con2.getTime(), c.getDuration()).isEmpty()){
						c.setStartTime(con.getTime());
						c.setEndTime(con2.getTime());
						isConsisent = true;
						break;
					}
				}
			}
		}

		if(!isConsisent){
			result.add("Alternatively inconsistency: cannot find node consistent scenario " +
					"for node "+resultHelper(c)+".");
		}

		return result;
	}

	private ArrayList<String> asynConsistency(ChannelPlace cp, Collection<ChannelPlace> sync) throws InvalidStructureException{
		ArrayList<String> result = new ArrayList<String>();
		//check all transitionNodes first
		Interval start = null;
		TransitionNode input = null;
		if(net.getInputSONConnections(cp).size() == 1){
			SONConnection con = net.getInputSONConnections(cp).iterator().next();
			input = (TransitionNode)con.getFirst();
			start = con.getTime();
		}

		Interval end = null;
		TransitionNode output = null;
		if(net.getOutputSONConnections(cp).size() == 1){
			SONConnection con = net.getOutputSONConnections(cp).iterator().next();
			output = (TransitionNode)con.getSecond();
			end = con.getTime();
		}

		if(start==null || end==null ||input==null || output == null){
			throw new InvalidStructureException("Empty channel place input/output: "+ net.getNodeReference(cp));
		}

		cp.setStartTime(start);
		cp.setEndTime(end);

		if(sync.contains(cp)){
			if(!start.isSpecified())
				cp.setStartTime(input.getEndTime());
			if(!end.isSpecified())
				cp.setEndTime(output.getStartTime());

			//Equation 17
			if(!cp.getStartTime().equals(input.getEndTime())){
				result.add("Sync inconsistency: start"+resultHelper(cp)
						+"!= end"+ resultHelper(input)+".");
			}
			//Equation 17
			if(!cp.getEndTime().equals(output.getStartTime())){
				result.add("Sync inconsistency: end"+resultHelper(cp)
						+"!= start"+ resultHelper(output)+".");
			}
			if(cp.getDuration().isSpecified() && !cp.getDuration().toString().equals("0000-0000")){
				result.add("Sync inconsistency: duration"+resultHelper(cp)+" != 0.");
			}
			//Equation 18
			if(!(input.getStartTime().equals(output.getStartTime())))
				result.add("Sync inconsistency: start"+resultHelper(input)
						+" != start"+resultHelper(output));
			if(!(input.getDuration().equals(output.getDuration()))){
				result.add("Sync inconsistency: duration"+resultHelper(input)
						+" != duration"+resultHelper(output));
			}
			if(!(input.getEndTime().equals(output.getEndTime()))){
				result.add("Sync inconsistency: end"+resultHelper(input)
						+" != end"+resultHelper(output));
			}
		}else{
			if(!nodeConsistency(cp, start, end, cp.getDuration()).isEmpty())
				result.add("Async inconsistency: "+resultHelper(cp)
						+"is not node consistency");
		}
		return result;
	}

	public ArrayList<String> bsonConsistency(TransitionNode t, Map<Condition, Collection<Phase>> phases, ScenarioRef s){
		ArrayList<String> result = new ArrayList<String>();
		BSONAlg bsonAlg = new BSONAlg(net);
		Before before = bsonAlg.before(t, phases);

		for(TransitionNode[] v : before){
        	TransitionNode v0 = v[0];
        	TransitionNode v1 = v[1];
			if(s != null){
	        	Collection<Node> scenarioNodes = s.getNodes(net);
	        	if(!scenarioNodes.contains(v1) || !scenarioNodes.contains(v0))continue;
			}

			//Equation 17
			if(!v0.getStartTime().isSpecified() || !v1.getStartTime().isSpecified()){
				result.add("Fail to run behavioural consistency checking: "+ resultHelper(v0) + " or " + resultHelper(v1)
						+" is node inconsistency.");
				continue;
			}

			int gsl = v0.getStartTime().getMin();
			int gsu = v0.getStartTime().getMax();
			int hsl = v1.getStartTime().getMin();
			int hsu = v1.getStartTime().getMax();

			//Equation 20
			if(!(gsl <= hsl)){
				result.add("Behavioural inconsistency: minStart"+resultHelper(v0)
						+" > "+"minStart"+resultHelper(v1)+".");
			}
			else if(!(gsu <= hsu)){
				result.add("Behavioural inconsistency: maxStart"+resultHelper(v0)
						+" > "+"maxStart"+resultHelper(v1)+".");
			}
		}
		return result;
	}

	public ArrayList<String> bsonConsistency2(Condition initialLow, ScenarioRef s){
		ArrayList<String> result = new ArrayList<String>();
		for(SONConnection con : net.getSONConnections()){
			if(s != null && !s.getConnections(net).contains(con)){
				continue;
			}
			if((con.getSemantics() == Semantics.BHVLINE) && (con.getFirst() == initialLow)){
				Condition c = (Condition)con.getSecond();
				if(!c.getStartTime().isSpecified() || !initialLow.getStartTime().isSpecified()){
					result.add("Fail to run behavioural consistency checking: "
							+ resultHelper(c) + " or " + resultHelper(initialLow)+" is node inconsistency.");
					return result;
				}
				if(!initialLow.getStartTime().equals(c.getStartTime()))
					result.add("Behavioural inconsistency: start"+resultHelper(initialLow)
							+" != "+"start"+resultHelper(c)+".");
			}
		}
		return result;
	}

	public ArrayList<String> bsonConsistency3(Condition finalLow, ScenarioRef s){
		ArrayList<String> result = new ArrayList<String>();
		for(SONConnection con : net.getSONConnections()){
			if(s != null && !s.getConnections(net).contains(con)){
				continue;
			}
			if((con.getSemantics() == Semantics.BHVLINE) && (con.getFirst() == finalLow)){
				Condition c = (Condition)con.getSecond();
				if(!c.getStartTime().isSpecified() || !finalLow.getStartTime().isSpecified()){
					result.add("Fail to run behavioural consistency checking: "
							+ resultHelper(c) + " or " + resultHelper(finalLow)+" is node inconsistency.");
					return result;
				}
				if(finalLow.getStartTime().equals(c.getStartTime()))
					result.add("Behavioural inconsistency: end"+resultHelper(finalLow)
							+" != "+"end"+resultHelper(c)+".");
			}
		}
		return result;
	}

	public ArrayList<String> specifiedValueChecking(Node node, boolean isSync, ScenarioRef s) throws InvalidStructureException{
		ArrayList<String> result = new ArrayList<String>();

		Collection<SONConnection> inputConnections;
		Collection<SONConnection> outputConnections;

		if(s!=null){
			inputConnections = net.getInputScenarioPNConnections(node, s);
			outputConnections = net.getOutputScenarioPNConnections(node, s);
		}else{
			inputConnections =  net.getInputPNConnections(node);
			outputConnections = net.getOutputPNConnections(node);
		}

		if ((node instanceof Time) && !((Time)node).getDuration().isSpecified() && !isSync){
			result.add("Fail to run time consistency checking, duration value is required.");
		}

		if(node instanceof TransitionNode){
			for(SONConnection con : inputConnections){
				if(!con.getTime().isSpecified()){
					result.add("Fail to run time consistency checking, node has unspecified start time value.");
					break;
				}
			}

			for(SONConnection con : outputConnections){
				if(!con.getTime().isSpecified()){
					result.add("Fail to run time consistency checking, node has unspecified end time value.");
					break;
				}
			}
		}else if (node instanceof Condition){
			Condition c = (Condition)node;

			boolean hasSpecifiedInput = false;
			//initial state
			if(inputConnections.isEmpty()){
				if(c.getStartTime().isSpecified())
					hasSpecifiedInput = true;
			}else{
				for(SONConnection con : inputConnections){
					if(con.getTime().isSpecified()){
						hasSpecifiedInput = true;
						break;
					}
				}
			}
			boolean hasSpecifiedOutput = false;
			//final state
			if(outputConnections.isEmpty()){
				if(c.getEndTime().isSpecified())
					hasSpecifiedOutput = true;
			}else{
				for(SONConnection con : outputConnections){
					if(con.getTime().isSpecified()){
						hasSpecifiedOutput = true;
						break;
					}
				}
			}
			if(!hasSpecifiedInput){
				result.add("Fail to run time consistency checking, at least one specified start time is required.");
			}
			if(!hasSpecifiedOutput){
				result.add("Fail to run time consistency checking, at least one specified end time is required.");
			}
		//check all transitionNodes first!
		}else if (node instanceof ChannelPlace){
			ChannelPlace cp = (ChannelPlace)node;
			Interval start = null;
			TransitionNode input = null;

			if(inputConnections.size() == 1){
				SONConnection con = inputConnections.iterator().next();
				start = con.getTime();
				input = (TransitionNode)con.getFirst();
			}

			Interval end = null;
			TransitionNode output = null;
			if(outputConnections.size() == 1){
				SONConnection con = outputConnections.iterator().next();
				end = con.getTime();
				output = (TransitionNode)con.getSecond();
			}

			if(start == null || end== null || input==null || output == null){
				throw new InvalidStructureException("Empty channel place input/output: "+ net.getNodeReference(cp));
			}

			if(isSync){
				if(!input.getStartTime().isSpecified() || !input.getDuration().isSpecified() || !input.getEndTime().isSpecified()){
					result.add("Fail to run time consistency checking, input node has unspecified time value.");
				}
				if(!output.getStartTime().isSpecified() || !output.getDuration().isSpecified() || !output.getEndTime().isSpecified()){
					result.add("Fail to run time consistency checking, output node has unspecified time value.");
				}
			}else{
				if(!start.isSpecified() || !end.isSpecified() || !cp.getDuration().isSpecified())
					result.add("Fail to run time consistency checking, (asynchronous) channel place has unspecified time value.");
			}
		}
		return result;
	}

	public ArrayList<String> onConsistecy(Node node, ScenarioRef s) throws InvalidStructureException{
		ArrayList<String> result = new ArrayList<String>();

		//ON time consistency checking.
		if(node instanceof TransitionNode){
			TransitionNode t = (TransitionNode)node;
			ArrayList<String> concurResult = concurConsistency(t);
			ArrayList<String> nodeResult = null;

			if(concurResult.isEmpty()){
				if(net.getInputSONConnections(t).size() > 0){
					SONConnection con = net.getInputSONConnections(t).iterator().next();
					t.setStartTime(con.getTime());
				}else
					throw new InvalidStructureException("Empty event input/output: "+ net.getNodeReference(t));

				if(net.getOutputSONConnections(t).size() > 0){
					SONConnection con = net.getOutputSONConnections(t).iterator().next();
					t.setEndTime(con.getTime());
				}else
					throw new InvalidStructureException("Empty event input/output: "+ net.getNodeReference(t));

				nodeResult = nodeConsistency(t, t.getStartTime(), t.getEndTime(), t.getDuration());
				result.addAll(nodeResult);

			}else{
				result.addAll(concurResult);
			}
		//ON time consistency checking
		}else if(node instanceof Condition){
			Condition c = (Condition)node;

			if(net.getInputPNConnections(c).size() > 1 || net.getOutputPNConnections(c).size() > 1){
				result.addAll(alterConsistency(c, s));
			}else{
				if(c.isInitial() && !c.isFinal()){
					SONConnection con = net.getOutputPNConnections(c).iterator().next();
					c.setEndTime(con.getTime());
				}else if(!c.isInitial() && c.isFinal()){
					SONConnection con = net.getInputPNConnections(c).iterator().next();
					c.setStartTime(con.getTime());
				}else if (!c.isInitial() && !c.isFinal()){
					SONConnection con = net.getInputPNConnections(c).iterator().next();
					SONConnection con2 = net.getOutputPNConnections(c).iterator().next();
					c.setStartTime(con.getTime());
					c.setEndTime(con2.getTime());
				}
				result.addAll(nodeConsistency(c, c.getStartTime(), c.getEndTime(), c.getDuration()));
			}
		}

		if(!result.isEmpty())
			setDefaultTime(node);

		return result;
	}

	public ArrayList<String> csonConsistecy(ChannelPlace cp, Collection<ChannelPlace> syncCPs, ScenarioRef s) throws InvalidStructureException{
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(asynConsistency(cp, syncCPs));

		if(!result.isEmpty())
			setDefaultTime(cp);

		return result;
	}

	public void setProperties(){
		for(Condition c : net.getConditions()){
			if(net.getInputPNConnections(c).isEmpty()){
				((Condition)c).setInitial(true);
			}
			if(net.getOutputPNConnections(c).isEmpty()){
				((Condition)c).setFinal(true);
			}
		}
	}

	public void removeProperties(){
		for(PlaceNode c : net.getPlaceNodes()){
			if(c instanceof Condition){
				((Condition)c).setInitial(false);
				((Condition)c).setFinal(false);
			}
		}
	}

	private void setDefaultTime(Node node){
		Interval input = new Interval(0000,9999);
		if(node instanceof Condition){
			Condition c = (Condition)node;
			if(c.isInitial() && !c.isFinal()){
				c.setEndTime(input);
				return;
			}else if(c.isFinal() && !c.isInitial()){
				c.setStartTime(input);
				return;
			}else if(!c.isFinal() && !c.isInitial()){
				c.setStartTime(input);
				c.setEndTime(input);
			}else{
				return;
			}
		}else if(node instanceof Time){
			((Time)node).setStartTime(input);
			((Time)node).setEndTime(input);
		}
	}


	private String resultHelper(Node node){
		return "("+net.getNodeReference(node)+")";
	}
}
