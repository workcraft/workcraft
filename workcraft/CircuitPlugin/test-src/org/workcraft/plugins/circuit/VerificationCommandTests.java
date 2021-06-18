package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.*;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class VerificationCommandTests {

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
    void bufferVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true,   // strict implementation
                true,  // binate implementation
                null // refinement
        );
    }

    @Test
    void withoutEnvironmentVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-without_environment.circuit.work");
        testVerificationCommands(workName,
                null,  // conformation
                true,  // deadlock freeness
                false, // output persistency
                null,   // strict implementation
                true,  // binate implementation
                null // refinement
        );
    }

    @Test
    void celementVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true,   // strict implementation
                true,  // binate implementation
                true // refinement
        );
    }

    @Test
    void mutexBufVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "mutex-buf.circuit.work");
        testVerificationCommands(workName,
                false,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                null,   // strict implementation
                true,  // binate implementation
                false // refinement
        );
    }

    @Test
    void mappedCelementDecomposedVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-decomposed-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                null,   // strict implementation
                true,  // binate implementation
                true // refinement
        );
    }

    @Test
    void mappedVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                null,  // strict implementation
                false,  // binate implementation
                true // refinement
        );
    }

    @Test
    void mappedAbcdBadVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "abcd-bad-tm.circuit.work");
        testVerificationCommands(workName,
                false,  // conformation
                true,  // deadlock freeness
                false,  // output persistency
                null,  // strict implementation
                true,  // binate implementation
                false // refinement
        );
    }

    @Test
    void mappedDlatchVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true,  // strict implementation
                false,  // binate implementation
                true // refinement
        );
    }

    @Test
    void mappedDlatchConsensusVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch-consensus-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true,  // strict implementation
                true,  // binate implementation
                true // refinement
        );
    }

    private void testVerificationCommands(String workName,
            Boolean conformation, Boolean deadlockFreeness, Boolean outputPersistency,
            Boolean strictImplementation, Boolean binateImplementation, Boolean refinement) throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());

        ConformationVerificationCommand conformationCommand = new ConformationVerificationCommand();
        Assertions.assertEquals(conformation, conformationCommand.execute(we));

        DeadlockFreenessVerificationCommand deadlockFreenessCommand = new DeadlockFreenessVerificationCommand();
        Assertions.assertEquals(deadlockFreeness, deadlockFreenessCommand.execute(we));

        OutputPersistencyVerificationCommand outputPersistencyCommand = new OutputPersistencyVerificationCommand();
        Assertions.assertEquals(outputPersistency, outputPersistencyCommand.execute(we));

        BinateImplementationVerificationCommand binateImplementationCommand = new BinateImplementationVerificationCommand();
        Assertions.assertEquals(binateImplementation, binateImplementationCommand.execute(we));

        StrictImplementationVerificationCommand strictImplementationCommand = new StrictImplementationVerificationCommand();
        Assertions.assertEquals(strictImplementation, strictImplementationCommand.execute(we));

        RefinementVerificationCommand refinementCommand = new RefinementVerificationCommand();
        Assertions.assertEquals(refinement, refinementCommand.execute(we));

        framework.closeWork(we);
    }

}
