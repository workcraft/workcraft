package org.workcraft.plugins.cpog;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.TestUtils;
import org.workcraft.plugins.cpog.tasks.ScencoExternalToolTask;
import org.workcraft.plugins.cpog.tasks.ScencoResultHandler;
import org.workcraft.plugins.cpog.tasks.ScencoSolver;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.PackageUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;
import java.util.Collection;

public class ScencoCommandTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        CpogSettings.setScencoCommand(TestUtils.getToolPath("ScEnco", "scenco"));
    }

    @Test
    public void cpogSettingsTest() throws DeserialisationException {
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

        Assert.assertEquals(2, cpog.getVariables().size());

        Collection<VisualScenarioPage> scenarioPages = cpog.getScenarioPages();
        Assert.assertEquals(3, scenarioPages.size());

        int nodeCount = 0;
        int arcCount = 0;
        for (VisualScenarioPage scenario : scenarioPages) {
            nodeCount += cpog.getVertices(scenario).size();
            arcCount += cpog.getArcs(scenario).size();
        }
        Assert.assertEquals(10, nodeCount);
        Assert.assertEquals(4, arcCount);
    }

}
