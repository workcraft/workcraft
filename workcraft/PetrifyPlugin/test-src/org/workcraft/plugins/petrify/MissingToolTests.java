package org.workcraft.plugins.petrify;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.petrify.commands.TechnologyMappingSynthesisCommand;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.net.URL;

class MissingToolTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testMissingPetrifyVerification() throws DeserialisationException {
        PetrifySettings.setCommand(BackendUtils.getTemplateToolPath("PetrifyTools", "petrify-missing"));
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib"));
        testMissingTool();
    }

    @Test
    void testMissingGenlibVerification() throws DeserialisationException {
        PetrifySettings.setCommand(BackendUtils.getTemplateToolPath("PetrifyTools", "petrify"));
        CircuitSettings.setGateLibrary(BackendUtils.getTemplateLibraryPath("workcraft.lib-missing"));
        testMissingTool();
    }

    private void testMissingTool() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String workName = PackageUtils.getPackagePath(getClass(), "buffer-compact.stg.work");
        URL url = classLoader.getResource(workName);
        WorkspaceEntry we = framework.loadWork(url.getFile());
        TechnologyMappingSynthesisCommand command = new TechnologyMappingSynthesisCommand();
        Assertions.assertNull(command.execute(we));
    }

}
