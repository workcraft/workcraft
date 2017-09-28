package org.workcraft.plugins.stg.commands;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.statistics.BasicStatisticsCommand;
import org.workcraft.workspace.WorkspaceEntry;

public class StgStatisticsCommandTests {

    private static final String[][] TEST_STG_WORKS_STATISTICS = {
        {"org/workcraft/plugins/stg/commands/celement.stg.work",
            "Component count:"
                    + "\n  Place -  8"
                    + "\n  Transition -  6"
                    + "\n  Arc -  16",
            "Signal Transition Graph analysis:"
                    + "\n  Transition count -  6"
                    + "\n    * Input / output / internal / dummy -  4 / 2 / 0 / 0"
                    + "\n    * Rising / falling / toggle -  3 / 3 / 0"
                    + "\n    * Fork / join -  2 / 2"
                    + "\n    * Source / sink -  0 / 0"
                    + "\n    * Max fanin / fanout -  2 / 2"
                    + "\n  Place count -  8"
                    + "\n    * Choice / merge -  0 / 0"
                    + "\n    * Source / sink -  0 / 0"
                    + "\n    * Mutex -  0"
                    + "\n    * Max fanin / fanout -  1 / 1"
                    + "\n  Arc count -  16"
                    + "\n    * Producing / consuming -  8 / 8"
                    + "\n    * Self-loop -  0"
                    + "\n  Token count / marked places -  2 / 2"
                    + "\n  Isolated transitions / places -  0 / 0"
                    + "\n  Net type:"
                    + "\n    * Marked graph -  true"
                    + "\n    * State machine -  false"
                    + "\n    * Free choice -  true"
                    + "\n    * Extended free choice -  true"
                    + "\n    * Pure -  true",
        },
        {"org/workcraft/plugins/stg/commands/buck.stg.work",
            "Component count:"
                    + "\n  Place -  29"
                    + "\n  Transition -  28"
                    + "\n  Arc -  62",
            "Signal Transition Graph analysis:"
                    + "\n  Transition count -  28"
                    + "\n    * Input / output / internal / dummy -  20 / 8 / 0 / 0"
                    + "\n    * Rising / falling / toggle -  14 / 14 / 0"
                    + "\n    * Fork / join -  3 / 3"
                    + "\n    * Source / sink -  0 / 0"
                    + "\n    * Max fanin / fanout -  2 / 2"
                    + "\n  Place count -  29"
                    + "\n    * Choice / merge -  1 / 1"
                    + "\n    * Source / sink -  0 / 0"
                    + "\n    * Mutex -  0"
                    + "\n    * Max fanin / fanout -  3 / 3"
                    + "\n  Arc count -  62"
                    + "\n    * Producing / consuming -  31 / 31"
                    + "\n    * Self-loop -  0"
                    + "\n  Token count / marked places -  1 / 1"
                    + "\n  Isolated transitions / places -  0 / 0"
                    + "\n  Net type:"
                    + "\n    * Marked graph -  false"
                    + "\n    * State machine -  false"
                    + "\n    * Free choice -  true"
                    + "\n    * Extended free choice -  true"
                    + "\n    * Pure -  true",
        },
        {"org/workcraft/plugins/stg/commands/vme.stg.work",
            "Component count:"
                    + "\n  Place -  17"
                    + "\n  Transition -  17"
                    + "\n  Arc -  38",
            "Signal Transition Graph analysis:"
                    + "\n  Transition count -  17"
                    + "\n    * Input / output / internal / dummy -  7 / 10 / 0 / 0"
                    + "\n    * Rising / falling / toggle -  10 / 7 / 0"
                    + "\n    * Fork / join -  2 / 2"
                    + "\n    * Source / sink -  0 / 0"
                    + "\n    * Max fanin / fanout -  2 / 2"
                    + "\n  Place count -  17"
                    + "\n    * Choice / merge -  2 / 2"
                    + "\n    * Source / sink -  0 / 0"
                    + "\n    * Mutex -  0"
                    + "\n    * Max fanin / fanout -  2 / 2"
                    + "\n  Arc count -  38"
                    + "\n    * Producing / consuming -  19 / 19"
                    + "\n    * Self-loop -  0"
                    + "\n  Token count / marked places -  2 / 2"
                    + "\n  Isolated transitions / places -  0 / 0"
                    + "\n  Net type:"
                    + "\n    * Marked graph -  false"
                    + "\n    * State machine -  false"
                    + "\n    * Free choice -  false"
                    + "\n    * Extended free choice -  false"
                    + "\n    * Pure -  true",
        },
    };

    @BeforeClass
    public static void initPlugins() {
        final Framework framework = Framework.getInstance();
        framework.initPlugins();
    }

    @Test
    public void testStgStatisticsCommands() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String[] testStgWorkStatistics: TEST_STG_WORKS_STATISTICS) {
            String stgWork = testStgWorkStatistics[0];
            URL url = classLoader.getResource(stgWork);
            WorkspaceEntry we = framework.loadWork(url.getFile());

            BasicStatisticsCommand basicStatisticsCommand = new BasicStatisticsCommand();
            String basicStatistics = basicStatisticsCommand.execute(we);
            String expectedBasicStatistics = testStgWorkStatistics[1];
            Assert.assertEquals(basicStatistics, expectedBasicStatistics);

            StgStatisticsCommand advancedStatisticsCommand = new StgStatisticsCommand();
            String advancedStatistics = advancedStatisticsCommand.execute(we);
            String expectedAdvancedStatistics = testStgWorkStatistics[2];
            Assert.assertEquals(advancedStatistics, expectedAdvancedStatistics);

            framework.closeWork(we);
        }
    }

}