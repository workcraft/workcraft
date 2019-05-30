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

public class ResetCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testCycleTmResetCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-tm.circuit.work");
        testResetCommands(workName, 2, -1, -1, 0, 2, 1, true, true);
    }

    @Test
    public void testChargeTmResetCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "charge-tm.circuit.work");
        testResetCommands(workName, 5, 5, 7, 7, 7, 6, false, true);
    }

    private void testResetCommands(String workName, int initNum, int inputNum, int problematicNum,
            int autoDiscardNum, int autoAppendNum, int finalNum, boolean activeLow, boolean pass)
            throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        if (initNum >= 0) {
            Assert.assertEquals(initNum, getForceInitCount(we));
        }

        new ForceInitClearAllTagCommand().execute(we);
        Assert.assertEquals(0, getForceInitCount(we));

        if (inputNum >= 0) {
            new ForceInitInputPortsTagCommand().execute(we);
            Assert.assertEquals(inputNum, getForceInitCount(we));
        }

        if (problematicNum >= 0) {
            new ForceInitProblematicPinsTagCommand().execute(we);
            Assert.assertEquals(problematicNum, getForceInitCount(we));
        }

        if (autoDiscardNum >= 0) {
            new ForceInitAutoDiscardTagCommand().execute(we);
            Assert.assertEquals(autoDiscardNum, getForceInitCount(we));
        }

        if (autoAppendNum >= 0) {
            new ForceInitAutoAppendTagCommand().execute(we);
            Assert.assertEquals(autoAppendNum, getForceInitCount(we));
        }

        if (finalNum >= 0) {
            if (activeLow) {
                new ResetActiveLowInsertionCommand().execute(we);
            } else {
                new ResetActiveHighInsertionCommand().execute(we);
            }
            Assert.assertEquals(finalNum, getForceInitCount(we));
        }

        Assert.assertEquals(pass, new ResetVerificationCommand().execute(we));

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
