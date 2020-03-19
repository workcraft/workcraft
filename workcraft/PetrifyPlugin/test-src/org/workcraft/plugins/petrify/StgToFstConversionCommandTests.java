package org.workcraft.plugins.petrify;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.TestUtils;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.petrify.commands.StgToBinaryFstConversionCommand;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StgToFstConversionCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PetrifySettings.setCommand(TestUtils.getToolPath("PetrifyTools", "petrify"));
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
        Set<String> srcInputs = srcStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcInternals = srcStg.getSignalReferences(Signal.Type.INTERNAL);
        Set<String> srcOutputs = srcStg.getSignalReferences(Signal.Type.OUTPUT);

        StgToBinaryFstConversionCommand command = new StgToBinaryFstConversionCommand();
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
                String stateName = fst.getMathReference(state);
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
