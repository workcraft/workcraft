package org.workcraft.plugins.circuit;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.circuit.commands.CircuitConformationVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitDeadlockVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitPersistencyVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitStrictImplementationVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.workspace.WorkspaceEntry;

public class CircuitVerificationCommandTests {

    private static final String[] TEST_CIRCUIT_WORKS = {
        "org/workcraft/plugins/circuit/buffer.circuit.work",
        "org/workcraft/plugins/circuit/celement.circuit.work",
    };

    private static final String[] TEST_TM_CIRCUIT_WORKS = {
        "org/workcraft/plugins/circuit/celement-decomposed-tm.circuit.work",
        "org/workcraft/plugins/circuit/vme-tm.circuit.work",
    };

    @BeforeClass
    public static void initPlugins() {
        final Framework framework = Framework.getInstance();
        framework.initPlugins();
        switch (DesktopApi.getOs()) {
        case LINUX:
            PcompSettings.setCommand("../dist-template/linux/tools/UnfoldingTools/pcomp");
            PunfSettings.setCommand("../dist-template/linux/tools/UnfoldingTools/punf");
            MpsatSettings.setCommand("../dist-template/linux/tools/UnfoldingTools/mpsat");
            break;
        case MACOS:
            PcompSettings.setCommand("../dist-template/osx/Contents/Resources/tools/UnfoldingTools/pcomp");
            PunfSettings.setCommand("../dist-template/osx/Contents/Resources/tools/UnfoldingTools/punf");
            MpsatSettings.setCommand("../dist-template/osx/Contents/Resources/tools/UnfoldingTools/mpsat");
            break;
        case WINDOWS:
            PcompSettings.setCommand("..\\dist-template\\windows\\tools\\UnfoldingTools\\pcomp.exe");
            PunfSettings.setCommand("..\\dist-template\\windows\\tools\\UnfoldingTools\\punf.exe");
            MpsatSettings.setCommand("..\\dist-template\\windows\\tools\\UnfoldingTools\\mpsat.exe");
            break;
        default:
        }
    }

    @Test
    public void testCircuitVerificationCommands() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testCircuitWork: TEST_CIRCUIT_WORKS) {
            URL url = classLoader.getResource(testCircuitWork);
            WorkspaceEntry we = framework.loadWork(url.getFile());

            CircuitDeadlockVerificationCommand deadlockVerificationCommand = new CircuitDeadlockVerificationCommand();
            Assert.assertTrue(deadlockVerificationCommand.execute(we));

            CircuitConformationVerificationCommand conformationVerificationCommand = new CircuitConformationVerificationCommand();
            Assert.assertTrue(conformationVerificationCommand.execute(we));

            CircuitPersistencyVerificationCommand persistencyVerificationCommand = new CircuitPersistencyVerificationCommand();
            Assert.assertTrue(persistencyVerificationCommand.execute(we));

            CircuitStrictImplementationVerificationCommand strictImpVerificationCommand = new CircuitStrictImplementationVerificationCommand();
            Assert.assertTrue(strictImpVerificationCommand.execute(we));

            framework.closeWork(we);
        }
    }

    @Test
    public void testMappedCircuitVerificationCommands() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testCircuitWork: TEST_TM_CIRCUIT_WORKS) {
            URL url = classLoader.getResource(testCircuitWork);
            WorkspaceEntry we = framework.loadWork(url.getFile());

            CircuitDeadlockVerificationCommand deadlockVerificationCommand = new CircuitDeadlockVerificationCommand();
            Assert.assertTrue(deadlockVerificationCommand.execute(we));

            CircuitConformationVerificationCommand conformationVerificationCommand = new CircuitConformationVerificationCommand();
            Assert.assertTrue(conformationVerificationCommand.execute(we));

            CircuitPersistencyVerificationCommand persistencyVerificationCommand = new CircuitPersistencyVerificationCommand();
            Assert.assertTrue(persistencyVerificationCommand.execute(we));

            framework.closeWork(we);
        }
    }

}
