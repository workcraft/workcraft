package org.workcraft.plugins.petrify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.petrify.commands.StgToBinaryFstConversionCommand;
import org.workcraft.plugins.petrify.commands.StgToFstConversionCommand;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class StgToFstConversionCommandTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PetrifySettings.setCommand(BackendUtils.getTemplateToolPath("PetrifyTools", "petrify"));
    }

    @Test
    void unsafeStgToFstConversion() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "unsafe.stg.work");
        testStgToFstConversionCommand(workName, 13, 19,  null);
    }

    @Test
    void vmeStgToBinaryFstConversion() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testStgToFstConversionCommand(workName, 24, 33,
                new String[] {"011101_csc", "011001_csc", "101001_csc"});
    }

    private void testStgToFstConversionCommand(String workName, int expectedStateCount, int expectedEventCount,
            String[] conflictStateSuffixes) throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry srcWe = framework.loadWork(url.getFile());

        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcInternals = srcStg.getSignalReferences(Signal.Type.INTERNAL);
        Set<String> srcOutputs = srcStg.getSignalReferences(Signal.Type.OUTPUT);

        StgToFstConversionCommand command = (conflictStateSuffixes == null)
                ? new StgToFstConversionCommand()
                : new StgToBinaryFstConversionCommand();

        WorkspaceEntry dstWe = command.execute(srcWe);

        Fst dstFst = WorkspaceUtils.getAs(dstWe, Fst.class);

        Assertions.assertEquals(expectedStateCount, dstFst.getStates().size());
        Assertions.assertEquals(expectedEventCount, dstFst.getEvents().size());

        Set<String> dstInputs = new HashSet<>();
        Set<String> dstInternals = new HashSet<>();
        Set<String> dstOutputs = new HashSet<>();
        for (org.workcraft.plugins.fst.Signal signal: dstFst.getSignals()) {
            String signalName = dstFst.getNodeReference(signal);
            switch (signal.getType()) {
                case INPUT -> dstInputs.add(signalName);
                case INTERNAL -> dstInternals.add(signalName);
                case OUTPUT -> dstOutputs.add(signalName);
                default -> { }
            }
        }

        Assertions.assertEquals(srcInputs, dstInputs);
        Assertions.assertEquals(srcInternals, dstInternals);
        Assertions.assertEquals(srcOutputs, dstOutputs);

        // Check the color of conflicting states (in case of binary-encoded FST)
        if (conflictStateSuffixes != null) {
            HashMap<String, Color> conflictColorMap = new HashMap<>();
            VisualFst fst = WorkspaceUtils.getAs(dstWe, VisualFst.class);
            for (VisualState state : fst.getVisualStates()) {
                String stateName = fst.getMathReference(state);
                Color stateColor = state.getFillColor();
                Color conflictColor = Color.WHITE;
                for (String conflictStateSuffix : conflictStateSuffixes) {
                    if (stateName.endsWith(conflictStateSuffix)) {
                        conflictColor = conflictColorMap.computeIfAbsent(conflictStateSuffix, s -> stateColor);
                        break;
                    }
                }
                Assertions.assertEquals(conflictColor, stateColor);
            }
        }
    }

}
