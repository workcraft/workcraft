package org.workcraft.plugins.policy.properties;

import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.policy.VisualBundle;
import org.workcraft.plugins.policy.VisualPolicyNet;

import java.util.Map;

public final class TransitionsOfBundlePropertyDescriptor implements PropertyDescriptor {
    private final VisualPolicyNet model;
    private final VisualBundle bundle;

    public TransitionsOfBundlePropertyDescriptor(VisualPolicyNet model, VisualBundle bundle) {
        this.model = model;
        this.bundle = bundle;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return model.getPolicyNet().getName(bundle.getReferencedBundle()) + " transitions";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        return model.getTransitionsOfBundleAsString(bundle);
    }

    @Override
    public void setValue(Object value) {
        model.setTransitionsOfBundleAsString(bundle, (String) value);
    }

}