package org.workcraft.plugins.mpsat;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.TestUtils;
import org.workcraft.plugins.mpsat.commands.TechnologyMappingSynthesisCommand;
import org.workcraft.plugins.punf.PunfSettings;
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
        PunfSettings.setCommand(TestUtils.getToolPath("UnfoldingTools", "punf-missing"));
        MpsatSynthesisSettings.setCommand(TestUtils.getToolPath("UnfoldingTools", "mpsat"));
        CircuitSettings.setGateLibrary(TestUtils.getLibraryPath("workcraft.lib"));
        testMissingTool();
    }

    @Test
    public void testMissingMpsatVerification() throws DeserialisationException {
        PunfSettings.setCommand(TestUtils.getToolPath("UnfoldingTools", "punf"));
        MpsatSynthesisSettings.setCommand(TestUtils.getToolPath("UnfoldingTools", "mpsat-missing"));
        CircuitSettings.setGateLibrary(TestUtils.getLibraryPath("workcraft.lib"));
        testMissingTool();
    }

    @Test
    public void testMissingGenlibVerification() throws DeserialisationException {
        PunfSettings.setCommand(TestUtils.getToolPath("UnfoldingTools", "punf"));
        MpsatSynthesisSettings.setCommand(TestUtils.getToolPath("UnfoldingTools", "mpsat"));
        CircuitSettings.setGateLibrary(TestUtils.getLibraryPath("workcraft.lib-missing"));
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
