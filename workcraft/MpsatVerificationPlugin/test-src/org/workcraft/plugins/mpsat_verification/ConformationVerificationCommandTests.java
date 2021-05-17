package org.workcraft.plugins.mpsat_verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.ConformationVerificationCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.net.URL;

public class ConformationVerificationCommandTests {

    @BeforeAll
    public static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void testCycleConformationVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "cycle-mutex.stg.work");
        String envName = PackageUtils.getPackagePath(getClass(), "charge.stg.work");
        testConformationVerificationCommands(workName, envName, true);
    }

    @Test
    public void testDevConformationVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dev.stg.work");
        String envName = PackageUtils.getPackagePath(getClass(), "env.stg.work");
        testConformationVerificationCommands(workName, envName, true);
    }

    @Test
    public void testDevBadConformationVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dev-bad.stg.work");
        String envName = PackageUtils.getPackagePath(getClass(), "env.stg.work");
        testConformationVerificationCommands(workName, envName, false);
    }

    @Test
    public void testDevFailConformationVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dev-bad.stg.work");
        String envName = PackageUtils.getPackagePath(getClass(), null);
        testConformationVerificationCommands(workName, envName, null);
    }

    private void testConformationVerificationCommands(String workName, String envName, Boolean expectedOutcome)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        ConformationVerificationCommand conformationCommand = new ConformationVerificationCommand();
        URL envUrl = classLoader.getResource(envName);
        File data = envUrl == null ? null : conformationCommand.deserialiseData(envUrl.getFile());
        Assertions.assertEquals(expectedOutcome, conformationCommand.execute(we, data));
    }

}
