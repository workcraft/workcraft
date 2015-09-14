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
import org.workcraft.plugins.son.TimeConsistencySettings;
import org.workcraft.plugins.son.VisualSON;
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
	private WorkspaceEntry we;
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
		this.we = we;
		net=(SON)we.getModelEntry().getMathModel();
		initialise();
	}

	@Override
	public Result<? extends VerificationResult> run(
			ProgressMonitor<? super VerificationResult> monitor) {
		//VisualSON visualNet = (VisualSON)we.getModelEntry().getVisualModel();

		infoMsg("-------------------------Time Consistency Checking Result-------------------------");

		ArrayList<Node> components = new ArrayList<Node>();
		Collection<ONGroup> groups = net.getGroups();
		//Collection<ONGroup> groups = settings.getSelectedGroups();

		if(!groups.isEmpty()){
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

			for(Node node : components){
				ArrayList<String> result = timeConsistencyTask(node);
				if(!result.isEmpty()){
					infoMsg("Node:" + net.getNodeReference(node));
					for(String str : timeConsistencyTask(node)){
						errMsg("-"+str);
						totalErrNum++;
					}
				}
			}
		}

		logger.info("\n\nVerification-Result : "+ totalErrNum + " Error(s).");

		return new Result<VerificationResult>(Outcome.FINISHED);
	}


	private ArrayList<String> timeConsistencyTask(Node node){
		ArrayList<String> result = new ArrayList<String>();
		Collection<String> onResult = new ArrayList<String>();
		Collection<String> csonResult = new ArrayList<String>();
		Collection<String> bsonResult = new ArrayList<String>();

		if(node instanceof TransitionNode){
			//ON checking
			try {
				onResult.addAll(timeAlg.onConsistecy(node));
			} catch (InvalidStructureException e) {
				e.printStackTrace();
			}
			if(!onResult.isEmpty()){
				result.addAll(onResult);
			}
			//BSON checking
			if(upperTransitionNodes.contains(node)){
				bsonResult = timeAlg.bsonConsistency((TransitionNode)node, phases);
			}

			if(!bsonResult.isEmpty()){
				result.addAll(bsonResult);
			}
		}else if(node instanceof Condition){
			Condition c = (Condition)node;
			//ON checking
			try {
				onResult.addAll(timeAlg.onConsistecy(c));
			} catch (InvalidStructureException e) {
				e.printStackTrace();
			}
			if(!onResult.isEmpty()){
				result.addAll(onResult);
			}
			//BSON checking
			if(lowerConditions.contains(c) && c.isInitial()){
				bsonResult = timeAlg.bsonConsistency2(c);
			}
			if(lowerConditions.contains(c) && c.isFinal()){
				bsonResult = timeAlg.bsonConsistency3(c);
			}
			if(!bsonResult.isEmpty()){
				result.addAll(bsonResult);
			}
		}else if(node instanceof ChannelPlace){
			//CSON checking
			try {
				csonResult.addAll(timeAlg.csonConsistecy((ChannelPlace)node, syncCPs));
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
