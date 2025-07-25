package org.workcraft.plugins.parity;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Zielonka game solver class.
 * Zielonka's algorithm is then applied to a SolvableGame,to generate the
 * winning regions for players 0 and 1, as well as the winning strategies for
 * both players.
 * <p>
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
    static SolvableGame subtractAttr(SolvableGame game, Boolean[] attrSet) {

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
                        if (attrSet[attrIter] && ((attrIter == outer) || (attrIter == inner))) {
                            game.adjMatrix[outer][inner] = false;
                        }
                    }
                }
            }
        }
        return game;
    }

    /**
     * Given two pairs of winning regions, union them together.
     * @param firstWin     First winning region
     * @param secondWin    Second winning region
     * @return             The union of the two regions
     */
    static Boolean[] unionRegions(Boolean[] firstWin, Boolean[] secondWin) {
        for (int winIter = 0; winIter < firstWin.length; ++winIter) {
            firstWin[winIter] = firstWin[winIter] || secondWin[winIter];
        }
        return firstWin;
    }

    /**
     * Given two pairs of strategies, union them together. We assert these will
     * always be disjoint, and if they are not, then something has gone wrong
     * within the code logic.
     * @param firstStrat     First strategy
     * @param secondStrat    Second strategy
     * @return               The union of the two strategies
     */
    static Integer[] unionStrategies(Integer[] firstStrat, Integer[] secondStrat) {
        Integer[] resultStrat = new Integer[firstStrat.length];
        for (int stratIter = 0; stratIter < firstStrat.length; ++stratIter) {
            resultStrat[stratIter] = -1;
            if (firstStrat[stratIter] != -1) {
                resultStrat[stratIter] = firstStrat[stratIter];
            } else if (secondStrat[stratIter] != -1) {
                resultStrat[stratIter] = secondStrat[stratIter];
            }
        }
        return resultStrat;
    }

    /**
     * Zielonka's recursive algorithm, as defined within Friedmann's paper.
     * This function will return the winning regions for
     * players 0 and 1, and also their strategy. These will be returned as a
     * pair.
     * @param game    Current subgame that will be divided via recursion
     * @return        A List of two pairs: One for player 0, and one for
     *                player 1. Each pair contains the winning region, followed
     *                by the strategy.
     */
    static List<Entry<Boolean[], Integer[]>> solve(SolvableGame game) {
        List<Entry<Boolean[], Integer[]>> resultPair = new ArrayList<>();
        Boolean[] emptyVertices = new Boolean[game.vertex.length];
        Integer[] emptyStrat = new Integer[game.vertex.length];
        for (int gameIter = 0; gameIter < game.vertex.length; ++gameIter) {
            emptyVertices[gameIter] = false;
            emptyStrat[gameIter] = -1;
        }
        Entry<Boolean[], Integer[]> emptyPair = new SimpleEntry<>(emptyVertices, emptyStrat);
        resultPair.add(emptyPair);
        resultPair.add(emptyPair);

        /*
          BASE CASE: When amount of vertices is 0, return empty winning
          regions and empty strategies
         */
        if (game.isArrEmpty(game.vertex)) {
            return resultPair;
        }

        // Find maximal priority p in graph
        int largestPrio = game.getHighPrio();

        /*
          player i will be equal to p mod 2.
          This is player who wins with maximal priority in graph.
          Also calculate set U, which by logic will be non-empty if we are at
          this point.
         */
        boolean maxPlayer = (largestPrio & 1) == 1;
        int maxPlayerIndex = maxPlayer ? 1 : 0;
        Boolean[] largestPrioVertex = game.getHighPrioVertices(largestPrio);

        /*
          Calculate attractor pair for Player i. We have no idea who this will
          be at this point, but we know it generates the attractor set A and
          strategy Tau Union TauPrime
         */
        Attractor attrSetA = new Attractor(game.adjMatrix, game.ownedBy, largestPrioVertex, game.vertex, maxPlayer);

        //game G' = G \ A. Calc attractor pairs here for G'
        SolvableGame gprime = subtractAttr(new SolvableGame(game), attrSetA.attrSet);
        List<Entry<Boolean[], Integer[]>> gprimeResult = solve(gprime);

        /*
          check if winning_regions[1-i] is empty.
          if it is:
          a) Set attractor pair for i to be g.vertices, and set its strategy to
             be the union of what it is in gprimeResult with i_attr_pair
          b) Set attractor pair for 1-i to be {EMPTY_SET, EMPTY_STRAT} like in the base case
          c) Return this attractor pair
         */
        if (gprime.isArrEmpty(gprimeResult.get(1 - maxPlayerIndex).getKey())) {
            Entry<Boolean[], Integer[]> replacement = new SimpleEntry<>(
                    game.vertex, unionStrategies(
                    gprimeResult.get(maxPlayerIndex).getValue(),
                    attrSetA.strategy));
            resultPair.set(maxPlayerIndex, replacement);
            return resultPair;
        }

        /*
          Calculate attractor pair for Player 1-i, using the winning region of
          1-i in gprimeResult as the target vertices.
         */
        Attractor attrSetB = new Attractor(game.adjMatrix, game.ownedBy,
                gprimeResult.get(1 - maxPlayerIndex).getKey(),
                game.vertex, !maxPlayer);

        //Game G'' = G \ B
        SolvableGame gprimeprime = subtractAttr(new SolvableGame(game),
                attrSetB.attrSet);
        List<Entry<Boolean[], Integer[]>> gprimeprimeResult = solve(gprimeprime);

        // Calculate attractor pairs for i and 1-i,
        resultPair.set(maxPlayerIndex, gprimeprimeResult.get(maxPlayerIndex));
        Boolean[] winRegion = unionRegions(gprimeprimeResult.get(1 - maxPlayerIndex).getKey(), attrSetB.attrSet);
        Integer[] winStrat = unionStrategies(gprimeprimeResult.get(1 - maxPlayerIndex).getValue(), attrSetB.strategy);
        winStrat = unionStrategies(winStrat, gprimeResult.get(1 - maxPlayerIndex).getValue());
        Entry<Boolean[], Integer[]> replacement = new SimpleEntry<>(winRegion, winStrat);
        resultPair.set(1 - maxPlayerIndex, replacement);
        return resultPair;
    }

}
