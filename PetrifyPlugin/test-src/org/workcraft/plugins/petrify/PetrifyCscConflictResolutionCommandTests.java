package org.workcraft.plugins.petrify;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.utils.DesktopApi;
import org.workcraft.plugins.petrify.commands.PetrifyCscConflictResolutionCommand;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class PetrifyCscConflictResolutionCommandTests {

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
    public void vmeCscConflictResolution() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testCscConflictResolutionCommand(workName, new String[] {"csc0"});
    }

    @Test
    public void arbitrationCscConflictResolution() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "arbitration-3-hierarchy.stg.work");
        testCscConflictResolutionCommand(workName, new String[] {});
    }

    private void testCscConflictResolutionCommand(String workName, String[] cscSignals)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry srcWe = framework.loadWork(url.getFile());

        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcInternals = srcStg.getSignalReferences(Signal.Type.INTERNAL);
        Set<String> srcOutputs = srcStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> srcMutexes = MutexUtils.getMutexPlaceReferences(srcStg);

        PetrifyCscConflictResolutionCommand command = new PetrifyCscConflictResolutionCommand();
        WorkspaceEntry dstWe = command.execute(srcWe);

        Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);
        Set<String> dstInputs = dstStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> dstInternals = dstStg.getSignalReferences(Signal.Type.INTERNAL);
        Set<String> dstOutputs = dstStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> dstMutexes = MutexUtils.getMutexPlaceReferences(dstStg);

        Set<String> expInternals = new HashSet<>();
        expInternals.addAll(srcInternals);
        if (cscSignals != null) {
            for (String cscSignal: cscSignals) {
                expInternals.add(cscSignal);
            }
        }

        Assert.assertEquals(srcInputs, dstInputs);
        Assert.assertEquals(expInternals, dstInternals);
        Assert.assertEquals(srcOutputs, dstOutputs);
        Assert.assertEquals(srcMutexes.size(), dstMutexes.size());
    }

}
