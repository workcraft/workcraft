package org.workcraft.plugins.policy.properties;

import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicyNet;

public final class BundlesOfTransitionPropertyDescriptor implements PropertyDescriptor {
    private final VisualPolicyNet model;
    private final VisualBundledTransition transition;

    public BundlesOfTransitionPropertyDescriptor(VisualPolicyNet model, VisualBundledTransition transition) {
        this.model = model;
        this.transition = transition;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return "Bundles";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        return model.getBundlesOfTransitionAsString(transition);
    }

    @Override
    public void setValue(Object value) {
        model.setBundlesOfTransitionAsString(transition, (String) value);
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isCombinable() {
        return false;
    }

    @Override
    public boolean isTemplatable() {
        return false;
    }

}