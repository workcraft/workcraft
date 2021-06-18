package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.SplitGateTransformationCommand;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Collection;
import java.util.Set;

class SplitGateTransformationCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testDisconnectedSplitGateTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "split-disconnected.circuit.work");
        testSplitGateTransformationCommand(workName, 0, 3, 5, 2);
    }

    @Test
    void testLiteralReuseSplitGateTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "split-literal_reuse.circuit.work");
        testSplitGateTransformationCommand(workName, 0, 3, 5, 2);
    }

    @Test
    void testVmeSplitGateTransformationCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testSplitGateTransformationCommand(workName, 15, 18, 85, 0);
    }

    private void testSplitGateTransformationCommand(String workName, int expMappedGateCount, int expUnmappedGateCount,
            int expConnectionCount, int expDisconnectedContactCount) throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);

        circuit.selectAll();

        SplitGateTransformationCommand command = new SplitGateTransformationCommand();
        command.execute(we);

        int mappedGateCount = 0;
        int unmappedGateCount = 0;
        for (VisualFunctionComponent component: circuit.getVisualFunctionComponents()) {
            if (component.isMapped()) {
                mappedGateCount++;
            } else {
                unmappedGateCount++;
            }
        }

        Assertions.assertEquals(expMappedGateCount, mappedGateCount);
        Assertions.assertEquals(expUnmappedGateCount, unmappedGateCount);

        Collection<VisualConnection> connections = Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class);
        Assertions.assertEquals(expConnectionCount, connections.size());

        int disconnectedContactCount = 0;
        for (VisualFunctionContact contact : circuit.getVisualFunctionContacts()) {
            Set<VisualConnection> contactConnections = circuit.getConnections(contact);
            if (contactConnections.isEmpty()) {
                disconnectedContactCount++;
            }
        }
        Assertions.assertEquals(expDisconnectedContactCount, disconnectedContactCount);

        framework.closeWork(we);
    }

}
