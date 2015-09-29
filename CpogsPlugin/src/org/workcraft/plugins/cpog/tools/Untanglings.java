package org.workcraft.plugins.cpog.tools;


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


public class Untanglings {

	private NetSystem sys;
	private LinkedList<Place> p;
	private LinkedList<Transition> t;
	private UntanglingSetup setup;
	private ReductionBasedRepresentativeUntangling untangling;
	private ArrayList<String> partialOrders;

	public Untanglings(PnToCpogSettings settings){
		this.sys = new NetSystem();
		this.p = new LinkedList<Place>();
		this.t = new LinkedList<Transition>();
		this.setup = new UntanglingSetup();
		this.partialOrders = new ArrayList<String>();

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
	 *  untangling into a set of partial order graph  **/
	public ArrayList<String> getPartialOrders(){

		for(IProcess<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> pi : untangling.getProcesses()){

			String process = new String();
			ArrayList<UntanglingNode> transitions = new ArrayList<UntanglingNode>();

			// adding transitions into a list
			for(Flow edge : pi.getOccurrenceNet().getEdges()){

				if (edge.getSource() instanceof Transition){

					addTransition(edge.getSource(), transitions);

				}
			}

			// sorting the list
			sortTransitions(transitions);

			// renaming transitions showing up with same name but different id
			renameTransitions(transitions);

			// connecting transitions while skipping the places
			for(Flow edge1 : pi.getOccurrenceNet().getEdges()){

				if (edge1.getSource() instanceof Transition){

					for(Flow edge2 : pi.getOccurrenceNet().getEdges()){

						if(edge2.getSource().getLabel().equals(edge1.getTarget().getLabel())){

							process = process.concat(connectVertices(transitions, edge1.getSource(), edge2.getTarget(), process));

						}
					}
				}
			}
			partialOrders.add(process);
		}

		return partialOrders;

	}

	/** Adds a transition of the untangling's process  *
	 *  separating label and id into a unsorted list. **/
	private void addTransition(Node source, ArrayList<UntanglingNode> transitions) {

		boolean add = true;
		UntanglingNode nodeToAdd = new UntanglingNode(Integer.parseInt(source.getLabel().replaceAll(".*-", "")), source.getLabel().replaceAll("-.*", ""));
		for(int i = 0; i < transitions.size() && add; i++){
			if(transitions.get(i).getId() == nodeToAdd.getId()){
				add = false;
			}
		}
		if(add){
			transitions.add(nodeToAdd);
		}
	}

	/** Sort the list of the untangling's vertices by the id **/
	@SuppressWarnings("unchecked")
	private void sortTransitions(ArrayList<UntanglingNode> transitions) {
		Collections.sort(transitions, new Comparator() {

			@Override
			public int compare(Object node1, Object node2) {
				return (((UntanglingNode) node2).getId() < ((UntanglingNode) node1).getId()) ? 1 : -1;
			}

		});
	}

	/** Rename with a " _n " the transitions with same names but different *
	 *  id, in order to be coherent with partial order notation            **/
	private void renameTransitions(ArrayList<UntanglingNode> transitions) {

		for(int i = 0; i < transitions.size(); i++){
			int k = 1;
			for(int j = i+1; j < transitions.size(); j++){
				if(transitions.get(i).getLabel().equals(transitions.get(j).getLabel())){
					String replaceName = new String(transitions.get(j).getLabel());
					replaceName = replaceName.concat("_" + (k+1));
					k++;
					transitions.get(j).setLabel(replaceName);
				}
			}
		}

	}

	/** Connect two transitions by looking at the name  *
	 *  contained inside the list "transitions", where  *
	 *  each transitions has a different name          **/
	private String connectVertices(ArrayList<UntanglingNode> transitions,
			Node source, Node target, String process) {

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

		return (sourceName + "," + targetName + ";");

	}
}
