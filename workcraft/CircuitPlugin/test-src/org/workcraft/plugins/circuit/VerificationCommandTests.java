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
                true,  // output persistency
                true,  // deadlock freeness
                true,  // strict implementation
                true,  // binate implementation
                null   // refinement
        );
    }

    @Test
    void withoutEnvironmentVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-without_environment.circuit.work");
        testVerificationCommands(workName,
                null,  // conformation
                false, // output persistency
                true,  // deadlock freeness
                null,  // strict implementation
                true,  // binate implementation
                null   // refinement
        );
    }

    @Test
    void celementVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // output persistency
                true,  // deadlock freeness
                true,  // strict implementation
                true,  // binate implementation
                true   // refinement
        );
    }

    @Test
    void celementDeadlockVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-deadlock.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // output persistency
                false, // deadlock freeness
                true,  // strict implementation
                true,  // binate implementation
                true   // refinement
        );
    }

    @Test
    void mutexEarlyBufVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "mutex-early-buf.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // output persistency
                true,  // deadlock freeness
                null,  // strict implementation
                true,  // binate implementation
                false  // refinement
        );
    }

    @Test
    void mutexLateBufVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "mutex-late-buf.circuit.work");
        testVerificationCommands(workName,
                false,  // conformation
                true,  // output persistency
                true,  // deadlock freeness
                null,  // strict implementation
                true,  // binate implementation
                false  // refinement
        );
    }

    @Test
    void mappedCelementDecomposedVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-decomposed-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // output persistency
                true,  // deadlock freeness
                null,  // strict implementation
                true,  // binate implementation
                true   // refinement
        );
    }

    @Test
    void mappedVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // output persistency
                true,  // deadlock freeness
                null,  // strict implementation
                false, // binate implementation
                true   // refinement
        );
    }

    @Test
    void mappedAbcdBadVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "abcd-bad-tm.circuit.work");
        testVerificationCommands(workName,
                false,  // conformation
                false, // output persistency
                true,  // deadlock freeness
                null,  // strict implementation
                true,  // binate implementation
                false  // refinement
        );
    }

    @Test
    void mappedDlatchVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // output persistency
                true,  // deadlock freeness
                true,  // strict implementation
                false, // binate implementation
                true   // refinement
        );
    }

    @Test
    void mappedDlatchConsensusVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch-consensus-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // conformation
                true,  // output persistency
                true,  // deadlock freeness
                true,  // strict implementation
                true,  // binate implementation
                true   // refinement
        );
    }

    private void testVerificationCommands(String workName,
            Boolean conformation,
            Boolean outputPersistency,
            Boolean deadlockFreeness,
            Boolean strictImplementation,
            Boolean binateImplementation,
            Boolean refinement)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());

        Assertions.assertEquals(conformation,
                new ConformationVerificationCommand().execute(we));

        Assertions.assertEquals(outputPersistency,
                new OutputPersistencyVerificationCommand().execute(we));

        Assertions.assertEquals(deadlockFreeness,
                new DeadlockFreenessVerificationCommand().execute(we));

        Assertions.assertEquals(binateImplementation,
                new BinateImplementationVerificationCommand().execute(we));

        Assertions.assertEquals(strictImplementation,
                new StrictImplementationVerificationCommand().execute(we));

        Assertions.assertEquals(refinement,
                new RefinementVerificationCommand().execute(we));

        framework.closeWork(we);
    }

}
