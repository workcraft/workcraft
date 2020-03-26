package org.workcraft.plugins.punf;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.punf.commands.SpotAssertionVerificationCommand;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class CustomAssertionVerificationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        PunfSettings.setLtl2tgbaCommand(BackendUtils.getTemplateToolPath("Spot", "ltl2tgba"));
    }

    @Test
    public void testVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        SpotAssertionVerificationCommand spotAssertion = new SpotAssertionVerificationCommand();
        Assert.assertNull(spotAssertion.execute(we, spotAssertion.deserialiseData("incorrect - expression")));
        Assert.assertFalse(spotAssertion.execute(we, spotAssertion.deserialiseData("G((\"dsr\") & (\"dsw\"))")));
        Assert.assertTrue(spotAssertion.execute(we, spotAssertion.deserialiseData("G((!\"dsr\") | (!\"dsw\"))")));
    }

}
