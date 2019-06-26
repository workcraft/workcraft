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

public class SubstitutionRulesTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CircuitSettings.setGateLibrary(TestUtils.getLibraryPath("workcraft.lib"));
        CommonDebugSettings.setShortExportHeader(true);
    }

    @Test
    public void testVmeWorkcraftSubstitution() throws DeserialisationException, SerialisationException, IOException {
        Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File dir = FileUtils.createTempDirectory();

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
        File vWorkcraftWorkcraftFile = new File(dir, "vme-tm.workcraft-workcraft.v");
        framework.exportWork(vWorkcraftWe, vWorkcraftWorkcraftFile, VerilogFormat.getInstance());
        Assert.assertEquals(FileUtils.readAllText(vWorkcraftFile), FileUtils.readAllText(vWorkcraftWorkcraftFile));

        CircuitSettings.setExportSubstitutionLibrary(TestUtils.getLibraryPath("workcraft-tsmc_ghp.cnv"));
        File vWorkcraftTsmcghpFile = new File(dir, "vme-tm.workcraft-tsmc_ghp.v");
        framework.exportWork(vWorkcraftWe, vWorkcraftTsmcghpFile, VerilogFormat.getInstance());
        Assert.assertEquals(FileUtils.readAllText(vTsmcghpFile), FileUtils.readAllText(vWorkcraftTsmcghpFile));

        CircuitSettings.setExportSubstitutionLibrary(TestUtils.getLibraryPath("workcraft-tsmc_bcd.cnv"));
        File vWorkcraftTsmcbcdFile = new File(dir, "vme-tm.workcraft-tsmc_bcd.v");
        framework.exportWork(vWorkcraftWe, vWorkcraftTsmcbcdFile, VerilogFormat.getInstance());
        Assert.assertEquals(FileUtils.readAllText(vTsmcbcdFile), FileUtils.readAllText(vWorkcraftTsmcbcdFile));

        framework.closeWork(vWorkcraftWe);

        // Import from TSMC_GHP-mapped Verilog
        CircuitSettings.setImportSubstitutionLibrary(TestUtils.getLibraryPath("workcraft-tsmc_ghp.cnv"));
        WorkspaceEntry vTsmcghpWe = framework.loadWork(vTsmcghpFile);

        CircuitSettings.setExportSubstitutionLibrary("");
        File vTsmcghpWorkcraftFile = new File(dir, "vme-tm.tsmc_ghp-workcraft.v");
        framework.exportWork(vTsmcghpWe, vTsmcghpWorkcraftFile, VerilogFormat.getInstance());
        Assert.assertEquals(FileUtils.readAllText(vWorkcraftFile), FileUtils.readAllText(vTsmcghpWorkcraftFile));

        CircuitSettings.setExportSubstitutionLibrary(TestUtils.getLibraryPath("workcraft-tsmc_ghp.cnv"));
        File vTsmcghpTsmcghpFile = new File(dir, "vme-tm.tsmc_ghp-tsmc_ghp.v");
        framework.exportWork(vTsmcghpWe, vTsmcghpTsmcghpFile, VerilogFormat.getInstance());
        Assert.assertEquals(FileUtils.readAllText(vTsmcghpFile), FileUtils.readAllText(vTsmcghpTsmcghpFile));

        CircuitSettings.setExportSubstitutionLibrary(TestUtils.getLibraryPath("workcraft-tsmc_bcd.cnv"));
        File vTsmcghpTsmcbcdFile = new File(dir, "vme-tm.tsmc_ghp-tsmc_bcd.v");
        framework.exportWork(vTsmcghpWe, vTsmcghpTsmcbcdFile, VerilogFormat.getInstance());
        Assert.assertEquals(FileUtils.readAllText(vTsmcbcdFile), FileUtils.readAllText(vTsmcghpTsmcbcdFile));

        framework.closeWork(vTsmcghpWe);

        // Import from TSMC_BCD-mapped Verilog
        CircuitSettings.setImportSubstitutionLibrary(TestUtils.getLibraryPath("workcraft-tsmc_bcd.cnv"));
        WorkspaceEntry vTsmcbcdWe = framework.loadWork(vTsmcbcdFile);

        CircuitSettings.setExportSubstitutionLibrary("");
        File vTsmcbcdWorkcraftFile = new File(dir, "vme-tm.tsmc_bcd-workcraft.v");
        framework.exportWork(vTsmcbcdWe, vTsmcbcdWorkcraftFile, VerilogFormat.getInstance());
        Assert.assertEquals(FileUtils.readAllText(vWorkcraftFile), FileUtils.readAllText(vTsmcbcdWorkcraftFile));

        CircuitSettings.setExportSubstitutionLibrary(TestUtils.getLibraryPath("workcraft-tsmc_ghp.cnv"));
        File vTsmcbcdTsmcghpFile = new File(dir, "vme-tm.tsmc_bcd-tsmc_ghp.v");
        framework.exportWork(vTsmcbcdWe, vTsmcbcdTsmcghpFile, VerilogFormat.getInstance());
        Assert.assertEquals(FileUtils.readAllText(vTsmcghpFile), FileUtils.readAllText(vTsmcbcdTsmcghpFile));

        CircuitSettings.setExportSubstitutionLibrary(TestUtils.getLibraryPath("workcraft-tsmc_bcd.cnv"));
        File vTsmcbcdTsmcbcdFile = new File(dir, "vme-tm.tsmc_bcd-tsmc_bcd.v");
        framework.exportWork(vTsmcbcdWe, vTsmcbcdTsmcbcdFile, VerilogFormat.getInstance());
        Assert.assertEquals(FileUtils.readAllText(vTsmcbcdFile), FileUtils.readAllText(vTsmcbcdTsmcbcdFile));

        framework.closeWork(vTsmcbcdWe);
    }


}
