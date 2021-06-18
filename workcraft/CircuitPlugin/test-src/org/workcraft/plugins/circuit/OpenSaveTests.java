package org.workcraft.plugins.circuit;

import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

class OpenSaveTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testCelementNotChangedAfterOpen() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "celement.circuit.work");
        testNotChangedAfterOpen(workName);
    }

    @Test
    void testVmeTmNotChangedAfterOpen() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme-tm.circuit.work");
        testNotChangedAfterOpen(workName);
    }

    private void testNotChangedAfterOpen(String workName) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(workName);
        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Assertions.assertFalse(srcWe.isChanged());
        framework.closeWork(srcWe);
    }

}
