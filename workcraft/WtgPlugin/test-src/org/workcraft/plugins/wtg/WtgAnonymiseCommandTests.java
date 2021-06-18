package org.workcraft.plugins.wtg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.builtin.commands.AnonymiseTransformationCommand;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

class WtgAnonymiseCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testDlatchWtgAnonymiseCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.wtg.work");
        testWtgAnonymiseCommands(workName, new String[]{"s1"}, new String[]{"w1."}, new String[]{"x0", "x1", "x2"});
    }

    private void testWtgAnonymiseCommands(String workName, String[] stateRefs, String[] waveformRefs, String[] signalRefs)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        Wtg wtg = WorkspaceUtils.getAs(we, Wtg.class);

        AnonymiseTransformationCommand command = new AnonymiseTransformationCommand();
        command.execute(we);

        HashSet<String> expectedStateRefs = new HashSet<>(Arrays.asList(stateRefs));
        HashSet<String> actualStateRefs = new HashSet<>();
        for (State state : wtg.getStates()) {
            actualStateRefs.add(wtg.getNodeReference(state));
        }
        Assertions.assertEquals(expectedStateRefs, actualStateRefs);

        HashSet<String> expectedWaveformRefs = new HashSet<>(Arrays.asList(waveformRefs));
        HashSet<String> actualWaveformRefs = new HashSet<>();
        for (Waveform waveform : wtg.getWaveforms()) {
            actualWaveformRefs.add(wtg.getNodeReference(waveform));
        }
        Assertions.assertEquals(expectedWaveformRefs, actualWaveformRefs);

        HashSet<String> expectedSignalRefs = new HashSet<>(Arrays.asList(signalRefs));
        Collection<String> actualSignalRefs = wtg.getSignalNames();
        Assertions.assertEquals(expectedSignalRefs, actualSignalRefs);

        framework.closeWork(we);
    }

}
