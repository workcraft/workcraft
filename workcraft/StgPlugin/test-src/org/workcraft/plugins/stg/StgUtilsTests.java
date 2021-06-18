package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.*;

class StgUtilsTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testVmeInitialState() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testInitialState(workName, 10, Collections.emptyList(), Collections.emptyList());
    }

    @Test
    void testBuckInitialState() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.stg.work");
        testInitialState(workName, 10, Arrays.asList("gn", "gn_ack"), Collections.emptyList());
    }

    @Test
    void testChoice9InitialState() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "choice9.stg.work");
        testInitialState(workName, 300, Collections.emptyList(), Collections.singletonList("out"));
    }

    private void testInitialState(String workName, int timeout,
            Collection<String> highSignals, Collection<String> undefinedSignals)
            throws DeserialisationException {

        Framework framework = Framework.getInstance();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);

        Map<String, Boolean> expInitialState = new HashMap<>();
        stg.getSignalReferences().forEach(signalRef -> expInitialState.put(signalRef, false));
        highSignals.forEach(signalRef -> expInitialState.put(signalRef, true));
        undefinedSignals.forEach(expInitialState::remove);

        Map<String, Boolean> initialState = StgUtils.getInitialState(stg, timeout);
        Assertions.assertEquals(expInitialState, initialState);
    }

}
