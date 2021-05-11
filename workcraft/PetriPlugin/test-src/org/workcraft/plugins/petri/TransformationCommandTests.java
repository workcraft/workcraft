package org.workcraft.plugins.petri;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.petri.commands.*;
import org.workcraft.plugins.petri.utils.ConnectionUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class TransformationCommandTests {

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testArcTransformationCommands() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String workName = PackageUtils.getPackagePath(getClass(), "basic.pn.work");
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualPetri petri = WorkspaceUtils.getAs(we, VisualPetri.class);
        checkArcCount(petri, 3, 3, 1);

        petri.selectNone();
        new ReadArcToDualArcTransformationCommand().execute(we);
        checkArcCount(petri, 4, 4, 0);

        VisualPlace p1 = petri.getVisualComponentByMathReference("p1", VisualPlace.class);
        VisualTransition t2 = petri.getVisualComponentByMathReference("t2", VisualTransition.class);
        VisualConnection connection = petri.getConnection(p1, t2);
        Assertions.assertNotNull(connection);

        petri.select(connection);
        new DirectedArcToReadArcTransformationCommand().execute(we);
        checkArcCount(petri, 3, 3, 1);

        petri.selectNone();
        new DualArcToReadArcTransformationCommand().execute(we);
        checkArcCount(petri, 2, 2, 2);

        framework.closeWork(we);
    }

    private void checkArcCount(VisualPetri petri, int consumingArcCount, int producingArcCount, int readArcCount) {
        Assertions.assertEquals(consumingArcCount, ConnectionUtils.getVisualConsumingArcs(petri).size());
        Assertions.assertEquals(producingArcCount, ConnectionUtils.getVisualProducingArcs(petri).size());
        Assertions.assertEquals(readArcCount, ConnectionUtils.getVisualReadArcs(petri).size());
    }

    @Test
    public void testProxyTransformationCommands() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String workName = PackageUtils.getPackagePath(getClass(), "basic.pn.work");
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualPetri petri = WorkspaceUtils.getAs(we, VisualPetri.class);
        Assertions.assertEquals(0, ConnectionUtils.getVisualReplicaPlaces(petri).size());

        VisualPlace p1 = petri.getVisualComponentByMathReference("p1", VisualPlace.class);
        VisualTransition t2 = petri.getVisualComponentByMathReference("t2", VisualTransition.class);
        VisualConnection readArc = petri.getConnection(p1, t2);
        Assertions.assertTrue(readArc instanceof VisualReadArc);

        petri.select(readArc);
        new ProxyReadArcPlaceTransformationCommand().execute(we);
        Assertions.assertEquals(1, ConnectionUtils.getVisualReplicaPlaces(petri).size());

        VisualPlace p2 = petri.getVisualComponentByMathReference("p2", VisualPlace.class);
        VisualConnection producingArc = petri.getConnection(t2, p2);
        Assertions.assertNotNull(producingArc);

        petri.select(producingArc);
        new ProxyDirectedArcPlaceTransformationCommand().execute(we);
        Assertions.assertEquals(2, ConnectionUtils.getVisualReplicaPlaces(petri).size());

        petri.selectNone();
        new CollapseProxyTransformationCommand().execute(we);
        Assertions.assertEquals(0, ConnectionUtils.getVisualReplicaPlaces(petri).size());

        framework.closeWork(we);
    }

    @Test
    public void testContractionTransformationCommands() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String workName = PackageUtils.getPackagePath(getClass(), "basic.pn.work");
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualPetri petri = WorkspaceUtils.getAs(we, VisualPetri.class);
        checkArcCount(petri, 3, 3, 1);
        Assertions.assertEquals(3, petri.getVisualPlaces().size());
        Assertions.assertEquals(3, petri.getVisualTransitions().size());

        VisualTransition t0 = petri.getVisualComponentByMathReference("t0", VisualTransition.class);
        Assertions.assertNotNull(t0);

        petri.select(t0);
        new ContractTransitionTransformationCommand().execute(we);
        checkArcCount(petri, 2, 2, 1);
        Assertions.assertEquals(2, petri.getVisualPlaces().size());
        Assertions.assertEquals(2, petri.getVisualTransitions().size());

        framework.closeWork(we);
    }

    @Test
    public void testMergeTransitionsTransformationCommands() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String workName = PackageUtils.getPackagePath(getClass(), "basic.pn.work");
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualPetri petri = WorkspaceUtils.getAs(we, VisualPetri.class);
        checkArcCount(petri, 3, 3, 1);
        Assertions.assertEquals(3, petri.getVisualPlaces().size());
        Assertions.assertEquals(3, petri.getVisualTransitions().size());

        VisualTransition t0 = petri.getVisualComponentByMathReference("t0", VisualTransition.class);
        Assertions.assertNotNull(t0);
        VisualTransition t1 = petri.getVisualComponentByMathReference("t1", VisualTransition.class);
        Assertions.assertNotNull(t1);

        petri.selectNone();
        petri.addToSelection(t0);
        petri.addToSelection(t1);
        new MergeTransitionTransformationCommand().execute(we);
        checkArcCount(petri, 3, 3, 1);
        Assertions.assertEquals(3, petri.getVisualPlaces().size());
        Assertions.assertEquals(2, petri.getVisualTransitions().size());

        VisualPlace p0 = petri.getVisualComponentByMathReference("p0", VisualPlace.class);
        Assertions.assertNotNull(p0);
        VisualPlace p2 = petri.getVisualComponentByMathReference("p2", VisualPlace.class);
        Assertions.assertNotNull(p2);

        petri.selectNone();
        petri.addToSelection(p0);
        petri.addToSelection(p2);
        new MergePlaceTransformationCommand().execute(we);
        checkArcCount(petri, 3, 3, 1);
        Assertions.assertEquals(2, petri.getVisualPlaces().size());
        Assertions.assertEquals(2, petri.getVisualTransitions().size());

        framework.closeWork(we);
    }

}
