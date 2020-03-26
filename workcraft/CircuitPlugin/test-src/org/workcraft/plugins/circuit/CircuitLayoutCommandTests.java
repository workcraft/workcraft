package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.CircuitLayoutCommand;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class CircuitLayoutCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
    }

    @Test
    public void testBufferTmImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-tm.circuit.work");
        testImport(workName);
    }

    @Test
    public void testCelementTmImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-tm.circuit.work");
        testImport(workName);
    }

    @Test
    public void testVmeTmImport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testImport(workName);
    }

    private void testImport(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        Set<String> srcInputs = new HashSet<>();
        Set<String> srcOutputs = new HashSet<>();
        Set<String> srcGates = new HashSet<>();
        countNodes(we, srcInputs, srcOutputs, srcGates);

        CircuitLayoutCommand command = new CircuitLayoutCommand();
        command.execute(we);

        Set<String> dstInputs = new HashSet<>();
        Set<String> dstOutputs = new HashSet<>();
        Set<String> dstGates = new HashSet<>();
        countNodes(we, dstInputs, dstOutputs, dstGates);

        Assert.assertEquals(srcInputs, dstInputs);
        Assert.assertEquals(srcOutputs, dstOutputs);
        Assert.assertEquals(srcGates, dstGates);

        framework.closeWork(we);
    }

    private void countNodes(WorkspaceEntry we, Set<String> inputs, Set<String> outputs, Set<String> gates) {
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
