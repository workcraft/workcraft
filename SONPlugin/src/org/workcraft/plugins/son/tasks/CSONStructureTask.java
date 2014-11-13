package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.elements.ChannelPlace;


public class CSONStructureTask extends AbstractStructuralVerification{

	private SON net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private Collection<Node> relationErrors = new ArrayList<Node>();
	private Collection<Path> cycleErrors = new ArrayList<Path>();
	private Collection<ONGroup> groupErrors = new HashSet<ONGroup>();

	private boolean hasErr = false;
	private int errNumber = 0;
	private int warningNumber = 0;

	public CSONStructureTask(SON net){
		super(net);
		this.net = net;
	}

	public void task(Collection<ONGroup> groups){

		logger.info("-----------------Communication-SON Verification-----------------");

		//group info
		logger.info("Initialising selected group components...");
		ArrayList<Node> components = new ArrayList<Node>();
		for(ONGroup group : groups){
			components.addAll(group.getComponents());
		}

		logger.info("Selected Groups = " +  groups.size());
		logger.info("Group Components = " + components.size());

		ArrayList<ChannelPlace> relatedcPlaces = new ArrayList<ChannelPlace>();
		relatedcPlaces.addAll(getRelationAlg().getRelatedChannelPlace(groups));
		components.addAll(relatedcPlaces);

		logger.info("Channel Place(s) = " + relatedcPlaces.size());

		if(relatedcPlaces.isEmpty()){
			logger.info("Task termination: no a/synchronous connections in selected groups.");
			return;
		}

		//channel place relation
		logger.info("Running model structure and components relation check...");
		Collection<ChannelPlace> task1 = cPlaceRelationTask(relatedcPlaces);
		Collection<ChannelPlace> task2 = cPlaceConTypeTask(relatedcPlaces);
		relationErrors.addAll(task1);
		relationErrors.addAll(task2);

		if(relationErrors.isEmpty() && relationErrors.isEmpty())
			logger.info("Correct channel place relation.");
		else{
			hasErr = true;
			errNumber = errNumber + relationErrors.size();
			for(Node cPlace : task1)
				logger.error("ERROR : Incorrect channel place relation: " + net.getNodeReference(cPlace) + "(" + net.getComponentLabel(cPlace) + ")  : " +
						"input/output size != 1");

			for(Node cPlace : task2)
				logger.error("ERROR : Incorrect communication types: " + net.getNodeReference(cPlace) + "(" + net.getComponentLabel(cPlace) + ")  :" +
						"different input and output connection types");
		}

/*		//channel place structure
		//cPlaceStructureResult = cPlaceStructureTask(relatedcPlaces);
		cPlaceStructureResult = new ArrayList<List<ChannelPlace>>();
		if(cPlaceStructureResult.isEmpty())
			logger.info("Correct communication strucuture.");
		else{
			hasErr = true;
			errNumber = errNumber + cPlaceStructureResult.size();
			for (List<ChannelPlace> list : cPlaceStructureResult){
				ArrayList<String> cpName = new ArrayList<String>();
				for(ChannelPlace cPlace : list){
					cpName.add(net.getName(cPlace) + "(" + net.getComponentLabel(cPlace) + ")");
				}
				logger.error("ERROR : Incorrect communication structure:" + cpName.toString());
			}
		}  */

		logger.info("Model strucuture and components relation task complete.");

		//global cycle detection
		logger.info("Running cycle detection...");
		cycleErrors.addAll(getCSONPathAlg().cycleTask(components));

		if (cycleErrors.isEmpty() )
			logger.info("Acyclic structure correct");
		else{
			hasErr = true;
			errNumber++;
			logger.error("ERROR : model involves global cycle paths = "+ cycleErrors.size() + ".");
			int i = 1;
			for(Path cycle : cycleErrors){
				logger.error("Cycle " + i + ": " + cycle.toString(net));
				i++;
			}
		}

		logger.info("Cycle detection complete.\n");

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

/*	private Collection<List<ChannelPlace>> cPlaceStructureTask(ArrayList<ChannelPlace> cPlaces){
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

		int compare = first.compareTo(second);

		if (compare>0)
			result = first + second;
		else if (compare < 0)
			result = second + first;
		else
			System.err.println("same nodes"+this.toString());

		return result;
	}*/

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
