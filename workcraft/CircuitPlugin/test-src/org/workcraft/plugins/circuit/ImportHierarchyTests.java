package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.plugins.circuit.interop.VerilogFormat;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

class ImportHierarchyTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
        EditorCommonSettings.setExportHeaderStyle(EditorCommonSettings.ExportHeaderStyle.BRIEF);
    }

    @Test
    void testImportExportHierBuckControl() throws DeserialisationException, SerialisationException, IOException {
        testImportExport("hier_buck_control.v", "CTRL",
                new HashSet<>(Arrays.asList("CHARGE.work", "CYCLE.work", "CYCLE_CTRL.work", "CHARGE_CTRL.work", "WAIT2.work")));
    }

    private void testImportExport(String fileName, String topModuleName, Set<String> expectedFileNames)
            throws DeserialisationException, SerialisationException, IOException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        String vName = PackageUtils.getPackagePath(getClass(), fileName);
        File vFile = new File(classLoader.getResource(vName).getFile());

        File workingDirectory = framework.getWorkingDirectory();
        File tmpDirectory = FileUtils.createTempDirectory(FileUtils.getTempPrefix(fileName));

        framework.setWorkingDirectory(tmpDirectory);
        WorkspaceEntry we = framework.importWork(vFile, topModuleName);
        framework.setWorkingDirectory(workingDirectory);
        Assertions.assertNotNull(we);
        Assertions.assertEquals(we.getTitle(), topModuleName);

        List<File> directoryFiles = FileUtils.getDirectoryFiles(tmpDirectory);
        Set<String> actualFileNames = directoryFiles.stream().map(File::getName).collect(Collectors.toSet());
        Assertions.assertEquals(expectedFileNames, actualFileNames);

        File vOutFile = new File(tmpDirectory, fileName);
        framework.exportWork(we, vOutFile, VerilogFormat.DEFAULT);

        String expectedVerilog = FileUtils.readAllText(vFile);
        String actualVerilog = FileUtils.readAllText(vOutFile);
        Assertions.assertEquals(getModuleData(expectedVerilog), getModuleData(actualVerilog));

        framework.closeWork(we);
        FileUtils.deleteOnExitRecursively(tmpDirectory);
    }

    private Map<String, Integer> getModuleData(String text) {
        Map<String, Integer> result = new LinkedHashMap<>();
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
