package org.workcraft.plugins.son.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;


public class ONStructureTask extends AbstractStructuralVerification{

	private SONModel net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private Collection<Node> relationErrors = new HashSet<Node>();
	private Collection<ArrayList<Node>> cycleErrors = new HashSet<ArrayList<Node>>();
	private Collection<ONGroup> groupErrors = new HashSet<ONGroup>();

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;

	public ONStructureTask(SONModel net){
		super(net);
		this.net = net;
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

		Collection<Node> task1, task2, task3, task4;
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

			if(!getRelationAlg().hasFinal(groupComponents) || !getRelationAlg().hasInitial(groupComponents)){
				logger.error("ERROR : Occurrence net must have at least one initial state and one final state \n");
				hasErr = true;
				errNumber ++;
				continue;
			}

			//initial state result
			task1 = iniStateTask(groupComponents);

			if (task1.isEmpty())
				logger.info("Initial states correct.");
			else{
				hasErr = true;
				errNumber = errNumber + task1.size();
				for(Node node : task1){
					relationErrors.add(node);
					logger.error("ERROR : Incorrect initial state: " + net.getName(node) + "(" + net.getComponentLabel(node) + ")  ");
				}
			}

			//final state result
			task2 = finalStateTask(groupComponents);
			if (task2.isEmpty())
				logger.info("Final states correct.");
			else{
				hasErr = true;
				errNumber = errNumber + task2.size();
				for(Node node : task2){
					relationErrors.add(node);
					logger.error("ERROR : Incorrect final state: " + net.getName(node) + "(" + net.getComponentLabel(node) + ")  ");
				}
			}

			//conflict result
			task3 = postConflictTask(groupComponents);
			task4 = preConflictTask(groupComponents);

			if (task3.isEmpty() && task4.isEmpty())
				logger.info("Condition structure correct.");
			else{
				hasErr = true;
				errNumber = errNumber + task3.size()+ task4.size();
				for(Node condition : task3){
					relationErrors.add(condition);
					logger.error("ERROR : Post set nodes in conflict: " + net.getName(condition) + "(" + net.getComponentLabel(condition) + ")  ");
					}
				for(Node condition : task4){
					relationErrors.add(condition);
					logger.error("ERROR : Pre set nodes in conflict: " + net.getName(condition) + "(" + net.getComponentLabel(condition) + ")  ");
				}
			}
			logger.info("Components relation task complete.");

			//cycle detection result
			logger.info("Running cycle detection...");

			cycleResult = getPathAlg().cycleTask(groupComponents);

			backwardCycleResult = new ArrayList<ArrayList<Node>>();
			backwardCycleResult.addAll(getPathAlg().backwardCycleTask(groupComponents));

			cycleErrors.addAll(cycleResult);
			cycleErrors.addAll(backwardCycleResult);

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
				if(getRelationAlg().isInitial(node))
					result.add(node);
		return result;
	}

	private Collection<Node> finalStateTask(Collection<Node> groupNodes){
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : groupNodes)
			if(node instanceof Event || node instanceof Block)
				if(getRelationAlg().isFinal(node))
					result.add(node);
		return result;
	}

	private Collection<Node> postConflictTask(Collection<Node> groupNodes){
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : groupNodes)
			if(node instanceof Condition)
				if(getRelationAlg().hasPostConflictEvents(node))
					result.add(node);
		return result;
	}

	private Collection<Node> preConflictTask(Collection<Node> groupNodes){
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : groupNodes)
			if(node instanceof Condition)
				if(getRelationAlg().hasPreConflictEvents(node))
					result.add(node);
		return result;
	}
	@Override
	public void errNodesHighlight(){
		for(Node node : this.relationErrors){
			this.net.setFillColor(node, SONSettings.getRelationErrColor());
		}

		for (ArrayList<Node> list : this.cycleErrors)
			for (Node node : list)
				this.net.setForegroundColor(node, SONSettings.getCyclePathColor());
	}

	@Override
	public Collection<String> getRelationErrors() {
		return getRelationErrorsSetReferences(relationErrors);
	}

	@Override
	public Collection<ArrayList<String>> getCycleErrors() {
		return getcycleErrorsSetReferences(cycleErrors);
	}

	@Override
	public Collection<String> getGroupErrors() {
		return getGroupErrorsSetReferences(groupErrors);
	}

	@Override
	public boolean hasErr(){
		return this.hasErr;
	}
	@Override
	public int getErrNumber(){
		return this.errNumber;
	}
	@Override
	public int getWarningNumber(){
		return this.warningNumber;
	}

}
