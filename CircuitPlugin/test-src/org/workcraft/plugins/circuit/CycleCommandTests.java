package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class CycleCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testCycleTmLoopbreakerCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-tm.circuit.work");
        testLoopbreakerCommands(workName, 0, true);
    }

    @Test
    public void testChargeTmLoopbreakerCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "charge-tm.circuit.work");
        testLoopbreakerCommands(workName, 3, true);
    }

    private void testLoopbreakerCommands(String workName, int breakCount, boolean pass)
            throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);

        new PathBreakerClearAllTagCommand().execute(we);
        Assert.assertEquals(0, countPathBreaker(circuit));

        new PathBreakerAutoAppendTagCommand().execute(we);
        int count = countPathBreaker(circuit);
        Assert.assertEquals(breakCount, count);

        if (count > 0) {
            new TestableGateInsertionCommand().execute(we);
            Assert.assertEquals(breakCount, countPathBreaker(circuit));

            new ScanInsertionCommand().execute(we);

            Pair<String, String> scanckPortPin = CircuitSettings.parseScanckPortPin();
            Pair<String, String> scanenPortPin = CircuitSettings.parseScanckPortPin();
            Pair<String, String> scaninPortPin = CircuitSettings.parseScanckPortPin();
            Pair<String, String> scanoutPortPin = CircuitSettings.parseScanckPortPin();

            MathNode scanckPort = circuit.getNodeByReference(scanckPortPin.getFirst());
            Assert.assertTrue(scanckPort instanceof FunctionContact);

            MathNode scanenPort = circuit.getNodeByReference(scanenPortPin.getFirst());
            Assert.assertTrue(scanenPort instanceof FunctionContact);

            MathNode scaninPort = circuit.getNodeByReference(scaninPortPin.getFirst());
            Assert.assertTrue(scaninPort instanceof FunctionContact);

            MathNode scanoutPort = circuit.getNodeByReference(scanoutPortPin.getFirst());
            Assert.assertTrue(scanoutPort instanceof FunctionContact);

            for (FunctionComponent component : circuit.getFunctionComponents()) {
                if (ScanUtils.hasPathBreakerOutput(component)) {
                    MathNode scanckPin = circuit.getNodeByReference(component, scanckPortPin.getSecond());
                    Assert.assertTrue(scanckPin instanceof FunctionContact);
                    Assert.assertEquals(scanckPort, CircuitUtils.findDriver(circuit, scanckPin, false));

                    MathNode scanenPin = circuit.getNodeByReference(component, scanenPortPin.getSecond());
                    Assert.assertTrue(scanenPin instanceof FunctionContact);
                    Assert.assertEquals(scanenPort, CircuitUtils.findDriver(circuit, scanenPin, false));
                }
            }
        }

        Assert.assertEquals(pass, new CycleFreenessVerificationCommand().execute(we));

        framework.closeWork(we);
    }

    private int countPathBreaker(Circuit circuit) {
        int result = 0;
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (ScanUtils.hasPathBreakerOutput(component)) {
                result++;
            }
            for (Contact contact : component.getInputs()) {
                if (contact.getPathBreaker()) {
                    result++;
                }
            }
        }
        return result;
    }

}
