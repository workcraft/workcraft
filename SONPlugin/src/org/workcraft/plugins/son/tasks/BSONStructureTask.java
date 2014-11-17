package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.PathAlgorithm;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;

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

		//Abstract group structure task
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
		errNumber = errNumber + task2.size();
		if(relationErrors.isEmpty())
			logger.info("Correct behavioural relation");
		else{
			hasErr = true;
			for(ChannelPlace cPlace : task2){
				logger.error("ERROR: Invalid communication relation (A/SYN communication between abstract and behavioural ONs)" + net.getNodeReference(cPlace));
			}
		}
		logger.info("Behavioural groups structure task complete.");

		//phase decomposition task
		logger.info("Running phase decomposition task...");
		Collection<ONGroup> abstractGroups = getAbstractGroups(groups);
		Collection<Condition> task3 = phaseTask1(abstractGroups);
		relationErrors.addAll(task3);
		errNumber = errNumber + task3.size();

			if(!task3.isEmpty()){
				hasErr = true;
				for(Condition c : task3)
					logger.error("ERROR: Invalid Phase (disjointed elements): " + net.getNodeReference(c)+ "(" + net.getComponentLabel(c) + ")  ");
			}else{
				Collection<Condition> task4 =phaseTask2(abstractGroups);
				relationErrors.addAll(task4);
				errNumber = errNumber + task4.size();
				if(!task4.isEmpty()){
					hasErr = true;
					for(Node c : task4)
						logger.error("ERROR: Invalid Phase (phase does not reach initial/final state): " + net.getNodeReference(c)+ "(" + net.getComponentLabel(c) + ")  ");
				}
			}
			if(!hasErr){
				String result = "";
				for(ONGroup group : abstractGroups)
					for(Condition c : group.getConditions()){
						result = this.phaseTask3(getBSONAlg().getPhase(c), c);
							if(result!=""){
								hasErr = true;
								logger.error("ERROR:"+ result + net.getNodeReference(c)+ "(" + net.getComponentLabel(c) + ")  ");
								relationErrors.add(c);
								errNumber++;
					}
				}
			}else{
				logger.info("WARNING : Relation error exist, cannot run phase structure task." );
				warningNumber++;
			}

			if(relationErrors.isEmpty())
				logger.info("Correct phase decomposition");

		logger.info("phase decomposition task complete.");


		//Abstract event relation task
		logger.info("Running abstract events relation task...");
		Collection<ArrayList<TransitionNode>> absEventsTask = SyncAbstractEventsTask(groups);
		if(!absEventsTask.isEmpty()){
			for(ArrayList<TransitionNode> list : absEventsTask){
				List<String> output = new ArrayList<String>();
				for (Node node : list){
					output.add(net.getNodeReference(node) + " (" + net.getComponentLabel(node) + ")");
				}
				logger.info("Error: Invalid abtract events relation, " +
						"events " + output + " are not in synchronous relation");
				relationErrors.addAll(list);
			}
			errNumber = errNumber + absEventsTask.size();
		}else{
			logger.info("Abstract events relation correct");
		}

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
			logger.error("ERROR : model involves BSCON cycle paths = "+ cycleErrors.size() + ".");
			int i = 1;
			for(Path cycle : cycleErrors){
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

		errNumber = errNumber + groupErrors.size();
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
		Collection<ONGroup> abstractGroups = getAbstractGroups(groups);

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

	//task1: if a phase is in a single ON.
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
		PathAlgorithm alg = new PathAlgorithm(net);
		ONGroup bhvGroup = getBSONAlg().getBhvGroup(phase);
		Collection<Path> paths = pathTask(bhvGroup.getComponents(), alg);

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
					result.add(net.getNodeReference(n));
				return  "Minimal phase" +result.toString() + "is not a cut: ";
			}

			for(Condition con : maximal)
				if(path.contains(con)) {
					hasMaxPhaseNode = true;
					maxNodeInPath++;
				}
			if (maxNodeInPath>1){
				for(Node n : maximal)
					result.add(net.getNodeReference(n));
				return "Maximal phase" +result.toString() + "is not a cut: ";
			}
			if (!hasMinPhaseNode){
				for(Node n : minimal)
					result.add(net.getNodeReference(n));
				 return  "Minimal phase" +result.toString() + "is not a cut: ";
			}
			if (!hasMaxPhaseNode){
				for(Node n : maximal)
					result.add(net.getNodeReference(n));
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
					return "Invalid phase joint (not match Max("+ net.getNodeReference(pre) + ")): ";
				if(!getRelationAlg().getInitial(bhvGroup.getComponents()).containsAll(minimal)){
					return "Invalid phase joint (not match Max("+ net.getNodeReference(pre) + ")): ";
				}
			}
		}

		return "";
	}

	private Collection<Path> pathTask (Collection<Node> nodes, PathAlgorithm alg){
		List<Path> result = new ArrayList<Path>();
		for(Node start : getRelationAlg().getInitial(nodes))
			for(Node end : getRelationAlg().getFinal(nodes)){
				result.addAll(PathAlgorithm.getPaths(start, end, alg.createAdj(nodes)));
			}
		 return result;
	}

	private Collection<ArrayList<TransitionNode>> SyncAbstractEventsTask(Collection<ONGroup> groups){
		Collection<ArrayList<TransitionNode>> result = new ArrayList<ArrayList<TransitionNode>>();
		Collection<ONGroup> bhvGroups = getBSONAlg().getBhvGroups(groups);

		for(ONGroup group : bhvGroups){
			//get final states of a behavioral group
			for(Condition c : group.getConditions()){
				if(getRelationAlg().isFinal(c)){
					//for each final state, if it has more than one abstract groups and its corresponding
					//abstract conditions are not final states, then those abstract conditions should be
					//in synchronous communication.
					if(getBSONAlg().getAbstractGroups(c).size() > 1){
						ArrayList<TransitionNode> subResult = new ArrayList<TransitionNode>();
						for(Condition post : getRelationAlg().getPostBhvSet(c)){
							if(!getRelationAlg().isFinal(post) && !getRelationAlg().getPostPNSet(post).isEmpty())
								subResult.add((TransitionNode)getRelationAlg().getPostPNSet(post).iterator().next());
						}
						//check if they are in synchronous cycle
						if(subResult.size() > 1){
							Collection<Node> nodes = new HashSet<Node>();
							nodes.addAll(subResult);
							nodes.addAll(net.getChannelPlaces());
							CSONCycleAlg cson = new CSONCycleAlg(net);
							boolean contains = false;
							for(Path cycle : cson.syncCycleTask(nodes)){
								if(cycle.containsAll(nodes)){
									contains = true;
									break;
								}
							}
							if(!contains)
								result.add(subResult);
						}
					}
				}
			}
		}

		return result;
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
