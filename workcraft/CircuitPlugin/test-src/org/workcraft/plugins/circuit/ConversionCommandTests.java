package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.CircuitToStgConversionCommand;
import org.workcraft.plugins.circuit.commands.CircuitToStgWithEnvironmentConversionCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

class ConversionCommandTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
    }

    @Test
    void testBufferConversionCommands() throws IOException, DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.circuit.work");
        testConversionCommands(workName, false, 0);
    }

    @Test
    void testCelementConversionCommands() throws IOException, DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.circuit.work");
        testConversionCommands(workName, false, 0);
    }

    @Test
    void testVmeTmConversionCommands() throws IOException, DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        //testConversionCommands(workName, false, 0);
        testConversionCommands(workName, true, 17);
    }

    private void testConversionCommands(String workName, boolean composeEnvironment, int extraPlaceCount)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Circuit srcCircuit = WorkspaceUtils.getAs(srcWe, Circuit.class);

        Set<String> srcCircuitInputs = new HashSet<>();
        Set<String> srcCircuitOutputs = new HashSet<>();
        for (Contact port: srcCircuit.getPorts()) {
            if (port.isInput()) {
                srcCircuitInputs.add(port.getName());
            }
            if (port.isOutput()) {
                srcCircuitOutputs.add(port.getName());
            }
        }
        int srcCircuitSignalCount = srcCircuitInputs.size();
        for (FunctionComponent component: srcCircuit.getFunctionComponents()) {
            if (!component.getIsZeroDelay()) {
                srcCircuitSignalCount++;
            }
        }

        CircuitToStgConversionCommand command = composeEnvironment
                ? new CircuitToStgWithEnvironmentConversionCommand()
                : new CircuitToStgConversionCommand();

        WorkspaceEntry dstWe = command.execute(srcWe);

        Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);
        int dstStgPlaceCount = dstStg.getPlaces().size();
        Set<String> dstStgInputs = dstStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> dstStgOutputs = dstStg.getSignalReferences(Signal.Type.OUTPUT);

        Assertions.assertEquals(2 * srcCircuitSignalCount + extraPlaceCount, dstStgPlaceCount);
        Assertions.assertEquals(srcCircuitInputs, dstStgInputs);
        Assertions.assertEquals(srcCircuitOutputs, dstStgOutputs);

        framework.closeWork(srcWe);
        framework.closeWork(dstWe);
    }

}
