package org.workcraft.plugins.mpsat_synthesis;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.mpsat_synthesis.commands.TechnologyMappingSynthesisCommand;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class MissingToolTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testMissingPunfVerification() throws DeserialisationException {
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf-missing"));
        MpsatSynthesisSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
        testMissingTool();
    }

    @Test
    public void testMissingMpsatVerification() throws DeserialisationException {
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatSynthesisSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat-missing"));
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
        testMissingTool();
    }

    @Test
    public void testMissingGenlibVerification() throws DeserialisationException {
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatSynthesisSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib-missing"));
        testMissingTool();
    }

    private void testMissingTool() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-compact.stg.work");
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        TechnologyMappingSynthesisCommand command = new TechnologyMappingSynthesisCommand();
        Assert.assertNull(command.execute(we));
    }

}
