package org.workcraft.plugins.wtg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.wtg.commands.InputPropernessVerificationCommand;
import org.workcraft.plugins.wtg.commands.ReachabilityVerificationCommand;
import org.workcraft.plugins.wtg.commands.SoundnessVerificationCommand;
import org.workcraft.plugins.wtg.commands.SynthesisGuidelinesVerificationCommand;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class WtgVerificationCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testDlatchVerificationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.wtg.work");
        testVerificationCommands(workName, true, true, true, false);
    }

    @Test
    void testBuckVerificationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.wtg.work");
        testVerificationCommands(workName, true, true, true, true);
    }

    @Test
    void testGuardsVerificationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "instruction_decoder.wtg.work");
        testVerificationCommands(workName, true, true, true, true);
    }

    private void testVerificationCommands(String workName, Boolean inputProperness,
            Boolean reachability, Boolean soundness, Boolean guidelines) throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());

        InputPropernessVerificationCommand inputPropernessCommand = new InputPropernessVerificationCommand();
        Assertions.assertEquals(inputProperness, inputPropernessCommand.execute(we));

        ReachabilityVerificationCommand reachabilityCommand = new ReachabilityVerificationCommand();
        Assertions.assertEquals(reachability, reachabilityCommand.execute(we));

        SoundnessVerificationCommand soundnessCommand = new SoundnessVerificationCommand();
        Assertions.assertEquals(soundness, soundnessCommand.execute(we));

        SynthesisGuidelinesVerificationCommand guidelinesCommand = new SynthesisGuidelinesVerificationCommand();
        Assertions.assertEquals(guidelines, guidelinesCommand.execute(we));

        framework.closeWork(we);
    }

}
