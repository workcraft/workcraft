package org.workcraft.plugins.xbm;

import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;

//TODO: Unit testing for the XbmPlugin will be completed at a later date
public class XbmTests {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void fakeTest() {
        // This is a fake test to make Gradle 5.x happy
    }
}
