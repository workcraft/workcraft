package org.workcraft.plugins.son.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Block;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.algorithm.PathAlgorithm;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;


public class ONStructureTask implements SONStructureVerification{

	private SONModel net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private Collection<Node> errorousNodes = new HashSet<Node>();
	private Collection<ArrayList<Node>> cyclePaths = new HashSet<ArrayList<Node>>();

	private PathAlgorithm onPathAlg;
	private RelationAlgorithm relation;

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;

	public ONStructureTask(SONModel net){
		this.net = net;
		relation = new RelationAlgorithm(net);
		onPathAlg = new PathAlgorithm(net);

	}

	public void task(Collection<ONGroup> groups){

		logger.info("-------------------------Occurrence Net Verification-------------------------");

		ArrayList<Node> components = new ArrayList<Node>();

		for(ONGroup group : groups){
			components.addAll(group.getComponents());
		}

		logger.info("Selected Groups = " +  groups.size());
		logger.info("Group Components = " + components.size()+"\n");

		for(ONGroup group : groups){

		Collection<Node> iniStateResult;
		Collection<Node> finalStateResult ;
		Collection<Node> postConflictResult, preConflictResult;
		Collection<ArrayList<Node>> cycleResult, backwardCycleResult;

		//group info
			logger.info("Initialising group components...");

			Collection<Node> groupComponents = group.getComponents();
				if(group.getLabel().isEmpty())
					logger.info("Group label : empty" );
				else
					logger.info("Group label : " + group.getLabel() );

			logger.info("Condition(s) = "+group.getConditions().size()+".\n" +"Event(s) = "+group.getEvents().size()
					+".\n" + "Collapsed Block(s) = " + group.getCollapsedBlocks().size()+".");
			logger.info("Running components relation task...");

			if(!relation.hasFinal(groupComponents) || !relation.hasInitial(groupComponents)){
				logger.error("ERROR : Occurrence net must have at least one initial state and one final state");
				hasErr = true;
				errNumber ++;
				return;
			}

			//initial state result
			iniStateResult = iniStateTask(groupComponents);

			if (iniStateResult.isEmpty())
				logger.info("Initial states correct.");
			else{
				hasErr = true;
				errNumber = errNumber + iniStateResult.size();
				for(Node node : iniStateResult){
					errorousNodes.add(node);
					logger.error("ERROR : Incorrect initial state: " + net.getName(node) + "(" + net.getComponentLabel(node) + ")  ");
				}
			}

			//final state result
			finalStateResult = finalStateTask(groupComponents);
			if (finalStateResult.isEmpty())
				logger.info("Final states correct.");
			else{
				hasErr = true;
				errNumber = errNumber + finalStateResult.size();
				for(Node node : finalStateResult){
					errorousNodes.add(node);
					logger.error("ERROR : Incorrect final state: " + net.getName(node) + "(" + net.getComponentLabel(node) + ")  ");
				}
			}

			//conflict result
			postConflictResult = postConflictTask(groupComponents);
			preConflictResult = preConflictTask(groupComponents);

			if (postConflictResult.isEmpty() && preConflictResult.isEmpty())
				logger.info("Condition structure correct.");
			else{
				hasErr = true;
				errNumber = errNumber + postConflictResult.size()+ preConflictResult.size();
				for(Node condition : postConflictResult){
					errorousNodes.add(condition);
					logger.error("ERROR : Post set nodes in conflict: " + net.getName(condition) + "(" + net.getComponentLabel(condition) + ")  ");
					}
				for(Node condition : preConflictResult){
					errorousNodes.add(condition);
					logger.error("ERROR : Pre set nodes in conflict: " + net.getName(condition) + "(" + net.getComponentLabel(condition) + ")  ");
				}
			}
			logger.info("Components relation task complete.");

			//cycle detection result
			logger.info("Running cycle detection...");

			cycleResult = onPathAlg.cycleTask(groupComponents);

			backwardCycleResult = new ArrayList<ArrayList<Node>>();
			backwardCycleResult.addAll(this.onPathAlg.backwardCycleTask(groupComponents));

			cyclePaths.addAll(cycleResult);
			cyclePaths.addAll(backwardCycleResult);

			if (cycleResult.isEmpty() && backwardCycleResult.isEmpty())
				logger.info("Acyclic structure correct");
			else{
				hasErr = true;
				errNumber++;
				logger.error("ERROR : Forward cycles = "+ cycleResult.size()+", " + "Backward cycles = "+backwardCycleResult.size()+".");
			}
			logger.info("Cycle detection complete.\n");
		}
	}

	private Collection<Node> iniStateTask(Collection<Node> groupNodes){
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : groupNodes)
			if(node instanceof Event ||node instanceof Block)
				if(relation.isInitial(node))
					result.add(node);
		return result;
	}

	private Collection<Node> finalStateTask(Collection<Node> groupNodes){
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : groupNodes)
			if(node instanceof Event || node instanceof Block)
				if(relation.isFinal(node))
					result.add(node);
		return result;
	}

	private Collection<Node> postConflictTask(Collection<Node> groupNodes){
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : groupNodes)
			if(node instanceof Condition)
				if(relation.hasPostConflictEvents(node))
					result.add(node);
		return result;
	}

	private Collection<Node> preConflictTask(Collection<Node> groupNodes){
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : groupNodes)
			if(node instanceof Condition)
				if(relation.hasPreConflictEvents(node))
					result.add(node);
		return result;
	}

	public void errNodesHighlight(){
		for(Node node : this.errorousNodes){
			this.net.setFillColor(node, SONSettings.getRelationErrColor());
		}

		for (ArrayList<Node> list : this.cyclePaths)
			for (Node node : list)
				this.net.setForegroundColor(node, SONSettings.getCyclePathColor());
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
