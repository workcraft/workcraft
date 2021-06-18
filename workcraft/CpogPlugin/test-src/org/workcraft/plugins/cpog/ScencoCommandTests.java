package org.workcraft.plugins.cpog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.cpog.tasks.ScencoExternalToolTask;
import org.workcraft.plugins.cpog.tasks.ScencoResultHandler;
import org.workcraft.plugins.cpog.tasks.ScencoSolver;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Collection;

class ScencoCommandTests {

    @BeforeAll
    static void skipOnMac() {
        Assumptions.assumeFalse(DesktopApi.getOs().isMac());
    }

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CpogSettings.setScencoCommand(BackendUtils.getTemplateToolPath("ScEnco", "scenco"));
    }

    @Test
    void cpogSettingsTest() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String workName = PackageUtils.getPackagePath(getClass(), "instructions-scenario.cpog.work");
        URL url = classLoader.getResource(workName);

        WorkspaceEntry we = framework.loadWork(url.getFile());
        VisualCpog cpog = WorkspaceUtils.getAs(we, VisualCpog.class);

        EncoderSettings settings = new EncoderSettings(10, EncoderSettings.GenerationMode.SEQUENTIAL, false, false);
        settings.setBits(2);
        settings.setGenerationModeInt(5);
        final ScencoSolver solver = new ScencoSolver(settings, we);
        final ScencoExternalToolTask task = new ScencoExternalToolTask(we, solver);
        final ScencoResultHandler monitor = new ScencoResultHandler(task);
        final TaskManager taskManager = framework.getTaskManager();
        we.getModelEntry().getVisualModel().selectAll();
        taskManager.execute(task, "Sequential encoding", monitor);

        Assertions.assertEquals(2, cpog.getVariables().size());

        Collection<VisualScenarioPage> scenarioPages = cpog.getScenarioPages();
        Assertions.assertEquals(3, scenarioPages.size());

        int nodeCount = 0;
        int arcCount = 0;
        for (VisualScenarioPage scenario : scenarioPages) {
            nodeCount += cpog.getVertices(scenario).size();
            arcCount += cpog.getArcs(scenario).size();
        }
        Assertions.assertEquals(10, nodeCount);
        Assertions.assertEquals(4, arcCount);
    }

}
