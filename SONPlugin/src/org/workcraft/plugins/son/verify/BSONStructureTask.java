package org.workcraft.plugins.son.verify;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.BSONPathAlg;
import org.workcraft.plugins.son.algorithm.PathAlgorithm;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;

public class BSONStructureTask implements StructuralVerification{


	private SONModel net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private RelationAlgorithm relationAlg;
	private BSONAlg bsonAlg;
	private BSONPathAlg bsonPathAlg;

	private Collection<ONGroup> groupErrors = new HashSet<ONGroup>();
	private Collection<Node> relationErrors= new HashSet<Node>();
	private Collection<ArrayList<Node>> cycleErrors = new ArrayList<ArrayList<Node>>();

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;

	public BSONStructureTask(SONModel net){
		this.net = net;
		relationAlg = new RelationAlgorithm(net);
		bsonAlg = new BSONAlg(net);
		bsonPathAlg = new BSONPathAlg(net);
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

		if(!net.getSONConnectionTypes(components).contains("BHVLINE")){
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
						result = this.phaseTask3(bsonAlg.getPhase(c), c);
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
		cycleErrors.addAll(bsonPathAlg.cycleTask(components));

		if (cycleErrors.isEmpty() )
			logger.info("Acyclic structure correct");
		else{
			hasErr = true;
			errNumber++;
			logger.error("ERROR : BSON cycles = "+ cycleErrors.size() + ".");
		}

		logger.info("Cycle detection complete.\n");
		}else{
			cycleErrors = new HashSet<ArrayList<Node>>();
			logger.info("WARNING : Relation error exist, cannot run cycle detection task.\n" );
			warningNumber++;
		}

		errNumber = errNumber + relationErrors.size() + groupErrors.size();
	}

	private Collection<ONGroup> groupTask1(Collection<ONGroup> groups){
		Collection<ONGroup> result = new HashSet<ONGroup>();

		for(ONGroup group : groups){
			if(bsonAlg.isLineLikeGroup(group)){

				boolean isInput = false;
				boolean isOutput = false;

				for(Node node : group.getComponents()){
					if(net.getInputSONConnectionTypes(node).contains("BHVLINE"))
						isInput = true;
					if(net.getOutputSONConnectionTypes(node).contains("BHVLINE"))
						isOutput = true;
				}

				if(isInput && isOutput)
					result.add(group);
			}
			else{
				for(Node node : group.getComponents()){
					if(net.getInputSONConnectionTypes(node).contains("BHVLINE"))
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

		for(ChannelPlace cPlace : relationAlg.getRelatedChannelPlace(groups)){
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
				if(bsonAlg.getBhvGroups(c).size()>1)
					result.add(c);
		return result;
	}

	//task2: if initial/final states involve in a phase
	private Collection<Condition> phaseTask2(Collection<ONGroup> abstractGroups){
		Collection<Condition> result = new HashSet<Condition>();
		for(ONGroup group : abstractGroups)
			for(Condition c : group.getConditions()){
				if(relationAlg.getPrePNSet(c).isEmpty()){
					Collection<Condition> min = bsonAlg.getMinimalPhase(bsonAlg.getPhase(c));
					for(Condition c2 : min)
						if(!relationAlg.isInitial(c2))
							result.add(c);
				}
				if(relationAlg.getPostPNSet(c).isEmpty()){
					Collection<Condition> max = bsonAlg.getMaximalPhase(bsonAlg.getPhase(c));
					for(Condition c2 : max)
						if(!relationAlg.isFinal(c2))
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
			if(bsonAlg.isLineLikeGroup(group) && !groupErrors.contains(group)){
				for(Node node : group.getComponents()){
					if(net.getInputSONConnectionTypes(node).contains("BHVLINE"))
						isInput = true;
				}
				if(isInput)
					result.add(group);
			}
		}
		return result;
	}

	//task3: if min/max phase is a cut
	private String phaseTask3(Collection<Condition> phase, Condition c){
		Collection<Condition> minimal = bsonAlg.getMinimalPhase(phase);
		Collection<Condition> maximal = bsonAlg.getMaximalPhase(phase);
		Collection<String> result = new ArrayList<String>();
		PathAlgorithm alg = new PathAlgorithm(net);
		ONGroup bhvGroup = bsonAlg.getBhvGroup(phase);
		Collection<ArrayList<Node>> paths = alg.pathTask(bhvGroup.getComponents());

		for(ArrayList<Node> path : paths){
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
		Collection<Condition> preConditions = relationAlg.getPrePNCondition(c);
		for(Condition pre : preConditions){
			Collection<Condition> prePhase = bsonAlg.getPhase(pre);
			Collection<Condition> preMaximal = bsonAlg.getMaximalPhase(prePhase);
			ONGroup preBhvGroup = bsonAlg.getBhvGroup(prePhase);

			if(!preMaximal.containsAll(minimal)){
				if(!relationAlg.getFinal(preBhvGroup.getComponents()).containsAll(preMaximal))
					return "Invalid phase joint (not match Max("+ net.getName(pre) + ")): ";
				if(!relationAlg.getInitial(bhvGroup.getComponents()).containsAll(minimal)){
					return "Invalid phase joint (not match Max("+ net.getName(pre) + ")): ";
				}
			}
		}

		return "";
	}

	@Override
	public void errNodesHighlight(){
		for(ONGroup group : groupErrors){
			group.setForegroundColor(Color.RED);
		}

		for(Node c : this.relationErrors){
			this.net.setFillColor(c, SONSettings.getRelationErrColor());
		}

		for (ArrayList<Node> list : this.cycleErrors)
			for (Node node : list)
				this.net.setForegroundColor(node, SONSettings.getCyclePathColor());
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

	@Override
	public Collection<Node> getRelationErrors() {
		return this.relationErrors;
	}

	@Override
	public Collection<ArrayList<Node>> getCycleErrors() {
		return this.cycleErrors;
	}

	@Override
	public Collection<ONGroup> getGroupErrors() {
		return groupErrors;
	}

}
