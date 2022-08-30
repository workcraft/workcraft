package org.workcraft.plugins.cflt.ecc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.plugins.cflt.Edge;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.utils.EccUtils;

public class SequenceHeuristic {

    public static ArrayList<ArrayList<String>> getEdgeCliqueCover(Graph g, ArrayList<Edge> optionalEdges) {

        //number of cliques
        int cliqueNumber = -1;
        //is the edge covered, yes 1, no 0
        HashMap<String, Integer> isCovered = new HashMap<>();
        //the uncovered degree of each vertex
        HashMap<String, Integer> uncoveredDegree = new HashMap<>();
        //all neighbours of a vertex
        HashMap<String, HashSet<String>> allNeighbours = EccUtils.initialiseNeighbours(g);

        //final cliques as a list of vertices
        ArrayList<ArrayList<String>> finalCliques = new ArrayList<>();
        //final cliques as a list of edges (ie. v1 + v2)
        ArrayList<ArrayList<String>> finalCliquesEdges = new ArrayList<>();

        //how many cliques an edge is contained in
        HashMap<String, Integer> edgeToNoOfCliquesItsContainedIn = new HashMap<>();

        //the local uncovered degree of each vertex
        HashMap<String, Integer> localUncoveredDegree = new HashMap<>();

        //optional edges
        HashSet<String> optionalEdgeSet = new HashSet<>();

        for (Edge e : optionalEdges) {
            optionalEdgeSet.add(e.getFirstVertex() + e.getSecondVertex());
            optionalEdgeSet.add(e.getSecondVertex() + e.getFirstVertex());
        }

        // all edges are initialised to uncovered
        for (Edge e : g.getEdges()) {
            isCovered.put(e.getFirstVertex() + e.getSecondVertex(), 0);
            isCovered.put(e.getSecondVertex() + e.getFirstVertex(), 0);

            edgeToNoOfCliquesItsContainedIn.put(e.getFirstVertex() + e.getSecondVertex(), 0);
            edgeToNoOfCliquesItsContainedIn.put(e.getSecondVertex() + e.getFirstVertex(), 0);
        }

        //initially, the uncovered degree of each vertex is set to it's degree
        for (String vertex : g.getVertices()) {
            if (allNeighbours.get(vertex) != null) {
                uncoveredDegree.put(vertex, allNeighbours.get(vertex).size());
            } else {
                //if the vertex is not contained in the allNeighbours map, it has no neighbours
                uncoveredDegree.put(vertex, 0);
            }
        }

        int maxCliqueSize = 0;
        int currentCliqueSize = 0;

        //for each vertex
        for (String i : g.getVertices()) {

            //while there are still uncovered edges adjacent to the vertex
            while (uncoveredDegree.get(i) > 0) {

                if (currentCliqueSize > maxCliqueSize) {
                    maxCliqueSize = currentCliqueSize;
                }
                currentCliqueSize = 1;
                cliqueNumber += 1;

                finalCliques.add(cliqueNumber, new ArrayList<String>());
                finalCliques.get(cliqueNumber).add(i);

                finalCliquesEdges.add(new ArrayList<String>());

                @SuppressWarnings("unchecked")
                HashSet<String> localNeighbourhoodOfi = (HashSet<String>) allNeighbours.get(i).clone();

                for (String j : localNeighbourhoodOfi) {
                    localUncoveredDegree.put(j, 1 - isCovered.get(i + j));
                }

                String u = EccUtils.argmax(localUncoveredDegree, localNeighbourhoodOfi);

                while (localUncoveredDegree.get(u) > 0) {

                    boolean isOptional = true;
                    for (String j : finalCliques.get(cliqueNumber)) {

                        if (isCovered.get(u + j) == 0 && isCovered.get(j + u) == 0) {
                            isCovered.replace(u + j, 1);
                            isCovered.replace(j + u, 1);

                            int temp = uncoveredDegree.get(u);
                            uncoveredDegree.replace(u, temp - 1);
                            temp = uncoveredDegree.get(j);
                            uncoveredDegree.replace(j, temp - 1);
                        }
                        //adding the key edges of the clique
                        finalCliquesEdges.get(cliqueNumber).add(u + j);
                        finalCliquesEdges.get(cliqueNumber).add(j + u);

                        //updating the number of cliques the edge is contained in
                        int oldVal = edgeToNoOfCliquesItsContainedIn.get(u + j);
                        edgeToNoOfCliquesItsContainedIn.replace(u + j, oldVal + 1);
                        edgeToNoOfCliquesItsContainedIn.replace(j + u, oldVal + 1);

                        if (!optionalEdgeSet.contains(u + j) || !optionalEdgeSet.contains(j + u)) {
                            isOptional = false;
                        }
                    }
                    if (!isOptional) {
                        finalCliques.get(cliqueNumber).add(u);
                    }
                    currentCliqueSize += 1;
                    localNeighbourhoodOfi.retainAll(allNeighbours.get(u));

                    for (String j : localNeighbourhoodOfi) {
                        if (isCovered.get(u + j) == 0) {
                            int temp = localUncoveredDegree.get(j);
                            localUncoveredDegree.replace(j, temp + 1);
                        }
                    }
                    u = EccUtils.argmax(localUncoveredDegree, localNeighbourhoodOfi);
                    if (localNeighbourhoodOfi.isEmpty()) { break; }
                }
            }

        }

        //if a clique only contains edges from the optional edge list it needs to be removed
        for (int x = 0; x < finalCliquesEdges.size(); x++) {

            boolean containsOnlyOptionalEdges = true;
            for (String edge : finalCliquesEdges.get(x)) {

                if (!optionalEdgeSet.contains(edge)) {
                    containsOnlyOptionalEdges = false;
                    break;
                }
            }
            if (containsOnlyOptionalEdges) {
                finalCliques.remove(x);
                finalCliques.add(x, new ArrayList<String>());
            }
        }

        //dealing with cliques which are not maximal
        int currentCliqueIndex = 0;
        for (ArrayList<String> finalClique : finalCliques) {
            //if the clique is not maximal
            if (finalClique.size() < maxCliqueSize && !finalClique.isEmpty()) {

                @SuppressWarnings("unchecked")
                HashSet<String> neightboursOfFirstVertex = (HashSet<String>) allNeighbours.get(finalClique.get(0)).clone();
                ArrayList<String> verticesToBeAdded = new ArrayList<>(neightboursOfFirstVertex);

                for (int x = 1; x < finalClique.size(); x++) {
                    verticesToBeAdded.retainAll(allNeighbours.get(finalClique.get(x)));
                }

                while (!verticesToBeAdded.isEmpty()) {
                    String i = verticesToBeAdded.get(0);
                    finalClique.add(i);
                    verticesToBeAdded.retainAll(allNeighbours.get(i));

                    for (String s : finalClique) {
                        if (!i.equals(s)) {
                            finalCliquesEdges.get(currentCliqueIndex).add(i + s);
                            finalCliquesEdges.get(currentCliqueIndex).add(s + i);

                            //updating the number of cliques the edge is contained in
                            int oldVal = edgeToNoOfCliquesItsContainedIn.get(i + s);
                            edgeToNoOfCliquesItsContainedIn.replace(i + s, oldVal + 1);
                            edgeToNoOfCliquesItsContainedIn.replace(s + i, oldVal + 1);
                        }
                    }
                }
            }
            currentCliqueIndex++;
        }
        //remove redundant cliques
        for (int x = 0; x < finalCliques.size(); x++) {
            //if the clique is redundant
            if (EccUtils.checkRedundancy(edgeToNoOfCliquesItsContainedIn, finalCliquesEdges.get(x))) {

                //update number of cliques an edge is contained in
                for (String edge : finalCliquesEdges.get(x)) {
                    int temp = edgeToNoOfCliquesItsContainedIn.get(edge);
                    edgeToNoOfCliquesItsContainedIn.replace(edge, temp - 1);
                }
                finalCliquesEdges.remove(x);
                finalCliques.remove(x);
            }
        }
        return finalCliques;
    }
}
