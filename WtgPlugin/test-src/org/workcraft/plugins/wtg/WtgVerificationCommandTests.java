package org.workcraft.plugins.wtg;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.wtg.commands.WtgInputPropernessVerificationCommand;
import org.workcraft.plugins.wtg.commands.WtgReachabilityVerificationCommand;
import org.workcraft.plugins.wtg.commands.WtgSoundnessVerificationCommand;
import org.workcraft.util.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class WtgVerificationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testDlatchWtgVerificationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.wtg.work");
        testWtgVerificationCommands(workName, true, true, true);
    }

    @Test
    public void testBuckWtgVerificationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.wtg.work");
        testWtgVerificationCommands(workName, true, true, true);
    }

    @Test
    public void testGuardsWtgVerificationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "guards.wtg.work");
        testWtgVerificationCommands(workName, true, true, true);
    }

    private void testWtgVerificationCommands(String workName, Boolean inputProperness,
            Boolean reachability, Boolean soundness) throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());

        WtgInputPropernessVerificationCommand inputPropernessCommand = new WtgInputPropernessVerificationCommand();
        Assert.assertEquals(inputProperness, inputPropernessCommand.execute(we));

        WtgReachabilityVerificationCommand reachabilityCommand = new WtgReachabilityVerificationCommand();
        Assert.assertEquals(reachability, reachabilityCommand.execute(we));

        WtgSoundnessVerificationCommand soundnessCommand = new WtgSoundnessVerificationCommand();
        Assert.assertEquals(soundness, soundnessCommand.execute(we));

        framework.closeWork(we);
    }

}
