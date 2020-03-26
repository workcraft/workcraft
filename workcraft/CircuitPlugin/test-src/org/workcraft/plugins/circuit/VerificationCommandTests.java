package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class VerificationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatVerificationSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void bufferVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true,   // strict implementation
                true  // binate implementation
        );
    }

    @Test
    public void withoutEnvironmentVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-without_environment.circuit.work");
        testVerificationCommands(workName,
                null,  // conformation
                true,  // deadlock freeness
                false, // output persistency
                null,   // strict implementation
                true  // binate implementation
        );
    }

    @Test
    public void celementVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true,   // strict implementation
                true  // binate implementation
        );
    }

    @Test
    public void mappedCelementDecomposedVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-decomposed-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                null,   // strict implementation
                true  // binate implementation
        );
    }

    @Test
    public void mappedVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                null,  // strict implementation
                false  // binate implementation
        );
    }

    @Test
    public void mappedAbcdBadVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "abcd-bad-tm.circuit.work");
        testVerificationCommands(workName,
                false,  // conformation
                true,  // deadlock freeness
                false,  // output persistency
                null,  // strict implementation
                true  // binate implementation
        );
    }

    @Test
    public void mappedDlatchVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true,  // strict implementation
                false  // binate implementation
        );
    }

    @Test
    public void mappedDlatchConsensusVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch-consensus-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true,  // strict implementation
                true  // binate implementation
        );
    }

    private void testVerificationCommands(String workName,
            Boolean conformation, Boolean deadlockFreeness, Boolean outputPersistency,
            Boolean strictImplementation, Boolean binateImplementation) throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());

        ConformationVerificationCommand conformationCommand = new ConformationVerificationCommand();
        Assert.assertEquals(conformation, conformationCommand.execute(we));

        DeadlockFreenessVerificationCommand deadlockFreenessCommand = new DeadlockFreenessVerificationCommand();
        Assert.assertEquals(deadlockFreeness, deadlockFreenessCommand.execute(we));

        OutputPersistencyVerificationCommand outputPersistencyCommand = new OutputPersistencyVerificationCommand();
        Assert.assertEquals(outputPersistency, outputPersistencyCommand.execute(we));

        StrictImplementationVerificationCommand strictImplementationCommand = new StrictImplementationVerificationCommand();
        Assert.assertEquals(strictImplementation, strictImplementationCommand.execute(we));

        BinateImplementationVerificationCommand binateImplementationCommand = new BinateImplementationVerificationCommand();
        Assert.assertEquals(binateImplementation, binateImplementationCommand.execute(we));

        framework.closeWork(we);
    }

}
