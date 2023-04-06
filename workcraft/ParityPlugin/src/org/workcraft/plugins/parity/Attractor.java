package org.workcraft.plugins.parity;

/**
 * Fixed point attractor class.
 * Generated and used by Zielonka's algorithm to solve parity games.
 * The way in which fixed point attractors are built can be found in the
 * Parity Game Wikipedia page, under the subtitle
 * 'Recursive algorithm for solving parity games'.
 * There is also a solid explanation found on page 5 of:
 * 'Graph games with perfect information' by Dietmar Berwanger.
 *
 * Although this generates the set of reachable states for some set of target
 * states F, this is extended to parity by treating vertices with a given value
 * as said target states and then calculating the fixed point attractor for such
 * a reachability game.
 */

public class Attractor {

    /**
     * Bit vector to represent the attractor set.
     * It is not made clear yet whether Player 0 or 1 is Player i; that will be
     * made evident through makeAttractorSet function usage.
     */
    Boolean[] attrSet;
    /**
     * Rank used to calculate the strategy.
     * All initially starts as -1.
     */
    Integer[] rank;
    /**
     * Strategy taken at a given vertex by Player i to reach the fixpoint.
     * If fixpoint cannot be reached, or the vertex is not owned by Player i,
     * mark as -1.
     */
    Integer[] strategy;

    //Amount of successors. Used exclusively for attrSet construction
    Integer[] successorAmount;
    /**
     * Matrix holding the predecessors of the vertices.
     * i.e. predecessors[1][3] = 3 is a predecessor of 1 (3->1 exists as edge)
     */
    Boolean[][] predecessors;

    /**
     * Constructor. Immediately runs all relevant construction functions to
     * build the i-attractor.
     * @param adjMatrix       Adjacency matrix
     * @param ownedBy         Two bit vectors signifying player ownership
     * @param targetStates    Set F of reachability target vertices
     * @param vertices        Bit vector of vertices
     * @param player          Player i that the attractor set is being built for.
     */
    Attractor(Boolean[][] adjMatrix, Boolean[][] ownedBy, Boolean[] targetStates,
            Boolean[] vertices, boolean player) {
        int inputsize = adjMatrix.length;
        attrSet = new Boolean[inputsize];
        successorAmount = new Integer[inputsize];
        rank = new Integer[inputsize];
        strategy = new Integer[inputsize];
        for (int popiter = 0; popiter < inputsize; ++popiter) {
            attrSet[popiter] = false;
            successorAmount[popiter] = 0;
            rank[popiter] = -1;
            strategy[popiter] = -1;
        }

        predecessors = new Boolean[inputsize][inputsize];
        for (int popiter = 0; popiter < inputsize; ++popiter) {
            for (int inner = 0; inner < inputsize; ++inner) {
                predecessors[popiter][inner] = false;
            }
        }

        makeAttractorSet(adjMatrix, ownedBy, targetStates, vertices, player);
        calculateStrategy(adjMatrix, ownedBy, vertices, player);
    }

    /**
     * Populate the predecessor matrix, conversely also updating the amount of
     * successors every vertex has via updating Attractor.successorAmount.
     * @param adjMatrix       Adjacency matrix (edges of game)
     * @param vertices        Bit vector of vertices
     */
    Boolean[][] populatePredSucc(Boolean[][] adjMatrix, Boolean[] vertices) {

        for (int rowIter = 0; rowIter < adjMatrix.length; ++rowIter) {

            for (int colIter = 0; colIter < adjMatrix.length; ++colIter) {
                if (adjMatrix[rowIter][colIter] && vertices[rowIter]
                        && vertices[colIter]) {
                    predecessors[colIter][rowIter] = true;
                    ++successorAmount[rowIter];
                }
            }
        }

        return predecessors;
    }

    /**
     * Propagation function that builds the fixpoint attractor set, as defined
     * in Berwanger's paper. Updates the attrSet, successorAmount, and rank.
     * @param ownedBy         Two bit vectors where ownedBy[0] are vertices
     *                        owned by player 0, and ownedBy[1] are player 1
     *                        vertices.
     * @param currentV        Current target vertex being propagated
     * @param currentRank     Current rank in this recursive propagation. Starts
     *                        at 0 and increments for every propagate call.
     * @param player          Player i that the attractor set is being built for.
     *                        false = player 0, true = player 1
     */
    void propagate(Boolean[][] ownedBy, int currentV, int currentRank,
            boolean player) {

        if (attrSet[currentV]) {
            rank[currentV] = Math.min(rank[currentV], currentRank);
            return;
        }
        attrSet[currentV] = true;
        rank[currentV] = currentRank;
        for (int predIter = 0; predIter < predecessors.length; ++predIter) {

            if (predecessors[currentV][predIter]) {
                --successorAmount[predIter];
                int playerNum = player ? 1 : 0;
                if (ownedBy[playerNum][predIter] ||
                        successorAmount[predIter] == 0) {
                    propagate(ownedBy, predIter, ++currentRank, player);
                }
            }
        }
    }

    /**
     * Predicate function to check if one vector x is fully included in another
     * vector y
     * @param x    Vector that is being checked as the 'subset'
     * @param y    Vector that may include x inside itself
     * @return     True if x is included in y
     */
    boolean isIncluded(Boolean[] x, Boolean[] y) {

        for (int xIter = 0; xIter < x.length; ++xIter) {
            if (x[xIter] && !y[xIter]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Using previously defined function calls, the attractor set will be
     * generated here. Void function that updates attrSet. It also updates rank,
     * preparing it to be used for strategy calculation.
     * @param adjMatrix       Adjacency matrix
     * @param ownedBy         Two bit vectors signifying player ownership
     * @param targetStates    Set F of reachability target vertices
     * @param vertices        Bit vector of vertices
     * @param player          Player i that the attractor set is being built for.
     *                        false = player 0, true = player 1
     */
    void makeAttractorSet(Boolean[][] adjMatrix, Boolean[][] ownedBy,
            Boolean[] targetStates, Boolean[] vertices,
            boolean player) {

        /**
         * If the target states are not included within vertices, an
         * ERROR STATE has been reached. This should not be possible.
         */
        if (!isIncluded(targetStates, vertices)) {
            System.out.print("TARGET STATES NOT A SUBSET OF VERTICES. ERROR\n"
                    + "TARGET STATES: ");
            for (Boolean tState : targetStates) {
                System.out.print(tState + " ");
            }
            System.out.print("\nVERTICES IN SET: ");
            for (Boolean vState : vertices) {
                System.out.print(vState + " ");
            }
            System.out.println("");
            return;
        }

        predecessors = populatePredSucc(adjMatrix, vertices);
        for (int tStateIter = 0; tStateIter < targetStates.length; ++tStateIter) {
            if (targetStates[tStateIter]) {
                propagate(ownedBy, tStateIter, 0, player);
            }
        }
    }

    /**
     * The attractor set has been generated, and the rank array has been
     * populated. Now we generate the strategy by looking for edges that are
     * 'closer' to the fixpoint that shall be selected. Closer = smaller rank
     * @param adjMatrix    Adjacency matrix
     * @param ownedBy      Two bit vectors signifying player ownership
     * @param vertices     Bit vector of vertices
     * @param player       Player i that the attractor set is being built for.
     *                     false = player 0, true = player 1
     */
    void calculateStrategy(Boolean[][] adjMatrix, Boolean[][] ownedBy,
            Boolean[] vertices, boolean player) {

        for (int rankIter = 0; rankIter < rank.length; ++rankIter) {
            if (rank[rankIter] == 0) {
                for (int outgoing = 0; outgoing < adjMatrix[rankIter].length;
                        ++outgoing) {
                    int playerInt = player ? 1 : 0;
                    if (adjMatrix[rankIter][outgoing] && vertices[rankIter] &&
                            vertices[outgoing] && ownedBy[playerInt][rankIter]) {
                        strategy[rankIter] = outgoing;
                        break;
                    }
                }
            } else if (rank[rankIter] > 0) {
                for (int outgoing = 0; outgoing < adjMatrix[rankIter].length;
                        ++outgoing) {
                    int playerInt = player ? 1 : 0;
                    if (adjMatrix[rankIter][outgoing] && vertices[rankIter] &&
                            vertices[outgoing] && ownedBy[playerInt][rankIter]
                            && rank[rankIter] > rank[outgoing]
                            && rank[outgoing] > -1) {
                        strategy[rankIter] = outgoing;
                        break;
                    }
                }
            }
        }
    }

    /**
     * print function used exclusively for testing
     * @param player          false = player 0, true = player 1
     * @param targetStates    reachability target states
     */
    void printAttractorSet(boolean player, Boolean[] targetStates) {
        int temp = player ? 1 : 0;
        System.out.print("ATTRACTOR SET FOR PLAYER " + temp
                + " ON TARGET STATES ");
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < targetStates.length; ++i) {
            if (targetStates[i]) {
                sb.append(Integer.toString(i));
                sb.append(",");
            }
        }
        sb.setCharAt(sb.length() - 1, '}');
        System.out.println(sb + ":");

        for (int i = 0; i < attrSet.length; ++i) {
            if (attrSet[i]) {
                System.out.print(Integer.toString(i) + " ");
            }
        }
        System.out.println("");
    }
}