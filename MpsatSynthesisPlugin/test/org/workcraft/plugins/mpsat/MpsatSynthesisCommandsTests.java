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
import org.workcraft.plugins.mpsat.commands.MpsatAbstractSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.MpsatComplexGateSynthesisCommand;
import org.workcraft.plugins.mpsat.commands.MpsatTechnologyMappingSynthesisCommand;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatSynthesisCommandsTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        switch (DesktopApi.getOs()) {
        case LINUX:
            PunfSettings.setCommand("../dist-template/linux/tools/UnfoldingTools/punf");
            MpsatSynthesisSettings.setCommand("../dist-template/linux/tools/UnfoldingTools/mpsat");
            CircuitSettings.setGateLibrary("../dist-template/linux/libraries/workcraft.lib");
            break;
        case MACOS:
            PunfSettings.setCommand("../dist-template/osx/Contents/Resources/tools/UnfoldingTools/punf");
            MpsatSynthesisSettings.setCommand("../dist-template/osx/Contents/Resources/tools/UnfoldingTools/mpsat");
            CircuitSettings.setGateLibrary("../dist-template/osx/Contents/Resources/libraries/workcraft.lib");
            break;
        case WINDOWS:
            PunfSettings.setCommand("..\\dist-template\\windows\\tools\\UnfoldingTools\\punf.exe");
            MpsatSynthesisSettings.setCommand("..\\dist-template\\windows\\tools\\UnfoldingTools\\mpsat.exe");
            CircuitSettings.setGateLibrary("..\\dist-template\\windows\\libraries\\workcraft.lib");
            break;
        default:
        }
    }

    @Test
    public void bufferComplexGateSynthesis() {
        testComplexGateSynthesisCommand("org/workcraft/plugins/mpsat/buffer-compact.stg.work", 1);
    }

    @Test
    public void celementComplexGateSynthesis() {
        testComplexGateSynthesisCommand("org/workcraft/plugins/mpsat/celement-compact.stg.work", 1);
    }

    @Test
    public void constComplexGateSynthesis() {
        testComplexGateSynthesisCommand("org/workcraft/plugins/mpsat/const.stg.work", 3);
    }

    @Test
    public void arbitrationComplexGateSynthesis() {
        testComplexGateSynthesisCommand("org/workcraft/plugins/mpsat/arbitration-3.stg.work", 6);
    }

    //@Test
    public void edcComplexGateSynthesis() {
        testComplexGateSynthesisCommand("org/workcraft/plugins/mpsat/edc-csc.stg.work", 6);
    }

    @Test
    public void bufferTechnologyMappingSynthesis() {
        testTechnologyMappingSynthesisCommand("org/workcraft/plugins/mpsat/buffer-compact.stg.work", 1);
    }

    @Test
    public void celementTechnologyMappingSynthesis() {
        testTechnologyMappingSynthesisCommand("org/workcraft/plugins/mpsat/celement-compact.stg.work", 1);
    }

    @Test
    public void constTechnologyMappingSynthesis() {
        testTechnologyMappingSynthesisCommand("org/workcraft/plugins/mpsat/const.stg.work", 5);
    }

    @Test
    public void arbitrationTechnologyMappingSynthesis() {
        testTechnologyMappingSynthesisCommand("org/workcraft/plugins/mpsat/arbitration-3.stg.work", 6);
    }

    //@Test
    public void edcTechnologyMappingSynthesis() {
        testTechnologyMappingSynthesisCommand("org/workcraft/plugins/mpsat/edc-csc.stg.work", 10);
    }

    private void testComplexGateSynthesisCommand(String testStgWork, int expectedGateCount) {
        try {
            testSynthesisCommand(MpsatComplexGateSynthesisCommand.class, testStgWork, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void testTechnologyMappingSynthesisCommand(String testStgWork, int expectedGateCount) {
        try {
            testSynthesisCommand(MpsatTechnologyMappingSynthesisCommand.class, testStgWork, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private <C extends MpsatAbstractSynthesisCommand> void testSynthesisCommand(Class<C> cls, String testStgWork, int expectedGateCount)
            throws DeserialisationException, InstantiationException, IllegalAccessException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(testStgWork);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalNames(Type.INPUT, null);
        Set<String> srcOutputs = srcStg.getSignalNames(Type.OUTPUT, null);
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
