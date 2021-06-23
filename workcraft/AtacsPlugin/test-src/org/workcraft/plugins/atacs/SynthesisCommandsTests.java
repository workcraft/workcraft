package org.workcraft.plugins.atacs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.commands.AbstractSynthesisCommand;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.atacs.commands.ComplexGateSynthesisCommand;
import org.workcraft.plugins.atacs.commands.GeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.atacs.commands.StandardCelementSynthesisCommand;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

class SynthesisCommandsTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        AtacsSettings.setCommand(BackendUtils.getTemplateToolPath("ATACS", "atacs"));
    }

    @Test
    void irreducibleConflictComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "irreducible_conflict.stg.work");
        testComplexGateSynthesisCommand(workName, null);
    }

    @Test
    void bufferComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    void celementComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    void busCtrlComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testComplexGateSynthesisCommand(workName, 2);
    }

    @Test
    void dlatchComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.stg.work");
        testComplexGateSynthesisCommand(workName, 1);
    }

    @Test
    void edcCscComplexGateSynthesis() {
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
                | NoSuchMethodException | InvocationTargetException e) {

            e.printStackTrace();
        }
    }

    @Test
    void bufferGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 1);
    }

    @Test
    void celementGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 1);
    }

    @Test
    void busCtrlGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 2);
    }

    @Test
    void dlatchGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 1);
    }

    @Test
    void edcCscGeneralisedCelementSynthesis() {
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
                | NoSuchMethodException | InvocationTargetException e) {

            e.printStackTrace();
        }
    }

    @Test
    void bufferStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer.stg.work");
        testStandardCelementSynthesisCommand(workName, 3);
    }

    @Test
    void celementStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testStandardCelementSynthesisCommand(workName, 3);
    }

    @Test
    void busCtrlStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testStandardCelementSynthesisCommand(workName, 8);
    }

    @Test
    void dlatchStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.stg.work");
        testStandardCelementSynthesisCommand(workName, 3);
    }

    @Test
    void edcCscStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 19);
    }

    @Test
    void arbitrationStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testStandardCelementSynthesisCommand(workName, 12);
    }

    @Test
    void duplicatorCscHierStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 18);
    }

    private void testStandardCelementSynthesisCommand(String workName, Integer expectedGateCount) {
        try {
            checkSynthesisCommand(StandardCelementSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {

            e.printStackTrace();
        }
    }

    private <C extends AbstractSynthesisCommand> void checkSynthesisCommand(Class<C> cls, String workName,
            Integer expectedComponentCount) throws DeserialisationException, InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {

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

    private static Set<String> getMutexComponentReferences(Circuit circuit) {
        HashSet<String> result = new HashSet<>();
        Set<String> mutexModuleNames = org.workcraft.plugins.circuit.utils.MutexUtils.getMutexModuleNames();
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            String moduleName = component.getModule();
            if (mutexModuleNames.contains(moduleName)) {
                String ref = circuit.getNodeReference(component);
                result.add(Identifier.truncateNamespaceSeparator(ref));
            }
        }
        return result;
    }

}
