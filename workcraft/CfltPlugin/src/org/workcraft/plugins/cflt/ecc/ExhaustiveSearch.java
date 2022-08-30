package org.workcraft.plugins.cflt.ecc;

import java.util.ArrayList;

import org.workcraft.plugins.cflt.AdvancedGraph;
import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;

/**
 *
 * This algorithm is not as efficient as it should be, it is merely a temporary place holder until a SAT solver
 * is used to solve the problem and replace it.
 */

public class ExhaustiveSearch {

    public static ArrayList<ArrayList<String>> getEdgeCliqueCover(Graph initG, ArrayList<Edge> optionalEdges) {

        //the solution, ie. a list of the vertices contained in each final clique
        ArrayList<ArrayList<String>> ecc = new ArrayList<>();
        ArrayList<ArrayList<String>> allMaxCliques = null;

        try {
            allMaxCliques = MaxCliqueEnumerator.getAllMaxCliques(initG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //printSolution(allMaxCliques);
        AdvancedGraph g = null;

        if (initG.getEdges().size() != 0) {

            //max number of cliques to be used in the final ecc (ie. the depth of the tree to be traversed)
            int k = 0;
            while (ecc.isEmpty()) {

                g = new AdvancedGraph(initG, allMaxCliques);
                ecc = branch(g, k, new ArrayList<ArrayList<String>>(), optionalEdges);
                k += 1;
            }
        }
        return ecc;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<ArrayList<String>> branch(AdvancedGraph g, int k, ArrayList<ArrayList<String>> ecc, ArrayList<Edge> optionalEdges) {

        if (g.isCovered(ecc, optionalEdges)) { return ecc; }

        if (k < 0) { return new ArrayList<ArrayList<String>>(); }

        Edge e = g.selectEdge();
        if (e == null) { return ecc; }

        for (ArrayList<String> maxClique : g.getMaximalCliques(e)) {

            ArrayList<ArrayList<String>> newEcc = (ArrayList<ArrayList<String>>) ecc.clone();
            newEcc.add(maxClique);

            ArrayList<ArrayList<String>> eccPrime = branch(g, k - 1, newEcc, optionalEdges);
            if (!eccPrime.isEmpty()) { return eccPrime; }
        }
        return new ArrayList<ArrayList<String>>();
    }
}
