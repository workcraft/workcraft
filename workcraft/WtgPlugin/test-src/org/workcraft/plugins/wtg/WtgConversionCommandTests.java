package org.workcraft.plugins.wtg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.wtg.commands.WtgToStgConversionCommand;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

class WtgConversionCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testDlatchConversionCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.wtg.work");
        testConversionCommands(workName);
    }

    @Test
    void testBuckConversionCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.wtg.work");
        testConversionCommands(workName);
    }

    @Test
    void testGuardsConversionCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "instruction_decoder.wtg.work");
        testConversionCommands(workName);
    }

    private void testConversionCommands(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry wtgWe = framework.loadWork(url.getFile());
        Wtg wtg = WorkspaceUtils.getAs(wtgWe, Wtg.class);

        Set<String> wtgInputs = new HashSet<>();
        Set<String> wtgInternal = new HashSet<>();
        Set<String> wtgOutputs = new HashSet<>();
        for (Signal signal : wtg.getSignals()) {
            String name = wtg.getName(signal);
            if (signal.getType() == Signal.Type.INPUT) {
                wtgInputs.add(name);
            }
            if (signal.getType() == Signal.Type.INTERNAL) {
                wtgInternal.add(name);
            }
            if (signal.getType() == Signal.Type.OUTPUT) {
                wtgOutputs.add(name);
            }
        }

        WtgToStgConversionCommand command = new WtgToStgConversionCommand();
        WorkspaceEntry stgWe = command.execute(wtgWe);

        Stg stg = WorkspaceUtils.getAs(stgWe, Stg.class);
        Set<String> stgInputs = stg.getSignalReferences(org.workcraft.plugins.stg.Signal.Type.INPUT);
        Set<String> stgInternal = stg.getSignalReferences(org.workcraft.plugins.stg.Signal.Type.INTERNAL);
        Set<String> stgOutputs = stg.getSignalReferences(org.workcraft.plugins.stg.Signal.Type.OUTPUT);

        Assertions.assertEquals(wtgInputs, stgInputs);
        Assertions.assertEquals(wtgInternal, stgInternal);
        Assertions.assertEquals(wtgOutputs, stgOutputs);

        framework.closeWork(wtgWe);
        framework.closeWork(stgWe);
    }

}
