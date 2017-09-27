package org.workcraft.plugins.circuit;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.workspace.WorkspaceEntry;

public class CircuitOpenSaveTests {

    private static final String[] TEST_CIRCUIT_WORKS = {
        "org/workcraft/plugins/circuit/celement.circuit.work",
        "org/workcraft/plugins/circuit/vme-tm.circuit.work",
    };

    @BeforeClass
    public static void initPlugins() {
        final Framework framework = Framework.getInstance();
        framework.initPlugins();
    }

    @Test
    public void testNotChangedAfterOpen() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testCircuitWork: TEST_CIRCUIT_WORKS) {
            URL srcUrl = classLoader.getResource(testCircuitWork);
            WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
            Assert.assertFalse(srcWe.isChanged());
            framework.closeWork(srcWe);
        }
    }

}
