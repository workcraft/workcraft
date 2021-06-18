package org.workcraft.plugins.wtg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.builtin.commands.BasicStatisticsCommand;
import org.workcraft.plugins.wtg.commands.StructureWaveformTransformationCommand;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class WtgTransformationCommandTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testDlatchTransformationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "dlatch.wtg.work");
        testTransformationCommands(workName);
    }

    @Test
    void testBuckTransformationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "buck.wtg.work");
        testTransformationCommands(workName);
    }

    @Test
    void testGuardsTransformationCommands() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "instruction_decoder.wtg.work");
        testTransformationCommands(workName);
    }

    private void testTransformationCommands(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());

        BasicStatisticsCommand statCommand = new BasicStatisticsCommand();
        String beforeStats = statCommand.getStatistics(we);

        StructureWaveformTransformationCommand command = new StructureWaveformTransformationCommand();
        command.execute(we);

        String afterStats = statCommand.getStatistics(we);
        Assertions.assertEquals(beforeStats, afterStats);

        framework.closeWork(we);
    }

}
