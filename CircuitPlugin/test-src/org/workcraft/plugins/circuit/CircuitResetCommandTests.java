package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.net.URL;

public class CircuitResetCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testCycleTmCircuitResetCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-tm.circuit.work");
        testCircuitResetCommand(workName, 2, -1, 0, 0, 0, 2, 1, true, true);
    }

    @Test
    public void testChargeTmCircuitResetCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "charge-tm.circuit.work");
        testCircuitResetCommand(workName, 5, 5, 8, 9, 7, 7, 6, false, true);
    }

    private void testCircuitResetCommand(String workName, int initNum, int inputNum, int loopNum, int seqNum, int clearNum,
            int completeNum, int finalNum, boolean activeLow, boolean pass)
            throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        if (initNum >= 0) {
            Assert.assertEquals(initNum, getForceInitCount(we));
        }

        new ClearForceInitCommand().execute(we);
        Assert.assertEquals(0, getForceInitCount(we));

        if (inputNum >= 0) {
            new ForceInitInputPortsCommand().execute(we);
            Assert.assertEquals(inputNum, getForceInitCount(we));
        }

        if (loopNum >= 0) {
            new ForceInitSelfLoopsCommand().execute(we);
            Assert.assertEquals(loopNum, getForceInitCount(we));
        }

        if (seqNum >= 0) {
            new ForceInitSequentialGatesCommand().execute(we);
            Assert.assertEquals(seqNum, getForceInitCount(we));
        }

        if (clearNum >= 0) {
            new ProcessRedundantForceInitPinsCommand().execute(we);
            Assert.assertEquals(clearNum, getForceInitCount(we));
        }

        if (completeNum >= 0) {
            new ProcessNecessaryForceInitPinsCommand().execute(we);
            Assert.assertEquals(completeNum, getForceInitCount(we));
        }

        if (finalNum >= 0) {
            if (activeLow) {
                new CircuitResetActiveLowCommand().execute(we);
            } else {
                new CircuitResetActiveHighCommand().execute(we);
            }
            Assert.assertEquals(finalNum, getForceInitCount(we));
        }

        Assert.assertEquals(pass, new CircuitResetVerificationCommand().execute(we));

        framework.closeWork(we);
    }

    private int getForceInitCount(WorkspaceEntry we) {
        int result = 0;
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isForcedDriver()) {
                result++;
            }
        }
        return result;
    }

}
