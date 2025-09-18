package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.circuit.commands.SquashComponentTransformationCommand;
import org.workcraft.plugins.circuit.interop.VerilogFormat;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

class ImportExportTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
        CircuitSettings.setDissolveSingletonBus(false);
        CircuitSettings.setAcceptInoutPort(true);
    }

    @Test
    void testBufferImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.circuit.work");
        testImportExport(workName, null);
    }

    @Test
    void testCelementImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.circuit.work");
        testImportExport(workName, null);
    }

    @Test
    void testBufferTmImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-tm.circuit.work");
        testImportExport(workName, null);
    }

    @Test
    void testCelementTmImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-tm.circuit.work");
        testImportExport(workName, null);
    }

    @Test
    void testVmeTmImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testImportExport(workName, null);
    }

    @Test
    void testBusDetailedImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "bus.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "bus-detailed.circuit.v");
        testImportExport(workName, verilogName);
    }

    @Test
    void testBusCompactImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "bus.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "bus-compact.circuit.v");
        testImportExport(workName, verilogName);
    }

    @Test
    void testMutexEarlyImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "mutex-early-buf.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "mutex-early-buf.circuit.v");
        testImportExport(workName, verilogName);
    }

    @Test
    void testMutexLateImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "mutex-late-buf.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "mutex-late-buf.circuit.v");
        testImportExport(workName, verilogName);
    }

    @Test
    void testSkippedConnectionsImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "skipped_connections.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "skipped_connections.circuit.v");
        testImportExport(workName, verilogName);
    }

    @Test
    void testBusConcatenationImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "bus-concatenation.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "bus-concatenation.circuit.v");
        testImportExport(workName, verilogName);
    }

    @Test
    void testBusHierImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "bus-hier.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "bus-hier.circuit.v");
        testImportExport(workName, verilogName);
    }

    @Test
    void testBusHierMixedImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "bus-hier-mixed.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "bus-hier-mixed.circuit.v");
        testImportExport(workName, verilogName);
    }

    @Test
    void testRedundantInoutImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "redundant_inout-pruned.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "redundant_inout.circuit.v");
        testImportExport(workName, verilogName);
    }

    private void testImportExport(String workName, String verilogName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        URL wUrl = classLoader.getResource(workName);

        WorkspaceEntry wWe = framework.loadWork(wUrl.getFile());
        Set<String> wInputs = new HashSet<>();
        Set<String> wOutputs = new HashSet<>();
        Set<String> wGates = new HashSet<>();
        collectNodes(wWe, wInputs, wOutputs, wGates);

        WorkspaceEntry vWe = null;
        Set<String> vInputs = new HashSet<>();
        Set<String> vOutputs = new HashSet<>();
        Set<String> vGates = new HashSet<>();
        try {
            File vFile = File.createTempFile("workcraft-", ".v");
            vFile.deleteOnExit();
            framework.exportWork(wWe, vFile, VerilogFormat.DEFAULT);
            vWe = framework.importWork(vFile);
            collectNodes(vWe, vInputs, vOutputs, vGates);
        } catch (IOException | SerialisationException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(wInputs, vInputs);
        Assertions.assertEquals(wOutputs, vOutputs);
        Assertions.assertEquals(wGates, vGates);

        if (verilogName != null) {
            URL sUrl = classLoader.getResource(verilogName);

            File workingDirectory = framework.getWorkingDirectory();
            File tmpDirectory = FileUtils.createTempDirectory(FileUtils.getTempPrefix(verilogName));
            framework.setWorkingDirectory(tmpDirectory);
            WorkspaceEntry sWe = framework.importWork(sUrl.getFile());
            framework.setWorkingDirectory(workingDirectory);

            // Squash hierarchical modules
            new SquashComponentTransformationCommand().run(sWe);
            Set<String> sInputs = new HashSet<>();
            Set<String> sOutputs = new HashSet<>();
            Set<String> sGates = new HashSet<>();
            collectNodes(sWe, sInputs, sOutputs, sGates);

            Assertions.assertEquals(wInputs, sInputs);
            Assertions.assertEquals(wOutputs, sOutputs);
            Assertions.assertEquals(wGates, sGates);

            framework.closeWork(sWe);
        }

        framework.closeWork(wWe);
        framework.closeWork(vWe);
    }

    private void collectNodes(WorkspaceEntry we, Set<String> inputs, Set<String> outputs, Set<String> gates) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        for (Contact port : circuit.getPorts()) {
            if (port.isInput()) {
                inputs.add(circuit.getNodeReference(port));
            }
            if (port.isOutput()) {
                outputs.add(circuit.getNodeReference(port));
            }
        }
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            String ref = circuit.getComponentReference(component);
            gates.add(component.getModule() + ' ' + NamespaceHelper.flattenReference(ref));
        }
    }

}
