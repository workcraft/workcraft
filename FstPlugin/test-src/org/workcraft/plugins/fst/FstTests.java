package org.workcraft.plugins.fst;

import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Framework;

public class FstTests {

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
