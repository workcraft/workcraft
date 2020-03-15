package org.workcraft.plugins.mpsat;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat.commands.CombinedVerificationCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
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
    public void testMissingPcompVerification() throws DeserialisationException {
        PcompSettings.setCommand(TestUtils.getUnfoldingToolsPath("pcomp-missing"));
        PunfSettings.setCommand(TestUtils.getUnfoldingToolsPath("punf"));
        MpsatVerificationSettings.setCommand(TestUtils.getUnfoldingToolsPath("mpsat"));
        testMissingTool();
    }

    @Test
    public void testMissingPunfVerification() throws DeserialisationException {
        PcompSettings.setCommand(TestUtils.getUnfoldingToolsPath("pcomp"));
        PunfSettings.setCommand(TestUtils.getUnfoldingToolsPath("punf-missing"));
        MpsatVerificationSettings.setCommand(TestUtils.getUnfoldingToolsPath("mpsat"));
        testMissingTool();
    }

    @Test
    public void testMissingMpsatVerification() throws DeserialisationException {
        PcompSettings.setCommand(TestUtils.getUnfoldingToolsPath("pcomp"));
        PunfSettings.setCommand(TestUtils.getUnfoldingToolsPath("punf"));
        MpsatVerificationSettings.setCommand(TestUtils.getUnfoldingToolsPath("mpsat-missing"));
        testMissingTool();
    }

    private void testMissingTool() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        CombinedVerificationCommand command = new CombinedVerificationCommand();
        Assert.assertNull(command.execute(we));
    }

}
