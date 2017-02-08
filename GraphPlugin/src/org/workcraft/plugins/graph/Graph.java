package org.workcraft.plugins.graph;

import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.graph.propertydescriptors.VertexSymbolPropertyDescriptor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class Graph extends AbstractMathModel {
    public static final String EPSILON_SERIALISATION = "epsilon";

    public Graph() {
        this(null, (References) null);
    }

    public Graph(Container root, References refs) {
        this(root, new HierarchicalUniqueNameReferenceManager(refs) {
            @Override
            public String getPrefix(Node node) {
                if (node instanceof Vertex) return "v";
                return super.getPrefix(node);
            }
        });
    }

    public Graph(Container root, ReferenceManager man) {
        super(root, man);
        new SymbolConsistencySupervisor(this).attach(getRoot());
    }

    public boolean keepUnusedSymbols() {
        return false;
    }

    public MathConnection connect(Node first, Node second) {
        MathConnection con = new MathConnection((MathNode) first, (MathNode) second);
        Hierarchy.getNearestContainer(first, second).add(con);
        return con;
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
        return Hierarchy.getDescendantsOfType(getRoot(), Vertex.class, new Func<Vertex, Boolean>() {
            @Override
            public Boolean eval(Vertex arg) {
                return arg.getSymbol() == symbol;
            }
        });
    }

    @Override
    public void reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<Node> srcChildren) {
        if (srcModel == null) {
            srcModel = this;
        }
        HierarchicalUniqueNameReferenceManager refManager = (HierarchicalUniqueNameReferenceManager) getReferenceManager();
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
        super.reparent(dstContainer, srcModel, srcRoot, srcChildren);
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
