package org.workcraft.plugins.mpsat_verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.ConsistencyVerificationCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class ConsistencyVerificationCommandTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    void testInconsistentAlternationVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "consistency_violation-no_alternation.stg.work");
        testConsistencyVerificationCommand(workName, false);
    }

    @Test
    void testInconsistentConflictVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "consistency_violation-conflict.stg.work");
        testConsistencyVerificationCommand(workName, false);
    }

    private void testConsistencyVerificationCommand(String workName, Boolean expected)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        ConsistencyVerificationCommand consistencyCommand = new ConsistencyVerificationCommand();
        Assertions.assertEquals(expected, consistencyCommand.execute(we));
    }

}
