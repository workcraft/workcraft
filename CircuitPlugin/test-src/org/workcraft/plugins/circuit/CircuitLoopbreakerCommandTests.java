package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.CircuitInsertLoopbreakerBuffersCommand;
import org.workcraft.plugins.circuit.commands.CircuitInsertPathbreakerScanCommand;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Iterator;

public class CircuitLoopbreakerCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testCycleTmCircuitLoopbreakerCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-tm.circuit.work");
        testCircuitLoopbreakerCommand(workName, 0);
    }

    @Test
    public void testChargeTmCircuitLoopbreakerCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "charge-tm.circuit.work");
        testCircuitLoopbreakerCommand(workName, 3);
    }

    private void testCircuitLoopbreakerCommand(String workName, int expLoopbreakerCount)
            throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);

        new CircuitInsertLoopbreakerBuffersCommand().execute(we);
        int loopbreakerCount = 0;
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getPathBreaker()) {
                loopbreakerCount++;
            }
        }
        Assert.assertEquals(expLoopbreakerCount, loopbreakerCount);

        if (loopbreakerCount > 0) {
            new CircuitInsertPathbreakerScanCommand().execute(we);


            Iterator<String> portNameIterator = CircuitSettings.parseScanPorts().iterator();
            Iterator<String> pinNameIterator = CircuitSettings.parseScanPins().iterator();

            while (portNameIterator.hasNext() || pinNameIterator.hasNext()) {
                String portName = portNameIterator.next();
                MathNode port = circuit.getNodeByReference(portName);
                Assert.assertTrue(port instanceof FunctionContact);

                String pinName = pinNameIterator.next();
                for (FunctionComponent component : circuit.getFunctionComponents()) {
                    if (component.getPathBreaker()) {
                        MathNode pin = circuit.getNodeByReference(component, pinName);
                        Assert.assertTrue(pin instanceof FunctionContact);
                        Assert.assertEquals(port, CircuitUtils.findDriver(circuit, pin, false));
                    }
                }
            }
        }

        framework.closeWork(we);
    }

}
