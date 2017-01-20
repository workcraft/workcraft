package org.workcraft.plugins.circuit;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.CircuitToStgConversionCommand;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CircuitConversionCommandsTests {

    private static final String[] TEST_CIRCUIT_WORKS = {
        "org/workcraft/plugins/circuit/buffer-tm.circuit.work",
//        "org/workcraft/plugins/circuit/celement-tm.circuit.work",
//        "org/workcraft/plugins/circuit/vme-tm.circuit.work",
    };

    @BeforeClass
    public static void initPlugins() {
        final Framework framework = Framework.getInstance();
        framework.initPlugins(false);
    }

    @Test
    public void testPetriConversionCommands() throws IOException, DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testCircuitWork: TEST_CIRCUIT_WORKS) {
            URL srcUrl = classLoader.getResource(testCircuitWork);

            WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
            Circuit srcCircuit = WorkspaceUtils.getAs(srcWe, Circuit.class);

            Set<String> srcCircuitInputs = new HashSet<>();
            Set<String> srcCircuitOutputs = new HashSet<>();
            for (Contact port: srcCircuit.getPorts()) {
                if (port.isInput()) {
                    srcCircuitInputs.add(port.getName());
                } else {
                    srcCircuitOutputs.add(port.getName());
                }
            }
            int srcCircuitSignalCount = srcCircuitInputs.size();
            for (FunctionComponent component: srcCircuit.getFunctionComponents()) {
                if (!component.getIsZeroDelay()) {
                    srcCircuitSignalCount++;
                }
            }

            CircuitToStgConversionCommand command = new CircuitToStgConversionCommand();
            WorkspaceEntry dstWe = command.execute(srcWe);
            Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);

            int dstStgPlaceCount = dstStg.getPlaces().size();
            Set<String> dstStgInputs = dstStg.getSignalNames(Type.INPUT, null);
            Set<String> dstStgOutputs = dstStg.getSignalNames(Type.OUTPUT, null);

            Assert.assertEquals(2 * srcCircuitSignalCount, dstStgPlaceCount);
            Assert.assertEquals(srcCircuitInputs, dstStgInputs);
            Assert.assertEquals(srcCircuitOutputs, dstStgOutputs);
        }
    }

}
