package org.workcraft.plugins.pcomp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.plugins.pcomp.commands.ParallelCompositionCommand;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.types.Pair;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class ParallelCompositionCommandTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
    }

    @Test
    void testIncorrectComposition() {
        ParallelCompositionCommand command = new ParallelCompositionCommand();
        Assertions.assertNull(command.execute(null, command.deserialiseData("incorrect work file names")));
    }

    @Test
    void testCycleAndChargeComposition() {
        String cycleWorkName = PackageUtils.getPackagePath(getClass(), "cycle-mutex.stg.work");
        String chargeWorkName = PackageUtils.getPackagePath(getClass(), "charge.stg.work");

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL cycleUrl = classLoader.getResource(cycleWorkName);
        URL chargeUrl = classLoader.getResource(chargeWorkName);

        ParallelCompositionCommand command = new ParallelCompositionCommand();
        String data = cycleUrl.getFile() + " " + chargeUrl.getFile();
        Pair<Collection<WorkspaceEntry>, PcompParameters> dataOriginal = command.deserialiseData(data);
        Pair<Collection<WorkspaceEntry>, PcompParameters> dataModified = Pair.of(dataOriginal.getFirst(),
                new PcompParameters(PcompParameters.SharedSignalMode.INTERNAL, false, false));

        WorkspaceEntry we = command.execute(null, dataModified);

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Set<String> inputRefs = stg.getSignalNames(Signal.Type.INPUT, null);
        Assertions.assertEquals(new HashSet<>(Arrays.asList("uv_san", "oc_san", "zc_san", "gp_ack", "gn_ack")), inputRefs);
        Assertions.assertEquals(10, stg.getSignalTransitions(Signal.Type.INPUT).size());

        Set<String> outputRefs = stg.getSignalNames(Signal.Type.OUTPUT, null);
        Assertions.assertEquals(new HashSet<>(Arrays.asList("uv_ctrl", "oc_ctrl", "zc_ctrl", "gp", "gn")), outputRefs);
        Assertions.assertEquals(10, stg.getSignalTransitions(Signal.Type.OUTPUT).size());

        Set<String> internalRefs = stg.getSignalNames(Signal.Type.INTERNAL, null);
        Assertions.assertEquals(new HashSet<>(Arrays.asList("chrg_req", "chrg_ack", "me_r1", "me_r2")), internalRefs);
        Assertions.assertEquals(8, stg.getSignalTransitions(Signal.Type.INTERNAL).size());
    }

}
