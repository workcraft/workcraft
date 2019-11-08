package org.workcraft.plugins.dtd;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractModelSettings;
import org.workcraft.utils.DialogUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class DtdSettings extends AbstractModelSettings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "DtdSettings";

    private static final String keyVerticalSeparation = prefix + ".verticalSeparation";
    private static final String keyTransitionSeparation = prefix + ".transitionSeparation";

    private static final Double defaultVerticalSeparation = 1.0;
    private static final Double defaultTransitionSeparation = 1.0;

    private static Double verticalSeparation = defaultVerticalSeparation;
    private static Double transitionSeparation = defaultTransitionSeparation;

    static {
        properties.add(new PropertyDeclaration<>(Double.class,
                "Vertical separation between signals",
                value -> setVerticalSeparationIfValid(value, DtdSettings::setVerticalSeparation),
                DtdSettings::getVerticalSeparation));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Horizontal separation between transitions",
                value -> setTransitionSeparationIfValid(value, DtdSettings::setTransitionSeparation),
                DtdSettings::getTransitionSeparation));
    }

    private static void setVerticalSeparationIfValid(Double value, Consumer<Double> setter) {
        if (value.isNaN() || value.isInfinite()) {
            DialogUtils.showError("Vertical separation has to be a valid number.");
        } else if (value < 0.5) {
            DialogUtils.showError("Vertical separation has to be at least 0.5.");
        } else {
            setter.accept(value);
        }

    }

    private static void setTransitionSeparationIfValid(Double value, Consumer<Double> setter) {
        if (value.isNaN() || value.isInfinite()) {
            DialogUtils.showError("Transition separation has to be a valid number.");
        } else if (value < 0) {
            DialogUtils.showError("Transition separation cannot be negative.");
        } else {
            setter.accept(value);
        }

    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setVerticalSeparation(config.getDouble(keyVerticalSeparation, defaultVerticalSeparation));
        setTransitionSeparation(config.getDouble(keyTransitionSeparation, defaultTransitionSeparation));
    }

    @Override
    public void save(Config config) {
        config.setDouble(keyVerticalSeparation, getVerticalSeparation());
        config.setDouble(keyTransitionSeparation, getTransitionSeparation());
    }

    @Override
    public String getName() {
        return "Digital Timing Diagram";
    }

    public static Double getVerticalSeparation() {
        return verticalSeparation;
    }

    public static Double getTransitionSeparation() {
        return transitionSeparation;
    }

    public static void setVerticalSeparation(Double value) {
        if ((!value.isNaN()) && (!value.isInfinite())) {
            verticalSeparation = value;
        }
    }

    public static void setTransitionSeparation(Double value) {
        if ((!value.isNaN()) && (!value.isInfinite())) {
            transitionSeparation = value;
        }
    }

}
