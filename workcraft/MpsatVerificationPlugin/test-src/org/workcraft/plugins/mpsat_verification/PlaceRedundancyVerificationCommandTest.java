package org.workcraft.plugins.mpsat_verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.PlaceRedundancyVerificationCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class PlaceRedundancyVerificationCommandTest {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    void testPhilosophersPlaceRedundancyVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "philosophers-deadlock.pn.work");
        testPlaceRedundancyVerificationCommands(workName, "north.p4 south.p5", true);
        testPlaceRedundancyVerificationCommands(workName, " fork1_free  north.p4 ", false);
    }

    @Test
    void testVmePlaceRedundancyVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testPlaceRedundancyVerificationCommands(workName, "<d+,dtack+>", false);
        testPlaceRedundancyVerificationCommands(workName, "<d+, dtack+>", null);
        testPlaceRedundancyVerificationCommands(workName, "<d+/100,dtack+/100>", null);
    }

    @Test
    void testReadArcPlaceRedundancyVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "read_arc_place_redundancy.stg.work");
        testPlaceRedundancyVerificationCommands(workName, "essential", false);
        testPlaceRedundancyVerificationCommands(workName, "redundant", true);
        testPlaceRedundancyVerificationCommands(workName, "missing", null);
    }

    private void testPlaceRedundancyVerificationCommands(String workName, String refs, Boolean redundant)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        PlaceRedundancyVerificationCommand command = new PlaceRedundancyVerificationCommand();

        Assertions.assertEquals(redundant, command.execute(we, command.deserialiseData(refs)));
    }

}
