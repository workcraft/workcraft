package org.workcraft.plugins.son.verify;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.algorithm.RelationAlg;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.elements.ChannelPlace;

public class BSONStructureTask implements SONStructureVerification{


	private SONModel net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private RelationAlg relation;

	private Collection<ONGroup> abstractGroupResult;
	private Collection<HashSet<SONConnection>> bhvRelationResult;

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;
	private Color lineColor;

	public BSONStructureTask(SONModel net){
		this.net = net;
		relation = new RelationAlg(net);
	}

	public void Task(Collection<ONGroup> groups){

		logger.info("------------------Behavioral-SON Verification------------------");

		//group info
		logger.info("Initialising selected groups elements...");
		ArrayList<Node> components = new ArrayList<Node>();
		for(ONGroup group : groups){
			components.addAll(group.getComponents());
		}

		logger.info("Selected Groups = " +  groups.size());
		logger.info("Group Components = " + components.size());

		//Abstract level structure
		logger.info("Running abstract group structure task...");
		abstractGroupResult = abstractGroupTask(groups);
		if(abstractGroupResult.isEmpty())
			logger.info("Correct abstract group structure.");
		else {
			hasErr = true;
			errNumber = errNumber + abstractGroupResult.size();
			for(ONGroup group : abstractGroupResult)
				logger.error("ERROR: Invalid abstract group structure(group label = "+group.getLabel() + ").");
		}
		logger.info("Abstract group structure checking complete.");

		//bhv relation task
		logger.info("Running behavioural relation checking task...");
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
				logger.error("ERROR: Invalid communication relation (A/SYN communication between abstract and behaviour ONs)" + conName.toString());
			}
		}

		logger.info("Behavioural relation checking complete.");
	}

	private Collection<ONGroup> abstractGroupTask(Collection<ONGroup> groups){
		Collection<ONGroup> result = new HashSet<ONGroup>();

		for(ONGroup group : groups){
			if(relation.isLineLikeGroup(group)){

				boolean isInput = false;
				boolean isOutput = false;

				for(Node node : group.getComponents()){
					if(net.getInputSONConnectionsTypes(node).contains("BHVLINE"))
						isInput = true;
					if(net.getOutputSONConnectionsTypes(node).contains("BHVLINE"))
						isOutput = true;
				}

				if(isInput && isOutput)
					result.add(group);
			}
			else{
				for(Node node : group.getComponents()){
					if(net.getInputSONConnectionsTypes(node).contains("BHVLINE"))
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

	private Collection<ONGroup> getAbstractGroups(Collection<ONGroup> groups){
		Collection<ONGroup> result = new HashSet<ONGroup>();

		for(ONGroup group : groups){
			boolean isInput = false;
			if(relation.isLineLikeGroup(group) && !abstractGroupResult.contains(group)){
				for(Node node : group.getComponents()){
					if(net.getInputSONConnectionsTypes(node).contains("BHVLINE"))
						isInput = true;
				}
				if(isInput)
					result.add(group);
			}
		}
		return result;
	}

	public void errNodesHighlight(){
		this.lineColor = new Color(255, 0, 0, 164);

		for(ONGroup group : abstractGroupResult){
			group.setForegroundColor(Color.RED);
		}

		for(HashSet<SONConnection> set : this.bhvRelationResult){
			for(SONConnection con : set)
				System.out.print("no color");
		}
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
