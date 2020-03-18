package org.workcraft.plugins.fst;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fst.commands.FsmToFstConversionCommand;
import org.workcraft.plugins.fst.commands.FstToFsmConversionCommand;
import org.workcraft.plugins.fst.commands.FstToStgConversionCommand;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class FstTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void testVmePetriConversionCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.fst.work");
        testStgConversionCommand(workName);
    }

    private void testStgConversionCommand(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(url.getFile());
        Fst src = WorkspaceUtils.getAs(srcWe, Fst.class);

        FstToStgConversionCommand fstToStgConversionCommand = new FstToStgConversionCommand();
        WorkspaceEntry stgWe = fstToStgConversionCommand.execute(srcWe);
        Stg stg = WorkspaceUtils.getAs(stgWe, Stg.class);

        Assert.assertEquals(src.getStates().size(), stg.getPlaces().size());
        for (Signal signal : src.getSignals()) {
            String signalRef = src.getNodeReference(signal);
            Assert.assertEquals(src.getSignalEvents(signal).size(), stg.getSignalTransitions(signalRef).size());
        }

        FstToFsmConversionCommand fstToFsmConversionCommand = new FstToFsmConversionCommand();
        WorkspaceEntry fsmWe = fstToFsmConversionCommand.execute(srcWe);
        Fsm fsm = WorkspaceUtils.getAs(fsmWe, Fsm.class);

        Assert.assertEquals(src.getStates().size(), fsm.getStates().size());
        Assert.assertEquals(src.getEvents().size(), fsm.getEvents().size());

        FsmToFstConversionCommand fsmToFstConversionCommand = new FsmToFstConversionCommand();
        WorkspaceEntry fstWe = fsmToFstConversionCommand.execute(srcWe);
        Fst fst = WorkspaceUtils.getAs(fstWe, Fst.class);

        Assert.assertEquals(src.getStates().size(), fst.getStates().size());
        Assert.assertEquals(src.getEvents().size(), fst.getEvents().size());
        Assert.assertEquals(0, getSignalCount(fst));
        Assert.assertEquals(getSignalCount(src), fst.getSignals(Signal.Type.DUMMY).size());

        framework.closeWork(srcWe);
        framework.closeWork(stgWe);
        framework.closeWork(fsmWe);
        framework.closeWork(fstWe);
    }

    private int getSignalCount(Fst fst) {
        return fst.getSignals(Signal.Type.INPUT).size() + fst.getSignals(Signal.Type.OUTPUT).size() + fst.getSignals(Signal.Type.INTERNAL).size();
    }

}
