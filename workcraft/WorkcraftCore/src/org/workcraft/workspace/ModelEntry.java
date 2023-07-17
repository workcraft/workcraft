package org.workcraft.workspace;

import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.utils.TextUtils;

import java.util.UUID;

public class ModelEntry {
    private final ModelDescriptor descriptor;
    private final Model model;
    private Stamp stamp;
    private String desiredName;

    public ModelEntry(ModelDescriptor descriptor, Model model) {
        this.descriptor = descriptor;
        this.model = model;
    }

    public ModelDescriptor getDescriptor() {
        return descriptor;
    }

    public Model getModel() {
        return model;
    }

    public VisualModel getVisualModel() {
        if (isVisual()) {
            return (VisualModel) getModel();
        } else {
            return null;
        }
    }

    public MathModel getMathModel() {
        if (isVisual()) {
            return getVisualModel().getMathModel();
        } else {
            return (MathModel) getModel();
        }
    }

    public boolean isVisual() {
        return getModel() instanceof VisualModel;
    }

    public boolean isApplicable(Class<?> cls) {
        return getAs(cls) != null;
    }

    public boolean isApplicableExact(Class<?> cls) {
        boolean result = false;
        final Model model = getModel();
        if (model.getClass() == cls) {
            result = true;
        } else if (isVisual()) {
            final MathModel mathModel = getMathModel();
            result = mathModel.getClass() == cls;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(Class<T> cls) {
        if (cls.isInstance(getModel())) {
            return (T) getModel();
        }
        if (isVisual()) {
            final MathModel mathModel = getMathModel();
            if (cls.isInstance(mathModel)) {
                return (T) mathModel;
            }
        }
        return null;
    }

    public void setStamp(Stamp stamp) {
        this.stamp = stamp;
    }

    public Stamp getStamp() {
        if (stamp == null) {
            String time = TextUtils.getCurrentTimestamp();
            String uuid = UUID.randomUUID().toString();
            stamp = new Stamp(time, uuid);
        }
        return stamp;
    }

    public String getDesiredName() {
        return desiredName;
    }

    public void setDesiredName(String desiredName) {
        this.desiredName = desiredName;
    }

}
