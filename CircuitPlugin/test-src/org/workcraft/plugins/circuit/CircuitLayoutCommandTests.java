package org.workcraft.plugins.circuit;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.circuit.commands.CircuitLayoutCommand;
import org.workcraft.util.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CircuitLayoutCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        switch (DesktopApi.getOs()) {
        case LINUX:
            CircuitSettings.setGateLibrary("dist-template/linux/libraries/workcraft.lib");
            break;
        case MACOS:
            CircuitSettings.setGateLibrary("dist-template/osx/Contents/Resources/libraries/workcraft.lib");
            break;
        case WINDOWS:
            CircuitSettings.setGateLibrary("dist-template\\windows\\libraries\\workcraft.lib");
            break;
        default:
        }
    }

    @Test
    public void testBufferTmCircuitImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-tm.circuit.work");
        testCircuitImport(workName);
    }

    @Test
    public void testCelementTmCircuitImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-tm.circuit.work");
        testCircuitImport(workName);
    }

    @Test
    public void testVmeTmCircuitImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testCircuitImport(workName);
    }

    private void testCircuitImport(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        Set<String> srcInputs = new HashSet<>();
        Set<String> srcOutputs = new HashSet<>();
        Set<String> srcGates = new HashSet<>();
        countCircuitNodes(we, srcInputs, srcOutputs, srcGates);

        CircuitLayoutCommand command = new CircuitLayoutCommand();
        command.execute(we);

        Set<String> dstInputs = new HashSet<>();
        Set<String> dstOutputs = new HashSet<>();
        Set<String> dstGates = new HashSet<>();
        countCircuitNodes(we, dstInputs, dstOutputs, dstGates);

        Assert.assertEquals(srcInputs, dstInputs);
        Assert.assertEquals(srcOutputs, dstOutputs);
        Assert.assertEquals(srcGates, dstGates);

        framework.closeWork(we);
    }

    private void countCircuitNodes(WorkspaceEntry we, Set<String> inputs, Set<String> outputs, Set<String> gates) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        for (Contact port: circuit.getPorts()) {
            if (port.isInput()) {
                inputs.add(port.getName());
            }
            if (port.isOutput()) {
                outputs.add(port.getName());
            }
        }
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            gates.add(component.getModule() + " " + component.getName());
        }
    }

}
