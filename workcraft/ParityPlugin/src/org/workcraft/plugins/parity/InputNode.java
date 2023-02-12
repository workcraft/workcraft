package org.workcraft.plugins.parity;

import java.util.ArrayList;

/**
 * InputNode class is used to represent part of the model of a parity game.
 * Each node is one of the vertices in the parity game, and through the outgoing
 * ArrayList will also store the edge information.
 *
 * An ArrayList of InputNodes represent the whole game.
 */
public class InputNode {
    /**
     * Automatically determined identifier. User does NOT decide the identifier;
     * this is separate to the name of the node in the workcraft model.
     */
    Integer id;

    /**
     * Vertex priority
     */
    Integer priority;

    /**
     * Determines which player owns the vertex. Will be false if Player 0 owns
     * the vertex, and true if Player 1 owns it.
     */
    Boolean ownedBy;

    /**
     * ArrayList of identifiers of other input nodes that have an outgoing edge
     * from the current node to those.
     *
     * i.e. if this node has an id of 1, and an outgoing of {2,3,4}, there will
     * be edges 1->2, 1->3, and 1->4.
     */
    ArrayList<Integer> outgoing;

    /**
     * Constructor.
     * @param id
     * @param priority
     * @param ownedBy
     * @param outgoing
     */
    public InputNode(Integer id, Integer priority, Boolean ownedBy,
            ArrayList<Integer> outgoing) {
        this.id = id;
        this.priority = priority;
        this.ownedBy = ownedBy;
        this.outgoing = outgoing;
    }

    /**
     * Get the identifier of an input node
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Get the priority of an input node
     * @return priority
     */
    public Integer getPrio() {
        return priority;
    }

    /**
     * Get the ownership of an input node
     * @return ownedBy
     */
    public Boolean getOwnership() {
        return ownedBy;
    }

    /**
     * Get the ArrayList of identifiers that the current node is connected to
     * through outgoing edges
     * @return outgoing
     */
    public ArrayList<Integer> getOutgoing() {
        return outgoing;
    }

}