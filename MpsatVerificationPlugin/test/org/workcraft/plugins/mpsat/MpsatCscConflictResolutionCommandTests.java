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
import org.workcraft.plugins.mpsat.commands.MpsatCscConflictResolutionCommand;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatCscConflictResolutionCommandTests {

    @BeforeClass
    public static void initPlugins() {
        final Framework framework = Framework.getInstance();
        framework.initPlugins();
        switch (DesktopApi.getOs()) {
        case LINUX:
            PunfSettings.setCommand("../dist-template/linux/tools/UnfoldingTools/punf");
            MpsatSettings.setCommand("../dist-template/linux/tools/UnfoldingTools/mpsat");
            break;
        case MACOS:
            PunfSettings.setCommand("../dist-template/osx/Contents/Resources/tools/UnfoldingTools/punf");
            MpsatSettings.setCommand("../dist-template/osx/Contents/Resources/tools/UnfoldingTools/mpsat");
            break;
        case WINDOWS:
            PunfSettings.setCommand("..\\dist-template\\windows\\tools\\UnfoldingTools\\punf.exe");
            MpsatSettings.setCommand("..\\dist-template\\windows\\tools\\UnfoldingTools\\mpsat.exe");
            break;
        default:
        }
    }

    @Test
    public void vmeCscConflictResolution() throws DeserialisationException {
        testCscConflictResolutionCommand("org/workcraft/plugins/mpsat/vme.stg.work", new String[] {"csc1", "csc2"});
    }

    @Test
    public void arbitrationCscConflictResolution() throws DeserialisationException {
        testCscConflictResolutionCommand("org/workcraft/plugins/mpsat/arbitration-3.stg.work", new String[] {});
    }

    private void testCscConflictResolutionCommand(String work, String[] cscSignals)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(work);
        WorkspaceEntry srcWe = framework.loadWork(url.getFile());

        Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
        Set<String> srcInputs = srcStg.getSignalNames(Type.INPUT, null);
        Set<String> srcInternals = srcStg.getSignalNames(Type.INTERNAL, null);
        Set<String> srcOutputs = srcStg.getSignalNames(Type.OUTPUT, null);
        Set<String> srcMutexes = getMutexNames(srcStg);

        MpsatCscConflictResolutionCommand command = new MpsatCscConflictResolutionCommand();
        WorkspaceEntry dstWe = command.execute(srcWe);

        Stg dstStg = WorkspaceUtils.getAs(dstWe, Stg.class);
        Set<String> dstInputs = dstStg.getSignalNames(Type.INPUT, null);
        Set<String> dstInternals = dstStg.getSignalNames(Type.INTERNAL, null);
        Set<String> dstOutputs = dstStg.getSignalNames(Type.OUTPUT, null);
        Set<String> dstMutexes = getMutexNames(dstStg);

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
        Assert.assertEquals(srcMutexes, dstMutexes);
    }

    private Set<String> getMutexNames(Stg stg) {
        HashSet<String> result = new HashSet<>();
        for (StgPlace place: stg.getMutexPlaces()) {
            result.add(stg.getNodeReference(place));
        }
        return result;
    }

}
