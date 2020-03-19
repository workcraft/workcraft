package org.workcraft.serialisation;

import org.workcraft.dom.Model;

public class DeserialisationResult {
    public Model model;
    public References references;

    public DeserialisationResult(Model model, References references) {
        this.model = model;
        this.references = references;
    }
}
