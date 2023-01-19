package org.workcraft.plugins.parity;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.FormatException;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.tools.CommentGeneratorTool;
import org.workcraft.gui.tools.ConnectionTool;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@DisplayName("Parity Game")
@ShortName("parity")

/**
 * Visual model of the Parity game. 
 * Subclass of AbstractVisualModel.
 */
public class VisualParity extends AbstractVisualModel {

    /**
     * Constructor where only the Parity game model is provided.
     * @param model    Model of Parity game
     */
    public VisualParity(Parity model) {
        this(model, null);
    }

    /**
     * Constructor where the Parity game model is provided, as well as the root
     * of a pre-existing VisualGroup.
     * @param model    Model of Parity game
     * @param root     Root of the VisualGroup. Contains the whole collection,
     *                 but points to root of collection.
     */
    public VisualParity(Parity model, VisualGroup root) {
        super(model, root);
    }

    /**
     * Add graph editor tools to the interface for Parity games.
     * This allows components to be placed on the game.
     */
    @Override
    public void registerGraphEditorTools() {
        addGraphEditorTool(new SelectionTool(true, false, true, true));
        addGraphEditorTool(new CommentGeneratorTool());
        addGraphEditorTool(new ConnectionTool(false, true, true));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(Player0.class)));
        addGraphEditorTool(new NodeGeneratorTool(new DefaultNodeGenerator(Player1.class)));
    }

    /**
     * Get the underlying Math Model for the Parity game.
     * @return    Current Parity game
     */
    @Override
    public Parity getMathModel() {
        return (Parity) super.getMathModel();
    }

    /**
     * Gather the properties of all components within the Graph game.
     * @param node           A VisualNode used to build the output from
     * @return properties    Model properties of the whole graph game
     */
    @Override
    public ModelProperties getProperties(VisualNode node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            Container container = NamespaceHelper.getMathContainer(this, getCurrentLevel());
            List<Symbol> symbols = new ArrayList<>(getMathModel().getSymbols(container));
            Collections.sort(symbols, Comparator.comparing(getMathModel()::getNodeReference));
            for (final Symbol symbol : symbols) {
                properties.add(getSymbolProperty(symbol));
            }
        } else if (node instanceof VisualPlayer0) {
            properties.add(getPlayer0SymbolProperty((VisualPlayer0) node));
        } else if (node instanceof VisualPlayer1) {
            properties.add(getPlayer1SymbolProperty((VisualPlayer1) node));
        } 

        return properties;
    }

    /**
     * Gather all of the Visual components of vertices owned by player 0.
     * @return    Player 0 vertices
     */
    public Collection<VisualPlayer0> getVisualPlayer0() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualPlayer0.class);
    }

    /**
     * Gather all of the Visual components of vertices owned by player 1.
     * @return    Player 1 vertices
     */
    public Collection<VisualPlayer1> getVisualPlayer1() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualPlayer1.class);
    }

    /**
     * Gather all of the Visual edge components.
     * @return    Visual edges
     */
    public Collection<VisualConnection> getEdges() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualConnection.class);
    }

    /**
     * Get the property symbols from all possible components in the Model, using
     * a symbol as the root. 
     * @param symbol    Root Symbol member
     * @return          PropertyDescriptor of the whole Model
     */
    private PropertyDescriptor getSymbolProperty(Symbol symbol) {
        return new PropertyDeclaration<>(String.class, getMathModel().getName(symbol) + " name",
                value -> {
                    Node node = getMathModel().getNodeByReference(value);
                    if (node == null) {
                        getMathModel().setName(symbol, value);
                        for (Player0 event: getMathModel().getPlayer0(symbol)) {
                            event.sendNotification(new PropertyChangedEvent(event, Player0.PROPERTY_SYMBOL));
                        }
                        for (Player1 event: getMathModel().getPlayer1(symbol)) {
                            event.sendNotification(new PropertyChangedEvent(event, Player1.PROPERTY_SYMBOL));
                        }
                    } else if (!(node instanceof Symbol)) {
                        throw new FormatException("Node '" + value + "' already exists and it is not a symbol.");
                    }
                },
                () -> getMathModel().getName(symbol));
    }

    /**
     * Get the Vertex objects of Player 0
     * @param event    Some kind of event that forces the Player 0 vertices to
     *                 update
     * @return         PropertyDescriptor of the whole Model
     */
    private PropertyDescriptor getPlayer0SymbolProperty(VisualPlayer0 event) {
        return new PropertyDeclaration<>(String.class, Player0.PROPERTY_SYMBOL,
                value -> {
                    Symbol symbol = null;
                    if (!value.isEmpty()) {
                        Node node = getMathModel().getNodeByReference(value);
                        if (node instanceof Symbol) {
                            symbol = (Symbol) node;
                        } else {
                            symbol = getMathModel().createSymbol(value);
                        }
                    }
                    event.getReferencedComponent().setSymbol(symbol);
                },
                () -> {
                    Symbol symbol = event.getReferencedComponent().getSymbol();
                    String symbolName = "";
                    if (symbol != null) {
                        symbolName = getMathModel().getName(symbol);
                    }
                    return symbolName;
                })
                .setCombinable().setTemplatable();
    }

    /**
     * Get the Vertex objects of Player 1
     * @param event    Some kind of event that forces the Player 1 vertices to
     *                 update
     * @return         PropertyDescriptor of the whole Model
     */
    private PropertyDescriptor getPlayer1SymbolProperty(VisualPlayer1 event) {
        return new PropertyDeclaration<>(String.class, Player1.PROPERTY_SYMBOL,
                value -> {
                    Symbol symbol = null;
                    if (!value.isEmpty()) {
                        Node node = getMathModel().getNodeByReference(value);
                        if (node instanceof Symbol) {
                            symbol = (Symbol) node;
                        } else {
                            symbol = getMathModel().createSymbol(value);
                        }
                    }
                    event.getReferencedComponent().setSymbol(symbol);
                },
                () -> {
                    Symbol symbol = event.getReferencedComponent().getSymbol();
                    String symbolName = "";
                    if (symbol != null) {
                        symbolName = getMathModel().getName(symbol);
                    }
                    return symbolName;
                })
                .setCombinable().setTemplatable();
    }

}