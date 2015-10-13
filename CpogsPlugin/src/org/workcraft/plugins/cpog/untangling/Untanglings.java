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
import org.workcraft.plugins.cpog.untangling.UntanglingNode.NodeType;

import com.sun.org.apache.bcel.internal.generic.Type;



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
			NodeList transitions = new NodeList();
			NodeList places = new NodeList();

			// adding transitions into a list
			for(Flow edge : pi.getOccurrenceNet().getEdges()){
				if (edge.getSource() instanceof Transition){
					transitions.addNode(edge.getSource());
				}
			}

			// adding places into a list
			if (settings.isRemoveNodes() == false){
				for(Flow edge : pi.getOccurrenceNet().getEdges()){
					if (edge.getSource() instanceof Place){
						places.addNode(edge.getSource());
					}
					if (edge.getTarget() instanceof Place){
						places.addNode(edge.getTarget());
					}
				}
			}

			// sorting the lists
			transitions.sortList();
			if ( !settings.isRemoveNodes() ) {
				places.sortList();
			}

			// renaming transitions showing up with same name but different id
			transitions.renameList();
			if ( !settings.isRemoveNodes() ){
				places.renameList();
			}

			// connecting transitions while skipping the places
			if (settings.isRemoveNodes()){
				connectTransitionsOnly(pi, process, transitions);
			}

			// nodes need to be present
			else {
				connectTransitionsAndPlaces(pi, process, transitions, places);
			}

			partialOrders.add(process);
		}

		return partialOrders;

	}

	/** Connects nodes and transitions in order to build the partial order **/
	private void connectTransitionsAndPlaces(
			IProcess<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> pi,
			PartialOrder process, NodeList transitions, NodeList places) {

		for(Flow edge : pi.getOccurrenceNet().getEdges()){
			Node source = edge.getSource();
			Node target = edge.getTarget();
			if(edge.getSource() instanceof Place){
				// place to transition connection
				process.add(connectNodes(places, transitions, source, target));
			}else{
				// transition to place connection
				UntanglingEdge connection = connectNodes(transitions, places, source, target);
				process.add(connection);
			}

		}
	}

	/** Connect transitions in order to build a partial order. Places are skipped **/
	private void connectTransitionsOnly(
			IProcess<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> pi,
			PartialOrder process, NodeList transitions) {

		for(Flow edge1 : pi.getOccurrenceNet().getEdges()){
			if (edge1.getSource() instanceof Transition){
				for(Flow edge2 : pi.getOccurrenceNet().getEdges()){
					if(edge2.getSource().getLabel().equals(edge1.getTarget().getLabel())){
						Node source = edge1.getSource();
						Node target = edge2.getTarget();
						UntanglingEdge connection = connectNodes(transitions, null, source, target);
						process.add(connection);
					}
				}
			}
		}
	}

	/** Connect two nodes **/
	private UntanglingEdge connectNodes(NodeList listSource,
			NodeList listTarget, Node source, Node target) {

		int sourceId = Integer.parseInt(source.getLabel().replaceAll(".*-", ""));
		int targetId = Integer.parseInt(target.getLabel().replaceAll(".*-", ""));
		UntanglingNode first = null;
		UntanglingNode second = null;

		// connect two transitions
		if(source instanceof Transition && target instanceof Transition){
			for(int i = 0; i < listSource.size(); i++){
				if (listSource.get(i).getId() == sourceId){
					first = listSource.get(i);
				}
				if (listSource.get(i).getId() == targetId){
					second = listSource.get(i);
				}
			}
		} else {
			// connect a transition with a place
			for(int i = 0; i < listSource.size(); i++){
				if (listSource.get(i).getId() == sourceId){
					first = listSource.get(i);
				}
			}
			for(int i = 0; i < listTarget.size(); i++){
				if (listTarget.get(i).getId() == targetId){
					second = listTarget.get(i);
				}
			}
		}

		return new UntanglingEdge(first, second);

	}
}
