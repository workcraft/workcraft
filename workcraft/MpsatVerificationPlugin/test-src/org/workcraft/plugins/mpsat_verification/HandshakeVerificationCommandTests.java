package org.workcraft.plugins.mpsat_verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.HandshakeVerificationCommand;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class HandshakeVerificationCommandTests {

    @BeforeAll
    public static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
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

        Assertions.assertNull(command.execute(we, command.deserialiseData("incorrect - expression")));
        Assertions.assertNull(command.execute(we, command.deserialiseData("{incorrect}{signals}")));

        Assertions.assertTrue(command.execute(we, command.deserialiseData("{r}{a}")));
        Assertions.assertTrue(command.execute(we, command.deserialiseData("  {  r  }  {  a  }  ")));
        Assertions.assertFalse(command.execute(we, command.deserialiseData("{a} {r}")));
        Assertions.assertTrue(command.execute(we, command.deserialiseData("{r1 r2} {a12}")));
        Assertions.assertFalse(command.execute(we, command.deserialiseData("{a12} {r1 r2}")));

        Assertions.assertFalse(command.execute(we, command.deserialiseData(
                "<settings><req name=\"a\"/><ack name=\"r\"/></settings>")));

        Assertions.assertTrue(command.execute(we, command.deserialiseData(
                "<settings><req name=\"r\"/><ack name=\"a\"/></settings>")));

        Assertions.assertTrue(command.execute(we, command.deserialiseData(
                "<settings><req name=\"r1\"/><req name=\"r2\"/><ack name=\"a12\"/></settings>")));

        Assertions.assertFalse(command.execute(we, command.deserialiseData(
                "<settings><req name=\"a12\"/><ack name=\"r1\"/><ack name=\"r2\"/></settings>")));

        Assertions.assertFalse(command.execute(we, command.deserialiseData(
                "<settings state=\"REQ1ACK0\" allow-inversion=\"false\"><req name=\"a12\"/><ack name=\"r1\"/><ack name=\"r2\"/></settings>")));

        Assertions.assertTrue(command.execute(we, command.deserialiseData(
                "<settings state=\"REQ1ACK0\" allow-inversion=\"true\"><req name=\"a12\"/><ack name=\"r1\"/><ack name=\"r2\"/></settings>")));
    }

}
