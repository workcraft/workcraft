package org.workcraft.plugins.parity;

import java.util.ArrayList;

/**
 * Parser class.
 * Built to parse InputNodes into SolvableGames, from which they can be solved,
 * and then to also build SolvableGames, resulting winning regions, and
 * resulting strategies into OutputNodes.
 */
public class Parser {

    /**
     * Build a fresh SolvableGame, and parse the InputNodes into the arrays and
     * matrices of this SolvableGame.
     * @param inputnodes    InputNodes gathered from the Workcraft GUI
     */
    static SolvableGame parseInputNodes(ArrayList<InputNode> inputnodes) {

        Boolean[] vertex = new Boolean[inputnodes.size()];
        for (int popiter = 0; popiter < inputnodes.size(); ++popiter) {
            vertex[popiter] = true;
        }

        //ownership only has two rows: [0] for player 0, [1] for player 1
        Boolean[][] ownedBy = new Boolean[2][inputnodes.size()];
        for (int popiter = 0; popiter < 2; ++popiter) {
            for (int inner = 0; inner < inputnodes.size(); ++inner) {
                ownedBy[popiter][inner] = false;
            }
        }

        Boolean[][] adjMatrix = new Boolean[inputnodes.size()][inputnodes.size()];
        for (int popiter = 0; popiter < inputnodes.size(); ++popiter) {
            for (int inner = 0; inner < inputnodes.size(); ++inner) {
                adjMatrix[popiter][inner] = false;
            }
        }

        Integer[] priority = new Integer[inputnodes.size()];
        for (int popiter = 0; popiter < inputnodes.size(); ++popiter) {
            priority[popiter] = 0;
        }

        for (int nodeIter = 0; nodeIter < inputnodes.size(); ++nodeIter) {

            if (inputnodes.get(nodeIter).getOwnership()) {
                ownedBy[1][nodeIter] = true;
            } else {
                ownedBy[0][nodeIter] = true;
            }

            for (Integer edgeDest : inputnodes.get(nodeIter).getOutgoing()) {
                adjMatrix[nodeIter][edgeDest] = true;
            }

            priority[nodeIter] = inputnodes.get(nodeIter).getPrio();
        }

        return new SolvableGame(vertex, ownedBy, adjMatrix, priority);
    }

    /**
     * Generate an ArrayList of the OutputNode objects, ready to be processed
     * further.
     * @param winningRegions    [2][n] array symbolising the winning regions for
     *                          each player
     * @param strategy          [2][n] array symbolising the winning strategy
     *                          for each player, implying one exists
     * @return                  Built OutputNodes
     */
    static ArrayList<OutputNode> buildOutputNodes(Boolean[][] winningRegions,
            Integer[][] strategy) {

        ArrayList<OutputNode> outputnodes = new ArrayList<>();
        for (int nodeIter = 0; nodeIter < winningRegions[0].length; ++nodeIter) {

            boolean wonByPlayer1;
            int strat;
            /**
             * if player 0 wins, mark wonByPlayer1 as false and check strategy[0]
             * for a valid strategy. Else, do this with strategy[1].
             */
            if (winningRegions[0][nodeIter]) {
                wonByPlayer1 = false;
                strat = strategy[0][nodeIter];
            } else {
                wonByPlayer1 = true;
                strat = strategy[1][nodeIter];
            }

            if (strat == -1) {
                OutputNode tempOutputNode = new OutputNode(nodeIter, wonByPlayer1);
                outputnodes.add(tempOutputNode);
            } else {
                OutputNode tempOutputNode = new OutputNode(nodeIter, wonByPlayer1,
                        strat);
                outputnodes.add(tempOutputNode);
            }
        }

        return outputnodes;
    }
}