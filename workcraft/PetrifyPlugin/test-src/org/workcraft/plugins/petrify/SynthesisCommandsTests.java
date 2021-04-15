package org.workcraft.plugins.petrify;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.commands.AbstractSynthesisCommand;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.petrify.commands.ComplexGateSynthesisCommand;
import org.workcraft.plugins.petrify.commands.GeneralisedCelementSynthesisCommand;
import org.workcraft.plugins.petrify.commands.StandardCelementSynthesisCommand;
import org.workcraft.plugins.petrify.commands.TechnologyMappingSynthesisCommand;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class SynthesisCommandsTests {

    @BeforeAll
    public static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PetrifySettings.setCommand(BackendUtils.getTemplateToolPath("PetrifyTools", "petrify"));
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
        String workName = PackageUtils.getPackagePath(getClass(), "edc.stg.work");
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
            checkSynthesisCommand(ComplexGateSynthesisCommand.class, workName, expectedGateCount);
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
        String workName = PackageUtils.getPackagePath(getClass(), "edc.stg.work");
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
            checkSynthesisCommand(GeneralisedCelementSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void busCtrlStandardCelementSynthesis() {
        // FIXME: Skip this test on Windows as petrify.exe produces significantly different circuit
        Assumptions.assumeFalse(DesktopApi.getOs().isWindows());
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testStandardCelementSynthesisCommand(workName, 4, 5);
    }

    @Test
    public void edcStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc.stg.work");
        testStandardCelementSynthesisCommand(workName, 7);
    }

    @Test
    public void arbitrationStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testStandardCelementSynthesisCommand(workName, 6);
    }

    @Test
    public void duplicatorCscHierStandardCelementSynthesis() {
        // FIXME: Skip this test on Windows as petrify.exe produces significantly different circuit
        Assumptions.assumeFalse(DesktopApi.getOs().isWindows());
        String workName = PackageUtils.getPackagePath(getClass(), "duplicator-hier-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 8, 9);
    }

    private void testStandardCelementSynthesisCommand(String workName, int expectedGateCount) {
        testStandardCelementSynthesisCommand(workName, expectedGateCount, expectedGateCount);
    }

    private void testStandardCelementSynthesisCommand(String workName, int minGateCount, int maxGateCount) {
        try {
            checkSynthesisCommand(StandardCelementSynthesisCommand.class, workName,
                    minGateCount, maxGateCount);
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
        testTechnologyMappingSynthesisCommand(workName, 3);
    }

    @Test
    public void busCtrlTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "bus_ctrl.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 4);
    }

    @Test
    public void edcTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc.stg.work");
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
        testTechnologyMappingSynthesisCommand(workName, 13);
    }

    private void testTechnologyMappingSynthesisCommand(String workName, int expectedGateCount) {
        try {
            checkSynthesisCommand(TechnologyMappingSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private <C extends AbstractSynthesisCommand> void checkSynthesisCommand(Class<C> cls, String workName,
            int expectedComponentCount)
            throws DeserialisationException, InstantiationException, IllegalAccessException {

        checkSynthesisCommand(cls, workName, expectedComponentCount, expectedComponentCount);
    }

    private <C extends AbstractSynthesisCommand> void checkSynthesisCommand(Class<C> cls, String workName,
            int minComponentCount, int maxComponentCount)
            throws DeserialisationException, InstantiationException, IllegalAccessException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcOutputs = srcStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> srcMutexes = MutexUtils.getMutexPlaceReferences(srcStg);
        Set<String> srcPageRefs = new HashSet<>();
        for (PageNode page: Hierarchy.getChildrenOfType(srcStg.getRoot(), PageNode.class)) {
            boolean hasInputs = !srcStg.getSignalNames(Signal.Type.INPUT, page).isEmpty();
            boolean hasOutputs = !srcStg.getSignalNames(Signal.Type.OUTPUT, page).isEmpty();
            if (hasInputs || hasOutputs) {
                String srcPageRef = srcStg.getNodeReference(page);
                srcPageRefs.add(srcPageRef);
            }
        }

        C command = cls.newInstance();
        WorkspaceEntry dstWe = command.execute(srcWe);
        Circuit dstCircuit = WorkspaceUtils.getAs(dstWe, Circuit.class);
        Set<String> dstInputs = new HashSet<>();
        Set<String> dstOutputs = new HashSet<>();
        // Process primary ports
        for (Contact port: dstCircuit.getPorts()) {
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
        for (PageNode page: Hierarchy.getChildrenOfType(dstCircuit.getRoot(), PageNode.class)) {
            for (Contact port: dstCircuit.getPorts()) {
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
        Assertions.assertTrue(minComponentCount <= dstComponentCount);
        Assertions.assertTrue(maxComponentCount >= dstComponentCount);
        Assertions.assertEquals(srcPageRefs, dstPageRefs);
    }

    private Set<String> getMutexComponentReferences(Circuit circuit) {
        HashSet<String> result = new HashSet<>();
        Mutex mutex = CircuitSettings.parseMutexData();
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            if (mutex.name.equals(component.getModule())) {
                String ref = circuit.getNodeReference(component);
                result.add(Identifier.truncateNamespaceSeparator(ref));
            }
        }
        return result;
    }

}
