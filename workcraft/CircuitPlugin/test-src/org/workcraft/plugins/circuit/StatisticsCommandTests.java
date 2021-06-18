package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.builtin.commands.BasicStatisticsCommand;
import org.workcraft.plugins.circuit.commands.StatisticsCommand;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class StatisticsCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
    }

    @Test
    void testBufferTmStatisticsCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-tm.circuit.work");
        testStatisticsCommand(workName,
                "Component count:"
                        + "\n  Component -  1"
                        + "\n  Port -  2"
                        + "\n",
                "Circuit analysis:"
                        + "\n  Component count (mapped + unmapped) -  1 (1 + 0)"
                        + "\n  Area of mapped components -  0.0"
                        + "\n  Driver pin count (combinational + sequential + undefined) -  1 (1 + 0 + 0)"
                        + "\n  Literal count combinational / sequential (set + reset) -  1 / 0 (0 + 0)"
                        + "\n  Port count (input + output) -  2 (1 + 1)"
                        + "\n  Max fanin / fanout -  1 / 1"
                        + "\n  Fanin distribution [0 / 1 / 2 ...] -  0 / 1"
                        + "\n  Fanout distribution [0 / 1 / 2 ...] -  0 / 2"
                        + "\n  Isolated components / ports / pins -  0 / 0 / 0"
                        + "\n");
    }

    @Test
    void testCelementDecomposedTmStatisticsCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement-decomposed-tm.circuit.work");
        testStatisticsCommand(workName,
                "Component count:"
                        + "\n  Component -  5"
                        + "\n  Port -  3"
                        + "\n",
                "Circuit analysis:"
                        + "\n  Component count (mapped + unmapped) -  5 (5 + 0)"
                        + "\n  Area of mapped components -  72.0"
                        + "\n  Driver pin count (combinational + sequential + undefined) -  5 (5 + 0 + 0)"
                        + "\n  Literal count combinational / sequential (set + reset) -  11 / 0 (0 + 0)"
                        + "\n  Port count (input + output) -  3 (2 + 1)"
                        + "\n  Max fanin / fanout -  3 / 2"
                        + "\n  Fanin distribution [0 / 1 / 2 ...] -  0 / 0 / 4 / 1"
                        + "\n  Fanout distribution [0 / 1 / 2 ...] -  0 / 2 / 5"
                        + "\n  Isolated components / ports / pins -  0 / 0 / 0"
                        + "\n");
    }

    @Test
    void testVmeTmStatisticsCommand() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testStatisticsCommand(workName,
                "Component count:"
                        + "\n  Component -  21"
                        + "\n  Port -  6"
                        + "\n",
                "Circuit analysis:"
                        + "\n  Component count (mapped + unmapped) -  21 (21 + 0)"
                        + "\n  Area of mapped components -  292.0"
                        + "\n  Driver pin count (combinational + sequential + undefined) -  21 (20 + 1 + 0)"
                        + "\n  Literal count combinational / sequential (set + reset) -  47 / 4 (2 + 2)"
                        + "\n  Port count (input + output) -  6 (3 + 3)"
                        + "\n  Max fanin / fanout -  6 / 8"
                        + "\n  Fanin distribution [0 / 1 / 2 ...] -  0 / 12 / 2 / 1 / 1 / 4 / 1"
                        + "\n  Fanout distribution [0 / 1 / 2 ...] -  0 / 17 / 1 / 1 / 2 / 0 / 1 / 0 / 2"
                        + "\n  Isolated components / ports / pins -  0 / 0 / 0"
                        + "\n");
    }

    private void testStatisticsCommand(String workName, String expectedBasicStatistics, String expectedAdvancedStatistics)
            throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());

        BasicStatisticsCommand basicStatisticsCommand = new BasicStatisticsCommand();
        String basicStatistics = basicStatisticsCommand.execute(we);
        Assertions.assertEquals(expectedBasicStatistics, basicStatistics);

        StatisticsCommand advancedStatisticsCommand = new StatisticsCommand();
        String advancedStatistics = advancedStatisticsCommand.execute(we);
        Assertions.assertEquals(expectedAdvancedStatistics, advancedStatistics);

        framework.closeWork(we);
    }

}
