package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.circuit.interop.VerilogFormat;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class ImportHierarchyTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
        DebugCommonSettings.setShortExportHeader(true);
    }

    @Test
    void testImportExportHierBuckControl() throws DeserialisationException, SerialisationException, IOException {
        testImportExport("hier_buck_control.v");
    }

    private void testImportExport(String fileName) throws DeserialisationException, SerialisationException, IOException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        String vName = PackageUtils.getPackagePath(getClass(), fileName);
        File vFile = new File(classLoader.getResource(vName).getFile());

        File workingDirectory = framework.getWorkingDirectory();
        File tmpDirectory = FileUtils.createTempDirectory(FileUtils.getTempPrefix(fileName));

        framework.setLastDirectory(tmpDirectory);
        framework.setWorkingDirectory(tmpDirectory);
        WorkspaceEntry we = framework.loadWork(vFile);
        framework.setWorkingDirectory(workingDirectory);

        File vOutFile = new File(tmpDirectory, fileName);
        framework.exportWork(we, vOutFile, VerilogFormat.getInstance());

        String expectedVerilog = FileUtils.readAllText(vFile);
        String actualVerilog = FileUtils.readAllText(vOutFile);
        Assertions.assertEquals(getModuleData(expectedVerilog), getModuleData(actualVerilog));

        framework.closeWork(we);
        FileUtils.deleteOnExitRecursively(tmpDirectory);
    }

    private Map<String, Integer> getModuleData(String text) {
        Map<String, Integer> result = new HashMap<>();
        String moduleHeader = null;
        int lineCount = 0;
        for (String line : text.split("\n")) {
            lineCount++;
            if (line.startsWith("module ")) {
                moduleHeader = line;
                lineCount = 0;
            } else if (line.startsWith("endmodule")) {
                result.put(moduleHeader, lineCount);
            }
        }
        return result;
    }

}
