package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.Scenario;
import org.workcraft.plugins.son.TimeConsistencySettings;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.algorithm.TimeAlg;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.InvalidStructureException;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeConsistencyTask implements Task<VerificationResult>{

	private SON net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private TimeAlg timeAlg;
	private BSONAlg bsonAlg;
	private TimeConsistencySettings settings;
	private Collection<Condition> lowerConditions;
	private Collection<TransitionNode> upperTransitionNodes;

	private Collection<ChannelPlace> syncCPs;
	private Map<Condition, Collection<Phase>> phases;

	private int totalErrNum = 0;

	public TimeConsistencyTask(WorkspaceEntry we, TimeConsistencySettings settings) {
		this.settings = settings;
		net=(SON)we.getModelEntry().getMathModel();
		initialise();
	}

	@Override
	public Result<? extends VerificationResult> run(
			ProgressMonitor<? super VerificationResult> monitor) {

		Collection<Node> components = new ArrayList<Node>();

		infoMsg("-------------------------Time Consistency Checking Result-------------------------");
		if(settings.getTabIndex() == 0){
			components = new ArrayList<Node>();
			Collection<ONGroup> groups = settings.getSelectedGroups();

			RelationAlgorithm relationAlg = new RelationAlgorithm(net);
			//group info
			infoMsg("Initialising selected groups and components...");

			for(ONGroup group : groups){
				components.addAll(group.getComponents());
			}

			infoMsg("Selected Groups : " +  net.toString(groups));

			ArrayList<ChannelPlace> relatedCPlaces = new ArrayList<ChannelPlace>();
			relatedCPlaces.addAll(relationAlg.getRelatedChannelPlace(groups));
			components.addAll(relatedCPlaces);

			infoMsg("Channel Places = " + relatedCPlaces.size()+"\n");

		}else if(settings.getTabIndex() == 1){
			infoMsg("Initialising selected scenario...");
			components = settings.getSeletedScenario().getNodes(net);
			infoMsg("Nodes = " + components.size()+"\n");

		}else if(settings.getTabIndex() == 2){
			//node info
			infoMsg("Initialising selected components...");
			components = settings.getSeletedNodes();
			infoMsg("Selected nodes = " + components.size()+"\n");

		}

		for(Node node : components){
			ArrayList<String> result;
			if(settings.getTabIndex() == 1){
				result = timeConsistencyTask(node, settings.getSeletedScenario());
			}else{
				result = timeConsistencyTask(node, null);
			}

			if(!result.isEmpty()){
				infoMsg("Node:" + net.getNodeReference(node));
				for(String str : result){
					errMsg("-"+str);
					totalErrNum++;
				}
			}
		}

		logger.info("\n\nVerification-Result : "+ totalErrNum + " Error(s).");

		return new Result<VerificationResult>(Outcome.FINISHED);
	}


	private ArrayList<String> timeConsistencyTask(Node node, Scenario s){
		ArrayList<String> result = new ArrayList<String>();
		Collection<String> onResult = new ArrayList<String>();
		Collection<String> csonResult = new ArrayList<String>();
		Collection<String> bsonResult = new ArrayList<String>();

		if(node instanceof TransitionNode){
			//ON checking
			try {
				onResult.addAll(timeAlg.onConsistecy(node, s));
			} catch (InvalidStructureException e) {
				e.printStackTrace();
			}
			if(!onResult.isEmpty()){
				result.addAll(onResult);
			}
			//BSON checking
			if(upperTransitionNodes.contains(node)){
				bsonResult = timeAlg.bsonConsistency((TransitionNode)node, phases, s);
			}

			if(!bsonResult.isEmpty()){
				result.addAll(bsonResult);
			}
		}else if(node instanceof Condition){
			Condition c = (Condition)node;
			//ON checking
			try {
				onResult.addAll(timeAlg.onConsistecy(c, s));
			} catch (InvalidStructureException e) {
				e.printStackTrace();
			}
			if(!onResult.isEmpty()){
				result.addAll(onResult);
			}
			//BSON checking
			if(lowerConditions.contains(c) && c.isInitial()){
				bsonResult = timeAlg.bsonConsistency2(c, s);
			}
			if(lowerConditions.contains(c) && c.isFinal()){
				bsonResult = timeAlg.bsonConsistency3(c, s);
			}
			if(!bsonResult.isEmpty()){
				result.addAll(bsonResult);
			}
		}else if(node instanceof ChannelPlace){
			//CSON checking
			try {
				csonResult.addAll(timeAlg.csonConsistecy((ChannelPlace)node, syncCPs, s));
			} catch (InvalidStructureException e) {
				e.printStackTrace();
			}
			if(!csonResult.isEmpty()){
				result.addAll(csonResult);
			}
		}

		return result;
	}

	protected void initialise(){
		timeAlg = new TimeAlg(net);
		timeAlg.removeProperties();
		timeAlg.setProperties();

		syncCPs = getSyncCPs();

		bsonAlg = new BSONAlg(net);
		phases = bsonAlg.getAllPhases();
		lowerConditions= getLowerConditions();
		upperTransitionNodes= getUpperTransitionNodes();
	}

	private Collection<ChannelPlace> getSyncCPs(){
		Collection<ChannelPlace> result = new HashSet<ChannelPlace>();
		HashSet<Node> nodes = new HashSet<Node>();
		nodes.addAll(net.getTransitionNodes());
		nodes.addAll(net.getChannelPlaces());
		CSONCycleAlg cycleAlg = new CSONCycleAlg(net);

		for(Path path : cycleAlg.syncCycleTask(nodes)){
			for(Node node : path){
				if(node instanceof ChannelPlace)
					result.add((ChannelPlace)node);
			}
		}
		return result;
	}

	private Collection<TransitionNode> getUpperTransitionNodes(){
		Collection<TransitionNode> result = new ArrayList<TransitionNode>();
		Collection<ONGroup> upperGroups = bsonAlg.getUpperGroups(net.getGroups());

		for(ONGroup group : upperGroups){
			result.addAll(group.getTransitionNodes());
		}
		return result;
	}

	private Collection<Condition> getLowerConditions(){
		Collection<Condition> result = new ArrayList<Condition>();
		Collection<ONGroup> lowerGroups = bsonAlg.getLowerGroups(net.getGroups());

		for(ONGroup group : lowerGroups){
			result.addAll(group.getConditions());
		}
		return result;
	}

	public void infoMsg(String msg){
		logger.info(msg);
	}

	public void errMsg(String msg){
		logger.info(msg);
	}
}
