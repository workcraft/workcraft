package org.workcraft.plugins.policy.properties;

import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.policy.VisualBundle;
import org.workcraft.plugins.policy.VisualPolicyNet;

import java.util.Map;

public final class BundleNamePropertyDescriptor implements PropertyDescriptor {
    private final VisualPolicyNet model;
    private final VisualBundle bundle;

    public BundleNamePropertyDescriptor(VisualPolicyNet model, VisualBundle bundle) {
        this.model = model;
        this.bundle = bundle;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return model.getPolicyNet().getName(bundle.getReferencedBundle()) + " name";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        return model.getPolicyNet().getName(bundle.getReferencedBundle());
    }

    @Override
    public void setValue(Object value) {
        model.getPolicyNet().setName(bundle.getReferencedBundle(), (String) value);
    }

}