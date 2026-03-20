package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.ConnectHangingInputPinsToPortsTransformationCommand;
import org.workcraft.plugins.circuit.commands.ConnectHangingOutputPinsToPortsTransformationCommand;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ConnectHangingPinsToPortsTransformationCommandTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testConnectHangingPinsToPortsTransformationCommand() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String workName = PackageUtils.getPackagePath(getClass(), "hanging_pins.circuit.work");
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualCircuit visualCircuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        Assertions.assertEquals(Set.of("i2 -> {g1.i2}"), getInputPortInfos(circuit));
        Assertions.assertEquals(Set.of("o1.O -> i1"), getOutputPortInfos(circuit));

        SelectionHelper.selectByMathReferences(visualCircuit, Set.of("g0.i0"));
        new ConnectHangingInputPinsToPortsTransformationCommand().execute(we);
        Assertions.assertEquals(Set.of("i0 -> {g0.i0}", "i2 -> {g1.i2}"), getInputPortInfos(circuit));
        Assertions.assertEquals(Set.of("o1.O -> i1"), getOutputPortInfos(circuit));

        SelectionHelper.selectByMathReferences(visualCircuit, Set.of("g0"));
        new ConnectHangingInputPinsToPortsTransformationCommand().execute(we);
        // Name i1 is taken by output port, so g0.i1 remains unconnected
        Assertions.assertEquals(Set.of("i0 -> {g0.i0}", "i2 -> {g0.i2, g1.i2}"), getInputPortInfos(circuit));
        Assertions.assertEquals(Set.of("o1.O -> i1"), getOutputPortInfos(circuit));

        SelectionHelper.selectByMathReferences(visualCircuit, Set.of("g1"));
        new ConnectHangingOutputPinsToPortsTransformationCommand().execute(we);
        // Name o1 is taken by BUF instance, so g1.o1 remains unconnected
        Assertions.assertEquals(Set.of("i0 -> {g0.i0}", "i2 -> {g0.i2, g1.i2}"), getInputPortInfos(circuit));
        Assertions.assertEquals(Set.of("g1.o0 -> o0", "o1.O -> i1"), getOutputPortInfos(circuit));

        visualCircuit.selectNone();
        new ConnectHangingOutputPinsToPortsTransformationCommand().execute(we);
        // Existing input port i2 can be reused, but connected output port o0 cannot be reused
        Assertions.assertEquals(Set.of("i0 -> {g0.i0}", "i2 -> {g0.i2, g1.i2}"), getInputPortInfos(circuit));
        Assertions.assertEquals(Set.of("g1.o0 -> o0", "g0.o2 -> o2", "o1.O -> i1"), getOutputPortInfos(circuit));

        framework.closeWork(we);
    }

    private Set<String> getInputPortInfos(Circuit circuit) {
        Set<String> result = new HashSet<>();
        for (Contact inputPort : circuit.getInputPorts()) {
            Set<Contact> driven = CircuitUtils.findDriven(circuit, inputPort, false);
            Set<String> drivenRefs = ReferenceHelper.getReferenceSet(circuit, driven);
            List<String> orderedDrivenRefs = SortUtils.getSortedNatural(drivenRefs);
            result.add(inputPort.getName() + " -> {" + String.join(", ", orderedDrivenRefs) + "}");
        }
        return result;
    }

    private Set<String> getOutputPortInfos(Circuit circuit) {
        Set<String> result = new HashSet<>();
        for (Contact outputPort : circuit.getOutputPorts()) {
            Contact driver = CircuitUtils.findDriver(circuit, outputPort, false);
            String driverRef = (driver == null) ? "" : circuit.getNodeReference(driver);
            result.add(driverRef + " -> " + outputPort.getName());
        }
        return result;
    }

}
