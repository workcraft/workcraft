package org.workcraft.plugins.mpsat_verification;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.SpotAssertionVerificationCommand;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class SpotAssertionVerificationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PunfSettings.setLtl2tgbaCommand(BackendUtils.getTemplateToolPath("Spot", "ltl2tgba"));
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
    }

    @Test
    public void testVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        SpotAssertionVerificationCommand command = new SpotAssertionVerificationCommand();
        Assert.assertNull(command.execute(we, command.deserialiseData("incorrect - expression")));
        Assert.assertNull(command.execute(we, command.deserialiseData("G({\"r1\"} |=> \"g1\")")));
        Assert.assertFalse(command.execute(we, command.deserialiseData("G((\"dsr\") & (\"dsw\"))")));
        Assert.assertTrue(command.execute(we, command.deserialiseData("G((!\"dsr\") | (!\"dsw\"))")));
    }

}
