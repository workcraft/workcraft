package org.workcraft.plugins.parity;

/**
 * Class that models collected InputNodes as a solvable game, using arrays
 * instead of ArrayLists.
 */
public class SolvableGame {

    Boolean[] vertex;
    Boolean[][] ownedBy;
    Boolean[][] adjMatrix;
    Integer[] priority;

    /**
     * Constructor. Builds the gathered input nodes into boolean arrays, ready
     * to be processed.
     * @param vertex       Bit vector of game vertices
     * @param ownedBy      [2][n] matrix determining ownership. [0] = player 0,
     *                     [1] = player 1
     * @param adjMatrix    [n][n] matrix mapping all the edge transitions
     * @param priority     Bit vector of vertex priorities
     */
    SolvableGame(Boolean[] vertex, Boolean[][] ownedBy, Boolean[][] adjMatrix,
            Integer[] priority) {

        this.vertex = vertex;
        this.ownedBy = ownedBy;
        this.adjMatrix = adjMatrix;
        this.priority = priority;
    }

    /**
     * Copy constructor
     * @param game    game to be copied
     */
    SolvableGame(SolvableGame game) {
        this.vertex = (Boolean[]) game.vertex.clone();
        this.ownedBy = new Boolean[game.ownedBy.length][game.ownedBy[0].length];
        for (int outer = 0; outer < game.ownedBy.length; ++outer) {
            for (int inner = 0; inner < game.ownedBy[outer].length; ++inner) {
                this.ownedBy[outer][inner] = game.ownedBy[outer][inner];
            }
        }
        this.adjMatrix = new Boolean[game.adjMatrix.length][game.adjMatrix[0].length];
        for (int outer = 0; outer < game.adjMatrix.length; ++outer) {
            for (int inner = 0; inner < game.adjMatrix[outer].length; ++inner) {
                this.adjMatrix[outer][inner] = game.adjMatrix[outer][inner];
            }
        }
        this.priority = (Integer[]) game.priority.clone();
    }

    /**
     * Print function to ensure game has been built correctly
     */
    void printSolvableGame() {

        System.out.print("VERTICES: ");
        for (int i = 0; i < vertex.length; ++i) {
            System.out.print(vertex[i] ? i + " " : "");
        }
        System.out.print("\nPLAYER 0 OWNS: ");
        for (int i = 0; i < ownedBy[0].length; ++i) {
            System.out.print(ownedBy[0][i] ? i + " " : "");
        }
        System.out.print("\nPLAYER 1 OWNS: ");
        for (int i = 0; i < ownedBy[1].length; ++i) {
            System.out.print(ownedBy[1][i] ? i + " " : "");
        }
        System.out.println("\n\nADJ MATRIX:");
        for (int i = 0; i < adjMatrix.length; ++i) {
            for (int j = 0; j < adjMatrix.length; ++j) {
                System.out.print(adjMatrix[i][j] ? "1 " : "0 ");
            }
            System.out.println("");
        }
        System.out.print("\nPRIORITIES: ");
        for (int i = 0; i < priority.length; ++i) {
            System.out.print(priority[i] + " ");
        }
        System.out.print("\n\n");
    }

    /**
     * Out of all of the priorities in priority[], gather the largest.
     * Iterates through vertex[] to account for possibly removed vertices.
     * @return    largest priority
     */
    int getHighPrio() {
        int largest = -1;
        for (int vIter = 0; vIter < vertex.length; ++vIter) {
            if (vertex[vIter]) {
                largest = Math.max(largest, priority[vIter]);
            }
        }
        return largest;
    }

    /**
     * Collects a bit vector containing the vertices that all have the largest
     * priority found. This will be used as the target states for building an
     * attractor set.
     * @param largest    Largest valid priority
     * @return           Bit vector of vertices with the largest priority
     */
    Boolean[] getHighPrioVertices(int largest) {

        Boolean[] prioVertex = new Boolean[vertex.length];
        for (int fillIter = 0; fillIter < vertex.length; ++fillIter) {
            prioVertex[fillIter] = false;
        }
        for (int vIter = 0; vIter < vertex.length; ++vIter) {
            if (vertex[vIter] && priority[vIter] == largest) {
                prioVertex[vIter] = true;
            }
        }
        return prioVertex;
    }

    /**
     * Predicate function to check if all of a given array has been marked
     * false.
     * @param arrToCheck    Array being checked
     * @return              true if no more valid vertices within array
     */
    Boolean isArrEmpty(Boolean[] arrToCheck) {
        for (Boolean arrIter: arrToCheck) {
            if (arrIter) {
                return false;
            }
        }
        return true;
    }
}