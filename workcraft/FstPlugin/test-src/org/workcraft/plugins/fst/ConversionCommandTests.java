package org.workcraft.plugins.fst;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

class ConversionCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    void testVmeConversionCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.fst.work");
        testConversionCommands(workName);
    }

    private void testConversionCommands(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry srcWe = framework.loadWork(url.getFile());
        Fst src = WorkspaceUtils.getAs(srcWe, Fst.class);

        FstToStgConversionCommand fstToStgConversionCommand = new FstToStgConversionCommand();
        WorkspaceEntry stgWe = fstToStgConversionCommand.execute(srcWe);
        Stg stg = WorkspaceUtils.getAs(stgWe, Stg.class);

        Assertions.assertEquals(src.getStates().size(), stg.getPlaces().size());
        for (Signal signal : src.getSignals()) {
            String signalRef = src.getNodeReference(signal);
            Assertions.assertEquals(src.getSignalEvents(signal).size(), stg.getSignalTransitions(signalRef).size());
        }

        FstToFsmConversionCommand fstToFsmConversionCommand = new FstToFsmConversionCommand();
        WorkspaceEntry fsmWe = fstToFsmConversionCommand.execute(srcWe);
        Fsm fsm = WorkspaceUtils.getAs(fsmWe, Fsm.class);

        Assertions.assertEquals(src.getStates().size(), fsm.getStates().size());
        Assertions.assertEquals(src.getEvents().size(), fsm.getEvents().size());

        FsmToFstConversionCommand fsmToFstConversionCommand = new FsmToFstConversionCommand();
        WorkspaceEntry fstWe = fsmToFstConversionCommand.execute(fsmWe);
        Fst fst = WorkspaceUtils.getAs(fstWe, Fst.class);

        Assertions.assertEquals(src.getStates().size(), fst.getStates().size());
        Assertions.assertEquals(src.getEvents().size(), fst.getEvents().size());
        Assertions.assertEquals(0, getSignalCount(fst));
        Assertions.assertEquals(2 * getSignalCount(src), fst.getSignals(Signal.Type.DUMMY).size());

        framework.closeWork(srcWe);
        framework.closeWork(stgWe);
        framework.closeWork(fsmWe);
        framework.closeWork(fstWe);
    }

    private int getSignalCount(Fst fst) {
        return fst.getSignals(Signal.Type.INPUT).size() + fst.getSignals(Signal.Type.OUTPUT).size() + fst.getSignals(Signal.Type.INTERNAL).size();
    }

}
