package org.workcraft.plugins.mpsat_verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.RefinementVerificationCommand;
import org.workcraft.plugins.mpsat_verification.commands.RelaxedRefinementVerificationCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.net.URL;

public class RefinementVerificationCommandTests {

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
    public void testModelSelectorCscRefinementVerification() throws DeserialisationException {
        String specificationFileName = PackageUtils.getPackagePath(getClass(), "mode_selector.stg.work");
        String implementationFileName = PackageUtils.getPackagePath(getClass(), "mode_selector-csc.stg.work");
        testRefinementVerificationCommands(specificationFileName, implementationFileName, false, true);
        testRefinementVerificationCommands(specificationFileName, implementationFileName, true, true);
    }

    @Test
    public void testModelSelectorCrRefinementVerification() throws DeserialisationException {
        String specificationFileName = PackageUtils.getPackagePath(getClass(), "mode_selector.stg.work");
        String implementationFileName = PackageUtils.getPackagePath(getClass(), "mode_selector-cr.stg.work");
        testRefinementVerificationCommands(specificationFileName, implementationFileName, false, false);
        testRefinementVerificationCommands(specificationFileName, implementationFileName, true, true);
    }

    @Test
    public void testModelSelectorCrCscDumRefinementVerification() throws DeserialisationException {
        String specificationFileName = PackageUtils.getPackagePath(getClass(), "mode_selector-csc-dum.stg.work");
        String implementationFileName = PackageUtils.getPackagePath(getClass(), "mode_selector-cr-dum.stg.work");
        testRefinementVerificationCommands(specificationFileName, implementationFileName, false, false);
        testRefinementVerificationCommands(specificationFileName, implementationFileName, true, true);
    }

    @Test
    public void testReadCompletionTaRefinementVerification() throws DeserialisationException {
        String specificationFileName = PackageUtils.getPackagePath(getClass(), "read_completion.stg.work");
        String implementationFileName = PackageUtils.getPackagePath(getClass(), "read_completion-ta.stg.work");
        testRefinementVerificationCommands(specificationFileName, implementationFileName, false, false);
        testRefinementVerificationCommands(specificationFileName, implementationFileName, true, false);
    }

    @Test
    public void testMissingSpecificationRefinementVerification() throws DeserialisationException {
        String specificationFileName = PackageUtils.getPackagePath(getClass(), null);
        String implementationFileName = PackageUtils.getPackagePath(getClass(), "read_completion-ta.stg.work");
        testRefinementVerificationCommands(specificationFileName, implementationFileName, false, null);
        testRefinementVerificationCommands(specificationFileName, implementationFileName, true, null);
    }

    private void testRefinementVerificationCommands(String specificationFileName,
            String implementationFileName, boolean relaxed, Boolean expectedOutcome)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL implementationUrl = classLoader.getResource(implementationFileName);
        WorkspaceEntry we = framework.loadWork(implementationUrl.getFile());

        RefinementVerificationCommand command = relaxed
                ? new RelaxedRefinementVerificationCommand()
                : new RefinementVerificationCommand();

        URL specificationUrl = classLoader.getResource(specificationFileName);
        File data = specificationUrl == null ? null : command.deserialiseData(specificationUrl.getFile());
        Assertions.assertEquals(expectedOutcome, command.execute(we, data));
    }

}
