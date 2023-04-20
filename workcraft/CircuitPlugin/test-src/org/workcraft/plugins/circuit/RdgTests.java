package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.refinement.RdgToPetriConverter;
import org.workcraft.plugins.circuit.refinement.RefinementDependencyGraph;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.utils.DirectedGraphUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

class RdgTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testInvalidRdg() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "rdg-invalid/top.circuit.work");
        RefinementDependencyGraph rdg = buildRdg(workName);
        testRdg(rdg, 5, 6, 4);
        testPetri(rdg, 5, 7, 12, true);
    }

    @Test
    void testCyclicRdg() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "rdg-cyclic/top.circuit.work");
        RefinementDependencyGraph rdg = buildRdg(workName);
        testRdg(rdg, 3, 5, 4);
        testPetri(rdg, 3, 6, 11, false);
    }

    @Test
    void testAcyclicRdg() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "rdg-acyclic/top.circuit.work");
        RefinementDependencyGraph rdg = buildRdg(workName);
        testRdg(rdg, 4, 6, 4);
        testPetri(rdg, 4, 7, 12, true);
    }

    private RefinementDependencyGraph buildRdg(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());

        RefinementDependencyGraph rdg = new RefinementDependencyGraph(srcWe);
        framework.closeWork(srcWe);

        return rdg;
    }

    private void testRdg(RefinementDependencyGraph rdg, int vertexCount, int edgeCount, int metaEdgeCount) {
        Assertions.assertEquals(vertexCount, rdg.getVertices().size());
        Assertions.assertEquals(edgeCount, rdg.getVertices().stream()
                .map(rdg::getInstanceDependencyMap)
                .mapToInt(Map::size)
                .sum());

        Map<File, Set<File>> graph = rdg.getSimpleGraph();
        Assertions.assertEquals(vertexCount, graph.size());
        Assertions.assertEquals(metaEdgeCount, rdg.getVertices().stream()
                .mapToInt(file -> rdg.getDependencies(file).size())
                .sum());
    }

    private void testPetri(RefinementDependencyGraph rdg, int pageCount, int placeCount, int arcCount, boolean noCycles) {
        RdgToPetriConverter converter = new RdgToPetriConverter(rdg);
        VisualPetri petri = converter.getPetri();
        Assertions.assertEquals(pageCount, Hierarchy.getDescendantsOfType(petri.getRoot(), VisualPage.class).size());
        Assertions.assertEquals(pageCount, petri.getVisualTransitions().size());

        Assertions.assertEquals(placeCount, petri.getVisualPlaces().size());

        Assertions.assertEquals(arcCount, Hierarchy.getDescendantsOfType(petri.getRoot(), VisualConnection.class).size());

        Map<File, Set<File>> graph = rdg.getSimpleGraph();
        Set<List<File>> simpleCycles = DirectedGraphUtils.findSimpleCycles(graph);
        Assertions.assertEquals(noCycles, simpleCycles.isEmpty());
        if (!simpleCycles.isEmpty()) {
            converter.highlightCycle(simpleCycles.iterator().next(), Color.RED);
            boolean hasColoredPlaces = petri.getVisualPlaces().stream()
                    .anyMatch(place -> Color.RED.equals(place.getForegroundColor()));

            boolean hasColoredTransitions = petri.getVisualTransitions().stream()
                    .anyMatch(place -> Color.RED.equals(place.getForegroundColor()));

            Assertions.assertEquals(noCycles, !hasColoredPlaces && !hasColoredTransitions);
        }
    }

}
