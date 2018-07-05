package org.workcraft.plugins.circuit;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.circuit.commands.CircuitToStgConversionCommand;
import org.workcraft.plugins.circuit.commands.CircuitToStgWithEnvironmentConversionCommand;
import org.workcraft.plugins.pcomp.PcompSettings;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.util.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CircuitConversionCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        switch (DesktopApi.getOs()) {
        case LINUX:
            PcompSettings.setCommand("dist-template/linux/tools/UnfoldingTools/pcomp");
            break;
        case MACOS:
            PcompSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/pcomp");
            break;
        case WINDOWS:
            PcompSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\pcomp.exe");
            break;
        default:
        }
    }

    @Test
    public void testBufferCircuitConversionCommands() throws IOException, DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.circuit.work");
        testCircuitConversionCommands(workName, false, 0);
    }

    @Test
    public void testCelementCircuitConversionCommands() throws IOException, DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.circuit.work");
        testCircuitConversionCommands(workName, false, 0);
    }

    @Test
    public void testVmeCircuitConversionCommands() throws IOException, DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testCircuitConversionCommands(workName, false, 0);
        testCircuitConversionCommands(workName, true, 17);
    }

    private void testCircuitConversionCommands(String workName, boolean composeEnvironment, int extraPlaceCount)
            throws IOException, DeserialisationException {

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

        CircuitToStgConversionCommand command = new CircuitToStgConversionCommand();
        if (composeEnvironment) {
            command = new CircuitToStgWithEnvironmentConversionCommand();
        }
        WorkspaceEntry dstWe = command.execute(srcWe);

        Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);
        int dstStgPlaceCount = dstStg.getPlaces().size();
        Set<String> dstStgInputs = dstStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> dstStgOutputs = dstStg.getSignalReferences(Signal.Type.OUTPUT);

        Assert.assertEquals(2 * srcCircuitSignalCount + extraPlaceCount, dstStgPlaceCount);
        Assert.assertEquals(srcCircuitInputs, dstStgInputs);
        Assert.assertEquals(srcCircuitOutputs, dstStgOutputs);

        framework.closeWork(srcWe);
        framework.closeWork(dstWe);
    }

}
