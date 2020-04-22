package org.workcraft.plugins.mpsat_verification;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.ReachAssertionVerificationCommand;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class ReachAssertionVerificationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void testVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        ReachAssertionVerificationCommand command = new ReachAssertionVerificationCommand();
        Assert.assertNull(command.execute(we, command.deserialiseData("incorrect - expression")));
        Assert.assertFalse(command.execute(we, command.deserialiseData("$S\"dsr\" ^ $S\"dsw\"")));
        Assert.assertTrue(command.execute(we, command.deserialiseData("$S\"dsr\" & $S\"dsw\"")));

        // Should be True because of the inversePredicate=false
        Assert.assertTrue(command.execute(we, command.deserialiseData(
                "<settings inversePredicate=\"false\"><reach>$S\"dsr\" ^ $S\"dsw\"</reach></settings>")));

        // Should be False because of the inversePredicate=false
        Assert.assertFalse(command.execute(we, command.deserialiseData(
                "<settings inversePredicate=\"false\"><reach>$S\"dsr\" &amp; $S\"dsw\"</reach></settings>")));
    }

}
