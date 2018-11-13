package org.workcraft.plugins.wtg;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.wtg.commands.WtgInputPropernessVerificationCommand;
import org.workcraft.plugins.wtg.commands.WtgReachabilityVerificationCommand;
import org.workcraft.plugins.wtg.commands.WtgSoundnessVerificationCommand;
import org.workcraft.plugins.wtg.commands.WtgSynthesisGuidelinesVerificationCommand;
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
    public void testDlatchVerificationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.wtg.work");
        testVerificationCommands(workName, true, true, true, false);
    }

    @Test
    public void testBuckVerificationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.wtg.work");
        testVerificationCommands(workName, true, true, true, true);
    }

    @Test
    public void testGuardsVerificationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "instruction_decoder.wtg.work");
        testVerificationCommands(workName, true, true, true, true);
    }

    private void testVerificationCommands(String workName, Boolean inputProperness,
            Boolean reachability, Boolean soundness, Boolean guidelines) throws DeserialisationException {

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

        WtgSynthesisGuidelinesVerificationCommand guidelinesCommand = new WtgSynthesisGuidelinesVerificationCommand();
        Assert.assertEquals(guidelines, guidelinesCommand.execute(we));

        framework.closeWork(we);
    }

}
