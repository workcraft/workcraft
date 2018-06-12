package org.workcraft.plugins.circuit;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.workspace.WorkspaceEntry;

public class CircuitOpenSaveTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testCelementNotChangedAfterOpen() throws DeserialisationException {
        testNotChangedAfterOpen("org/workcraft/plugins/circuit/celement.circuit.work");
    }

    @Test
    public void testVmeTmNotChangedAfterOpen() throws DeserialisationException {
        testNotChangedAfterOpen("org/workcraft/plugins/circuit/vme-tm.circuit.work");
    }

    private void testNotChangedAfterOpen(String testCircuitWork) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(testCircuitWork);
        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Assert.assertFalse(srcWe.isChanged());
        framework.closeWork(srcWe);
    }

}
