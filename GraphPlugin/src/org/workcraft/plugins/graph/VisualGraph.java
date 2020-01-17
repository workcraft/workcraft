package org.workcraft.plugins.graph;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.FormatException;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.tools.*;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.graph.tools.GraphSimulationTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@DisplayName("Directed Graph")
@ShortName("graph")
public class VisualGraph extends AbstractVisualModel {

    public VisualGraph(Graph model) {
        this(model, null);
    }

    public VisualGraph(Graph model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new SelectionTool(true, false, true, true));
        tools.add(new CommentGeneratorTool());
        tools.add(new ConnectionTool(false, true, true));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(Vertex.class)));
        tools.add(new GraphSimulationTool());
        setGraphEditorTools(tools);
    }

    @Override
    public Graph getMathModel() {
        return (Graph) super.getMathModel();
    }

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
        } else if (node instanceof VisualVertex) {
            properties.add(getVertexSymbolProperty((VisualVertex) node));
        }
        return properties;
    }

    private PropertyDescriptor getSymbolProperty(Symbol symbol) {
        return new PropertyDeclaration<>(String.class, getMathModel().getName(symbol) + " name",
                value -> {
                    Node node = getMathModel().getNodeByReference(value);
                    if (node == null) {
                        getMathModel().setName(symbol, value);
                        for (Vertex event: getMathModel().getVertices(symbol)) {
                            event.sendNotification(new PropertyChangedEvent(event, Vertex.PROPERTY_SYMBOL));
                        }
                    } else if (!(node instanceof Symbol)) {
                        throw new FormatException("Node '" + value + "' already exists and it is not a symbol.");
                    }
                },
                () -> getMathModel().getName(symbol));
    }

    private PropertyDescriptor getVertexSymbolProperty(VisualVertex event) {
        return new PropertyDeclaration<>(String.class, Vertex.PROPERTY_SYMBOL,
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
