package org.workcraft.plugins.pcomp;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.plugins.pcomp.commands.ParallelCompositionCommand;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.types.Pair;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ParallelCompositionCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        PcompSettings.setCommand(BackendUtils.getTemplateToolPath("UnfoldingTools", "pcomp"));
    }

    @Test
    public void testIncorrectComposition() {
        ParallelCompositionCommand command = new ParallelCompositionCommand();
        Assert.assertNull(command.execute(null, command.deserialiseData("incorrect work file names")));
    }

    @Test
    public void testCycleAndChargeComposition() {
        String cycleWorkName = PackageUtils.getPackagePath(getClass(), "cycle.stg.work");
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
        Assert.assertEquals(new HashSet<>(Arrays.asList("uv_san", "oc_san", "zc_san", "gp_ack", "gn_ack")), inputRefs);
        Assert.assertEquals(10, stg.getSignalTransitions(Signal.Type.INPUT).size());

        Set<String> outputRefs = stg.getSignalNames(Signal.Type.OUTPUT, null);
        Assert.assertEquals(new HashSet<>(Arrays.asList("uv_ctrl", "oc_ctrl", "zc_ctrl", "gp", "gn")), outputRefs);
        Assert.assertEquals(10, stg.getSignalTransitions(Signal.Type.OUTPUT).size());

        Set<String> internalRefs = stg.getSignalNames(Signal.Type.INTERNAL, null);
        Assert.assertEquals(new HashSet<>(Arrays.asList("chrg_req", "chrg_ack")), internalRefs);
        Assert.assertEquals(4, stg.getSignalTransitions(Signal.Type.INTERNAL).size());
    }

}
