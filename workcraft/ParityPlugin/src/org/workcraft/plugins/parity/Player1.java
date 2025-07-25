package org.workcraft.plugins.parity;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

@DisplayName("Player1")
@IdentifierPrefix("p1")
@VisualClass(VisualPlayer1.class)

/**
 * Player 1 class.
 * This is a vertex belonging to Player 1 in a Parity game.
 * Player 1 is wins if the largest infinitely appearing number is odd.
 */
public class Player1 extends MathNode {

    /**
     * Identifying title of the Symbol
     */
    public static final String PROPERTY_SYMBOL = "Symbol";

    /**
     * Identifying title of the Priority
     */
    public static final String PROPERTY_PRIORITY = "Priority";

    /**
     * Identifying title of the vertex identifier
     */
    public static final String PROPERTY_ID = "ID";

    /**
     * Vertex priority
     */
    private int priority = 0;

    /**
     * Vertex identifier. Automatically determined and not selected by the user.
     * Separate to the name of the vertex.
     */
    private int id;

    /**
     * MathNode Symbol associated with this vertex
     */
    private Symbol symbol;

    /**
     * Void constructor with no input.
     */
    public Player1() {

    }

    /**
     * Constructor that will instantiate a symbol inside the vertex
     * if one has been provided.
     * @param symbol    Input symbol
     */
    public Player1(Symbol symbol) {
        super();
        this.symbol = symbol;
    }

    /**
     * Get the symbol associated with this vertex
     * @return symbol
     */
    public Symbol getSymbol() {
        return symbol;
    }

    /**
     * Set the symbol for this vertex
     * @param value    Symbol to change the symbol member to in Player 1
     */
    public void setSymbol(Symbol value) {
        if (symbol != value) {
            symbol = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_SYMBOL));
        }
    }

    /**
     * Get the priority value associated with this vertex
     * @return priority
     */
    public int getPrio() {
        return priority;
    }

    /**
     * Set the priority value associated with this vertex
     * @param prio    Priority to change the priority member to in Player 1
     */
    public void setPrio(int prio) {
        priority = prio;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_PRIORITY));
    }

    /**
     * Get the identity associated with this vertex
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Set the identity associated with this vertex
     * @param identity    Value to change the identifier of Player 1 to
     */
    public void setId(int identity) {
        id = identity;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_ID));
    }

}
