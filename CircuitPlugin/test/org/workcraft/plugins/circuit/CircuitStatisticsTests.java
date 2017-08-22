package org.workcraft.plugins.circuit;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.commands.CircuitStatisticsCommand;
import org.workcraft.plugins.statistics.BasicStatisticsCommand;
import org.workcraft.workspace.WorkspaceEntry;

public class CircuitStatisticsTests {

    private static final String[][] TEST_CIRCUIT_WORKS_STATISTICS = {
        {"org/workcraft/plugins/circuit/buffer-tm.circuit.work",
            "Component count:\n" +
                    "  Component -  1\n" +
                    "  Port -  2",
            "Circuit analysis:\n" +
                    "  Component count -  1\n" +
                    "    * Fanin distribution (0 / 1 / 2 ...) -  0 / 1\n" +
                    "  Port count -  2\n" +
                    "    * Input / output -  1 / 1\n" +
                    "  Fanout distribution (0 / 1 / 2 ...) -  0 / 2\n" +
                    "  Disconnected components / ports / pins -  0 / 0 / 0",
        },
        {"org/workcraft/plugins/circuit/celement-decomposed-tm.circuit.work",
            "Component count:\n" +
                    "  Component -  5\n" +
                    "  Port -  3",
            "Circuit analysis:\n" +
                    "  Component count -  5\n" +
                    "    * Fanin distribution (0 / 1 / 2 ...) -  0 / 0 / 4 / 1\n" +
                    "  Port count -  3\n" +
                    "    * Input / output -  2 / 1\n" +
                    "  Fanout distribution (0 / 1 / 2 ...) -  0 / 2 / 5\n" +
                    "  Disconnected components / ports / pins -  0 / 0 / 0",
        },
        {"org/workcraft/plugins/circuit/vme-tm.circuit.work",
            "Component count:\n" +
                    "  Component -  21\n" +
                    "  Port -  6",
            "Circuit analysis:\n" +
                    "  Component count -  21\n" +
                    "    * Fanin distribution (0 / 1 / 2 ...) -  0 / 12 / 2 / 1 / 1 / 4 / 1\n" +
                    "  Port count -  6\n" +
                    "    * Input / output -  3 / 3\n" +
                    "  Fanout distribution (0 / 1 / 2 ...) -  0 / 17 / 1 / 1 / 2 / 0 / 1 / 0 / 2\n" +
                    "  Disconnected components / ports / pins -  0 / 0 / 0",
        },
    };

    @BeforeClass
    public static void initPlugins() {
        final Framework framework = Framework.getInstance();
        framework.initPlugins();
    }

    @Test
    public void testCircuitStatisticsCommand() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String[] testCircuitWorkStatistics: TEST_CIRCUIT_WORKS_STATISTICS) {
            String circuitWork = testCircuitWorkStatistics[0];
            URL srcUrl = classLoader.getResource(circuitWork);
            WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());

            BasicStatisticsCommand basicStatisticsCommand = new BasicStatisticsCommand();
            String basicStatistics = basicStatisticsCommand.getStatistics(srcWe);
            String expectedBasicStatistics = testCircuitWorkStatistics[1];
            Assert.assertEquals(basicStatistics, expectedBasicStatistics);

            CircuitStatisticsCommand advancedStatisticsCommand = new CircuitStatisticsCommand();
            String advancedStatistics = advancedStatisticsCommand.getStatistics(srcWe);
            String expectedAdvancedStatistics = testCircuitWorkStatistics[2];
            Assert.assertEquals(advancedStatistics, expectedAdvancedStatistics);

            framework.closeWork(srcWe);
        }
    }

}
