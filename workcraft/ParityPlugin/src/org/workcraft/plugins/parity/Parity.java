package org.workcraft.plugins.parity;

import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.plugins.parity.observers.SymbolConsistencySupervisor;
import org.workcraft.serialisation.References;
import org.workcraft.utils.Hierarchy;

import java.util.*;
import java.util.Map.Entry;

/**
 * Class to model the entire Parity game.
 * This is a subclass of the AbstractMathModel.
 *
 * A Parity game is a type of infinite graph game where the vertices are owned
 * by either Player 0 or Player 1. A token starts on a selected vertex, and the
 * player who owns a given vertex decides which edge the token travels across.
 * A strategy is followed (an edge is selected) by each player for each vertex
 * they own, and this edge selection is guaranteed to be the same regardless of
 * context.
 *
 * The game ends once an infinite cycle has been identified, and then the group
 * of vertices that make up this are compared to see which infinitely occurring
 * vertex has the largest attached priority value. If it is even then Player 0
 * wins, and if it is odd then Player 1 wins.
 *
 * In the graph, Player 0 vertices are identified by Circles, and Player 1
 * vertices are identified by Squares. Once the game has been solved, a vertex
 * will be coloured Blue if Player 0 wins, and will be coloured Red if Player 1
 * wins. If a player wins at a vertex and also owns it, then that means there is
 * a winning strategy for them from that vertex. This winning strategy edge will
 * be coloured blue or red for Players 0 or 1, respectively.
 */
public class Parity extends AbstractMathModel {

    public static final String EPSILON_SERIALISATION = "epsilon";

    /**
     * Empty constructor
     */
    public Parity() {
        this(null, null);
    }

    /**
     * Constructor that will instantiate a root Container, and References from
     * the AbstractMathModel superclass. This is to ensure all of the components
     * in the Parity game are well connected. Components in a Workcraft graph
     * are accessed in a similar structure to linked lists.
     * @param root    Abstract container to hold components
     * @param refs    Reference to all other linked components, managed by the
     *                ReferenceManager
     */
    public Parity(Container root, References refs) {
        super(root, refs);
        new SymbolConsistencySupervisor(this).attach(getRoot());
    }

    /**
     * Function that always returns false. Context is to ensure unused symbols
     * are not cached.
     * @return    false
     */
    public boolean keepUnusedSymbols() {
        return false;
    }

    /**
     * Generates a null symbol object, which is a MathNode subclass.
     * @return    null Symbol
     */
    public Symbol createSymbol(String name) {
        return createNode(name, null, Symbol.class);
    }

    /**
     * Get the set of symbols within the MathModel, with no argument provided.
     * This will be the Symbol components.
     * @return    Collection of Symbols in Model
     */
    public Collection<Symbol> getSymbols() {
        return Hierarchy.getDescendantsOfType(getRoot(), Symbol.class);
    }

    /**
     * Get the set of symbols within the MathModel, with the container of
     * components given as an argument.
     * @param container    Container of components
     * @return             Collection of Symbols in Model
     */
    public Collection<Symbol> getSymbols(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getChildrenOfType(container, Symbol.class);
    }

    /**
     * Get the set of Player 0 vertices within the MathModel, no arguments.
     * @return    Collection of Player 0 owned vertices within Model
     */
    public Collection<Player0> getPlayer0() {
        return Hierarchy.getDescendantsOfType(getRoot(), Player0.class);
    }

    /**
     * Get the set of Player 0 vertices within the MathModel, with a symbol
     * provided as an argument. The symbol is used as a filter to only get
     * descendants of whatever the symbol is inside the Player 0 vertex provided.
     * @param symbol    MathNode symbol to use as a filter as a temporary root
     * @return          Collection of Player 0 owned vertices within Model
     */
    public Collection<Player0> getPlayer0(final Symbol symbol) {
        return Hierarchy.getDescendantsOfType(getRoot(), Player0.class,
            p0 -> p0.getSymbol() == symbol);
    }

    /**
     * Get the set of Player 1 vertices within the MathModel, no arguments.
     * @return Collection of Player 1 owned vertices within Model
     */
    public Collection<Player1> getPlayer1() {
        return Hierarchy.getDescendantsOfType(getRoot(), Player1.class);
    }

    /**
     * Get the set of Player 1 vertices within the MathModel, with a symbol
     * provided as an argument. The symbol is used as a filter to only get
     * descendants of whatever the symbol is inside the Player 1 vertex provided.
     * @param symbol    MathNode symbol to use as a filter as a temporary root
     * @return          Collection of Player 1 owned vertices within Model
     */
    public Collection<Player1> getPlayer1(final Symbol symbol) {
        return Hierarchy.getDescendantsOfType(getRoot(), Player1.class,
            p1 -> p1.getSymbol() == symbol);
    }

    /**
     * Get the set of Connection (edge) components within the Parity game.
     * @return    Collection of edges within Model
     */
    public final Collection<MathConnection> getConnections() {
        return Hierarchy.getDescendantsOfType(getRoot(), MathConnection.class);
    }

    /**
     * Predicate function to ensure user has placed vertices on game graph.
     * If there are no vertices, owned by either player 0 or 1 on the graph,
     * false will be returned.
     * @return    true if there is at least one vertex in Model
     */
    public boolean isNonEmpty() {
        Collection<Player0> p0nodes = getPlayer0();
        Collection<Player1> p1nodes = getPlayer1();

        return (p0nodes.isEmpty() && p1nodes.isEmpty()) ? false : true;
    }

    /**
     * Predicate function to ensure all vertices have a non-negative priority.
     * It is best practice to guarantee the vertex priorities are non-negative,
     * although it is possible to just increase the value of all priorities by
     * some uniform value such that the lowest negative number is >= 0.
     *
     * Returns true if All vertices have a priority that is 0 or larger.
     * @return    true if all vertices have priority of 0 or higher
     */
    public boolean isNonNegative() {
        Collection<Player0> p0nodes = getPlayer0();
        Iterator<Player0> p0iter = p0nodes.iterator();
        while (p0iter.hasNext()) {
            Player0 tempNode = p0iter.next();
            if (tempNode.getPrio() < 0) {
                return false;
            }
        }
        Collection<Player1> p1nodes = getPlayer1();
        Iterator<Player1> p1iter = p1nodes.iterator();
        while (p1iter.hasNext()) {
            Player1 tempNode = p1iter.next();
            if (tempNode.getPrio() < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Builds the input format as a collection of input nodes, which are
     * InputNode objects. Each of these objects hold the automatically
     * assigned identifier 0-n, the priority of the vertex, which player owns
     * the vertex, and what vertices can be directly reached from the input node.
     * @return    ArrayList of InputNodes
     */
    public ArrayList<InputNode> buildInputNodes() {
        Collection<Player0> p0nodes = getPlayer0();
        Collection<Player1> p1nodes = getPlayer1();
        Collection<MathConnection> edges = getConnections();
        Iterator<Player0> p0iter = p0nodes.iterator();
        Iterator<Player1> p1iter = p1nodes.iterator();
        Iterator<MathConnection> edgeIter = edges.iterator();
        HashMap<String, Integer> nameToId = new HashMap<>();
        int nodeCounter = 0;    //Nodes must have identifiers of interval [0,N-1)
                                //where N = amount of vertices in game

        /*
         * Initially gather ArrayLists of parameters from which to build the
         * InputNodes. The index of the ArrayList refers to the identifier
         * of a given input node. Data will be collected from the Model to
         * fill these ArrayLists.
         */
        ArrayList<Integer> inputPriority = new ArrayList<>();
        ArrayList<Boolean> ownedBy = new ArrayList<>();
        ArrayList<ArrayList<Integer>> outgoing = new ArrayList<>();
        ArrayList<InputNode> inputNodes = new ArrayList<>();

        while (p0iter.hasNext()) {
            Player0 tempNode = p0iter.next();
            tempNode.setId(nodeCounter);
            inputPriority.add(tempNode.getPrio());
            ownedBy.add(false);
            nameToId.put(getName(tempNode), nodeCounter++);
            ArrayList<Integer> tempArray = new ArrayList<>();
            outgoing.add(tempArray);
        }

        while (p1iter.hasNext()) {
            Player1 tempNode = p1iter.next();
            tempNode.setId(nodeCounter);
            inputPriority.add(tempNode.getPrio());
            ownedBy.add(true);
            nameToId.put(getName(tempNode), nodeCounter++);
            ArrayList<Integer> tempArray = new ArrayList<>();
            outgoing.add(tempArray);
        }

        while (edgeIter.hasNext()) {
            MathConnection tempEdge = edgeIter.next();
            MathNode tempFirst = tempEdge.getFirst();
            MathNode tempSecond = tempEdge.getSecond();
            Integer toBeAdded = nameToId.get(getName(tempSecond));
            Integer indexToAddTo = nameToId.get(getName(tempFirst));
            outgoing.get(indexToAddTo).add(toBeAdded);
        }

        /*
         * All of the information to build InputNode objects has been
         * collected; input nodes will now be built.
         */
        for (int inputNodeIter = 0; inputNodeIter < inputPriority.size();
                ++inputNodeIter) {

            InputNode inputNode = new InputNode(
                    inputNodeIter, inputPriority.get(inputNodeIter),
                    ownedBy.get(inputNodeIter), outgoing.get(inputNodeIter));
            inputNodes.add(inputNode);
        }

        return inputNodes;
    }

    /**
     * Predicate function to check that every vertex in the parity game have
     * at least one successor (outgoing edge). Returns true if all vertices have
     * a successor.
     * @param inputNodes    ArrayList of InputNodes gathered from Model.
     * @return              true if every vertex has at least one outgoing edge
     */
    public boolean isInfinite(ArrayList<InputNode> inputNodes) {

        for (int inputNodeIter = 0; inputNodeIter < inputNodes.size();
                ++inputNodeIter) {

            if (inputNodes.get(inputNodeIter).getOutgoing().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Solves the parity game using Zielonka's algorithm. More information can
     * be found in regards to this within Zielonka.java.
     * @param inputNodes    ArrayList of InputNodes gathered from Model.
     * @return              ArrayList of OutputNodes from which the game can be
     *                      solved.
     */
    public ArrayList<OutputNode> solveGame(ArrayList<InputNode> inputNodes) {
        SolvableGame sg = Parser.parseInputNodes(inputNodes);
        List<Entry<Boolean[], Integer[]>> winning = Zielonka.solve(sg);
        Boolean[][] winner = new Boolean[2][inputNodes.size()];
        winner[0] = winning.get(0).getKey();
        winner[1] = winning.get(1).getKey();
        Integer[][] strategy = new Integer[2][inputNodes.size()];
        strategy[0] = winning.get(0).getValue();
        strategy[1] = winning.get(1).getValue();
        return Parser.buildOutputNodes(winner, strategy);
    }

    /**
     * Overridden function used to reparent all vertices within the list as
     * necessary. This will need to be repeated for vertices owned by Player 0,
     * and also vertices owned by Player 1.
     * @param dstContainer    Abstract destination container of what the model
     *                        will become.
     * @param srcModel        Source model of vertices and edges; what the model
                              was before reparenting.
     * @param srcRoot         Source root component.
     * @param srcChildren     Source children of root component.
     */
    @Override
    public boolean reparent(Container dstContainer, Model srcModel,
            Container srcRoot, Collection<? extends MathNode> srcChildren) {
        if (srcModel == null) {
            srcModel = this;
        }
        HierarchyReferenceManager refManager = getReferenceManager();
        NameManager nameManager = refManager.getNameManager(null);
        for (MathNode srcNode: srcChildren) {
            if (srcNode instanceof Player0 srcPlayer0) {
                Symbol dstSymbol = null;
                Symbol srcSymbol = srcPlayer0.getSymbol();
                if (srcSymbol != null) {
                    String symbolName = srcModel.getNodeReference(srcSymbol);
                    Node dstNode = getNodeByReference(symbolName);
                    if (dstNode instanceof Symbol) {
                        dstSymbol = (Symbol) dstNode;
                    } else {
                        if (dstNode != null) {
                            symbolName = nameManager.getDerivedName(null, symbolName);
                        }
                        dstSymbol = createSymbol(symbolName);
                    }
                }
                srcPlayer0.setSymbol(dstSymbol);
            } else if (srcNode instanceof Player1 srcPlayer1) {
                Symbol dstSymbol = null;
                Symbol srcSymbol = srcPlayer1.getSymbol();
                if (srcSymbol != null) {
                    String symbolName = srcModel.getNodeReference(srcSymbol);
                    Node dstNode = getNodeByReference(symbolName);
                    if (dstNode instanceof Symbol) {
                        dstSymbol = (Symbol) dstNode;
                    } else {
                        if (dstNode != null) {
                            symbolName = nameManager.getDerivedName(null, symbolName);
                        }
                        dstSymbol = createSymbol(symbolName);
                    }
                }
                srcPlayer1.setSymbol(dstSymbol);
            }
        }
        return super.reparent(dstContainer, srcModel, srcRoot, srcChildren);
    }
}