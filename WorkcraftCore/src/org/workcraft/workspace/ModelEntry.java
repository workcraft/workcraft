package org.workcraft.workspace;

import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;

public class ModelEntry {
    private final ModelDescriptor descriptor;
    private Model model;

    public ModelEntry(ModelDescriptor descriptor, Model model) {
        this.descriptor = descriptor;
        this.model = model;
    }

    public ModelDescriptor getDescriptor() {
        return descriptor;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    public VisualModel getVisualModel() {
        if (isVisual()) {
            return (VisualModel) model;
        } else {
            return null;
        }
    }

    public MathModel getMathModel() {
        if (isVisual()) {
            return getVisualModel().getMathModel();
        } else {
            return (MathModel) model;
        }
    }

    public boolean isVisual() {
        return model instanceof VisualModel;
    }

}
