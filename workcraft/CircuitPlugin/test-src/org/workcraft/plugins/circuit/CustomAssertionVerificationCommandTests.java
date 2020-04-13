package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.ReachAssertionVerificationCommand;
import org.workcraft.plugins.circuit.commands.SignalAssertionVerificationCommand;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.pcomp.PcompSettings;
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
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void testVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        SignalAssertionVerificationCommand signalAssertion = new SignalAssertionVerificationCommand();
        Assert.assertNull(signalAssertion.execute(we, signalAssertion.deserialiseData("incorrect - expression")));
        Assert.assertFalse(signalAssertion.execute(we, signalAssertion.deserialiseData("dsr && dsw")));
        Assert.assertTrue(signalAssertion.execute(we, signalAssertion.deserialiseData("!dsr || !dsw")));

        ReachAssertionVerificationCommand reachAssertion = new ReachAssertionVerificationCommand();
        Assert.assertNull(reachAssertion.execute(we, reachAssertion.deserialiseData("incorrect - expression")));
        Assert.assertFalse(reachAssertion.execute(we, reachAssertion.deserialiseData("$S\"dsr\" ^ $S\"dsw\"")));
        Assert.assertTrue(reachAssertion.execute(we, reachAssertion.deserialiseData("$S\"dsr\" & $S\"dsw\"")));

        // Should be True because of the inversePredicate=false
        Assert.assertTrue(reachAssertion.execute(we, reachAssertion.deserialiseData(
                "<settings inversePredicate=\"false\"><reach>$S\"dsr\" ^ $S\"dsw\"</reach></settings>")));

        // Should be False because of the inversePredicate=false
        Assert.assertFalse(reachAssertion.execute(we, reachAssertion.deserialiseData(
                "<settings inversePredicate=\"false\"><reach>$S\"dsr\" &amp; $S\"dsw\"</reach></settings>")));
    }

}
