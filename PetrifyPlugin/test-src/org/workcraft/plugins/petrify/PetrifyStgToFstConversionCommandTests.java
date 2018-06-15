package org.workcraft.plugins.petrify;

import java.awt.Color;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.petrify.commands.PetrifyStgToBinaryFstConversionCommand;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.util.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetrifyStgToFstConversionCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        switch (DesktopApi.getOs()) {
        case LINUX:
            PetrifySettings.setCommand("dist-template/linux/tools/PetrifyTools/petrify");
            break;
        case MACOS:
            PetrifySettings.setCommand("dist-template/osx/Contents/Resources/tools/PetrifyTools/petrify");
            break;
        case WINDOWS:
            PetrifySettings.setCommand("dist-template\\windows\\tools\\PetrifyTools\\petrify.exe");
            break;
        default:
        }
    }

    @Test
    public void vmeStgToFstConversion() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testStgToFstConversionCommand(workName, new String[] {"011101_csc", "011001_csc", "101001_csc"});
    }

    private void testStgToFstConversionCommand(String workName, String[] conflictStateSuffixes)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry srcWe = framework.loadWork(url.getFile());

        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalNames(Signal.Type.INPUT, null);
        Set<String> srcInternals = srcStg.getSignalNames(Signal.Type.INTERNAL, null);
        Set<String> srcOutputs = srcStg.getSignalNames(Signal.Type.OUTPUT, null);

        PetrifyStgToBinaryFstConversionCommand command = new PetrifyStgToBinaryFstConversionCommand();
        WorkspaceEntry dstWe = command.execute(srcWe);

        Fst dstFst = WorkspaceUtils.getAs(dstWe, Fst.class);
        Set<String> dstInputs = new HashSet<>();
        Set<String> dstInternals = new HashSet<>();
        Set<String> dstOutputs = new HashSet<>();
        for (org.workcraft.plugins.fst.Signal signal: dstFst.getSignals()) {
            String signalName = dstFst.getNodeReference(signal);
            switch (signal.getType()) {
            case INPUT:
                dstInputs.add(signalName);
                break;
            case INTERNAL:
                dstInternals.add(signalName);
                break;
            case OUTPUT:
                dstOutputs.add(signalName);
                break;
            default:
                break;
            }
        }

        Assert.assertEquals(srcInputs, dstInputs);
        Assert.assertEquals(srcInternals, dstInternals);
        Assert.assertEquals(srcOutputs, dstOutputs);

        // Check the color of conflicting states
        HashMap<String, Color> conflictColorMap = new HashMap<>();
        VisualFst fst = WorkspaceUtils.getAs(dstWe, VisualFst.class);
        for (String conflictStateSuffix: conflictStateSuffixes) {
            for (VisualState state: fst.getVisualStates()) {
                String stateName = fst.getNodeMathReference(state);
                if (!stateName.endsWith(conflictStateSuffix)) continue;
                Color conflictColor = conflictColorMap.get(conflictStateSuffix);
                if (conflictColor != null) {
                    Assert.assertEquals(conflictColor, state.getFillColor());
                } else {
                    conflictColor = state.getFillColor();
                    Assert.assertNotEquals(Color.WHITE, conflictColor);
                    conflictColorMap.put(conflictStateSuffix, conflictColor);
                }
            }
        }
    }

}
