package org.workcraft.plugins.mpsat_temporal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_temporal.commands.SpotAssertionVerificationCommand;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class SpotAssertionVerificationCommandTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        MpsatTemporalSettings.setLtl2tgbaCommand(BackendUtils.getTemplateToolPath("Spot", "ltl2tgba"));
        MpsatTemporalSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    void testVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        SpotAssertionVerificationCommand command = new SpotAssertionVerificationCommand();
        Assertions.assertNull(command.execute(we, command.deserialiseData("incorrect - expression")));
        Assertions.assertNull(command.execute(we, command.deserialiseData("G({\"r1\"} |=> \"g1\")")));
        Assertions.assertFalse(command.execute(we, command.deserialiseData("G((\"dsr\") & (\"dsw\"))")));
        Assertions.assertTrue(command.execute(we, command.deserialiseData("G((!\"dsr\") | (!\"dsw\"))")));
    }

}
