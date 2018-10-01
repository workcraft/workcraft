package org.workcraft.plugins.dtd;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.util.DialogUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class DtdSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "DtdSettings";

    private static final String keyVerticalSeparation = prefix + ".verticalSeparation";
    private static final String keyTransitionSeparation = prefix + ".transitionSeparation";

    private static final Double defaultVerticalSeparation = 1.0;
    private static final Double defaultTransitionSeparation = 1.0;

    private static Double verticalSeparation = defaultVerticalSeparation;
    private static Double transitionSeparation = defaultTransitionSeparation;

    public DtdSettings() {
        properties.add(new PropertyDeclaration<DtdSettings, Double>(
                this, "Vertical separation between signals",
                Double.class, true, false, false) {
            protected void setter(DtdSettings object, Double value) {
                setVerticalSeparationIfValid(value, DtdSettings::setVerticalSeparation);
            }

            protected Double getter(DtdSettings object) {
                return getVerticalSeparation();
            }
        });

        properties.add(new PropertyDeclaration<DtdSettings, Double>(
                this, "Horizontal separation between transitions",
                Double.class, true, false, false) {
            protected void setter(DtdSettings object, Double value) {
                setTransitionSeparationIfValid(value, DtdSettings::setTransitionSeparation);
            }

            protected Double getter(DtdSettings object) {
                return getTransitionSeparation();
            }
        });
    }

    private void setVerticalSeparationIfValid(Double value, Consumer<Double> setter) {
        if (value.isNaN() || value.isInfinite()) {
            DialogUtils.showError("Vertical separation has to be a valid number.");
        } else if (value < 0.5) {
            DialogUtils.showError("Vertical separation has to be at least 0.5.");
        } else {
            setter.accept(value);
        }

    }

    private void setTransitionSeparationIfValid(Double value, Consumer<Double> setter) {
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
    public String getSection() {
        return "Models";
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
