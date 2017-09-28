package org.workcraft.plugins.mpsat;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.mpsat.commands.MpsatCombinedVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatConformationVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatConsistencyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatCscVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatDeadlockVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatDiInterfaceVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatInputPropernessVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatMutexImplementabilityVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatNormalcyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatOutputPersistencyVerificationCommand;
import org.workcraft.plugins.mpsat.commands.MpsatUscVerificationCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatVerificationCommandTests {

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
    public void vmeVerification() throws DeserialisationException {
        testVerificationCommands("org/workcraft/plugins/mpsat/vme.stg.work",
                true,  // combined
                true,  // consistency
                true,  // deadlock
                true,  // input properness
                true,  // output persistency
                false, // CSC
                false, // USC
                true,  // DI interface
                false, // normalcy
                null,  // mutex implementability
                null, null // conformation
        );
    }

    @Test
    public void arbitrationVerification() throws DeserialisationException {
        testVerificationCommands("org/workcraft/plugins/mpsat/arbitration-3.stg.work",
                true,  // combined
                true,  // consistency
                true,  // deadlock
                true,  // input properness
                true,  // output persistency
                true,  // CSC
                true,  // USC
                false, // DI interface
                false, // normalcy
                true,  // mutex implementability
                null, null // conformation
        );
    }

    @Test
    public void badVerification() throws DeserialisationException {
        testVerificationCommands("org/workcraft/plugins/mpsat/bad.stg.work",
                false, // combined
                true,  // consistency
                false, // deadlock
                true,  // input properness
                false, // output persistency
                true,  // CSC
                false, // USC
                false, // DI interface
                false, // normalcy
                null,  // mutex implementability
                null, null // conformation
        );
    }

    @Test
    public void cycleVerification() throws DeserialisationException {
        testVerificationCommands("org/workcraft/plugins/mpsat/cycle.stg.work",
                true, // combined
                true,  // consistency
                true, // deadlock
                true,  // input properness
                true, // output persistency
                true,  // CSC
                true, // USC
                true, // DI interface
                true, // normalcy
                false,  // mutex implementability
                "org/workcraft/plugins/mpsat/charge.stg.work", true // conformation
        );
    }

    private void testVerificationCommands(String work, Boolean combined,
            Boolean consistency, Boolean deadlock,
            Boolean inputProperness, Boolean outputPersistency,
            Boolean csc, Boolean usc,
            Boolean diInterface, Boolean normalcy,
            Boolean mutexImplementability,
            String envToConform, Boolean conformation)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(work);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        MpsatCombinedVerificationCommand combinedCommand = new MpsatCombinedVerificationCommand();
        Assert.assertEquals(combined, combinedCommand.execute(we));

        MpsatConsistencyVerificationCommand consistencyCommand = new MpsatConsistencyVerificationCommand();
        Assert.assertEquals(consistency, consistencyCommand.execute(we));

        MpsatDeadlockVerificationCommand deadlockCommand = new MpsatDeadlockVerificationCommand();
        Assert.assertEquals(deadlock, deadlockCommand.execute(we));

        MpsatInputPropernessVerificationCommand inputPropernessCommand = new MpsatInputPropernessVerificationCommand();
        Assert.assertEquals(inputProperness, inputPropernessCommand.execute(we));

        MpsatOutputPersistencyVerificationCommand persistencyCommand = new MpsatOutputPersistencyVerificationCommand();
        Assert.assertEquals(outputPersistency, persistencyCommand.execute(we));

        MpsatCscVerificationCommand cscCommand = new MpsatCscVerificationCommand();
        Assert.assertEquals(csc, cscCommand.execute(we));

        MpsatUscVerificationCommand uscCommand = new MpsatUscVerificationCommand();
        Assert.assertEquals(usc, uscCommand.execute(we));

        MpsatDiInterfaceVerificationCommand diInterfaceCommand = new MpsatDiInterfaceVerificationCommand();
        Assert.assertEquals(diInterface, diInterfaceCommand.execute(we));

        MpsatNormalcyVerificationCommand normalcyCommand = new MpsatNormalcyVerificationCommand();
        Assert.assertEquals(normalcy, normalcyCommand.execute(we));

        MpsatMutexImplementabilityVerificationCommand mutexImplementabilityCommand = new MpsatMutexImplementabilityVerificationCommand();
        Assert.assertEquals(mutexImplementability, mutexImplementabilityCommand.execute(we));

        MpsatConformationVerificationCommand conformationCommand = new MpsatConformationVerificationCommand();
        if (envToConform != null) {
            URL envUrl = classLoader.getResource(envToConform);
            File envFile = new File(envUrl.getFile());
            conformationCommand.setEnvironment(envFile);
            Assert.assertEquals(conformation, conformationCommand.execute(we));
        }
    }

}
