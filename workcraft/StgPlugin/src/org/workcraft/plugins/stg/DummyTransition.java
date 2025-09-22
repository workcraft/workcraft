package org.workcraft.plugins.stg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.plugins.stg.references.StgNameManager;
import org.workcraft.serialisation.NoAutoSerialisation;

@DisplayName("Dummy transition")
@IdentifierPrefix(StgNameManager.DUMMY_PREFIX)
@VisualClass(VisualDummyTransition.class)
public class DummyTransition extends NamedTransition {

    private String name;

    // FIXME: As dummy name is node reference use Stg.setName instead!
    // This method is only to be used from StgNameManager.
    public void setNameQuiet(String value) {
        if (value == null) value = "";
        if (!value.equals(name)) {
            name = value;
        }
    }

    @NoAutoSerialisation
    @Override
    public String getName() {
        return name;
    }

}
