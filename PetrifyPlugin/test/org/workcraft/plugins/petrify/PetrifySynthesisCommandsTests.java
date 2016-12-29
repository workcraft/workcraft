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
import org.workcraft.plugins.petrify.commands.PetrifyTechnologyMappingSynthesisCommand;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetrifySynthesisCommandsTests {

    private static final String[] TOGGLE_STG_WORKS = {
        "org/workcraft/plugins/petrify/buffer-compact.stg.work",
        "org/workcraft/plugins/petrify/celement-compact.stg.work",
    };

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
    public void testPetrifyTechnologyMappingSynthesisCommand() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: TOGGLE_STG_WORKS) {
            URL srcUrl = classLoader.getResource(testStgWork);

            WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
            Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
            Set<String> srcInputs = srcStg.getSignalNames(Type.INPUT, null);
            Set<String> srcOutputs = srcStg.getSignalNames(Type.OUTPUT, null);

            PetrifyTechnologyMappingSynthesisCommand command = new PetrifyTechnologyMappingSynthesisCommand();
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
            Assert.assertEquals(dstGateCount, 1);
        }
    }

}
