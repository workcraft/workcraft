package org.workcraft.plugins.petri;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.builtin.commands.BasicStatisticsCommand;
import org.workcraft.plugins.petri.commands.PetriStatisticsCommand;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class StatisticsCommandTests {

    @BeforeAll
    static void init() {
        Framework.getInstance().init();
    }

    @Test
    void testArcStgStatisticsCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "basic.pn.work");
        testPetriStatisticsCommands(workName,
                "Component count:\n" +
                        "  Place -  3\n" +
                        "  Transition -  3\n" +
                        "  Arc -  8\n",
                "Petri net analysis:\n" +
                        "  Transition count -  3\n" +
                        "    * Fork / join -  1 / 1\n" +
                        "    * Source / sink -  0 / 0\n" +
                        "    * Max fanin / fanout -  2 / 2\n" +
                        "  Place count -  3\n" +
                        "    * Choice / merge -  1 / 1\n" +
                        "    * Source / sink  -  0 / 0\n" +
                        "    * Max fanin / fanout -  2 / 2\n" +
                        "  Arc count -  8\n" +
                        "    * Producing / consuming -  4 / 4\n" +
                        "    * Self-loop -  2\n" +
                        "  Token count / marked places -  2 / 2\n" +
                        "  Isolated transitions / places -  0 / 0\n" +
                        "  Net type:\n" +
                        "    * Marked graph -  false\n" +
                        "    * State machine -  false\n" +
                        "    * Free choice -  false\n" +
                        "    * Extended free choice -  false\n" +
                        "    * Pure -  false\n");
    }

    private void testPetriStatisticsCommands(String workName, String expectedBasicStatistics, String expectedAdvancedStatistics)
            throws DeserialisationException {

        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        BasicStatisticsCommand basicStatisticsCommand = new BasicStatisticsCommand();
        String basicStatistics = basicStatisticsCommand.execute(we);
        Assertions.assertEquals(expectedBasicStatistics, basicStatistics);

        PetriStatisticsCommand advancedStatisticsCommand = new PetriStatisticsCommand();
        String advancedStatistics = advancedStatisticsCommand.execute(we);
        Assertions.assertEquals(expectedAdvancedStatistics, advancedStatistics);

        framework.closeWork(we);
    }

}
