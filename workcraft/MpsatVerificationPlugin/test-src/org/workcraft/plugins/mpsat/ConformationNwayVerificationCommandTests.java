package org.workcraft.plugins.mpsat;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat.commands.ConformationNwayVerificationCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;

import java.net.URL;

public class ConformationNwayVerificationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void testHoldNwayConformationVerification() throws DeserialisationException {
        testConformationNwayVerificationCommand(true,
                PackageUtils.getPackagePath(getClass(), "block1.stg.work"),
                PackageUtils.getPackagePath(getClass(), "block2.stg.work"),
                PackageUtils.getPackagePath(getClass(), "block3.stg.work"));
    }

    @Test
    public void testViolateNwayConformationVerification() throws DeserialisationException {
        testConformationNwayVerificationCommand(false,
                PackageUtils.getPackagePath(getClass(), "block1-bad.stg.work"),
                PackageUtils.getPackagePath(getClass(), "block2.stg.work"),
                PackageUtils.getPackagePath(getClass(), "block3.stg.work"));
    }

    @Test
    public void testFailNwayConformationVerification() throws DeserialisationException {
        testConformationNwayVerificationCommand(null,
                PackageUtils.getPackagePath(getClass(), "block1.stg.work"));
    }

    private void testConformationNwayVerificationCommand(Boolean result, String... workNames) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        framework.closeAllWorks();

        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String workName : workNames) {
            URL url = classLoader.getResource(workName);
            framework.loadWork(url.getFile());
        }

        ConformationNwayVerificationCommand command = new ConformationNwayVerificationCommand();
        Assert.assertEquals(result, command.execute(null));
    }

}
