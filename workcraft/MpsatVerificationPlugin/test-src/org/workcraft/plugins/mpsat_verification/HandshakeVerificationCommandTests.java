package org.workcraft.plugins.mpsat_verification;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.HandshakeVerificationCommand;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class HandshakeVerificationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void testVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "call-final-a12.stg.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        HandshakeVerificationCommand command = new HandshakeVerificationCommand();

        Assert.assertNull(command.execute(we, command.deserialiseData("incorrect - expression")));
        Assert.assertNull(command.execute(we, command.deserialiseData("{incorrect}{signals}")));

        Assert.assertTrue(command.execute(we, command.deserialiseData("{r}{a}")));
        Assert.assertTrue(command.execute(we, command.deserialiseData("  {  r  }  {  a  }  ")));
        Assert.assertFalse(command.execute(we, command.deserialiseData("{a} {r}")));
        Assert.assertTrue(command.execute(we, command.deserialiseData("{r1 r2} {a12}")));
        Assert.assertFalse(command.execute(we, command.deserialiseData("{a12} {r1 r2}")));

        Assert.assertFalse(command.execute(we, command.deserialiseData(
                "<settings type=\"PASSIVE\"><req name=\"a\"/><ack name=\"r\"/></settings>")));

        Assert.assertTrue(command.execute(we, command.deserialiseData(
                "<settings type=\"ACTIVE\"><req name=\"r\"/><ack name=\"a\"/></settings>")));

        Assert.assertTrue(command.execute(we, command.deserialiseData(
                "<settings><req name=\"r1\"/><req name=\"r2\"/><ack name=\"a12\"/></settings>")));

        Assert.assertFalse(command.execute(we, command.deserialiseData(
                "<settings><req name=\"a12\"/><ack name=\"r1\"/><ack name=\"r2\"/></settings>")));

        Assert.assertNull(command.execute(we, command.deserialiseData(
                "<settings state=\"REQ1ACK0\" allow-inversion=\"false\"><req name=\"a12\"/><ack name=\"r1\"/><ack name=\"r2\"/></settings>")));

        Assert.assertTrue(command.execute(we, command.deserialiseData(
                "<settings state=\"REQ1ACK0\" allow-inversion=\"true\"><req name=\"a12\"/><ack name=\"r1\"/><ack name=\"r2\"/></settings>")));
    }

}
