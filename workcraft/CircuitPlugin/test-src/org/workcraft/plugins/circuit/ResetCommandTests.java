package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.net.URL;

class ResetCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testCycleTmResetCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-tm.circuit.work");
        testResetCommands(workName, 2, -1, -1, 0, 2, 1, true, true);
    }

    @Test
    void testChargeTmResetCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "charge-tm.circuit.work");
        testResetCommands(workName, 5, 5, 7, 7, 7, 6, false, true);
    }

    @Test
    void testConstResetCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "const.circuit.work");
        testResetCommands(workName, 0, -1, 0, 0, 0, 0, false, true);
    }

    @Test
    void testExcitedResetCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "excited.circuit.work");
        testResetCommands(workName, 0, 3, -1, 3, 4, 4, true, true);
    }

    private void testResetCommands(String workName, int initNum, int inputNum, int problematicNum,
            int autoDiscardNum, int autoAppendNum, int finalNum, boolean activeLow, boolean pass)
            throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        if (initNum >= 0) {
            Assertions.assertEquals(initNum, getForcedInitCount(we));
        }

        new ForcedInitClearAllTagCommand().execute(we);
        Assertions.assertEquals(0, getForcedInitCount(we));

        if (inputNum >= 0) {
            new ForcedInitInputPortsTagCommand().execute(we);
            Assertions.assertEquals(inputNum, getForcedInitCount(we));
        }

        if (problematicNum >= 0) {
            new ForcedInitProblematicPinsTagCommand().execute(we);
            Assertions.assertEquals(problematicNum, getForcedInitCount(we));
        }

        if (autoDiscardNum >= 0) {
            new ForcedInitAutoDiscardTagCommand().execute(we);
            Assertions.assertEquals(autoDiscardNum, getForcedInitCount(we));
        }

        if (autoAppendNum >= 0) {
            new ForcedInitAutoAppendTagCommand().execute(we);
            Assertions.assertEquals(autoAppendNum, getForcedInitCount(we));
        }

        if (finalNum >= 0) {
            if (activeLow) {
                new ResetActiveLowInsertionCommand().execute(we);
            } else {
                new ResetActiveHighInsertionCommand().execute(we);
            }
            Assertions.assertEquals(finalNum, getForcedInitCount(we));
        }

        Assertions.assertEquals(pass, new ResetVerificationCommand().execute(we));

        framework.closeWork(we);
    }

    private int getForcedInitCount(WorkspaceEntry we) {
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
