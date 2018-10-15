package org.workcraft.plugins.graph;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.*;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.graph.properties.SymbolPropertyDescriptor;
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
        if (root == null) {
            try {
                createDefaultFlatStructure();
            } catch (NodeCreationException e) {
                throw new RuntimeException(e);
            }
        }
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
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        Graph graph = (Graph) getMathModel();
        if (node == null) {
            Container container = NamespaceHelper.getMathContainer(this, getCurrentLevel());
            for (final Symbol symbol: graph.getSymbols(container)) {
                SymbolPropertyDescriptor symbolDescriptor = new SymbolPropertyDescriptor(graph, symbol);
                properties.insertOrderedByFirstWord(symbolDescriptor);
            }
        }
        return properties;
    }

}
