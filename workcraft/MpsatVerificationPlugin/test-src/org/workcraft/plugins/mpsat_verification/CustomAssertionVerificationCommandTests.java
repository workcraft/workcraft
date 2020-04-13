package org.workcraft.plugins.mpsat_verification;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.ReachAssertionVerificationCommand;
import org.workcraft.plugins.mpsat_verification.commands.SignalAssertionVerificationCommand;
import org.workcraft.plugins.mpsat_verification.commands.SpotAssertionVerificationCommand;
import org.workcraft.plugins.punf.PunfSettings;
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
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void testVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        ReachAssertionVerificationCommand reachAssertionCommand = new ReachAssertionVerificationCommand();
        Assert.assertNull(reachAssertionCommand.execute(we, reachAssertionCommand.deserialiseData("incorrect - expression")));
        Assert.assertFalse(reachAssertionCommand.execute(we, reachAssertionCommand.deserialiseData("$S\"dsr\" ^ $S\"dsw\"")));
        Assert.assertTrue(reachAssertionCommand.execute(we, reachAssertionCommand.deserialiseData("$S\"dsr\" & $S\"dsw\"")));

        // Should be True because of the inversePredicate=false
        Assert.assertTrue(reachAssertionCommand.execute(we, reachAssertionCommand.deserialiseData(
                "<settings inversePredicate=\"false\"><reach>$S\"dsr\" ^ $S\"dsw\"</reach></settings>")));

        // Should be False because of the inversePredicate=false
        Assert.assertFalse(reachAssertionCommand.execute(we, reachAssertionCommand.deserialiseData(
                "<settings inversePredicate=\"false\"><reach>$S\"dsr\" &amp; $S\"dsw\"</reach></settings>")));

        SignalAssertionVerificationCommand signalAssertionCommand = new SignalAssertionVerificationCommand();
        Assert.assertNull(signalAssertionCommand.execute(we, signalAssertionCommand.deserialiseData("incorrect - expression")));
        Assert.assertFalse(signalAssertionCommand.execute(we, signalAssertionCommand.deserialiseData("dsr && dsw")));
        Assert.assertTrue(signalAssertionCommand.execute(we, signalAssertionCommand.deserialiseData("!dsr || !dsw")));

        SpotAssertionVerificationCommand spotAssertionCocommand = new SpotAssertionVerificationCommand();
        Assert.assertNull(spotAssertionCocommand.execute(we, spotAssertionCocommand.deserialiseData("incorrect - expression")));
        Assert.assertFalse(spotAssertionCocommand.execute(we, spotAssertionCocommand.deserialiseData("G((\"dsr\") & (\"dsw\"))")));
        Assert.assertTrue(spotAssertionCocommand.execute(we, spotAssertionCocommand.deserialiseData("G((!\"dsr\") | (!\"dsw\"))")));
    }

}
