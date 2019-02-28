package org.workcraft.plugins.circuit;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class CircuitOpenSaveTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testCelementNotChangedAfterOpen() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.circuit.work");
        testNotChangedAfterOpen(workName);
    }

    @Test
    public void testVmeTmNotChangedAfterOpen() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testNotChangedAfterOpen(workName);
    }

    private void testNotChangedAfterOpen(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(workName);
        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Assert.assertFalse(srcWe.isChanged());
        framework.closeWork(srcWe);
    }

}
