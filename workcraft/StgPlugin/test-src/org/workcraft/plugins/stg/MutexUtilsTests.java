package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.stg.utils.MutexUtils;

class MutexUtilsTests {

    @Test
    void signalRequestTest() throws InvalidConnectionException {
        Stg stg = new Stg();
        createMutex(stg, stg.createSignalTransition("r1+", null));
        Assertions.assertEquals(1, MutexUtils.getMutexes(stg).size());
    }

    @Test
    void dummyRequestTest() throws InvalidConnectionException {
        Stg stg = new Stg();
        createMutex(stg, stg.createDummyTransition("d", null));
        Assertions.assertTrue(MutexUtils.getMutexes(stg).isEmpty());
    }

    private static void createMutex(Stg stg, MathNode request1) throws InvalidConnectionException {
        StgPlace mutexPlace = stg.createPlace("me", null);
        mutexPlace.setMutex(true);
        SignalTransition grant1 = stg.createSignalTransition("g1+", null);
        SignalTransition grant2 = stg.createSignalTransition("g2+", null);
        SignalTransition request2 = stg.createSignalTransition("r2+", null);
        StgPlace p1 = stg.createPlace("p1", null);
        StgPlace p2 = stg.createPlace("p2", null);
        stg.connect(mutexPlace, grant1);
        stg.connect(mutexPlace, grant2);
        stg.connect(request1, p1);
        stg.connect(p1, grant1);
        stg.connect(request2, p2);
        stg.connect(p2, grant2);
    }

}
