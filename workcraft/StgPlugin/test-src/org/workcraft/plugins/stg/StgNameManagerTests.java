package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.Model;
import org.workcraft.observation.PropertyChangedEvent;

import java.util.ArrayList;
import java.util.List;

class StgNameManagerTests {

    @Test
    void signalTransitionRenameNotificationTest() {
        Stg stg = new Stg();
        SignalTransition transition = stg.createSignalTransition("a+", null);
        List<String> propertyNames = new ArrayList<>();
        transition.addObserver(e -> {
            if (e instanceof PropertyChangedEvent propertyChangedEvent) {
                propertyNames.add(propertyChangedEvent.getPropertyName());
            }
        });

        stg.setName(transition, "a-");
        Assertions.assertEquals(SignalTransition.Direction.MINUS, transition.getDirection());
        Assertions.assertTrue(propertyNames.contains(Model.PROPERTY_NAME));

        propertyNames.clear();
        stg.setName(transition, "a-");
        Assertions.assertFalse(propertyNames.contains(Model.PROPERTY_NAME));
    }

}
