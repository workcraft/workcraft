package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.elements.ChannelPlace;


public class CSONStructureTask extends AbstractStructuralVerification{

	private SON net;

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

		infoMsg("-----------------Communication-SON Structure Verification-----------------");

		//group info
		infoMsg("Initialising selected groups and components...");
		ArrayList<Node> components = new ArrayList<Node>();
		for(ONGroup group : groups){
			components.addAll(group.getComponents());
		}

		infoMsg("Selected Groups : " +  net.toString(groups));

		ArrayList<ChannelPlace> relatedCPlaces = new ArrayList<ChannelPlace>();
		relatedCPlaces.addAll(getRelationAlg().getRelatedChannelPlace(groups));
		components.addAll(relatedCPlaces);

		infoMsg("Channel Places = " + relatedCPlaces.size());

		if(relatedCPlaces.isEmpty()){
			infoMsg("Task terminated: no communication abstractions in selected groups.");
			return;
		}

		//channel place relation
		infoMsg("Running component relation tasks...");
		Collection<ChannelPlace> task1 = cPlaceRelationTask(relatedCPlaces);
		Collection<ChannelPlace> task2 = cPlaceConTypeTask(relatedCPlaces);
		relationErrors.addAll(task1);
		relationErrors.addAll(task2);

		if(relationErrors.isEmpty() && relationErrors.isEmpty())
			infoMsg("Valid channel place relation.");
		else{
			hasErr = true;
			errNumber = errNumber + relationErrors.size();
			for(Node cPlace : task1)
				errMsg("ERROR : Invalid channel place relation (input/output size != 1).", cPlace);

			for(Node cPlace : task2)
				errMsg("ERROR : Invalid communication types (inconsistent input and output connection types).", cPlace);
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

		infoMsg("Component relation tasks complete.");

		//global cycle detection
		infoMsg("Running cycle detection task...");
		cycleErrors.addAll(getCSONPathAlg().cycleTask(components));

		if (cycleErrors.isEmpty() )
			infoMsg("Communication-SON is cycle free");
		else{
			hasErr = true;
			errNumber++;
			errMsg("ERROR : Communication-SON involves global cycle paths = "+ cycleErrors.size() + ".");
			int i = 1;
			for(Path cycle : cycleErrors){
				errMsg("Cycle " + i + ": " + cycle.toString(net));
				i++;
			}
		}

		infoMsg("Cycle detection task complete.\n");

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
