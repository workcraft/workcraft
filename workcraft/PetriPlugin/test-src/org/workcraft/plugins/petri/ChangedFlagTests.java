package org.workcraft.plugins.petri;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

class ChangedFlagTests {

    @BeforeAll
    static void init() {
        Framework.getInstance().init();
    }

    private WorkspaceEntry createUnchangedWork(VisualPetri petri) {
        WorkspaceEntry we = Framework.getInstance().createWork(new ModelEntry(new PetriDescriptor(), petri), null);
        we.setChanged(false);
        return we;
    }

    @Test
    void testDirectConnectionSetsChanged() throws InvalidConnectionException {
        VisualPetri petri = new VisualPetri(new Petri());
        VisualPlace place = petri.createPlace(null, null);
        VisualTransition transition = petri.createTransition(null, null);
        WorkspaceEntry we = createUnchangedWork(petri);

        petri.connect(place, transition);
        Assertions.assertTrue(we.isChanged());
    }

    @Test
    void testNodeRemovalSetsChanged() {
        VisualPetri petri = new VisualPetri(new Petri());
        VisualPlace place = petri.createPlace(null, null);
        WorkspaceEntry we = createUnchangedWork(petri);

        petri.remove(place);
        Assertions.assertTrue(we.isChanged());
    }

}
