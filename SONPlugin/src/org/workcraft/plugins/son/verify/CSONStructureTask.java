package org.workcraft.plugins.son.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.algorithm.CSONPathAlg;
import org.workcraft.plugins.son.algorithm.RelationAlg;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Event;

public class CSONStructureTask implements SONStructureVerification{

	private SONModel net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private RelationAlg relation;
	private CSONPathAlg traverse;

	private Collection<ChannelPlace> cPlaceResult;
	private Collection<ChannelPlace> cPlaceConTypeResult;
	private Collection<ArrayList<Node>> cycleResult;
	private Collection<List<ChannelPlace>> cPlaceStructureResult;

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;

	public CSONStructureTask(SONModel net){
		this.net = net;
		relation = new RelationAlg(net);
		traverse = new CSONPathAlg(net);
	}

	public void task(Collection<ONGroup> groups){

		logger.info("-----------------Communication-SON Verification-----------------");

		//group info
		logger.info("Initialising selected groups components...");
		ArrayList<Node> components = new ArrayList<Node>();
		for(ONGroup group : groups){
			components.addAll(group.getComponents());
		}

		logger.info("Selected Groups = " +  groups.size());
		logger.info("Group Components = " + components.size());

		ArrayList<ChannelPlace> relatedcPlaces = new ArrayList<ChannelPlace>();
		relatedcPlaces.addAll(relation.getRelatedChannelPlace(groups));
		components.addAll(relatedcPlaces);

		logger.info("Channel Place(s) = " + relatedcPlaces.size());

		//channel place relation
		logger.info("Running Communication-SON relation check...");
		cPlaceResult = cPlaceRelationTask(relatedcPlaces);
		cPlaceConTypeResult = cPlaceConTypeTask(relatedcPlaces);
		if(cPlaceResult.isEmpty() && cPlaceConTypeResult.isEmpty())
			logger.info("Correct channel place relation.");
		else{
			hasErr = true;
			errNumber = errNumber + cPlaceResult.size() + cPlaceConTypeResult.size();
			for(ChannelPlace cPlace : cPlaceResult)
				logger.error("ERROR : Incorrect channel place relation: " + net.getName(cPlace) + "(" + net.getNodeLabel(cPlace) + ")  ");

			for(ChannelPlace cPlace : cPlaceConTypeResult)
				logger.error("ERROR : Incorrect communication types: " + net.getName(cPlace) + "(" + net.getNodeLabel(cPlace) + ")  ");
		}

		//channel place structure
		cPlaceStructureResult = cPlaceStructureTask(relatedcPlaces);
		if(cPlaceStructureResult.isEmpty())
			logger.info("Correct communication strucuture.");
		else{
			hasErr = true;
			errNumber = errNumber + cPlaceStructureResult.size();
			for (List<ChannelPlace> list : cPlaceStructureResult){
				ArrayList<String> cpName = new ArrayList<String>();
				for(ChannelPlace cPlace : list){
					cpName.add(net.getName(cPlace) + "(" + net.getNodeLabel(cPlace) + ")");
				}
				logger.error("ERROR : Incorrect communication structure:" + cpName.toString());
			}
		}


		//global cycle detection
		logger.info("Running cycle detection...");
		cycleResult = traverse.cycleTask(components);

		if (cycleResult.isEmpty() )
			logger.info("Acyclic checking correct");
		else{
			hasErr = true;
			errNumber++;
			logger.error("ERROR : global cycles = "+ cycleResult.size() + ".");
		}

		logger.info("Cycle detection complete.");

	}

	private Collection<ChannelPlace> cPlaceRelationTask(ArrayList<ChannelPlace> cPlaces){
		ArrayList<ChannelPlace> result = new ArrayList<ChannelPlace>();

		for(ChannelPlace cPlace : cPlaces){
			if(net.getPostset(cPlace).size() != 1 || net.getPreset(cPlace).size() != 1)
				result.add(cPlace);
		}
		return result;
	}

	private Collection<ChannelPlace> cPlaceConTypeTask(ArrayList<ChannelPlace> cPlaces){
		ArrayList<ChannelPlace> result = new ArrayList<ChannelPlace>();

		for(ChannelPlace cPlace : cPlaces){
			if(net.getSONConnectionTypes(cPlace).size() > 1)
				result.add(cPlace);
		}
		return result;
	}

	private Collection<List<ChannelPlace>> cPlaceStructureTask(ArrayList<ChannelPlace> cPlaces){
		Collection<List<ChannelPlace>> result = new HashSet<List<ChannelPlace>>();

		//input/output events of each channel place
		Map<ChannelPlace, String> cpRelation = new HashMap<ChannelPlace, String>();
		for(ChannelPlace cPlace : cPlaces){
			Event[] connectedEvents = new Event[2];
			if(net.getPreset(cPlace).size() == 1)
				for(Node node : net.getPreset(cPlace)){
					if(node instanceof Event)
						connectedEvents[0] = (Event)node;
				}
			else
				continue;

			if(net.getPostset(cPlace).size() == 1)
				for(Node node : net.getPostset(cPlace)){
					if(node instanceof Event)
						connectedEvents[1] = (Event)node;
				}
			else
				continue;

			String first = net.getName(connectedEvents[0]);
			String second = net.getName(connectedEvents[1]);
			String orderedEvents = nodesOrder(first, second);

			cpRelation.put(cPlace, orderedEvents);
		}

		Iterator<ChannelPlace> iterator = cpRelation.keySet().iterator();
		List<ChannelPlace> list = new ArrayList<ChannelPlace>();
		//number of channel places between two events
		Map<String, List<ChannelPlace>> cpBetTwoEvents = new HashMap<String, List<ChannelPlace>>();

		while(iterator.hasNext()){
			ChannelPlace key = iterator.next();
			String value = cpRelation.get(key);
			if (cpRelation.containsValue(value)) {
				if (cpBetTwoEvents.containsKey(value)) {
					list = (List<ChannelPlace>)cpBetTwoEvents.get(value);
				}else
					list = new ArrayList<ChannelPlace>();
			}
			list.add((ChannelPlace)key);
			cpBetTwoEvents.put((String)value, list);
		}

		for (List<ChannelPlace> cpList : cpBetTwoEvents.values()){

			if (cpList.size() > 2)
				result.add(cpList);
			if (cpList.size() == 2)
				if(!(net.getPostset(cpList.get(0)).containsAll(net.getPreset(cpList.get(1)))
						&& net.getSONConnectionTypes(cpList.get(0)).contains("ASYNLINE")
						&& net.getSONConnectionTypes(cpList.get(1)).contains("ASYNLINE"))){
					result.add(cpList);
				}
		}
		return result;
	}

	private String nodesOrder(String first, String second){
		String result = new String();

		String newFirst = first.replaceAll("e", "");
		String newSecond = second.replaceAll("e", "");

		Integer firstNumber = Integer.valueOf(newFirst);
		Integer secondNumber = Integer.valueOf(newSecond);

		if (firstNumber < secondNumber)
			result = "e" + firstNumber.toString() + "e" + secondNumber.toString();
		else
			result = "e" + secondNumber.toString() + "e" + firstNumber.toString();

		return result;
	}

	public void errNodesHighlight(){

		for(ChannelPlace cPlace : cPlaceResult){
			this.net.setFillColor(cPlace, SONSettings.getRelationErrColor());
		}

		for(ChannelPlace cPlace : cPlaceConTypeResult){
			this.net.setFillColor(cPlace, SONSettings.getRelationErrColor());
		}

		for(List<ChannelPlace> list : cPlaceStructureResult)
			for(ChannelPlace cPlace : list)
			this.net.setFillColor(cPlace, SONSettings.getRelationErrColor());


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
