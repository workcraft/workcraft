package org.workcraft.plugins.stg.commands;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.workspace.WorkspaceEntry;

public class StgOpenSaveTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testVmeNotChangedAfterOpen() throws DeserialisationException {
        testNotChangedAfterOpen("org/workcraft/plugins/stg/vme.stg.work");
    }

    private void testNotChangedAfterOpen(String testStgWork) throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL srcUrl = classLoader.getResource(testStgWork);

        WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
        Assert.assertFalse(srcWe.isChanged());
        framework.closeWork(srcWe);
    }

}
