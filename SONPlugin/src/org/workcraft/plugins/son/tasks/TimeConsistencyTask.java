package org.workcraft.plugins.son.tasks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.ScenarioRef;
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
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
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

		Collection<Node> checkList = new ArrayList<Node>();
		Collection<Node> unspecifyResult = new ArrayList<Node>();
		Collection<Node> outOfBoundResult = new ArrayList<Node>();
		Collection<Node> inconsistencyResult = new ArrayList<Node>();

		infoMsg("-------------------------Time Consistency Checking Result-------------------------");
		if(settings.getTabIndex() == 0){
			checkList = new ArrayList<Node>();
			Collection<ONGroup> groups = settings.getSelectedGroups();

			RelationAlgorithm relationAlg = new RelationAlgorithm(net);
			//group info
			infoMsg("Initialising selected groups and components...");

			for(ONGroup group : groups){
				checkList.addAll(group.getComponents());
			}

			infoMsg("Selected Groups : " +  net.toString(groups));

			ArrayList<ChannelPlace> relatedCPlaces = new ArrayList<ChannelPlace>();
			relatedCPlaces.addAll(relationAlg.getRelatedChannelPlace(groups));
			checkList.addAll(relatedCPlaces);

			infoMsg("Channel Places = " + relatedCPlaces.size()+"\n");

		}else if(settings.getTabIndex() == 1){
			infoMsg("Initialising selected scenario...");
			if(settings.getSeletedScenario()!=null)
				checkList = settings.getSeletedScenario().getNodes(net);
			infoMsg("Nodes = " + checkList.size()+"\n");

		}else if(settings.getTabIndex() == 2){
			//node info
			infoMsg("Initialising selected components...");
			checkList = settings.getSeletedNodes();
			infoMsg("Selected nodes = " + checkList.size()+"\n");

		}

		if(settings.getGranularity() == Granularity.YEAR_YEAR){
			infoMsg("Time granularity: T:year  D:year");
		}else if(settings.getGranularity() == Granularity.HOUR_MINS){
			infoMsg("Time granularity: T:24 hour clock  D:minutes");
			infoMsg("Running time granularity checking task...");
			for(Node node : checkList){
				ArrayList<String> result = timeAlg.granularityHourMinsTask(node);
				if(!result.isEmpty()){
					outOfBoundResult.add(node);
					infoMsg("Node:" + net.getNodeReference(node));
					for(String str : result){
						errMsg("-"+str);
					}
				}
			}
		}

		infoMsg("Remove invalid time granularity nodes from checking list...");
		checkList.removeAll(outOfBoundResult);

		infoMsg("Running unspecified value checking task...");
		for(Node node : checkList){
			ArrayList<String> result;
			if(settings.getTabIndex() == 1){
				result = unspecifiedValueTask(node, syncCPs, settings.getSeletedScenario());
			}else{
				result = unspecifiedValueTask(node, syncCPs, null);
			}

			if(!result.isEmpty()){
				unspecifyResult.add(node);
				infoMsg("Node:" + net.getNodeReference(node));
				for(String str : result){
					errMsg("-"+str);
				}
			}
		}

		infoMsg("Remove unspecified nodes from checking list...");
		checkList.removeAll(unspecifyResult);

		infoMsg("Running time consistency checking task...");
		for(Node node : checkList){
			infoMsg("Node:" + net.getNodeReference(node));
			ArrayList<String> result;
			if(settings.getTabIndex() == 1){
				result = timeConsistencyTask(node, settings.getSeletedScenario(), settings.getGranularity());
			}else{
				result = timeConsistencyTask(node, null, settings.getGranularity());
			}

			if(!result.isEmpty()){
				inconsistencyResult.add(node);
				for(String str : result){
					errMsg("-"+str);
					totalErrNum++;
				}
			}
		}

		inconsistencyHighlight(settings.getInconsistencyHighlight(), inconsistencyResult);
		unspecifyHighlight(settings.getUnspecifyHighlight(), unspecifyResult);

		logger.info("\n\nVerification-Result : "+ totalErrNum + " Error(s).");
		if(!SONSettings.getTimeVisibility()){
			timeAlg.removeProperties();
		}
		return new Result<VerificationResult>(Outcome.FINISHED);
	}

	public ArrayList<String> unspecifiedValueTask(Node node, Collection<ChannelPlace> syncCPs, ScenarioRef s){
		ArrayList<String> result = new ArrayList<String>();

		//check for unspecified value.
		try {
			if(!(node instanceof ChannelPlace)){
					result.addAll(timeAlg.specifiedValueChecking(node, false, s));
			}else{
				if(syncCPs.contains(node)){
					result.addAll(timeAlg.specifiedValueChecking(node, true, s));
				}else{
					result.addAll(timeAlg.specifiedValueChecking(node, false, s));
				}
			}
		} catch (InvalidStructureException e) {
			e.printStackTrace();
		}

		return result;
	}


	private ArrayList<String> timeConsistencyTask(Node node, ScenarioRef s, Granularity g){
		ArrayList<String> result = new ArrayList<String>();
		Collection<String> onResult = new ArrayList<String>();
		Collection<String> csonResult = new ArrayList<String>();
		Collection<String> bsonResult = new ArrayList<String>();

		if(node instanceof TransitionNode){
			//ON checking
			try {
				onResult.addAll(timeAlg.onConsistecy(node, s, g));
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
				onResult.addAll(timeAlg.onConsistecy(c, s, g));
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
				csonResult.addAll(timeAlg.csonConsistecy((ChannelPlace)node, syncCPs, s, g));
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

	private void inconsistencyHighlight(boolean b, Collection<Node> nodes){
		if(b){
			for(Node node : nodes){
				net.setForegroundColor(node, SONSettings.getRelationErrColor());
			}
		}
	}

	private void unspecifyHighlight(boolean b, Collection<Node> nodes){
		if(b){
			for(Node node : nodes){
				net.setForegroundColor(node, new Color(204,204,255));
			}
		}
	}

	public void infoMsg(String msg){
		logger.info(msg);
	}

	public void errMsg(String msg){
		logger.info(msg);
	}
}
