package org.workcraft.plugins.mpsat;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.mpsat.commands.*;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.util.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatSynthesisCommandsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        switch (DesktopApi.getOs()) {
        case LINUX:
            PunfSettings.setCommand("dist-template/linux/tools/UnfoldingTools/punf");
            MpsatSynthesisSettings.setCommand("dist-template/linux/tools/UnfoldingTools/mpsat");
            CircuitSettings.setGateLibrary("dist-template/linux/libraries/workcraft.lib");
            break;
        case MACOS:
            PunfSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/punf");
            MpsatSynthesisSettings.setCommand("dist-template/osx/Contents/Resources/tools/UnfoldingTools/mpsat");
            CircuitSettings.setGateLibrary("dist-template/osx/Contents/Resources/libraries/workcraft.lib");
            break;
        case WINDOWS:
            PunfSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\punf.exe");
            MpsatSynthesisSettings.setCommand("dist-template\\windows\\tools\\UnfoldingTools\\mpsat.exe");
            CircuitSettings.setGateLibrary("dist-template\\windows\\libraries\\workcraft.lib");
            break;
        default:
        }
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
    public void arbitrationComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3.stg.work");
        testComplexGateSynthesisCommand(workName, 6);
    }

    @Test
    public void edcComplexGateSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testComplexGateSynthesisCommand(workName, 7);
    }

    private void testComplexGateSynthesisCommand(String workName, int expectedGateCount) {
        try {
            testSynthesisCommand(MpsatComplexGateSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void edcGeneralisedCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testGeneralisedCelementSynthesisCommand(workName, 7);
    }

    private void testGeneralisedCelementSynthesisCommand(String workName, int expectedGateCount) {
        try {
            testSynthesisCommand(MpsatGeneralisedCelementSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void edcStandardCelementSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testStandardCelementSynthesisCommand(workName, 7);
    }

    private void testStandardCelementSynthesisCommand(String workName, int expectedGateCount) {
        try {
            testSynthesisCommand(MpsatStandardCelementSynthesisCommand.class, workName, expectedGateCount);
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
    public void arbitrationTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 6);
    }

    @Test
    public void edcTechnologyMappingSynthesis() {
        String workName = PackageUtils.getPackagePath(getClass(), "edc-csc.stg.work");
        testTechnologyMappingSynthesisCommand(workName, 7);
    }

    private void testTechnologyMappingSynthesisCommand(String workName, int expectedGateCount) {
        try {
            testSynthesisCommand(MpsatTechnologyMappingSynthesisCommand.class, workName, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private <C extends MpsatAbstractSynthesisCommand> void testSynthesisCommand(Class<C> cls, String workName, int expectedGateCount)
            throws DeserialisationException, InstantiationException, IllegalAccessException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalNames(Signal.Type.INPUT, null);
        Set<String> srcOutputs = srcStg.getSignalNames(Signal.Type.OUTPUT, null);
        Set<String> srcMutexes = getMutexNames(srcStg);

        C command = cls.newInstance();
        WorkspaceEntry dstWe = command.execute(srcWe);
        Circuit dstCircuit = WorkspaceUtils.getAs(dstWe, Circuit.class);
        Set<String> dstInputs = new HashSet<>();
        Set<String> dstOutputs = new HashSet<>();
        for (Contact port: dstCircuit.getPorts()) {
            if (port.isInput()) {
                dstInputs.add(port.getName());
            }
            if (port.isOutput()) {
                dstOutputs.add(port.getName());
            }
        }
        Set<String> dstMutexes = getMutexNames(dstCircuit);
        int dstGateCount = dstCircuit.getFunctionComponents().size();

        Assert.assertEquals(srcInputs, dstInputs);
        Assert.assertEquals(srcOutputs, dstOutputs);
        Assert.assertEquals(srcMutexes, dstMutexes);
        Assert.assertEquals(expectedGateCount, dstGateCount);
    }

    private Set<String> getMutexNames(Stg stg) {
        HashSet<String> result = new HashSet<>();
        for (StgPlace place: stg.getMutexPlaces()) {
            result.add(stg.getNodeReference(place));
        }
        return result;
    }

    private Set<String> getMutexNames(Circuit circuit) {
        HashSet<String> result = new HashSet<>();
        Mutex mutex = CircuitSettings.parseMutexData();
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            if (mutex.name.equals(component.getModule())) {
                result.add(circuit.getNodeReference(component));
            }
        }
        return result;
    }

}
