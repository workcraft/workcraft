package org.workcraft.plugins.graph;

import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.references.UniqueReferenceManager;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.graph.observers.SymbolConsistencySupervisor;
import org.workcraft.plugins.graph.properties.VertexSymbolPropertyDescriptor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

import java.util.Collection;

public class Graph extends AbstractMathModel {

    public static final String EPSILON_SERIALISATION = "epsilon";

    public Graph() {
        this(null, null);
    }

    public Graph(Container root, References refs) {
        super(root, refs);
        new SymbolConsistencySupervisor(this).attach(getRoot());
    }

    public boolean keepUnusedSymbols() {
        return false;
    }

    public Symbol createSymbol(String name) {
        return createNode(name, null, Symbol.class);
    }

    public Collection<Symbol> getSymbols() {
        return Hierarchy.getDescendantsOfType(getRoot(), Symbol.class);
    }

    public Collection<Symbol> getSymbols(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getChildrenOfType(container, Symbol.class);
    }

    public Collection<Vertex> getVertices() {
        return Hierarchy.getDescendantsOfType(getRoot(), Vertex.class);
    }

    public Collection<Vertex> getVertices(final Symbol symbol) {
        return Hierarchy.getDescendantsOfType(getRoot(), Vertex.class, vertex -> vertex.getSymbol() == symbol);
    }

    @Override
    public boolean reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<Node> srcChildren) {
        if (srcModel == null) {
            srcModel = this;
        }
        UniqueReferenceManager refManager = (UniqueReferenceManager) getReferenceManager();
        NameManager nameManagerer = refManager.getNameManager(null);
        for (Node srcNode: srcChildren) {
            if (srcNode instanceof Vertex) {
                Vertex srcVertex = (Vertex) srcNode;
                Symbol dstSymbol = null;
                Symbol srcSymbol = srcVertex.getSymbol();
                if (srcSymbol != null) {
                    String symbolName = srcModel.getNodeReference(srcSymbol);
                    Node dstNode = getNodeByReference(symbolName);
                    if (dstNode instanceof Symbol) {
                        dstSymbol = (Symbol) dstNode;
                    } else {
                        if (dstNode != null) {
                            symbolName = nameManagerer.getDerivedName(null, symbolName);
                        }
                        dstSymbol = createSymbol(symbolName);
                    }
                }
                srcVertex.setSymbol(dstSymbol);
            }
        }
        return super.reparent(dstContainer, srcModel, srcRoot, srcChildren);
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node instanceof Vertex) {
            Vertex vertex = (Vertex) node;
            properties.add(new VertexSymbolPropertyDescriptor(this, vertex));
        }
        return properties;
    }

}
