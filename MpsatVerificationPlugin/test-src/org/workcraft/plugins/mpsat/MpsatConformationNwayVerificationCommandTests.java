package org.workcraft.plugins.mpsat;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.utils.DesktopApi;
import org.workcraft.plugins.mpsat.commands.MpsatConformationNwayVerificationCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.PackageUtils;

import java.net.URL;

public class MpsatConformationNwayVerificationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        switch (DesktopApi.getOs()) {
        case LINUX:
            PcompSettings.setCommand("dist-template/linux/tools/UnfoldingTools/pcomp");
            PunfSettings.setCommand("dist-template/linux/tools/UnfoldingTools/punf");
            MpsatVerificationSettings.setCommand("dist-template/linux/tools/UnfoldingTools/mpsat");
            break;
        case MACOS:
            PcompSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/pcomp");
            PunfSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/punf");
            MpsatVerificationSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/mpsat");
            break;
        case WINDOWS:
            PcompSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\pcomp.exe");
            PunfSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\punf.exe");
            MpsatVerificationSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\mpsat.exe");
            break;
        default:
        }
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

        MpsatConformationNwayVerificationCommand command = new MpsatConformationNwayVerificationCommand();
        Assert.assertEquals(result, command.execute(null));
    }

}
