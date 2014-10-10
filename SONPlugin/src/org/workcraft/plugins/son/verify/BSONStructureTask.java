package org.workcraft.plugins.son.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.ONPathAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;

public class BSONStructureTask extends AbstractStructuralVerification{

	private SON net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private Collection<Node> relationErrors = new ArrayList<Node>();
	private Collection<Path> cycleErrors = new ArrayList<Path>();
	private Collection<ONGroup> groupErrors = new HashSet<ONGroup>();

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;

	public BSONStructureTask(SON net){
		super(net);
		this.net = net;
	}

	public void task(Collection<ONGroup> groups){

		logger.info("-----------------Behavioral-SON Verification-----------------");

		//group info
		logger.info("Initialising selected group elements...");
		ArrayList<Node> components = new ArrayList<Node>();

		for(ONGroup group : groups){
			components.addAll(group.getComponents());
		}

		logger.info("Selected Groups = " +  groups.size());
		logger.info("Group Components = " + components.size());

		if(!net.getSONConnectionTypes(components).contains(Semantics.BHVLINE)){
			logger.info("Task terminated: no behavioural connections in selected groups.");
			return;
		}

		//Abstract level structure
		logger.info("Running model strucuture and components relation check...");
		logger.info("Running abstract group structure task...");
		groupErrors.addAll(groupTask1(groups));
		if(groupErrors.isEmpty())
			logger.info("Correct abstract group structure.");
		else {
			hasErr = true;
			for(ONGroup group : groupErrors)
				logger.error("ERROR: Invalid abstract group structure(group label = "+group.getLabel() + ").");
		}
		logger.info("Abstract group structure task complete.");

		//bhv group task
		logger.info("Running behavioural groups structure task...");
		Collection<ChannelPlace> task2 = groupTask2(groups);
		relationErrors.addAll(task2);
		if(relationErrors.isEmpty())
			logger.info("Correct behavioural relation");
		else{
			hasErr = true;
			for(ChannelPlace cPlace : task2){
				logger.error("ERROR: Invalid communication relation (A/SYN communication between abstract and behavioural ONs)" + net.getName(cPlace));
			}
		}
		logger.info("Behavioural groups structure task complete.");

		//phase decomposition task
		logger.info("Running phase decomposition task...");
		Collection<ONGroup> abstractGroups = this.getAbstractGroups(groups);
		Collection<Condition> task3 = phaseTask1(abstractGroups);
		relationErrors.addAll(task3);

			if(!task3.isEmpty()){
				hasErr = true;
				for(Condition c : task3)
					logger.error("ERROR: Invalid Phase (disjointed elements): " + net.getName(c)+ "(" + net.getComponentLabel(c) + ")  ");
			}else{
				Collection<Condition> task4 =phaseTask2(abstractGroups);
				relationErrors.addAll(task4);
				if(!task4.isEmpty()){
					hasErr = true;
					for(Node c : task4)
						logger.error("ERROR: Invalid Phase (phase does not reach initial/final state): " + net.getName(c)+ "(" + net.getComponentLabel(c) + ")  ");
				}
			}
			if(!hasErr){
				String result = "";
				for(ONGroup group : abstractGroups)
					for(Condition c : group.getConditions()){
						result = this.phaseTask3(getBSONAlg().getPhase(c), c);
							if(result!=""){
								hasErr = true;
								logger.error("ERROR:"+ result + net.getName(c)+ "(" + net.getComponentLabel(c) + ")  ");
								relationErrors.add(c);
					}
				}
			}else{
				logger.info("WARNING : Relation error exist, cannot run valid phase checking task." );
				warningNumber++;
			}

			if(relationErrors.isEmpty())
				logger.info("Correct phase decomposition");

		logger.info("phase decomposition task complete.");
		logger.info("Model strucuture and components relation task complete.");

		//BSON cycle task
		if(!hasErr){
		logger.info("Running cycle detection...");
		cycleErrors.addAll(getBSONPathAlg().cycleTask(components));

		if (cycleErrors.isEmpty() )
			logger.info("Acyclic structure correct");
		else{
			hasErr = true;
			errNumber++;
			logger.error("ERROR : model invloves BSCON cycle paths = "+ cycleErrors.size() + ".");
			for(Path cycle : cycleErrors){
				int i = 1;
				logger.error("Cycle " + i + ": " + cycle.toString(net));
				i++;
			}
		}

		logger.info("Cycle detection complete.\n");
		}else{
			cycleErrors = new HashSet<Path>();
			logger.info("WARNING : Relation error exist, cannot run cycle detection task.\n" );
			warningNumber++;
		}

		errNumber = errNumber + relationErrors.size() + groupErrors.size();
	}

	private Collection<ONGroup> groupTask1(Collection<ONGroup> groups){
		Collection<ONGroup> result = new HashSet<ONGroup>();

		for(ONGroup group : groups){
			if(getBSONAlg().isLineLikeGroup(group)){

				boolean isInput = false;
				boolean isOutput = false;

				for(Node node : group.getComponents()){
					if(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE))
						isInput = true;
					if(net.getOutputSONConnectionTypes(node).contains(Semantics.BHVLINE))
						isOutput = true;
				}

				if(isInput && isOutput)
					result.add(group);
			}
			else{
				for(Node node : group.getComponents()){
					if(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE))
						result.add(group);
				}
			}
		}
		return result;
	}

	//A/SYN communication between abstract and behavioural ONs
	private Collection<ChannelPlace> groupTask2(Collection<ONGroup> groups){
		Collection<ChannelPlace> result = new HashSet<ChannelPlace>();
		Collection<ONGroup> abstractGroups = this.getAbstractGroups(groups);

		for(ChannelPlace cPlace : getRelationAlg().getRelatedChannelPlace(groups)){
			int inAbGroup = 0;

			Collection<Node> connectedNodes = new HashSet<Node>();
			connectedNodes.addAll(net.getPostset(cPlace));
			connectedNodes.addAll(net.getPreset(cPlace));

				for(Node node : connectedNodes){
					for(ONGroup group : abstractGroups){
						if(group.getComponents().contains(node))
								inAbGroup ++;
					}
				}

			if(inAbGroup < connectedNodes.size() && inAbGroup != 0)
					result.add(cPlace);
			}

		return result;
	}

	//task1: if a phase is in one ON.
	private Collection<Condition> phaseTask1(Collection<ONGroup> abstractGroups){
		Collection<Condition> result = new HashSet<Condition>();
		for(ONGroup group : abstractGroups)
			for(Condition c : group.getConditions())
				if(getBSONAlg().getBhvGroups(c).size()>1)
					result.add(c);
		return result;
	}

	//task2: if initial/final states involve in a phase
	private Collection<Condition> phaseTask2(Collection<ONGroup> abstractGroups){
		Collection<Condition> result = new HashSet<Condition>();
		for(ONGroup group : abstractGroups)
			for(Condition c : group.getConditions()){
				if(getRelationAlg().getPrePNSet(c).isEmpty()){
					ArrayList<Condition> min = getBSONAlg().getMinimalPhase(getBSONAlg().getPhase(c));
					for(Condition c2 : min)
						if(!getRelationAlg().isInitial(c2))
							result.add(c);
				}
				if(getRelationAlg().getPostPNSet(c).isEmpty()){
					ArrayList<Condition> max = getBSONAlg().getMaximalPhase(getBSONAlg().getPhase(c));
					for(Condition c2 : max)
						if(!getRelationAlg().isFinal(c2))
							result.add(c);
				}

			}
		return result;
	}


	//get valid abstract groups
	private Collection<ONGroup> getAbstractGroups(Collection<ONGroup> groups){
		Collection<ONGroup> result = new HashSet<ONGroup>();

		for(ONGroup group : groups){
			boolean isInput = false;
			if(getBSONAlg().isLineLikeGroup(group) && !groupErrors.contains(group)){
				for(Node node : group.getComponents()){
					if(net.getInputSONConnectionTypes(node).contains(Semantics.BHVLINE))
						isInput = true;
				}
				if(isInput)
					result.add(group);
			}
		}
		return result;
	}

	//task3: if min/max phase is a cut
	private String phaseTask3(Phase phase, Condition c){
		Collection<Condition> minimal = getBSONAlg().getMinimalPhase(phase);
		Collection<Condition> maximal = getBSONAlg().getMaximalPhase(phase);
		Collection<String> result = new ArrayList<String>();
		ONPathAlg alg = new ONPathAlg(net);
		ONGroup bhvGroup = getBSONAlg().getBhvGroup(phase);
		Collection<Path> paths = alg.pathTask(bhvGroup.getComponents());

		for(Path path : paths){
			int minNodeInPath = 0;
			int maxNodeInPath = 0;
			boolean hasMinPhaseNode = false;
			boolean hasMaxPhaseNode = false;

			for(Condition con : minimal)
				if(path.contains(con)) {
					hasMinPhaseNode=true;
					minNodeInPath++;
				}
			if (minNodeInPath>1){
				for(Node n : minimal)
					result.add(net.getName(n));
				return  "Minimal phase" +result.toString() + "is not a cut: ";
			}

			for(Condition con : maximal)
				if(path.contains(con)) {
					hasMaxPhaseNode = true;
					maxNodeInPath++;
				}
			if (maxNodeInPath>1){
				for(Node n : maximal)
					result.add(net.getName(n));
				return "Maximal phase" +result.toString() + "is not a cut: ";
			}
			if (!hasMinPhaseNode){
				for(Node n : minimal)
					result.add(net.getName(n));
				 return  "Minimal phase" +result.toString() + "is not a cut: ";
			}
			if (!hasMaxPhaseNode){
				for(Node n : maximal)
					result.add(net.getName(n));
				return "Maximal phase" +result.toString() + "is not a cut: ";
			}
		}

		//Joint checking
		Collection<Condition> preConditions = getRelationAlg().getPrePNCondition(c);
		for(Condition pre : preConditions){
			Phase prePhase = getBSONAlg().getPhase(pre);
			ArrayList<Condition> preMaximal = getBSONAlg().getMaximalPhase(prePhase);
			ONGroup preBhvGroup = getBSONAlg().getBhvGroup(prePhase);

			if(!preMaximal.containsAll(minimal)){
				if(!getRelationAlg().getFinal(preBhvGroup.getComponents()).containsAll(preMaximal))
					return "Invalid phase joint (not match Max("+ net.getName(pre) + ")): ";
				if(!getRelationAlg().getInitial(bhvGroup.getComponents()).containsAll(minimal)){
					return "Invalid phase joint (not match Max("+ net.getName(pre) + ")): ";
				}
			}
		}

		return "";
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
