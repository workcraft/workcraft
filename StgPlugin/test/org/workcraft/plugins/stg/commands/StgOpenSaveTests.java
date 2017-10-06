package org.workcraft.plugins.stg.commands;

import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.workspace.WorkspaceEntry;

public class StgOpenSaveTests {

    private static final String[] TEST_STG_WORKS = {
        "org/workcraft/plugins/stg/commands/vme.stg.work",
    };

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testNotChangedAfterOpen() throws DeserialisationException {
        final Framework framework = Framework.getInstance();
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String testStgWork: TEST_STG_WORKS) {
            URL srcUrl = classLoader.getResource(testStgWork);

            WorkspaceEntry srcWe = framework.loadWork(srcUrl.getFile());
            Assert.assertFalse(srcWe.isChanged());
            framework.closeWork(srcWe);
        }
    }

}
