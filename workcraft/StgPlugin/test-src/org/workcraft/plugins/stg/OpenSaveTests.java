package org.workcraft.plugins.stg;

import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.PackageUtils;
import org.workcraft.workspace.WorkspaceEntry;

class OpenSaveTests {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    void testVmeNotChangedAfterOpen() throws DeserialisationException {
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
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

    @Test
    void testChangedAfterDirectConnection() throws Exception {
        final Framework framework = Framework.getInstance();
        String workName = PackageUtils.getPackagePath(getClass(), "vme.stg.work");
        URL srcUrl = ClassLoader.getSystemClassLoader().getResource(workName);
        WorkspaceEntry we = framework.loadWork(srcUrl.getFile());
        Assertions.assertFalse(we.isChanged());

        VisualStg stg = we.getModelEntry().getAs(VisualStg.class);
        VisualStgPlace place = stg.getVisualPlaces().iterator().next();
        boolean connected = false;
        for (VisualSignalTransition transition : Hierarchy.getDescendantsOfType(stg.getRoot(), VisualSignalTransition.class)) {
            try {
                stg.connect(place, transition);
                connected = true;
                break;
            } catch (InvalidConnectionException ignored) {
                // Try the next transition if the arc already exists
            }
        }
        Assertions.assertTrue(connected);
        Assertions.assertTrue(we.isChanged());
        framework.closeWork(we);
    }

}
