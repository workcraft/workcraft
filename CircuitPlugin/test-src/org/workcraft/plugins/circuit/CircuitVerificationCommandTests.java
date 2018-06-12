package org.workcraft.plugins.circuit;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.circuit.commands.CircuitConformationVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitDeadlockFreenessVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitOutputPersistencyVerificationCommand;
import org.workcraft.plugins.circuit.commands.CircuitStrictImplementationVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.workspace.WorkspaceEntry;

public class CircuitVerificationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        switch (DesktopApi.getOs()) {
        case LINUX:
            PcompSettings.setCommand("dist-template/linux/tools/UnfoldingTools/pcomp");
            PunfSettings.setCommand("dist-template/linux/tools/UnfoldingTools/punf");
            MpsatSettings.setCommand("dist-template/linux/tools/UnfoldingTools/mpsat");
            break;
        case MACOS:
            PcompSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/pcomp");
            PunfSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/punf");
            MpsatSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/mpsat");
            break;
        case WINDOWS:
            PcompSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\pcomp.exe");
            PunfSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\punf.exe");
            MpsatSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\mpsat.exe");
            break;
        default:
        }
    }

    @Test
    public void bufferVerification() throws DeserialisationException {
        testCircuitVerificationCommands("org/workcraft/plugins/circuit/buffer.circuit.work",
                true,  // combined
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true   // strict implementation
        );
    }

    @Test
    public void withoutEnvironmentVerification() throws DeserialisationException {
        testCircuitVerificationCommands("org/workcraft/plugins/circuit/buffer-without_environment.circuit.work",
                false, // combined
                null,  // conformation
                true,  // deadlock freeness
                false, // output persistency
                null   // strict implementation
        );
    }

    @Test
    public void celementVerification() throws DeserialisationException {
        testCircuitVerificationCommands("org/workcraft/plugins/circuit/celement.circuit.work",
                true,  // combined
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true   // strict implementation
        );
    }

    @Test
    public void mappedCelementVerification() throws DeserialisationException {
        testCircuitVerificationCommands("org/workcraft/plugins/circuit/celement-decomposed-tm.circuit.work",
                true,  // combined
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                null   // strict implementation
        );
    }

    @Test
    public void mappedVmeVerification() throws DeserialisationException {
        testCircuitVerificationCommands("org/workcraft/plugins/circuit/vme-tm.circuit.work",
                true,  // combined
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                null   // strict implementation
        );
    }

    @Test
    public void mappedAbcdVerification() throws DeserialisationException {
        testCircuitVerificationCommands("org/workcraft/plugins/circuit/abcd-tm.circuit.work",
                false,  // combined
                false,  // conformation
                true,  // deadlock freeness
                false,  // output persistency
                null   // strict implementation
        );
    }

    private void testCircuitVerificationCommands(String testStgWork, Boolean combined,
            Boolean conformation, Boolean deadlockFreeness, Boolean outputPersistency,
            Boolean strictImplementation) throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(testStgWork);

        WorkspaceEntry we = framework.loadWork(url.getFile());

        CircuitConformationVerificationCommand conformationCommand = new CircuitConformationVerificationCommand();
        Assert.assertEquals(conformation, conformationCommand.execute(we));

        CircuitDeadlockFreenessVerificationCommand deadlockFreenessCommand = new CircuitDeadlockFreenessVerificationCommand();
        Assert.assertEquals(deadlockFreeness, deadlockFreenessCommand.execute(we));

        CircuitOutputPersistencyVerificationCommand outputPersistencyCommand = new CircuitOutputPersistencyVerificationCommand();
        Assert.assertEquals(outputPersistency, outputPersistencyCommand.execute(we));

        CircuitStrictImplementationVerificationCommand strictImplementationCommand = new CircuitStrictImplementationVerificationCommand();
        Assert.assertEquals(strictImplementation, strictImplementationCommand.execute(we));

        framework.closeWork(we);
    }

}
