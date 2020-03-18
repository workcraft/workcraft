package org.workcraft.plugins.fst;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.builtin.commands.BasicStatisticsCommand;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

public class StatisticsCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testInstructionsScenariopogStatisticsCommands() throws DeserialisationException {
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
        Assert.assertEquals(expectedStatistics, statistics);

        framework.closeWork(we);
    }

}
