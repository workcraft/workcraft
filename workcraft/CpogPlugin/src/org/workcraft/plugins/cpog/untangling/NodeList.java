package org.workcraft.plugins.cpog.untangling;

import org.jbpt.petri.Node;
import org.jbpt.petri.Place;
import org.workcraft.plugins.cpog.untangling.UntanglingNode.NodeType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class NodeList extends ArrayList<UntanglingNode> {

    private final HashMap<Node, UntanglingNode> nodeToUntanglingNodMap = new HashMap<>();

    /** Adds a node of the untangling's process        *
     *  separating label and id into a unsorted list. **/
    public UntanglingNode addNode(Node node) {
        // check if the node is already present
        UntanglingNode result = nodeToUntanglingNodMap.get(node);

        if (result == null) {
            int id = Integer.parseInt(node.getLabel().replaceAll(".*-", ""));
            String label = node.getLabel().replaceAll("-.*", "");

            if (node instanceof Place) {
                result = new UntanglingNode(id, label, NodeType.PLACE);
            } else {
                result = new UntanglingNode(id, label, NodeType.TRANSITION);
            }
            nodeToUntanglingNodMap.put(node, result);
            this.add(result);
        }
        return result;
    }

    /** Sort the list of the untangling's vertices by the id **/
    public void sort() {
        this.sort(Comparator.comparingInt(UntanglingNode::getId));
    }

    /** Rename with a " _n " the node with same names but different *
     *  id, in order to be coherent with partial order notation    **/
    public void rename() {
        for (int i = 0; i < this.size(); i++) {
            int k = 1;
            for (int j = i + 1; j < this.size(); j++) {
                // get names of the nodes
                String formerNodeName = this.get(i).getLabel();
                String latterNodeName = this.get(j).getLabel();

                if (formerNodeName.equals(latterNodeName)) {
                    // append a number at the end representing
                    // the number of times that node occurs
                    String replaceName = latterNodeName.concat("_" + (k + 1));
                    k++;
                    this.get(j).setLabel(replaceName);
                }
            }
        }

    }

}
