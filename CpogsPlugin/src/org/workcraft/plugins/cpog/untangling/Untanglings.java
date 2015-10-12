package org.workcraft.plugins.cpog.untangling;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.jbpt.petri.Flow;
import org.jbpt.petri.Marking;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.jbpt.petri.unfolding.BPNode;
import org.jbpt.petri.unfolding.Condition;
import org.jbpt.petri.unfolding.Event;
import org.jbpt.petri.untangling.IProcess;
import org.jbpt.petri.untangling.ReductionBasedRepresentativeUntangling;
import org.jbpt.petri.untangling.SignificanceCheckType;
import org.jbpt.petri.untangling.UntanglingSetup;
import org.workcraft.plugins.cpog.PnToCpogSettings;
import org.workcraft.plugins.cpog.untangling.Node.Type;



public class Untanglings {

	private NetSystem sys;
	private LinkedList<Place> p;
	private LinkedList<Transition> t;
	private UntanglingSetup setup;
	private ReductionBasedRepresentativeUntangling untangling;
	private ArrayList<PartialOrder> partialOrders;

	public Untanglings(PnToCpogSettings settings){
		this.sys = new NetSystem();
		this.p = new LinkedList<Place>();
		this.t = new LinkedList<Transition>();
		this.setup = new UntanglingSetup();
		this.partialOrders = new ArrayList<PartialOrder>();

		// settings
		this.setup.ISOMORPHISM_REDUCTION = settings.isIsomorphism();
		this.setup.REDUCE = settings.isReduce();
		switch(settings.getSignificance()){
			case 0 :
				this.setup.SIGNIFICANCE_CHECK = SignificanceCheckType.EXHAUSTIVE;
				break;
			case 1 :
				this.setup.SIGNIFICANCE_CHECK = SignificanceCheckType.HASHMAP_BASED;
				break;
			case 2 :
				this.setup.SIGNIFICANCE_CHECK = SignificanceCheckType.TREE_OF_RUNS;
				break;
		}

	}

	/** adds place inside the conversion system **/
	public boolean addPlace(String placeName){
		if(p.add(new Place(placeName))){
			return true;
		}
		return false;
	}

	/** adds token inside a place inside the conversion system **/
	public boolean insertTokens(String placeName, int tokens){

		for(Place place : p){
			if (place.getLabel().equals(placeName)){
				sys.putTokens(place, tokens);

				// debug printing: tokens inserted inside the place
				// System.out.println(place.getLabel() + " = " + tokens);

				return true;
			}
		}

		return false;
	}

	/** adds transition inside the conversion system **/
	public boolean addTransition(String transitionName){
		if(t.add(new Transition(transitionName))){
			return true;
		}
		return false;
	}

	/** adds a connection from a place to a transition **/
	public boolean placeToTransition(String node1, String node2){

		for(Place place : p){

			// checking existence of the place
			if(place.getName().equals(node1)){
				// checking existence of the transition
				for (Transition transition : t){
					if(transition.getName().equals(node2)){
						// adding arc to the system
						sys.addFlow(place, transition);

						// debug: printing connection added
						// System.out.println(place.getName() + " -> " + transition.getName());

						return true;
					}
				}
			}
		}

		// if the two nodes are not present the connection
		// is not inserted
		return false;
	}

	/** adds a connection from a transition to a place  **/
	public boolean transitionToPlace(String node1, String node2){

		for(Transition transition : t){
			// checking existence of the place
			if(transition.getName().equals(node1)){
				// checking existence of the transition
				for (Place place : p){
					if(place.getName().equals(node2)){
						// adding arc to the system
						sys.addFlow(transition, place);

						// debug: printing connection added
						// System.out.println(transition.getName() + " -> " + place.getName());

						return true;
					}
				}
			}
		}

		// if the two nodes are not present the connection
		// is not inserted
		return false;
	}

	/** converts the Petri net introduced into multiple *
	 *  processes which compose the untangling         **/
	public boolean startConversion(){


		// starting conversion
		untangling = new ReductionBasedRepresentativeUntangling(sys,setup);

		// if Petri Net is not safe, stop the conversion
		if(untangling.isSafe() == false){
			System.out.println("Untangling cannot be constructed because the Petri Net is not safe.");
			return false;
		}
		// checking correct execution of conversion
		for(IProcess<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> pi : untangling.getProcesses()){

			if(pi.getOccurrenceNet().getVertices().isEmpty() == false){

				// printing out how many processes are needed to represent the untangling representation
				System.out.println("Number of untangled processes: " + untangling.getProcesses().size());

				return true;
			}
		}

		return false;
	}

	/** converts the set of processes that compose the *
	 *  untangling into a set of partial order graph
	 * @param settings **/
	public ArrayList<PartialOrder> getPartialOrders(PnToCpogSettings settings){

		for(IProcess<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> pi : untangling.getProcesses()){

			PartialOrder process = new PartialOrder();
			ArrayList<UntanglingNode> transitions = new ArrayList<UntanglingNode>();
			ArrayList<UntanglingNode> places = new ArrayList<UntanglingNode>();

			// adding transitions into a list
			for(Flow edge : pi.getOccurrenceNet().getEdges()){
				if (edge.getSource() instanceof Transition){
					addNode(edge.getSource(), transitions);
				}
			}

			// adding places into a list
			if (settings.isRemoveNodes() == false){
				for(Flow edge : pi.getOccurrenceNet().getEdges()){
					if (edge.getSource() instanceof Place){
						addNode(edge.getSource(), places);
					}
					if (edge.getTarget() instanceof Place){
						addNode(edge.getTarget(), places);
					}
				}
			}

			// sorting the lists
			sortList(transitions);
			if (settings.isRemoveNodes() == false){
				sortList(places);
			}

			// renaming transitions showing up with same name but different id
			renameList(transitions);
			if (settings.isRemoveNodes() == false){
				renameList(places);
			}

			// connecting transitions while skipping the places
			if(settings.isRemoveNodes() == true){
				for(Flow edge1 : pi.getOccurrenceNet().getEdges()){
					if (edge1.getSource() instanceof Transition){
						for(Flow edge2 : pi.getOccurrenceNet().getEdges()){
							if(edge2.getSource().getLabel().equals(edge1.getTarget().getLabel())){
								process.add(connectTransitions(transitions, edge1.getSource(), edge2.getTarget()));
							}
						}
					}
				}
			}

			// nodes need to be present
			else{
				for(Flow edge : pi.getOccurrenceNet().getEdges()){
					if(edge.getSource() instanceof Place){
						// place to transition connection
						process.add(connectPlaceAndTransition(places, transitions, edge.getSource(), edge.getTarget(), true));
					}else{
						// transition to place connection
						process.add(connectPlaceAndTransition(transitions, places, edge.getSource(), edge.getTarget(), false));
					}

				}
			}

			partialOrders.add(process);
		}

		return partialOrders;

	}

	/** Adds a node of the untangling's process        *
	 *  separating label and id into a unsorted list. **/
	private void addNode(Node source, ArrayList<UntanglingNode> list) {

		boolean add = true;
		UntanglingNode nodeToAdd = new UntanglingNode(Integer.parseInt(source.getLabel().replaceAll(".*-", "")), source.getLabel().replaceAll("-.*", ""));
		for(int i = 0; i < list.size() && add; i++){
			if(list.get(i).getId() == nodeToAdd.getId()){
				add = false;
			}
		}
		if(add){
			list.add(nodeToAdd);
		}
	}

	/** Sort the list of the untangling's vertices by the id **/
	@SuppressWarnings("unchecked")
	private void sortList(ArrayList<UntanglingNode> list) {
		Collections.sort(list, new Comparator() {

			@Override
			public int compare(Object node1, Object node2) {
				return (((UntanglingNode) node2).getId() < ((UntanglingNode) node1).getId()) ? 1 : -1;
			}

		});
	}

	/** Rename with a " _n " the transitions with same names but different *
	 *  id, in order to be coherent with partial order notation            **/
	private void renameList(ArrayList<UntanglingNode> list) {

		for(int i = 0; i < list.size(); i++){
			int k = 1;
			for(int j = i+1; j < list.size(); j++){
				if(list.get(i).getLabel().equals(list.get(j).getLabel())){
					String replaceName = new String(list.get(j).getLabel());
					replaceName = replaceName.concat("_" + (k+1));
					k++;
					list.get(j).setLabel(replaceName);
				}
			}
		}

	}

	/** Connect two transitions by looking at the name  *
	 *  contained inside the list "transitions", where  *
	 *  each transitions has a different name          **/
	private Edge connectTransitions(ArrayList<UntanglingNode> transitions,
			Node source, Node target) {

		int sourceId = Integer.parseInt(source.getLabel().replaceAll(".*-", ""));
		int targetId = Integer.parseInt(target.getLabel().replaceAll(".*-", ""));
		String sourceName = new String();
		String targetName = new String();

		for(int i = 0; i < transitions.size(); i++){
			if (transitions.get(i).getId() == sourceId){
				sourceName = transitions.get(i).getLabel();
			}
			if (transitions.get(i).getId() == targetId){
				targetName = transitions.get(i).getLabel();
			}
		}

		org.workcraft.plugins.cpog.untangling.Node first = new org.workcraft.plugins.cpog.untangling.Node(sourceName, Type.TRANSITION);
		org.workcraft.plugins.cpog.untangling.Node second = new org.workcraft.plugins.cpog.untangling.Node(targetName, Type.TRANSITION);

		return new Edge(first, second);

	}

	/** Connect places and transitions by looking at the *
	 *  name contained inside two lists, where           *
	 *  transitions and places have different names     **/
	private Edge connectPlaceAndTransition(ArrayList<UntanglingNode> listSource,
			ArrayList<UntanglingNode> listTarget, Node source, Node target,
			boolean placeFirst) {

		int sourceId = Integer.parseInt(source.getLabel().replaceAll(".*-", ""));
		int targetId = Integer.parseInt(target.getLabel().replaceAll(".*-", ""));
		String sourceName = new String();
		String targetName = new String();

		for(int i = 0; i < listSource.size(); i++){
			if (listSource.get(i).getId() == sourceId){
				sourceName = listSource.get(i).getLabel();
			}
		}
		for(int i = 0; i < listTarget.size(); i++){
			if (listTarget.get(i).getId() == targetId){
				targetName = listTarget.get(i).getLabel();
			}
		}

		org.workcraft.plugins.cpog.untangling.Node first;
		org.workcraft.plugins.cpog.untangling.Node second;
		if(placeFirst){
			first = new org.workcraft.plugins.cpog.untangling.Node(sourceName, Type.PLACE);
			second = new org.workcraft.plugins.cpog.untangling.Node(targetName, Type.TRANSITION);
		}else{
			first = new org.workcraft.plugins.cpog.untangling.Node(sourceName, Type.TRANSITION);
			second = new org.workcraft.plugins.cpog.untangling.Node(targetName, Type.PLACE);
		}

		return new Edge(first, second);
	}
}
