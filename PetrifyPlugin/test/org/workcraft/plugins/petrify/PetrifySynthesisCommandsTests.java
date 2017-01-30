package org.workcraft.plugins.petrify;

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
import org.workcraft.plugins.petrify.commands.PetrifyAbstractSynthesisCommand;
import org.workcraft.plugins.petrify.commands.PetrifyComplexGateSynthesisCommand;
import org.workcraft.plugins.petrify.commands.PetrifyTechnologyMappingSynthesisCommand;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetrifySynthesisCommandsTests {

    @BeforeClass
    public static void initPlugins() {
        final Framework framework = Framework.getInstance();
        framework.initPlugins(false);
        switch (DesktopApi.getOs()) {
        case LINUX:
            PetrifyUtilitySettings.setCommand("../dist-template/linux/tools/PetrifyTools/petrify");
            CircuitSettings.setGateLibrary("../dist-template/linux/libraries/workcraft.lib");
            break;
        case MACOS:
            PetrifyUtilitySettings.setCommand("../dist-template/osx/Contents/Resources/tools/PetrifyTools/petrify");
            CircuitSettings.setGateLibrary("../dist-template/osx/Contents/Resources/libraries/workcraft.lib");
            break;
        case WINDOWS:
            PetrifyUtilitySettings.setCommand("..\\dist-template\\windows\\tools\\PetrifyTools\\petrify.exe");
            CircuitSettings.setGateLibrary("..\\dist-template\\windows\\libraries\\workcraft.lib");
            break;
        default:
        }
    }

    @Test
    public void bufferComplexGateSynthesis() {
        testComplexGateSynthesisCommand("org/workcraft/plugins/petrify/buffer-compact.stg.work", 1);
    }

    @Test
    public void celementComplexGateSynthesis() {
        testComplexGateSynthesisCommand("org/workcraft/plugins/petrify/celement-compact.stg.work", 1);
    }

    @Test
    public void constComplexGateSynthesis() {
        testComplexGateSynthesisCommand("org/workcraft/plugins/petrify/const.stg.work", 3);
    }

    @Test
    public void bufferTechnologyMappingSynthesis() {
        testTechnologyMappingSynthesisCommad("org/workcraft/plugins/petrify/buffer-compact.stg.work", 1);
    }

    @Test
    public void celementTechnologyMappingSynthesis() {
        testTechnologyMappingSynthesisCommad("org/workcraft/plugins/petrify/celement-compact.stg.work", 1);
    }

    @Test
    public void constTechnologyMappingSynthesis() {
        testTechnologyMappingSynthesisCommad("org/workcraft/plugins/petrify/const.stg.work", 3);
    }

    private void testComplexGateSynthesisCommand(String testStgWork, int expectedGateCount) {
        try {
            testSynthesisCommand(PetrifyComplexGateSynthesisCommand.class, testStgWork, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void testTechnologyMappingSynthesisCommad(String testStgWork, int expectedGateCount) {
        try {
            testSynthesisCommand(PetrifyTechnologyMappingSynthesisCommand.class, testStgWork, expectedGateCount);
        } catch (DeserialisationException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private <C extends PetrifyAbstractSynthesisCommand> void testSynthesisCommand(Class<C> cls, String testStgWork, int expectedGateCount)
            throws DeserialisationException, InstantiationException, IllegalAccessException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(testStgWork);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalNames(Type.INPUT, null);
        Set<String> srcOutputs = srcStg.getSignalNames(Type.OUTPUT, null);

        C command = cls.newInstance();
        WorkspaceEntry dstWe = command.execute(srcWe);
        Circuit dstCircuit = WorkspaceUtils.getAs(dstWe, Circuit.class);
        Set<String> dstInputs = new HashSet<>();
        Set<String> dstOutputs = new HashSet<>();
        for (Contact port: dstCircuit.getPorts()) {
            if (port.isInput()) {
                dstInputs.add(port.getName());
            } else {
                dstOutputs.add(port.getName());
            }
        }

        int dstGateCount = dstCircuit.getFunctionComponents().size();

        Assert.assertEquals(srcInputs, dstInputs);
        Assert.assertEquals(srcOutputs, dstOutputs);
        Assert.assertEquals(expectedGateCount, dstGateCount);
    }

}
