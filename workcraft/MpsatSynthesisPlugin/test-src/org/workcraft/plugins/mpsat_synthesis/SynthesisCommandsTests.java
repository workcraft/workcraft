package org.workcraft.plugins.mpsat_synthesis;

import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.TestUtils;
import org.workcraft.plugins.mpsat_synthesis.commands.ComplexGateSynthesisCommand;
import org.workcraft.plugins.mpsat_synthesis.commands.GeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.mpsat_synthesis.commands.StandardCelementSynthesisCommand;
import org.workcraft.plugins.mpsat_synthesis.commands.TechnologyMappingSynthesisCommand;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;

public class SynthesisCommandsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PunfSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "punf"));
        MpsatSynthesisSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
        MpsatSynthesisSettings.setOpenSynthesisStg(true);
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
    }

    @Test
    public void bufferComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-compact.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    public void celementComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-compact.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    public void constComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "const.stg.work");
        testComplexGateSynthesisCommand(workName, 3);
    }

    @Test
    public void busCtrlComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testComplexGateSynthesisCommand(workName, 2);
    }

    @Test
    public void edcComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testComplexGateSynthesisCommand(workName, 7);
    }

    @Test
    public void arbitrationComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testComplexGateSynthesisCommand(workName, 6);
    }

    @Test
    public void duplicatorCscHierComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testComplexGateSynthesisCommand(workName, 4);
    }

    private void testComplexGateSynthesisCommand(String workName, int expectedGateCount) {
        try {
            TestUtils.checkSynthesisCommand(ComplexGateSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void busCtrlGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 2);
    }

    @Test
    public void edcGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 7);
    }

    @Test
    public void arbitrationGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 6);
    }

    @Test
    public void duplicatorCscHierGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 4);
    }

    private void testGeneralisedCelementSynthesisCommand(String workName, int expectedGateCount) {
        try {
            TestUtils.checkSynthesisCommand(GeneralisedCelementSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void busCtrlStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testStandardCelementSynthesisCommand(workName, 4);
    }

    @Test
    public void edcStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 7);
    }

    @Test
    public void arbitrationStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testStandardCelementSynthesisCommand(workName, 7);
    }

    @Test
    public void duplicatorCscHierStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 11);
    }

    private void testStandardCelementSynthesisCommand(String workName, int expectedGateCount) {
        try {
            TestUtils.checkSynthesisCommand(StandardCelementSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void bufferTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-compact.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 1);
    }

    @Test
    public void celementTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-compact.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 1);
    }

    @Test
    public void constTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "const.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 5);
    }

    @Test
    public void busCtrlTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 4);
    }

    @Test
    public void edcTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 7);
    }

    @Test
    public void arbitrationTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 6);
    }

    @Test
    public void duplicatorCscHierTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 11);
    }

    @Test
    public void dlatchSplitPlaceTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 2);
    }

    @Test
    public void dlatchHierSplitPlaceTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch-hier.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 2);
    }

    private void testTechnologyMappingSynthesisCommand(String workName, int expectedGateCount) {
        try {
            TestUtils.checkSynthesisCommand(TechnologyMappingSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
