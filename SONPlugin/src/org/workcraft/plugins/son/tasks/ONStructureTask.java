package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;


public class ONStructureTask extends AbstractStructuralVerification{

	private SON net;

	private Collection<Node> relationErrors = new HashSet<Node>();
	private Collection<Path> cycleErrors = new HashSet<Path>();
	private Collection<ONGroup> groupErrors = new HashSet<ONGroup>();

	private int errNumber = 0;
	private int warningNumber = 0;

	public ONStructureTask(SON net){
		super(net);
		this.net = net;
	}

	public void task(Collection<ONGroup> groups){

		infoMsg("-------------------------Occurrence Net Structure Verification-------------------------");

		ArrayList<Node> components = new ArrayList<Node>();

		for(ONGroup group : groups){
			components.addAll(group.getComponents());
		}

		infoMsg("Selected Groups = " +  groups.size());
		infoMsg("Group Components = " + components.size() + "\n");

		for(ONGroup group : groups){

		Collection<Node> task1, task2, task3, task4;
		Collection<Path> cycleResult;

		//group info
			infoMsg("Initialising selected groups and components...");

			Collection<Node> groupComponents = group.getComponents();
			infoMsg("Group name : ", group);
			infoMsg("Conditions = "+group.getConditions().size()+".\n" +"Events = "+group.getEvents().size()
					+".\n" + "Collapsed Blocks = " + group.getCollapsedBlocks().size()+".");

			infoMsg("Running component relation tasks...");

			if(!getRelationAlg().hasFinal(groupComponents) || !getRelationAlg().hasInitial(groupComponents)){
				errMsg("ERROR : Occurrence net must have at least one input and one output.");
				errNumber ++;
				continue;
			}

			//initial state result
			task1 = iniStateTask(groupComponents);

			if (task1.isEmpty())
				infoMsg("Valid occurrence net input.");
			else{
				errNumber = errNumber + task1.size();
				for(Node node : task1){
					relationErrors.add(node);
					errMsg("ERROR : Invalid occurrence net input (initial state is not a condition).", node);
				}
			}

			//final state result
			task2 = finalStateTask(groupComponents);
			if (task2.isEmpty())
				infoMsg("Valid occurrence net output.");
			else{
				errNumber = errNumber + task2.size();
				for(Node node : task2){
					relationErrors.add(node);
					errMsg("ERROR : Invalid occurrence net output (final state is not a condition).", node);
				}
			}

			//conflict result
			task3 = postConflictTask(groupComponents);
			task4 = preConflictTask(groupComponents);

			if (task3.isEmpty() && task4.isEmpty())
				infoMsg("Occurrence net is conflict free.");
			else{
				errNumber = errNumber + task3.size()+ task4.size();
				for(Node condition : task3){
					relationErrors.add(condition);
					errMsg("ERROR : Output events in conflict.", condition);
					}
				for(Node condition : task4){
					relationErrors.add(condition);
					errMsg("ERROR : Input events in conflict.", condition);
				}
			}
			infoMsg("Component relation tasks complete.");

			//cycle detection result
			infoMsg("Running cycle detection task...");

			cycleResult = getPathAlg().cycleTask(groupComponents);

			cycleErrors.addAll(cycleResult);

			if (cycleResult.isEmpty())
				infoMsg("Occurrence net is cycle free");
			else{
				errNumber++;
				errMsg("ERROR : Occurrence net involves cycle paths = "+ cycleResult.size() + ".");
				int i = 1;
				for(Path cycle : cycleResult){
					errMsg("Cycle " + i + ": " + cycle.toString(net));
					i++;
				}
			}
			infoMsg("Cycle detection task complete.\n");
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
	public Collection<String> getRelationErrors() {
		return getRelationErrorsSetRefs(relationErrors);
	}

	@Override
	public Collection<ArrayList<String>> getCycleErrors() {
		return getCycleErrorsSetRefs(cycleErrors);
	}

	@Override
	public Collection<String> getGroupErrors() {
		return getGroupErrorsSetRefs(groupErrors);
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
