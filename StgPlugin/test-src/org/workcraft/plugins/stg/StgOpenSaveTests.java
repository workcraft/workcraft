package org.workcraft.plugins.stg;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class StgOpenSaveTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testVmeNotChangedAfterOpen() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
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
