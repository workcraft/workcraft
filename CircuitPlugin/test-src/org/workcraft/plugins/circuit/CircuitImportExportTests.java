package org.workcraft.plugins.circuit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.circuit.interop.VerilogFormat;
import org.workcraft.util.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CircuitImportExportTests {

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
    public void testBufferCircuitImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.circuit.work");
        testCircuitImportExport(workName, null);
    }

    @Test
    public void testCelementCircuitImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.circuit.work");
        testCircuitImportExport(workName, null);
    }

    @Test
    public void testBufferTmCircuitImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-tm.circuit.work");
        testCircuitImportExport(workName, null);
    }

    @Test
    public void testCelementTmCircuitImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-tm.circuit.work");
        testCircuitImportExport(workName, null);
    }

    @Test
    public void testVmeTmCircuitImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testCircuitImportExport(workName, null);
    }

    @Test
    public void testBusCircuitImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "bus.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "bus.circuit.v");
        testCircuitImportExport(workName, verilogName);
    }

    @Test
    public void testMutexCircuitImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "mutex.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "mutex.circuit.v");
        testCircuitImportExport(workName, verilogName);
    }

    private void testCircuitImportExport(String workName, String verilogName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        URL wUrl = classLoader.getResource(workName);

        WorkspaceEntry wWe = framework.loadWork(wUrl.getFile());
        Set<String> wInputs = new HashSet<>();
        Set<String> wOutputs = new HashSet<>();
        Set<String> wGates = new HashSet<>();
        countCircuitNodes(wWe, wInputs, wOutputs, wGates);

        WorkspaceEntry vWe = null;
        Set<String> vInputs = new HashSet<>();
        Set<String> vOutputs = new HashSet<>();
        Set<String> vGates = new HashSet<>();
        try {
            File vFile = File.createTempFile("workcraft-", ".v");
            vFile.deleteOnExit();
            framework.exportModel(wWe.getModelEntry(), vFile, VerilogFormat.getInstance());
            vWe = framework.loadWork(vFile);
            countCircuitNodes(vWe, vInputs, vOutputs, vGates);
        } catch (IOException | SerialisationException e) {
        }

        Assert.assertEquals(wInputs, vInputs);
        Assert.assertEquals(wOutputs, vOutputs);
        Assert.assertEquals(wGates, vGates);

        if (verilogName != null) {
            URL sUrl = classLoader.getResource(verilogName);
            WorkspaceEntry sWe = framework.loadWork(sUrl.getFile());

            Set<String> sInputs = new HashSet<>();
            Set<String> sOutputs = new HashSet<>();
            Set<String> sGates = new HashSet<>();
            countCircuitNodes(sWe, sInputs, sOutputs, sGates);

            Assert.assertEquals(wInputs, sInputs);
            Assert.assertEquals(wOutputs, sOutputs);
            Assert.assertEquals(wGates, sGates);

            framework.closeWork(sWe);
        }

        framework.closeWork(wWe);
        framework.closeWork(vWe);
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
