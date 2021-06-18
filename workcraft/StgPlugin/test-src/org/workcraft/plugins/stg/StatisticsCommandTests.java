package org.workcraft.plugins.stg;

import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.builtin.commands.BasicStatisticsCommand;
import org.workcraft.plugins.stg.commands.StgStatisticsCommand;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

class StatisticsCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testCelementStgStatisticsCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.stg.work");
        testStgStatisticsCommands(workName,
                "Component count:"
                        + "\n  Place -  8"
                        + "\n  Transition -  6"
                        + "\n  Arc -  16"
                        + "\n",
                "Signal Transition Graph analysis:"
                        + "\n  Signal count -  3"
                        + "\n    * Input / output / internal -  2 / 1 / 0"
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
                        + "\n    * Pure -  true"
                        + "\n");
    }

    @Test
    void testBuckStgStatisticsCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.stg.work");
        testStgStatisticsCommands(workName,
                "Component count:"
                        + "\n  Place -  29"
                        + "\n  Transition -  28"
                        + "\n  Arc -  62"
                        + "\n",
                "Signal Transition Graph analysis:"
                        + "\n  Signal count -  7"
                        + "\n    * Input / output / internal -  5 / 2 / 0"
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
                        + "\n    * Pure -  true"
                        + "\n");
    }

    @Test
    void testVmeStgStatisticsCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        testStgStatisticsCommands(workName,
                "Component count:"
                        + "\n  Place -  17"
                        + "\n  Transition -  17"
                        + "\n  Arc -  38"
                        + "\n",
                "Signal Transition Graph analysis:"
                        + "\n  Signal count -  6"
                        + "\n    * Input / output / internal -  3 / 3 / 0"
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
                        + "\n    * Pure -  true"
                        + "\n");
    }

    private void testStgStatisticsCommands(String workName, String expectedBasicStatistics, String expectedAdvancedStatistics)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        BasicStatisticsCommand basicStatisticsCommand = new BasicStatisticsCommand();
        String basicStatistics = basicStatisticsCommand.execute(we);
        Assertions.assertEquals(expectedBasicStatistics, basicStatistics);

        StgStatisticsCommand advancedStatisticsCommand = new StgStatisticsCommand();
        String advancedStatistics = advancedStatisticsCommand.execute(we);
        Assertions.assertEquals(expectedAdvancedStatistics, advancedStatistics);

        framework.closeWork(we);
    }

}
