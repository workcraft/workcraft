package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.circuit.interop.VerilogFormat;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ImportExportTests {

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
    }

    @Test
    public void testBufferImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.circuit.work");
        testImportExport(workName, null);
    }

    @Test
    public void testCelementImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.circuit.work");
        testImportExport(workName, null);
    }

    @Test
    public void testBufferTmImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-tm.circuit.work");
        testImportExport(workName, null);
    }

    @Test
    public void testCelementTmImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-tm.circuit.work");
        testImportExport(workName, null);
    }

    @Test
    public void testVmeTmImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testImportExport(workName, null);
    }

    @Test
    public void testBusImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "bus.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "bus.circuit.v");
        testImportExport(workName, verilogName);
    }

    @Test
    public void testMutexImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "mutex.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "mutex.circuit.v");
        testImportExport(workName, verilogName);
    }

    @Test
    public void testSkippedConnectionsImportExport() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "skipped_connections.circuit.work");
        String verilogName = PackageUtils.getPackagePath(getClass(), "skipped_connections.circuit.v");
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
            framework.exportWork(wWe, vFile, VerilogFormat.getInstance());
            vWe = framework.loadWork(vFile);
            collectNodes(vWe, vInputs, vOutputs, vGates);
        } catch (IOException | SerialisationException e) {
        }

        Assertions.assertEquals(wInputs, vInputs);
        Assertions.assertEquals(wOutputs, vOutputs);
        Assertions.assertEquals(wGates, vGates);

        if (verilogName != null) {
            URL sUrl = classLoader.getResource(verilogName);
            WorkspaceEntry sWe = framework.loadWork(sUrl.getFile());

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
        for (Contact port: circuit.getPorts()) {
            if (port.isInput()) {
                inputs.add(circuit.getNodeReference(port));
            }
            if (port.isOutput()) {
                outputs.add(circuit.getNodeReference(port));
            }
        }
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            String ref = circuit.getNodeReference(component);
            gates.add(component.getModule() + " " + Identifier.truncateNamespaceSeparator(ref));
        }
    }

}
