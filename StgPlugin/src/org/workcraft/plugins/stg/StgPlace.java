package org.workcraft.plugins.stg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.plugins.petri.Place;

@VisualClass(org.workcraft.plugins.petri.VisualPlace.class)
@DisplayName("Place")
public class StgPlace extends Place {
    private boolean implicit = false;

    public void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }

    public boolean isImplicit() {
        return implicit;
    }
}