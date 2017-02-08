package org.workcraft.plugins.graph;

import java.util.Collection;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.graph.propertydescriptors.SymbolPropertyDescriptor;
import org.workcraft.util.Hierarchy;

@DisplayName("Directed Graph")
@ShortName("graph")
@CustomTools(GraphToolsProvider.class)
public class VisualGraph extends AbstractVisualModel {

    public VisualGraph(Graph model) {
        this(model, null);
    }

    public VisualGraph(Graph model, VisualGroup root) {
        super(model, root);
        if (root == null) {
            try {
                createDefaultFlatStructure();
            } catch (NodeCreationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if ((first instanceof VisualVertex) && (second instanceof VisualVertex)) return;

        throw new InvalidConnectionException("Invalid connection.");
    }

    @Override
    public VisualConnection connect(Node first, Node second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);

        VisualComponent v1 = (VisualComponent) first;
        VisualComponent v2 = (VisualComponent) second;
        Node m1 = v1.getReferencedComponent();
        Node m2 = v2.getReferencedComponent();

        if (mConnection == null) {
            mConnection = ((Graph) getMathModel()).connect(m1, m2);
        }
        VisualConnection vConnection = new VisualConnection(mConnection, v1, v2);
        Container container = Hierarchy.getNearestContainer(v1, v2);
        container.add(vConnection);
        return vConnection;
    }

    public Collection<VisualVertex> getVisualVertex() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualVertex.class);
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
