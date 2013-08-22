package org.workcraft.plugins.son.verify;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.algorithm.GroupCycleAlg;
import org.workcraft.plugins.son.algorithm.RelationAlg;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;


public class GroupStructureTask implements SONStructureVerification{

	private SONModel net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private Collection<Node> iniStateResult;
	private Collection<Node> finalStateResult ;
	private Collection<Node> conflictResult ;
	private Collection<ArrayList<Node>> cycleResult;
	private Collection<ArrayList<Node>> backwardCycleResult;

	private GroupCycleAlg traverse;
	private RelationAlg relation;
	private Color relationColor;
	private Color cycleColor;

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;

	public GroupStructureTask(SONModel net){
		this.net = net;
		relation = new RelationAlg(net);
		traverse = new GroupCycleAlg(net);

	}

	public void task(ONGroup group){

		logger.info("--------------------------Group Verification--------------------------");

		//group info
		logger.info("Initialising group components...");

		hasErr = false;

		Collection<Node> groupComponents = group.getComponents();
			if(group.getLabel().isEmpty())
				logger.info("Group label = empty" );
			else
				logger.info("Group label = " + group.getLabel() );

		logger.info("Condition(s) = "+group.getConditions().size()+"\n" +"Event(s) = "+group.getEvents().size()+".");
		logger.info("Running group relation check...");

		if(!relation.hasFinal(groupComponents) || !relation.hasInitial(groupComponents)){
			logger.error("ERROR : Occurrence net must have at least one initial state and one final state");
			hasErr = true;
			errNumber ++;
			return;
		}

		//initial state output
		iniStateResult = iniStateTask(groupComponents);

		if (iniStateResult.isEmpty())
			logger.info("Initial states correct.");
		else{
			hasErr = true;
			errNumber = errNumber + iniStateResult.size();
			for(Node event : iniStateResult)
				logger.error("ERROR : Incorrect initial state: " + net.getName(event) + "(" + net.getNodeLabel(event) + ")  ");
		}

		//final state output
		finalStateResult = finalStateTask(groupComponents);
		if (finalStateResult.isEmpty())
			logger.info("Final states correct.");
		else{
			hasErr = true;
			errNumber = errNumber + finalStateResult.size();
			for(Node event : finalStateResult)
				logger.error("ERROR : Incorrect final state: " + net.getName(event) + "(" + net.getNodeLabel(event) + ")  ");
		}

		//conflict output
		conflictResult = conflictTask(groupComponents);
		if (conflictResult.isEmpty())
			logger.info("Condition structure correct.");
		else{
			hasErr = true;
			errNumber = errNumber + conflictResult.size();
			for(Node condition : conflictResult)
				logger.error("ERROR : Pre/post set events in conflict: " + net.getName(condition) + "(" + net.getNodeLabel(condition) + ")  ");
		}
		logger.info("Relation checking complete.");

		//cycle detection
		logger.info("Running cycle detection...");

		cycleResult = traverse.cycleTask(groupComponents);

		backwardCycleResult = new ArrayList<ArrayList<Node>>();
		backwardCycleResult.addAll(this.traverse.backwardCycleTask(groupComponents));

		if (cycleResult.isEmpty() && backwardCycleResult.isEmpty())
			logger.info("Acyclic checking correct");
		else{
			hasErr = true;
			errNumber++;
			logger.error("ERROR : Forward cycles = "+ cycleResult.size()+", " + "Backward cycles = "+backwardCycleResult.size()+".");
		}
		logger.info("Cycle detection complete.");
	}

	private Collection<Node> iniStateTask(Collection<Node> groupNodes){
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : groupNodes)
			if(node instanceof Event)
				if(relation.isInitial(node))
					result.add(node);
		return result;
	}

	private Collection<Node> finalStateTask(Collection<Node> groupNodes){
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : groupNodes)
			if(node instanceof Event)
				if(relation.isFinal(node))
					result.add(node);
		return result;
	}

	private Collection<Node> conflictTask(Collection<Node> groupNodes){
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : groupNodes)
			if(node instanceof Condition)
				if(relation.isConflict(node))
					result.add(node);
		return result;
	}


	public void errNodesHighlight(){
		this.relationColor = new Color(255, 128, 0, 64);
		this.cycleColor = new Color(255, 0, 0, 164);

		for(Node node : this.iniStateResult){
			this.net.setFillColor(node, relationColor);
		}

		for(Node node : finalStateResult){
			this.net.setFillColor(node, relationColor);
		}

		for(Node node : conflictResult){
			this.net.setFillColor(node, relationColor);
		}

		for (ArrayList<Node> list : this.cycleResult)
			for (Node node : list)
				this.net.setForegroundColor(node, cycleColor);

		for (ArrayList<Node> list : this.backwardCycleResult)
			for (Node node : list)
				this.net.setForegroundColor(node, cycleColor);

	}

	public boolean hasErr(){
		return this.hasErr;
	}

	public int getErrNumber(){
		return this.errNumber;
	}

	public int getWarningNumber(){
		return this.warningNumber;
	}
}
