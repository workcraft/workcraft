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

class SubstitutionRulesTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
        DebugCommonSettings.setShortExportHeader(true);
    }

    @Test
    void testVmeWorkcraftSubstitution() throws DeserialisationException, SerialisationException, IOException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File tmpDirectory = FileUtils.createTempDirectory(FileUtils.getTempPrefix("vme-tm"));

        String vWorkcraftName = PackageUtils.getPackagePath(getClass(), "vme-tm.workcraft.v");
        File vWorkcraftFile = new File(classLoader.getResource(vWorkcraftName).getFile());

        String vTsmcghpName = PackageUtils.getPackagePath(getClass(), "vme-tm.tsmc_ghp.v");
        File vTsmcghpFile = new File(classLoader.getResource(vTsmcghpName).getFile());

        String vTsmcbcdName = PackageUtils.getPackagePath(getClass(), "vme-tm.tsmc_bcd.v");
        File vTsmcbcdFile = new File(classLoader.getResource(vTsmcbcdName).getFile());

        CircuitSettings.setInvertImportSubstitutionRules(true);
        CircuitSettings.setInvertExportSubstitutionRules(false);

        // Import from Workcraft-mapped Verilog
        CircuitSettings.setImportSubstitutionLibrary("");
        WorkspaceEntry vWorkcraftWe = framework.loadWork(vWorkcraftFile);

        CircuitSettings.setExportSubstitutionLibrary("");
        File vWorkcraftWorkcraftFile = new File(tmpDirectory, "vme-tm.workcraft-workcraft.v");
        framework.exportWork(vWorkcraftWe, vWorkcraftWorkcraftFile, VerilogFormat.getInstance());
        Assertions.assertEquals(FileUtils.readAllText(vWorkcraftFile), FileUtils.readAllText(vWorkcraftWorkcraftFile));

        CircuitSettings.setExportSubstitutionLibrary(BackendUtils.getTemplateLibraryPath("workcraft-tsmc_ghp.cnv"));
        File vWorkcraftTsmcghpFile = new File(tmpDirectory, "vme-tm.workcraft-tsmc_ghp.v");
        framework.exportWork(vWorkcraftWe, vWorkcraftTsmcghpFile, VerilogFormat.getInstance());
        Assertions.assertEquals(FileUtils.readAllText(vTsmcghpFile), FileUtils.readAllText(vWorkcraftTsmcghpFile));

        CircuitSettings.setExportSubstitutionLibrary(BackendUtils.getTemplateLibraryPath("workcraft-tsmc_bcd.cnv"));
        File vWorkcraftTsmcbcdFile = new File(tmpDirectory, "vme-tm.workcraft-tsmc_bcd.v");
        framework.exportWork(vWorkcraftWe, vWorkcraftTsmcbcdFile, VerilogFormat.getInstance());
        Assertions.assertEquals(FileUtils.readAllText(vTsmcbcdFile), FileUtils.readAllText(vWorkcraftTsmcbcdFile));

        framework.closeWork(vWorkcraftWe);

        // Import from TSMC_GHP-mapped Verilog
        CircuitSettings.setImportSubstitutionLibrary(BackendUtils.getTemplateLibraryPath("workcraft-tsmc_ghp.cnv"));
        WorkspaceEntry vTsmcghpWe = framework.loadWork(vTsmcghpFile);

        CircuitSettings.setExportSubstitutionLibrary("");
        File vTsmcghpWorkcraftFile = new File(tmpDirectory, "vme-tm.tsmc_ghp-workcraft.v");
        framework.exportWork(vTsmcghpWe, vTsmcghpWorkcraftFile, VerilogFormat.getInstance());
        Assertions.assertEquals(FileUtils.readAllText(vWorkcraftFile), FileUtils.readAllText(vTsmcghpWorkcraftFile));

        CircuitSettings.setExportSubstitutionLibrary(BackendUtils.getTemplateLibraryPath("workcraft-tsmc_ghp.cnv"));
        File vTsmcghpTsmcghpFile = new File(tmpDirectory, "vme-tm.tsmc_ghp-tsmc_ghp.v");
        framework.exportWork(vTsmcghpWe, vTsmcghpTsmcghpFile, VerilogFormat.getInstance());
        Assertions.assertEquals(FileUtils.readAllText(vTsmcghpFile), FileUtils.readAllText(vTsmcghpTsmcghpFile));

        CircuitSettings.setExportSubstitutionLibrary(BackendUtils.getTemplateLibraryPath("workcraft-tsmc_bcd.cnv"));
        File vTsmcghpTsmcbcdFile = new File(tmpDirectory, "vme-tm.tsmc_ghp-tsmc_bcd.v");
        framework.exportWork(vTsmcghpWe, vTsmcghpTsmcbcdFile, VerilogFormat.getInstance());
        Assertions.assertEquals(FileUtils.readAllText(vTsmcbcdFile), FileUtils.readAllText(vTsmcghpTsmcbcdFile));

        framework.closeWork(vTsmcghpWe);

        // Import from TSMC_BCD-mapped Verilog
        CircuitSettings.setImportSubstitutionLibrary(BackendUtils.getTemplateLibraryPath("workcraft-tsmc_bcd.cnv"));
        WorkspaceEntry vTsmcbcdWe = framework.loadWork(vTsmcbcdFile);

        CircuitSettings.setExportSubstitutionLibrary("");
        File vTsmcbcdWorkcraftFile = new File(tmpDirectory, "vme-tm.tsmc_bcd-workcraft.v");
        framework.exportWork(vTsmcbcdWe, vTsmcbcdWorkcraftFile, VerilogFormat.getInstance());
        Assertions.assertEquals(FileUtils.readAllText(vWorkcraftFile), FileUtils.readAllText(vTsmcbcdWorkcraftFile));

        CircuitSettings.setExportSubstitutionLibrary(BackendUtils.getTemplateLibraryPath("workcraft-tsmc_ghp.cnv"));
        File vTsmcbcdTsmcghpFile = new File(tmpDirectory, "vme-tm.tsmc_bcd-tsmc_ghp.v");
        framework.exportWork(vTsmcbcdWe, vTsmcbcdTsmcghpFile, VerilogFormat.getInstance());
        Assertions.assertEquals(FileUtils.readAllText(vTsmcghpFile), FileUtils.readAllText(vTsmcbcdTsmcghpFile));

        CircuitSettings.setExportSubstitutionLibrary(BackendUtils.getTemplateLibraryPath("workcraft-tsmc_bcd.cnv"));
        File vTsmcbcdTsmcbcdFile = new File(tmpDirectory, "vme-tm.tsmc_bcd-tsmc_bcd.v");
        framework.exportWork(vTsmcbcdWe, vTsmcbcdTsmcbcdFile, VerilogFormat.getInstance());
        Assertions.assertEquals(FileUtils.readAllText(vTsmcbcdFile), FileUtils.readAllText(vTsmcbcdTsmcbcdFile));

        framework.closeWork(vTsmcbcdWe);
        FileUtils.deleteOnExitRecursively(tmpDirectory);
    }


}
