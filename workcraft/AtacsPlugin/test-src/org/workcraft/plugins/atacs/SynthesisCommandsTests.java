package org.workcraft.plugins.atacs;

import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.atacs.commands.ComplexGateSynthesisCommand;
import org.workcraft.plugins.atacs.commands.GeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.atacs.commands.StandardCelementSynthesisCommand;
import org.workcraft.plugins.circuit.TestUtils;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;

public class SynthesisCommandsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        AtacsSettings.setCommand(BackendUtils.getTemplateToolPath("ATACS", "atacs"));
    }

    @Test
    public void bufferComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    public void celementComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    public void busCtrlComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testComplexGateSynthesisCommand(workName, 2);
    }

    @Test
    public void dlatchComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    public void edcCscComplexGateSynthesis() {
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
    public void bufferGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 1);
    }

    @Test
    public void celementGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 1);
    }

    @Test
    public void busCtrlGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 2);
    }

    @Test
    public void dlatchGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 1);
    }

    @Test
    public void edcCscGeneralisedCelementSynthesis() {
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
    public void bufferStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.stg.work");
        testStandardCelementSynthesisCommand(workName, 3);
    }

    @Test
    public void celementStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testStandardCelementSynthesisCommand(workName, 3);
    }

    @Test
    public void busCtrlStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testStandardCelementSynthesisCommand(workName, 8);
    }

    @Test
    public void dlatchStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.stg.work");
        testStandardCelementSynthesisCommand(workName, 3);
    }

    @Test
    public void edcCscStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 19);
    }

    @Test
    public void arbitrationStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testStandardCelementSynthesisCommand(workName, 12);
    }

    @Test
    public void duplicatorCscHierStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 18);
    }

    private void testStandardCelementSynthesisCommand(String workName, int expectedGateCount) {
        try {
            TestUtils.checkSynthesisCommand(StandardCelementSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
