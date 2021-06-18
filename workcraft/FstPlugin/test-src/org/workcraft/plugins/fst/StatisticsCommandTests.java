package org.workcraft.plugins.fst;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.builtin.commands.BasicStatisticsCommand;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class StatisticsCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testInstructionsScenariopogStatisticsCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.fst.work");
        testCpogStatisticsCommands(workName,
                "Component count:"
                        + "\n  State -  24"
                        + "\n  Signal -  6"
                        + "\n  SignalEvent -  33"
                        + "\n");
    }

    private void testCpogStatisticsCommands(String workName, String expectedStatistics)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        BasicStatisticsCommand basicStatisticsCommand = new BasicStatisticsCommand();
        String statistics = basicStatisticsCommand.execute(we);
        Assertions.assertEquals(expectedStatistics, statistics);

        framework.closeWork(we);
    }

}
