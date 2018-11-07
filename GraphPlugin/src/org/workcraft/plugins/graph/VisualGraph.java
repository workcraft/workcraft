package org.workcraft.plugins.graph;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.*;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.plugins.graph.properties.SymbolPropertyDescriptor;
import org.workcraft.plugins.graph.properties.VertexSymbolPropertyDescriptor;
import org.workcraft.plugins.graph.tools.GraphSimulationTool;

import java.util.ArrayList;
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
            for (final Symbol symbol: getMathModel().getSymbols(container)) {
                SymbolPropertyDescriptor symbolDescriptor = new SymbolPropertyDescriptor(getMathModel(), symbol);
                properties.insertOrderedByFirstWord(symbolDescriptor);
            }
        } else if (node instanceof VisualVertex) {
            VisualVertex vertex = (VisualVertex) node;
            properties.add(new VertexSymbolPropertyDescriptor(getMathModel(), vertex.getReferencedComponent()));
        }
        return properties;
    }

}
