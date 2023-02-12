package org.workcraft.plugins.parity;

/**
 * Zielonka game solver class.
 * Zielonka's algorithm is then applied to a SolvableGame,to generate the
 * winning regions for players 0 and 1, as well as the winning strategies for
 * both players.
 *
 * Zielonka's Algorithm is fully detailed in the paper:
 * 'Recursive algorithm for parity games requires exponential time'
 * by Oliver Friedmann.
 */
public class Zielonka {

    /**
     * Subtract the bit vector of an attractor set from the vertex array,
     * symbolising a new subgame. Also remove the edges from the adjMatrix for a
     * provided game.
     * @param game       Copy of current SolvableGame to be mutated
     * @param attrSet    Attractor set
     */
    static SolvableGame subtractAttr(SolvableGame game, boolean[] attrSet) {

        //remove vertices
        for (int vIter = 0; vIter < game.vertex.length; ++vIter) {
            if (game.vertex[vIter] && attrSet[vIter]) {
                game.vertex[vIter] = false;
            }
        }
        //remove edges
        for (int outer = 0; outer < game.adjMatrix.length; ++outer) {
            for (int inner = 0; inner < game.adjMatrix[outer].length; ++inner) {
                if (game.adjMatrix[outer][inner]) {
                    for (int attrIter = 0; attrIter < attrSet.length; ++attrIter) {
                        if (attrSet[attrIter] && (attrIter == outer ||
                                attrIter == inner)) {
                            game.adjMatrix[outer][inner] = false;
                        }
                    }
                }
            }
        }
        return game;
    }

    /**
     * Zielonka's recursive algorithm, as defined within Friedmann's paper.
     * Although calculating the strategy is part of Zielonka, this shall be done
     * in a later function. This function will return the winning regions for
     * players 0 and 1.
     * @param game    Current subgame that will be divided via recursion
     * @return        2d bit vector of dimensions [2][n] to signify winning
     *                regions. [0][n] = player 0 win, [1][n] = player 1 win
     */
    static boolean[][] solve(SolvableGame game) {

        boolean[][] winningRegions = new boolean[2][game.vertex.length];
        for (int outer = 0; outer < 2; ++outer) {
            for (int inner = 0; inner < game.vertex.length; ++inner) {
                winningRegions[outer][inner] = false;
            }
        }
        if (game.isArrEmpty(game.vertex)) {
            return winningRegions;
        }

        int largestPrio = game.getHighPrio();
        if (largestPrio == 0) {
            winningRegions[0] = game.vertex;
            return winningRegions;
        }

        //false if player 0, true if player 1
        boolean maxPlayer = (largestPrio & 1) == 1;
        boolean[] largestPrioVertex = game.getHighPrioVertices(largestPrio);
        //Attractor set for max player
        Attractor attrSetA = new Attractor(game.adjMatrix, game.ownedBy,
                largestPrioVertex, game.vertex, maxPlayer);
        SolvableGame gprime = subtractAttr(new SolvableGame(game), attrSetA.attrSet);
        winningRegions = solve(gprime);

        if (game.isArrEmpty(winningRegions[1 - (maxPlayer ? 1 : 0)])) {
            winningRegions[maxPlayer ? 1 : 0] = game.vertex;
            for (int winIter = 0;
                    winIter < winningRegions[1 - (maxPlayer ? 1 : 0)].length;
                    ++winIter) {
                winningRegions[1 - (maxPlayer ? 1 : 0)][winIter] = false;
                return winningRegions;
            }
        }

        Attractor attrSetB = new Attractor(game.adjMatrix, game.ownedBy,
                winningRegions[1 - (maxPlayer ? 1 : 0)], game.vertex, !maxPlayer);
        SolvableGame gprimeprime = subtractAttr(new SolvableGame(game),
                attrSetB.attrSet);
        winningRegions = solve(gprimeprime);

        for (int attrIter = 0; attrIter < attrSetB.attrSet.length; ++attrIter) {
            if (attrSetB.attrSet[attrIter]) {
                winningRegions[1 - (maxPlayer ? 1 : 0)][attrIter] = true;
            }
        }

        return winningRegions;
    }

    /**
     * Given a SolvableGame and the generated winning regions, return an array
     * of strategies that a player would follow, implying that the strategy is
     * winning for the player. There will be at most one path decided for each
     * player to take.
     * @param game              The whole game before any mutation
     * @param winningRegions    Generated winning regions
     * @return                  2d Array of ints [2][n], signifying the vertex
     *                          id the player would travel to for a winning
     *                          strategy. [0] = p0, [1] = p1
     */
    static int[][] calcStrategy(SolvableGame game, boolean[][] winningRegions) {

        /**
         * Strategy values are initialised as -1 to represent the player does
         * not have a strategy at that vertex. This may be because:
         * a) They do not own said vertex
         * b) They lose on said vertex
         */
        int[][] strategy = new int[winningRegions.length][winningRegions[0].length];
        for (int outer = 0; outer < winningRegions.length; ++outer) {
            for (int inner = 0; inner < winningRegions[outer].length; ++inner) {
                strategy[outer][inner] = -1;
            }
        }

        /**
         * Check if a winning region is present, and that the player also owns
         * the vertex. If they do, identify a strategy by finding a vertex they
         * can move to within their winning region.
         */
        for (int outer = 0; outer < winningRegions.length; ++outer) {

            for (int inner = 0; inner < winningRegions[outer].length; inner++) {

                if (winningRegions[outer][inner] && game.ownedBy[outer][inner]) {

                    for (int edgeIter = 0;
                            edgeIter < game.adjMatrix[inner].length;
                            ++edgeIter) {

                        if (game.adjMatrix[inner][edgeIter] &&
                                winningRegions[outer][edgeIter]) {
                            strategy[outer][inner] = edgeIter;
                        }
                    }
                }
            }
        }
        return strategy;
    }
}