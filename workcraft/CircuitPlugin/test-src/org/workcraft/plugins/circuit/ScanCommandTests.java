package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.ScanInsertionCommand;
import org.workcraft.plugins.circuit.commands.TestableGateInsertionCommand;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.*;

class ScanCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testScanChainInsertionCommand() throws DeserialisationException {
        CircuitSettings.setTbufData("FF (D, O)");
        CircuitSettings.setTinvData("FF (D, QN)");
        CircuitSettings.setScanSuffix("_SCAN");
        CircuitSettings.setScaninData("scanin / SI");
        CircuitSettings.setScanoutData("scanout / SO");
        CircuitSettings.setScanckData("scanen / SE");
        CircuitSettings.setScanckData("scanck / CK");
        CircuitSettings.setScantmData(null);
        CircuitSettings.setIndividualScan(false);

        String workName = PackageUtils.getPackagePath(getClass(), "scan-bufs_invs_boxes.circuit.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);

        Assertions.assertEquals(3, getHangingContactCount(circuit));

        Set<String> expectedInputSignals = new HashSet<>(Arrays.asList("in0", "in0a", "in0b", "in0c", "in0d"));
        Assertions.assertEquals(expectedInputSignals, RefinementUtils.getInputSignals(circuit));

        Set<String> expectedOutputSignals = new HashSet<>(Arrays.asList("out0", "out0a", "out0b", "out0c", "out0d"));
        Assertions.assertEquals(expectedOutputSignals, RefinementUtils.getOutputSignals(circuit));

        Map<String, Integer> expectedModuleInstanceCount = new HashMap<>();
        expectedModuleInstanceCount.put("BUF", 4);
        expectedModuleInstanceCount.put("INV", 2);
        expectedModuleInstanceCount.put("NAND2", 2);
        expectedModuleInstanceCount.put("OR2", 1);
        expectedModuleInstanceCount.put("", 1);
        Assertions.assertEquals(expectedModuleInstanceCount, getModuleCount(circuit));

        Map<String, Integer> expectedSignalConnections = new HashMap<>();
        expectedSignalConnections.put("in0", 1);
        expectedSignalConnections.put("in0a", 1);
        expectedSignalConnections.put("in0b", 1);
        expectedSignalConnections.put("in0c", 1);
        expectedSignalConnections.put("in0d", 1);
        expectedSignalConnections.put("out0", 1);
        expectedSignalConnections.put("out0a", 1);
        expectedSignalConnections.put("out0b", 1);
        expectedSignalConnections.put("out0c", 1);
        expectedSignalConnections.put("out0d", 1);
        Assertions.assertEquals(expectedSignalConnections, getSignalConnectionCount(circuit));

        // Insert testable gates
        new TestableGateInsertionCommand().run(we);

        Assertions.assertEquals(3, getHangingContactCount(circuit));
        Assertions.assertEquals(expectedInputSignals, RefinementUtils.getInputSignals(circuit));
        Assertions.assertEquals(expectedOutputSignals, RefinementUtils.getOutputSignals(circuit));

        expectedModuleInstanceCount.remove("BUF");
        expectedModuleInstanceCount.remove("INV");
        expectedModuleInstanceCount.put("BUF", 2);
        expectedModuleInstanceCount.put("FF", 4);
        Assertions.assertEquals(expectedModuleInstanceCount, getModuleCount(circuit));

        Assertions.assertEquals(expectedSignalConnections, getSignalConnectionCount(circuit));

        // Insert scan
        new ScanInsertionCommand().run(we);

        Assertions.assertEquals(0, getHangingContactCount(circuit));

        expectedInputSignals.addAll(Arrays.asList("scanin", "scanen", "scanck"));
        Assertions.assertEquals(expectedInputSignals, RefinementUtils.getInputSignals(circuit));

        expectedOutputSignals.addAll(Collections.singletonList("scanout"));
        Assertions.assertEquals(expectedOutputSignals, RefinementUtils.getOutputSignals(circuit));

        expectedModuleInstanceCount.remove("FF");
        expectedModuleInstanceCount.put("FF_SCAN", 4);
        expectedModuleInstanceCount.put("BUF", 3); // Add 1 buffer for fork to 2 output ports
        Assertions.assertEquals(expectedModuleInstanceCount, getModuleCount(circuit));

        expectedSignalConnections.put("scanin", 1);
        expectedSignalConnections.put("scanout", 1);
        expectedSignalConnections.put("scanen", 5);
        expectedSignalConnections.put("scanck", 4);
        Assertions.assertEquals(expectedSignalConnections, getSignalConnectionCount(circuit));

        framework.closeWork(we);
    }

    @Test
    void testScanIndividualInsertionCommand() throws DeserialisationException {
        CircuitSettings.setTbufData("MUX2 (I0, O)");
        CircuitSettings.setTinvData("NMUX2 (I0, ON)");
        CircuitSettings.setScanSuffix("");
        CircuitSettings.setScaninData("scanin / I1");
        CircuitSettings.setScanoutData("scanout / I0");
        CircuitSettings.setScanckData(null);
        CircuitSettings.setScantmData(null);
        CircuitSettings.setIndividualScan(true);

        String workName = PackageUtils.getPackagePath(getClass(), "scan-bufs_invs_boxes.circuit.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);

        Assertions.assertEquals(3, getHangingContactCount(circuit));

        Set<String> expectedInputSignals = new HashSet<>(Arrays.asList("in0", "in0a", "in0b", "in0c", "in0d"));
        Assertions.assertEquals(expectedInputSignals, RefinementUtils.getInputSignals(circuit));

        Set<String> expectedOutputSignals = new HashSet<>(Arrays.asList("out0", "out0a", "out0b", "out0c", "out0d"));
        Assertions.assertEquals(expectedOutputSignals, RefinementUtils.getOutputSignals(circuit));

        Map<String, Integer> expectedModuleInstanceCount = new HashMap<>();
        expectedModuleInstanceCount.put("BUF", 4);
        expectedModuleInstanceCount.put("INV", 2);
        expectedModuleInstanceCount.put("NAND2", 2);
        expectedModuleInstanceCount.put("OR2", 1);
        expectedModuleInstanceCount.put("", 1);
        Assertions.assertEquals(expectedModuleInstanceCount, getModuleCount(circuit));

        Map<String, Integer> expectedSignalConnections = new HashMap<>();
        expectedSignalConnections.put("in0", 1);
        expectedSignalConnections.put("in0a", 1);
        expectedSignalConnections.put("in0b", 1);
        expectedSignalConnections.put("in0c", 1);
        expectedSignalConnections.put("in0d", 1);
        expectedSignalConnections.put("out0", 1);
        expectedSignalConnections.put("out0a", 1);
        expectedSignalConnections.put("out0b", 1);
        expectedSignalConnections.put("out0c", 1);
        expectedSignalConnections.put("out0d", 1);
        Assertions.assertEquals(expectedSignalConnections, getSignalConnectionCount(circuit));

        // Insert testable gates
        new TestableGateInsertionCommand().run(we);

        Assertions.assertEquals(3, getHangingContactCount(circuit));
        Assertions.assertEquals(expectedInputSignals, RefinementUtils.getInputSignals(circuit));
        Assertions.assertEquals(expectedOutputSignals, RefinementUtils.getOutputSignals(circuit));

        expectedModuleInstanceCount.remove("BUF");
        expectedModuleInstanceCount.remove("INV");
        expectedModuleInstanceCount.put("BUF", 2);
        expectedModuleInstanceCount.put("MUX2", 2);
        expectedModuleInstanceCount.put("NMUX2", 2);
        Assertions.assertEquals(expectedModuleInstanceCount, getModuleCount(circuit));

        Assertions.assertEquals(expectedSignalConnections, getSignalConnectionCount(circuit));

        // Insert scan
        new ScanInsertionCommand().run(we);

        Assertions.assertEquals(0, getHangingContactCount(circuit));

        expectedInputSignals.addAll(Arrays.asList(
                "scanin__0", "scanin__1", "scanin__2", "scanin__3", "scanin__4",
                "scanen__0", "scanen__1", "scanen__2", "scanen__3", "scanen__4"
        ));
        Assertions.assertEquals(expectedInputSignals, RefinementUtils.getInputSignals(circuit));

        expectedOutputSignals.addAll(Arrays.asList(
                "scanout__0", "scanout__1", "scanout__2", "scanout__3", "scanout__4"
        ));
        Assertions.assertEquals(expectedOutputSignals, RefinementUtils.getOutputSignals(circuit));

        expectedModuleInstanceCount.put("INV", 2); // Add 2 inverters for initialisation of testable gates
        expectedModuleInstanceCount.put("BUF", 4); // Add 2 buffers for direct connections between input and output ports
        Assertions.assertEquals(expectedModuleInstanceCount, getModuleCount(circuit));

        expectedSignalConnections.put("in0a", 2);
        expectedSignalConnections.put("in0c", 2);
        expectedSignalConnections.put("scanin__0", 1);
        expectedSignalConnections.put("scanin__1", 1);
        expectedSignalConnections.put("scanin__2", 1);
        expectedSignalConnections.put("scanin__3", 1);
        expectedSignalConnections.put("scanin__4", 1);
        expectedSignalConnections.put("scanout__0", 1);
        expectedSignalConnections.put("scanout__1", 1);
        expectedSignalConnections.put("scanout__2", 1);
        expectedSignalConnections.put("scanout__3", 1);
        expectedSignalConnections.put("scanout__4", 1);
        expectedSignalConnections.put("scanen__0", 1);
        expectedSignalConnections.put("scanen__1", 1);
        expectedSignalConnections.put("scanen__2", 1);
        expectedSignalConnections.put("scanen__3", 1);
        expectedSignalConnections.put("scanen__4", 1);
        Assertions.assertEquals(expectedSignalConnections, getSignalConnectionCount(circuit));

        framework.closeWork(we);
    }

    private int getHangingContactCount(Circuit circuit) {
        int result = 0;
        for (Contact contact : Hierarchy.getDescendantsOfType(circuit.getRoot(), Contact.class)) {
            if (circuit.getConnections(contact).isEmpty()) {
                result++;
            }
        }
        return result;
    }

    private static Map<String, Integer> getModuleCount(Circuit circuit) {
        Map<String, Integer> result = new HashMap<>();
        for (CircuitComponent component : circuit.getFunctionComponents()) {
            String moduleName = component.getModule();
            Integer count = result.getOrDefault(moduleName, 0);
            result.put(moduleName, count + 1);
        }
        return result;
    }

    private static Map<String, Integer> getSignalConnectionCount(Circuit circuit) {
        Map<String, Integer> result = new HashMap<>();
        for (Contact port : circuit.getPorts()) {
            String portName = circuit.getNodeReference(port);
            if (port.isOutput()) {
                Set<MathConnection> connections = circuit.getConnections(port);
                result.put(portName, connections.size());
            } else {
                Collection<Contact> driven = CircuitUtils.findDriven(circuit, port, true);
                result.put(portName, driven.size());
            }
        }
        return result;
    }

}
