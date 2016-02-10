package org.workcraft.workspace;

import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.visual.VisualModel;

public class WorkspaceMemoryObject {
    private Model model;
    private ModelDescriptor descriptor;

    public WorkspaceMemoryObject(Model model, ModelDescriptor descriptor) {
        this.model = model;
        this.descriptor = descriptor;
    }

    public ModelDescriptor getDescriptor() {
        return descriptor;
    }

    public Model getModel() {
        return model;
    }

    public boolean isVisualModel() {
        return model instanceof VisualModel;
    }
}
