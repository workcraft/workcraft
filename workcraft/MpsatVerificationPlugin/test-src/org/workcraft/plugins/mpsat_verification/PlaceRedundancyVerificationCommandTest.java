package org.workcraft.plugins.mpsat_verification;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.mpsat_verification.commands.PlaceRedundancyVerificationCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

public class PlaceRedundancyVerificationCommandTest {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void testPhilosophersPlaceRedundancyVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "philosophers-deadlock.pn.work");
        testPlaceRedundancyVerificationCommands(workName, new String[]{"north.p4", "south.p5"}, true);
        testPlaceRedundancyVerificationCommands(workName, new String[]{"fork1_free", "north.p4"}, false);
    }

    @Test
    public void testVmePlaceRedundancyVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testPlaceRedundancyVerificationCommands(workName, new String[]{"<d+,dtack+>"}, false);
        testPlaceRedundancyVerificationCommands(workName, new String[]{"<d+/100,dtack+/100>"}, null);
    }

    private void testPlaceRedundancyVerificationCommands(String workName, String[] refs, Boolean redundant)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        PlaceRedundancyVerificationCommand command = new PlaceRedundancyVerificationCommand() {
            @Override
            protected HashSet<String> getSelectedPlaces(WorkspaceEntry we) {
                return new HashSet<>(Arrays.asList(refs));
            }
        };

        Assert.assertEquals(redundant, command.execute(we));
    }

}
