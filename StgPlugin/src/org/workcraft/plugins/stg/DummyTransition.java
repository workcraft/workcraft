package org.workcraft.plugins.stg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@DisplayName("Dummy transition")
@IdentifierPrefix("dum")
@VisualClass(org.workcraft.plugins.stg.VisualDummyTransition.class)
public class DummyTransition extends NamedTransition {
    public static final String PROPERTY_NAME = "Name";
    private String name;

    @NoAutoSerialisation
    public void setName(String value) {
        if (value == null) value = "";
        if (!value.equals(name)) {
            name = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_NAME));
        }
    }

    @NoAutoSerialisation
    @Override
    public String getName() {
        return name;
    }

}
