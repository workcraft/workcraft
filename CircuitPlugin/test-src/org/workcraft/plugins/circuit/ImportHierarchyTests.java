package org.workcraft.plugins.circuit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.builtin.settings.CommonDebugSettings;
import org.workcraft.plugins.circuit.interop.VerilogFormat;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;

public class ImportHierarchyTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CircuitSettings.setGateLibrary(TestUtils.getLibraryPath("workcraft.lib"));
        CommonDebugSettings.setShortExportHeader(true);
    }

    @Test
    public void testImportExportHierBuckControl() throws DeserialisationException, SerialisationException, IOException {
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
        Assert.assertEquals(FileUtils.readAllText(vFile), FileUtils.readAllText(vOutFile));

        framework.closeWork(we);
        FileUtils.deleteOnExitRecursively(tmpDirectory);
    }


}
