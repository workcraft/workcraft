package org.workcraft.plugins.cpog.untangling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jbpt.petri.Node;
import org.jbpt.petri.Place;
import org.workcraft.plugins.cpog.untangling.UntanglingNode.NodeType;

public class NodeList extends ArrayList<UntanglingNode> {

	/** Adds a node of the untangling's process        *
	 *  separating label and id into a unsorted list. **/
	void addNode(Node source) {

		boolean add = true;
		int id = Integer.parseInt(source.getLabel().replaceAll(".*-", ""));
		String label = source.getLabel().replaceAll("-.*", "");
		UntanglingNode nodeToAdd = null;

		if (source instanceof Place){
			nodeToAdd = new UntanglingNode(id, label, NodeType.PLACE);
		} else{
			nodeToAdd = new UntanglingNode(id, label, NodeType.TRANSITION);
		}

		for(int i = 0; i < this.size() && add; i++){
			if(this.get(i).getId() == nodeToAdd.getId()){
				add = false;
			}
		}
		if(add){
			this.add(nodeToAdd);
		}
	}

	/** Sort the list of the untangling's vertices by the id **/
	@SuppressWarnings("unchecked")
	void sortList() {
		Collections.sort(this, new Comparator() {

			@Override
			public int compare(Object node1, Object node2) {
				return (((UntanglingNode) node2).getId() < ((UntanglingNode) node1).getId()) ? 1 : -1;
			}

		});
	}

	/** Rename with a " _n " the transitions with same names but different *
	 *  id, in order to be coherent with partial order notation            **/
	void renameList() {

		for(int i = 0; i < this.size(); i++){
			int k = 1;
			for(int j = i+1; j < this.size(); j++){
				if(this.get(i).getLabel().equals(this.get(j).getLabel())){
					String replaceName = new String(this.get(j).getLabel());
					replaceName = replaceName.concat("_" + (k+1));
					k++;
					this.get(j).setLabel(replaceName);
				}
			}
		}

	}

}
