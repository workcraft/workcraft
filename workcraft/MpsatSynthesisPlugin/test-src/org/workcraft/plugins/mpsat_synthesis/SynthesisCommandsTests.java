package org.workcraft.plugins.mpsat_synthesis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.commands.AbstractSynthesisCommand;
import org.workcraft.dom.math.PageNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.utils.ArbitrationUtils;
import org.workcraft.plugins.mpsat_synthesis.commands.ComplexGateSynthesisCommand;
import org.workcraft.plugins.mpsat_synthesis.commands.GeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.mpsat_synthesis.commands.StandardCelementSynthesisCommand;
import org.workcraft.plugins.mpsat_synthesis.commands.TechnologyMappingSynthesisCommand;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

class SynthesisCommandsTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        MpsatSynthesisSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "mpsat"));
        MpsatSynthesisSettings.setOpenSynthesisStg(true);
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
    }

    @Test
    void irreducibleConflictComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "irreducible_conflict.stg.work");
        testComplexGateSynthesisCommand(workName, null);
    }

    @Test
    void bufferComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-compact.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    void celementComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-compact.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    void constComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "const.stg.work");
        testComplexGateSynthesisCommand(workName, 3);
    }

    @Test
    void busCtrlComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testComplexGateSynthesisCommand(workName, 2);
    }

    @Test
    void edcComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testComplexGateSynthesisCommand(workName, 7);
    }

    @Test
    void arbitrationComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testComplexGateSynthesisCommand(workName, 6);
    }

    @Test
    void duplicatorCscHierComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testComplexGateSynthesisCommand(workName, 4);
    }

    private void testComplexGateSynthesisCommand(String workName, Integer expectedGateCount) {
        try {
            checkSynthesisCommand(ComplexGateSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    void busCtrlGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 2);
    }

    @Test
    void edcGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 7);
    }

    @Test
    void arbitrationGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 6);
    }

    @Test
    void duplicatorCscHierGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 4);
    }

    private void testGeneralisedCelementSynthesisCommand(String workName, Integer expectedGateCount) {
        try {
            checkSynthesisCommand(GeneralisedCelementSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    void busCtrlStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testStandardCelementSynthesisCommand(workName, 4);
    }

    @Test
    void edcStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 7);
    }

    @Test
    void arbitrationStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testStandardCelementSynthesisCommand(workName, 7);
    }

    @Test
    void duplicatorCscHierStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 11);
    }

    private void testStandardCelementSynthesisCommand(String workName, Integer expectedGateCount) {
        try {
            checkSynthesisCommand(StandardCelementSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Test
    void irreducibleConflictTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "irreducible_conflict.stg.work");
        testTechnologyMappingSynthesisCommand(workName, null);
    }

    @Test
    void bufferTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-compact.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 1);
    }

    @Test
    void celementTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-compact.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 1);
    }

    @Test
    void constTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "const.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 5);
    }

    @Test
    void busCtrlTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 4);
    }

    @Test
    void edcTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 7);
    }

    @Test
    void arbitrationTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 6);
    }

    @Test
    void dlatchSplitPlaceTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 2);
    }

    @Test
    void dlatchHierSplitPlaceTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch-hier.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 2);
    }

    private void testTechnologyMappingSynthesisCommand(String workName, Integer expectedGateCount) {
        try {
            checkSynthesisCommand(TechnologyMappingSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private <C extends AbstractSynthesisCommand> void checkSynthesisCommand(Class<C> cls, String workName,
            Integer expectedComponentCount) throws DeserialisationException, InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcOutputs = srcStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> srcMutexes = org.workcraft.plugins.stg.utils.MutexUtils.getMutexPlaceReferences(srcStg);
        Set<String> srcPageRefs = new HashSet<>();
        for (PageNode page: Hierarchy.getChildrenOfType(srcStg.getRoot(), PageNode.class)) {
            boolean hasInputs = !srcStg.getSignalNames(Signal.Type.INPUT, page).isEmpty();
            boolean hasOutputs = !srcStg.getSignalNames(Signal.Type.OUTPUT, page).isEmpty();
            if (hasInputs || hasOutputs) {
                String srcPageRef = srcStg.getNodeReference(page);
                srcPageRefs.add(srcPageRef);
            }
        }

        C command = cls.getDeclaredConstructor().newInstance();
        WorkspaceEntry dstWe = command.execute(srcWe);
        if (expectedComponentCount == null) {
            Assertions.assertNull(dstWe);
        } else {
            Circuit dstCircuit = WorkspaceUtils.getAs(dstWe, Circuit.class);
            Set<String> dstInputs = new HashSet<>();
            Set<String> dstOutputs = new HashSet<>();
            // Process primary ports
            for (Contact port : dstCircuit.getPorts()) {
                String dstSignal = dstCircuit.getNodeReference(port);
                if (port.isInput()) {
                    dstInputs.add(dstSignal);
                }
                if (port.isOutput()) {
                    dstOutputs.add(dstSignal);
                }
            }
            // Process environment pins
            Set<String> dstPageRefs = new HashSet<>();
            for (PageNode page : Hierarchy.getChildrenOfType(dstCircuit.getRoot(), PageNode.class)) {
                for (Contact port : dstCircuit.getPorts()) {
                    if (port.getParent() == page) {
                        dstPageRefs.add(dstCircuit.getNodeReference(page));
                        break;
                    }
                }
            }
            Set<String> dstMutexes = getMutexComponentReferences(dstCircuit);
            int dstComponentCount = dstCircuit.getFunctionComponents().size();

            Assertions.assertEquals(srcInputs, dstInputs);
            Assertions.assertEquals(srcOutputs, dstOutputs);
            Assertions.assertEquals(srcMutexes, dstMutexes);
            Assertions.assertEquals(expectedComponentCount, dstComponentCount);
            Assertions.assertEquals(srcPageRefs, dstPageRefs);
        }
    }

    private Set<String> getMutexComponentReferences(Circuit circuit) {
        HashSet<String> result = new HashSet<>();
        Set<String> mutexModuleNames = ArbitrationUtils.getMutexModuleNames();
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            String moduleName = component.getModule();
            if (mutexModuleNames.contains(moduleName)) {
                result.add(circuit.getComponentReference(component));
            }
        }
        return result;
    }

}
