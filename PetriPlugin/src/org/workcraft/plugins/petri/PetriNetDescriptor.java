package org.workcraft.plugins.petri;

import org.workcraft.dom.ModelDescriptor;

public class PetriNetDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public PetriNet createMathModel() {
        return new PetriNet();
    }

    @Override
    public VisualPetriNetDescriptor getVisualModelDescriptor() {
        return new VisualPetriNetDescriptor();
    }

}
