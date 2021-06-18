package org.workcraft.plugins.cpog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.builtin.commands.BasicStatisticsCommand;
import org.workcraft.plugins.cpog.commands.CpogStatisticsCommand;
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
        String workName = PackageUtils.getPackagePath(getClass(), "instructions-scenario.cpog.work");
        testCpogStatisticsCommands(workName,
                "Component count:"
                        + "\n  Vertex -  10"
                        + "\n  Arc -  4"
                        + "\n  PageNode -  3"
                        + "\n",
                "Statistics for selected scenarios:"
                        + "\n  Vertex count -  10 (10 unconditional)"
                        + "\n  Arc count -  4 (4 unconditional)"
                        + "\n  Variable count -  0"
                        + "\n");
    }

    @Test
    void testInstructionsEncodingStatisticsCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "instructions-encoding.cpog.work");
        testCpogStatisticsCommands(workName,
                "Component count:"
                        + "\n  Variable -  2"
                        + "\n  Vertex -  4"
                        + "\n  Arc -  2"
                        + "\n",
                "Statistics for current scenario:"
                        + "\n  Vertex count -  4 (2 unconditional)"
                        + "\n  Arc count -  2 (2 unconditional)"
                        + "\n  Variable count -  2"
                        + "\n  Conditions (2 in total) - x_0, x_1"
                        + "\n");
    }

    private void testCpogStatisticsCommands(String workName, String expectedBasicStatistics, String expectedAdvancedStatistics)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        BasicStatisticsCommand basicStatisticsCommand = new BasicStatisticsCommand();
        String basicStatistics = basicStatisticsCommand.execute(we);
        Assertions.assertEquals(expectedBasicStatistics, basicStatistics);

        CpogStatisticsCommand advancedStatisticsCommand = new CpogStatisticsCommand();
        we.getModelEntry().getVisualModel().selectAll();
        String advancedStatistics = advancedStatisticsCommand.execute(we);
        Assertions.assertEquals(expectedAdvancedStatistics, advancedStatistics);

        framework.closeWork(we);
    }

}
