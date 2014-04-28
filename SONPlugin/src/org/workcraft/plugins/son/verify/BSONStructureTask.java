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
import org.workcraft.plugins.son.algorithm.BSONPathAlg;
import org.workcraft.plugins.son.algorithm.ONPathAlg;
import org.workcraft.plugins.son.algorithm.RelationAlg;
import org.workcraft.plugins.son.components.ChannelPlace;
import org.workcraft.plugins.son.components.Condition;
import org.workcraft.plugins.son.connections.SONConnection;

public class BSONStructureTask implements SONStructureVerification{


	private SONModel net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private RelationAlg relation;
	private BSONPathAlg traverse;

	private Collection<ONGroup> abstractGroupResult;
	private Collection<HashSet<SONConnection>> bhvRelationResult;
	private Collection<Condition> phaseTaskResult1, phaseTaskResult2;
	private Collection<ArrayList<Node>> cycleResult;

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;

	public BSONStructureTask(SONModel net){
		this.net = net;
		relation = new RelationAlg(net);
		traverse = new BSONPathAlg(net);
	}

	public void task(Collection<ONGroup> groups){

		logger.info("-----------------Behavioral-SON Verification-----------------");

		//group info
		logger.info("Initialising selected groups elements...");
		ArrayList<Node> components = new ArrayList<Node>();

		for(ONGroup group : groups){
			components.addAll(group.getComponents());
		}

		logger.info("Selected Groups = " +  groups.size());
		logger.info("Group Components = " + components.size());

		//Abstract level structure
		logger.info("Running model strucuture and components relation check...");
		logger.info("Running abstract group structure task...");
		abstractGroupResult = invalidAbstractGroups(groups);
		if(abstractGroupResult.isEmpty())
			logger.info("Correct abstract group structure.");
		else {
			hasErr = true;
			errNumber = errNumber + abstractGroupResult.size();
			for(ONGroup group : abstractGroupResult)
				logger.error("ERROR: Invalid abstract group structure(group label = "+group.getLabel() + ").");
		}
		logger.info("Abstract group structure task complete.");

		//bhv relation task
		logger.info("Running behavioural groups structure task...");
		bhvRelationResult = bhvRelationsTask(groups);
		if(bhvRelationResult.isEmpty())
			logger.info("Correct behavioural relation");
		else{
			hasErr = true;
			errNumber = errNumber + bhvRelationResult.size();
			for(HashSet<SONConnection> set : bhvRelationResult){
				ArrayList<String> conName = new ArrayList<String>();
				for(SONConnection con : set){
					conName.add(net.getName(con));
				}
				logger.error("ERROR: Invalid communication relation (A/SYN communication between abstract and behavioural ONs)" + conName.toString());
			}
		}
		logger.info("Behavioural groups structure task complete.");

		//phase decomposition task
		logger.info("Running phase decomposition task...");
		Collection<ONGroup> abstractGroups = this.getAbstractGroups(groups);

		phaseTaskResult1 = phaseTask1(abstractGroups);
		phaseTaskResult2 = new HashSet<Condition>();

			if(!phaseTaskResult1.isEmpty()){
				hasErr = true;
				errNumber = errNumber + phaseTaskResult1.size();
				for(Condition c : phaseTaskResult1)
					logger.error("ERROR: Invalid Phase (disjointed elements): " + net.getName(c)+ "(" + net.getComponentLabel(c) + ")  ");
			}else{
				phaseTaskResult2.addAll(phaseTask2(abstractGroups));
				if(!phaseTaskResult2.isEmpty()){
					hasErr = true;
					errNumber = errNumber + phaseTaskResult2.size();
					for(Condition c : phaseTaskResult2)
						logger.error("ERROR: Invalid Phase (phase does not reach initial/final state): " + net.getName(c)+ "(" + net.getComponentLabel(c) + ")  ");
				}
			}
			if(!hasErr){
				String result = "";
				for(ONGroup group : abstractGroups)
					for(Condition c : group.getConditions()){
						result = this.phaseTask3(relation.getPhase(c), c);
							if(result!=""){
								hasErr = true;
								errNumber ++;
								logger.error("ERROR:"+ result + net.getName(c)+ "(" + net.getComponentLabel(c) + ")  ");
								phaseTaskResult1.add(c);
					}
				}
			}else{
				logger.info("WARNING : Relation error exist, cannot run valid phase checking task." );
				warningNumber++;
			}

			if(phaseTaskResult1.isEmpty())
				logger.info("Correct phase decomposition");

		logger.info("phase decomposition task complete.");
		logger.info("Model strucuture and components relation task complete.");

		//BSON cycle task
		if(!hasErr){
		logger.info("Running cycle detection...");
		cycleResult = traverse.cycleTask(components);

		if (cycleResult.isEmpty() )
			logger.info("Acyclic structure correct");
		else{
			hasErr = true;
			errNumber++;
			logger.error("ERROR : BSON cycles = "+ cycleResult.size() + ".");
		}

		logger.info("Cycle detection complete.\n");
		}else{
			cycleResult = new HashSet<ArrayList<Node>>();
			logger.info("WARNING : Relation error exist, cannot run cycle detection task.\n" );
			warningNumber++;
		}
	}

	private Collection<ONGroup> invalidAbstractGroups(Collection<ONGroup> groups){
		Collection<ONGroup> result = new HashSet<ONGroup>();

		for(ONGroup group : groups){
			if(relation.isLineLikeGroup(group)){

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

	private Collection<HashSet<SONConnection>> bhvRelationsTask(Collection<ONGroup> groups){
		Collection<HashSet<SONConnection>> result = new HashSet<HashSet<SONConnection>>();
		Collection<ONGroup> abstractGroups = this.getAbstractGroups(groups);

		for(ChannelPlace cPlace : relation.getRelatedChannelPlace(groups)){
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

			if(inAbGroup < connectedNodes.size() && inAbGroup != 0){
				HashSet<SONConnection> subResult = new HashSet<SONConnection>();
					subResult.addAll(net.getSONConnections(cPlace));
					result.add(subResult);
				}
			}

		return result;
	}

	//task1: if a phase is in one ON.
	private Collection<Condition> phaseTask1(Collection<ONGroup> abstractGroups){
		Collection<Condition> result = new HashSet<Condition>();
		for(ONGroup group : abstractGroups)
			for(Condition c : group.getConditions())
				if(relation.getBhvGroups(c).size()>1)
					result.add(c);
		return result;
	}

	//task2: if initial/final states involve in a phase
	private Collection<Condition> phaseTask2(Collection<ONGroup> abstractGroups){
		Collection<Condition> result = new HashSet<Condition>();
		for(ONGroup group : abstractGroups)
			for(Condition c : group.getConditions()){;
				if(relation.getPrePNSet(c).isEmpty()){
					Collection<Condition> min = relation.getMinimalPhase(relation.getPhase(c));
					for(Condition c2 : min)
						if(!relation.isInitial(c2))
							result.add(c);
				}
				if(relation.getPostPNSet(c).isEmpty()){
					Collection<Condition> max = relation.getMaximalPhase(relation.getPhase(c));
					for(Condition c2 : max)
						if(!relation.isFinal(c2))
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
			if(relation.isLineLikeGroup(group) && !abstractGroupResult.contains(group)){
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
		Collection<Condition> minimal = relation.getMinimalPhase(phase);
		Collection<Condition> maximal = relation.getMaximalPhase(phase);
		Collection<String> result = new ArrayList<String>();
		ONPathAlg alg = new ONPathAlg(net);
		ONGroup bhvGroup = relation.getBhvGroup(phase);
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
		Collection<Condition> preConditions = relation.getPrePNCondition(c);
		for(Condition pre : preConditions){
			Collection<Condition> prePhase = relation.getPhase(pre);
			Collection<Condition> preMaximal = relation.getMaximalPhase(prePhase);
			ONGroup preBhvGroup = relation.getBhvGroup(prePhase);

			if(!preMaximal.containsAll(minimal)){
				if(!relation.getFinal(preBhvGroup.getComponents()).containsAll(preMaximal))
					return "Invalid phase joint (not match Max("+ net.getName(pre) + ")): ";
				if(!relation.getInitial(bhvGroup.getComponents()).containsAll(minimal)){
					return "Invalid phase joint (not match Max("+ net.getName(pre) + ")): ";
				}
			}
		}

		return "";
	}

	public void errNodesHighlight(){

		for(ONGroup group : abstractGroupResult){
			group.setForegroundColor(Color.RED);
		}

		for(HashSet<SONConnection> set : this.bhvRelationResult){
			for(SONConnection con : set)
				con.setColor(SONSettings.getConnectionErrColor());
		}

		for(Condition c : this.phaseTaskResult1){
			c.setFillColor(SONSettings.getRelationErrColor());
		}

		for(Condition c : this.phaseTaskResult2){
			c.setFillColor(SONSettings.getRelationErrColor());
		}

		for (ArrayList<Node> list : this.cycleResult)
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
