package org.workcraft.plugins.stg.dom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.VisualStgPlace;

class VisualStgPlaceTests {

    @Test
    void copyStyleMutexProtocolTest() {
        VisualStgPlace srcPlace = new VisualStgPlace(new StgPlace());
        srcPlace.getReferencedComponent().setMutex(true);
        srcPlace.getReferencedComponent().setMutexProtocol(Mutex.Protocol.LATE);

        VisualStgPlace dstPlace = new VisualStgPlace(new StgPlace());
        dstPlace.copyStyle(srcPlace);
        Assertions.assertTrue(dstPlace.getReferencedComponent().isMutex());
        Assertions.assertEquals(Mutex.Protocol.LATE, dstPlace.getReferencedComponent().getMutexProtocol());
    }

}
