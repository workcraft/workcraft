package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.algorithm.BSONCycleAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.PathAlgorithm;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.util.Phase;


public class BSONStructureTask extends AbstractStructuralVerification{

	private SON net;

	private Collection<Node> relationErrors = new ArrayList<Node>();
	private Collection<Path> cycleErrors = new ArrayList<Path>();
	private Collection<ONGroup> groupErrors = new HashSet<ONGroup>();

	private BSONAlg bsonAlg;
	private BSONCycleAlg bsonCycleAlg;
	private Map<Condition, Collection<Phase>> allPhases;

	private int errNumber = 0;
	private int warningNumber = 0;

	public BSONStructureTask(SON net){
		super(net);
		this.net = net;

		bsonAlg = new BSONAlg(net);
		allPhases = bsonAlg.getAllPhases();
		bsonCycleAlg = new BSONCycleAlg(net, allPhases);
	}

	public void task(Collection<ONGroup> groups){

		infoMsg("-----------------Behavioral-SON Structure Verification-----------------");

		//group info
		infoMsg("Initialising selected groups and components...");
		ArrayList<Node> components = new ArrayList<Node>();

		for(ONGroup group : groups){
			components.addAll(group.getComponents());
		}

		infoMsg("Selected Groups : " +  net.toString(groups));

		if(!net.getSONConnectionTypes(components).contains(Semantics.BHVLINE)){
			infoMsg("Task terminated: no behavioural abstraction in selected groups.");
			return;
		}

		 ArrayList<ChannelPlace> relatedCPlaces = new ArrayList<ChannelPlace>();
		 relatedCPlaces.addAll(getRelationAlg().getRelatedChannelPlace(groups));
		 components.addAll(relatedCPlaces);

		//Upper-level group structure task
		infoMsg("Running model structure and component relation tasks...");
		infoMsg("Running Upper-level ON structure task...");
		groupErrors.addAll(groupTask1(groups));
		if(groupErrors.isEmpty())
			infoMsg("Valid upper-level ON structure.");
		else {
			for(ONGroup group : groupErrors)
				errMsg("ERROR: Invalid Upper-level ON structure (not line-like/has both input and output behavioural relations).", group);
		}
		infoMsg("Upper-level ON structure task complete.");

		//a/synchronous relation group task
		infoMsg("Running a/synchronous relation task...");
		Collection<ChannelPlace> task2 = groupTask2(groups);
		relationErrors.addAll(task2);
		if(relationErrors.isEmpty())
			infoMsg("Valid a/synchronous relation.");
		else{
			for(ChannelPlace cPlace : task2){
				errMsg("ERROR: Invalid BSON structure "
						+ "(A/Synchronous communication between upper and lower level ONs).", cPlace);
			}
		}
		infoMsg("A/synchronous relation task complete.");

		//phase decomposition task
		infoMsg("Running phase structure task...");
		Collection<ONGroup> upperGroups = getBSONAlg().getUpperGroups(groups);

		Map<Condition , String> phaseResult = phaseMainTask(upperGroups);
		if(!phaseResult.keySet().isEmpty()){
			for(Condition c : phaseResult.keySet()){
				errMsg(phaseResult.get(c));
				relationErrors.add(c);
			}
		}else
			infoMsg("Valid phase structure.");

		infoMsg("Phase checking tasks complete.");


		//BSON cycle task
		infoMsg("Running cycle detection task...");
		cycleErrors.addAll(getBSONCycleAlg().cycleTask(components));

		if (cycleErrors.isEmpty() )
			infoMsg("Behavioral-SON is cycle free.");
		else{
			errNumber++;
			errMsg("ERROR : Model involves BSCON cycle paths = "+ cycleErrors.size() + ".");
			int i = 1;
			for(Path cycle : cycleErrors){
				infoMsg("Cycle " + i + ": " + cycle.toString(net));
				i++;
			}
		}

		infoMsg("Cycle detection task complete.");
		infoMsg("Model strucuture and component relation tasks complete.\n");

		errNumber = errNumber + relationErrors.size();
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

	//correctness for A/SYN communication between upper and lower level ONs
	private Collection<ChannelPlace> groupTask2(Collection<ONGroup> groups){
		Collection<ChannelPlace> result = new HashSet<ChannelPlace>();
		Collection<ONGroup> upperGroups = getBSONAlg().getUpperGroups(groups);

		for(ChannelPlace cPlace : getRelationAlg().getRelatedChannelPlace(groups)){
			int inUpperGroup = 0;

			Collection<Node> connectedNodes = new HashSet<Node>();
			connectedNodes.addAll(net.getPostset(cPlace));
			connectedNodes.addAll(net.getPreset(cPlace));

			for(Node node : connectedNodes){
				for(ONGroup group : upperGroups){
					if(group.getComponents().contains(node))
						inUpperGroup ++;
				}
			}

			if(inUpperGroup < connectedNodes.size() && inUpperGroup != 0)
					result.add(cPlace);
			}

		return result;
	}

	private Map<Condition , String> phaseMainTask(Collection<ONGroup> upperGroups){
		Map<Condition, String> result = new HashMap<Condition, String>();

		for(ONGroup uGroup : upperGroups){
			result.putAll(phaseTask1(uGroup));
			result.putAll(phaseTask2(uGroup));
			result.putAll(phaseTask3(uGroup));

			Map<Phase, ONGroup> orderedPhases = new HashMap<Phase, ONGroup>();
			for(Condition c : uGroup.getConditions()){
				//get ordered phases for each upper level condition of uGroup
				orderedPhases.putAll(getOrderedPhases(c));
			}

			Collection<ONGroup> lowerGroups2 = getBSONAlg().getLowerGroups(uGroup);

			for(ONGroup lGroup : lowerGroups2){
				Map<Condition, Phase> phaseMap = new HashMap<Condition, Phase>();
				for(Condition c : uGroup.getConditions()){
					Collection<Phase> phases = getAllPhases().get(c);
					for(Phase phase : phases){
						if(net.getGroup(phase.iterator().next()) == lGroup){
							phaseMap.put(c, phase);
						}
					}
				}
				Collection<Path> paths= pathTask(lGroup);
				result.putAll(phaseTask4(paths, phaseMap));
			}
		}

		return result;
	}

	//check for upper level condition
	private Map<Condition , String> phaseTask1(ONGroup upperGroup){
		Map<Condition, String> result = new HashMap<Condition, String>();

		for(Condition c : upperGroup.getConditions()){
			String ref = net.getNodeReference(c);
			if(!getBSONAlg().isUpperCondition(c))
				result.put(c, "ERROR: Upper level condition does not has phase: " +ref);
		}
		return result;
	}

	//check for upper level initial/final state
	private Map<Condition , String> phaseTask2(ONGroup upperGroup){
		Map<Condition, String> result = new HashMap<Condition, String>();

		for(Condition c : upperGroup.getConditions()){
			Collection<Phase> phases = getAllPhases().get(c);
			String ref = net.getNodeReference(c);
			//the minimal phases of every initial state of upper group must also be the initial state of lower group
			if(getRelationAlg().isInitial(c)){
				Collection<Condition> minSet = getBSONAlg().getMinimalPhase(phases);

				for(Condition min : minSet)
					if(!getRelationAlg().isInitial(min)){
						result.put(c, "ERROR: The minimal phase of "+ref+ " does not reach initial state.");
						break;
					}
			}
			//the maximal phases of every final state of upper group must also be the final state of lower group
			if(getRelationAlg().isFinal(c)){
				Collection<Condition> maxSet = getBSONAlg().getMaximalPhase(getAllPhases().get(c));

				for(Condition max : maxSet)
					if(!getRelationAlg().isFinal(max)){
						result.put(c, "ERROR: The maximal phase of "+ref+ " does not reach final state.");
						break;
					}
			}
		}
		return result;
	}


	//check for joint
	private Map<Condition, String> phaseTask3(ONGroup upperGroup){
		Map<Condition, String> result = new HashMap<Condition, String>();

		for(Condition c : upperGroup.getConditions()){
			Condition pre = null;
			Collection<Phase> phases = getAllPhases().get(c);

			if(!getRelationAlg().getPrePNCondition(c).isEmpty())
				pre = getRelationAlg().getPrePNCondition(c).iterator().next();

			if(pre != null){
				Collection<Phase> prePhases = getAllPhases().get(pre);
				Collection<Condition> maxSet = getBSONAlg().getMaximalPhase(prePhases);

				for(Phase phase : phases){
					boolean match = false;
					Collection<Condition> min = getBSONAlg().getMinimalPhase(phase);
					if(maxSet.containsAll(min))
						match = true;

					if(!match){
						match = true;
						ONGroup lowGroup = net.getGroup(phase.iterator().next());
						boolean containFinal = false;


						if(!min.containsAll(getRelationAlg().getONInitial(lowGroup.getConditions()))){
							match = false;
						}
						for(ONGroup group : getBSONAlg().getLowerGroups(pre)){
							if(maxSet.containsAll(getRelationAlg().getONFinal(group.getConditions()))){
								containFinal = true;
								break;
							}
						}
						if(!containFinal)
							match = false;
					}

					if(!match){
						String ref = net.getNodeReference(c);
						String ref2 = net.getNodeReference(pre);
						result.put(c, "ERROR: Disjoint phases between " + ref + " and " + ref2);
					}
				}
			}
		}
		return result;
	}

	//check for cut
	//input: 1 path collection from initial to final state
	//       2.phase collection between one upper group and one lower group
	private Map<Condition, String> phaseTask4(Collection<Path> paths, Map<Condition, Phase> phases){
		 Map<Condition, String> result = new HashMap<Condition, String>();

		for(Condition c : phases.keySet()){
			String ref = net.getNodeReference(c);
			Phase phase = phases.get(c);

			Collection<Condition> minimal = getBSONAlg().getMinimalPhase(phase);
			Collection<Condition> maximal = getBSONAlg().getMaximalPhase(phase);
			boolean minErr = false;
			boolean maxErr = false;

			//check if minimal/maximal phase is a cut
			for(Path path : paths){
				int minCount = 0;
				int maxCount = 0;

				for(Condition min : minimal)
					if(path.contains(min)) {
						minCount++;
					}

				if (minCount != 1){
					minErr = true;
				}

				for(Condition max : maximal)
					if(path.contains(max)) {
						maxCount++;
					}
				if (maxCount != 1){
					maxErr = true;
				}
			}

			if(minErr)
				result.put(c, "ERROR: Minimal phase " +net.toString(minimal) + " is not a cut: "+ref);

			if(maxErr)
				result.put(c, "ERROR: Maximal phase " +net.toString(maximal) + " is not a cut: "+ref);
		}
		return result;
	}


	private Collection<Path> pathTask (ONGroup group){
		List<Path> result = new ArrayList<Path>();
		PathAlgorithm alg = new PathAlgorithm(net);

		for(Node start : getRelationAlg().getONInitial(group.getConditions()))
			for(Node end : getRelationAlg().getONFinal(group.getConditions())){
				result.addAll(alg.getPaths(start, end, group.getComponents()));
			}

		 return result;
	}

	/**
	 * get the ordered phase map for a given upper-level conditions.
	 */
	private Map<Phase, ONGroup> getOrderedPhases(Condition c){
		Map<Phase, ONGroup> result = new HashMap<Phase, ONGroup>();
		for(Phase phase : getAllPhases().get(c)){
			ONGroup group = net.getGroup(phase.iterator().next());
			result.put(phase, group);
		}

		return result;
	}

	public BSONAlg getBSONAlg(){
		return this.bsonAlg;
	}

	public BSONCycleAlg getBSONCycleAlg(){
		return bsonCycleAlg;
	}

	public Map<Condition, Collection<Phase>> getAllPhases(){
		return allPhases;
	}

	@Override
	public Collection<String> getRelationErrors() {
		return getRelationErrorsSetRefs(relationErrors);
	}

	@Override
	public Collection<ArrayList<String>> getCycleErrors() {
		return getCycleErrorsSetRefs(cycleErrors);
	}

	@Override
	public Collection<String> getGroupErrors() {
		return getGroupErrorsSetRefs(groupErrors);
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
