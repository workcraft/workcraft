package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.ConformationVerificationCommand;
import org.workcraft.plugins.circuit.commands.DeadlockFreenessVerificationCommand;
import org.workcraft.plugins.circuit.commands.OutputPersistencyVerificationCommand;
import org.workcraft.plugins.circuit.commands.StrictImplementationVerificationCommand;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class VerificationCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(TestUtils.getToolPath("UnfoldingTools", "pcomp"));
        PunfSettings.setCommand(TestUtils.getToolPath("UnfoldingTools", "punf"));
        MpsatVerificationSettings.setCommand(TestUtils.getToolPath("UnfoldingTools", "mpsat"));
    }

    @Test
    public void bufferVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.circuit.work");
        testVerificationCommands(workName,
                true,  // combined
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true   // strict implementation
        );
    }

    @Test
    public void withoutEnvironmentVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-without_environment.circuit.work");
        testVerificationCommands(workName,
                false, // combined
                null,  // conformation
                true,  // deadlock freeness
                false, // output persistency
                null   // strict implementation
        );
    }

    @Test
    public void celementVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.circuit.work");
        testVerificationCommands(workName,
                true,  // combined
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                true   // strict implementation
        );
    }

    @Test
    public void mappedCelementDecomposedVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-decomposed-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // combined
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                null   // strict implementation
        );
    }

    @Test
    public void mappedVmeVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testVerificationCommands(workName,
                true,  // combined
                true,  // conformation
                true,  // deadlock freeness
                true,  // output persistency
                null   // strict implementation
        );
    }

    @Test
    public void mappedAbcdBadVerification() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "abcd-bad-tm.circuit.work");
        testVerificationCommands(workName,
                false,  // combined
                false,  // conformation
                true,  // deadlock freeness
                false,  // output persistency
                null   // strict implementation
        );
    }

    private void testVerificationCommands(String workName, Boolean combined,
            Boolean conformation, Boolean deadlockFreeness, Boolean outputPersistency,
            Boolean strictImplementation) throws DeserialisationException {

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

        framework.closeWork(we);
    }

}
