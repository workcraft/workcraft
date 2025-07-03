package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class SimulationDecorationSettings extends AbstractDecorationSettings {

    private static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    private static final String prefix = "SimulationDecorationSettings";

    private static final String keyExcitedComponentColor = prefix + ".excitedComponentColor";
    private static final String keySuggestedComponentColor = prefix + ".suggestedComponentColor";

    private static final Color defaultExcitedComponentColor = new Color(1.0f, 0.5f, 0.0f);
    private static final Color defaultSuggestedComponentColor = new Color(0.0f, 1.0f, 0.0f);

    private static Color simulationExcitedComponentColor = defaultExcitedComponentColor;
    private static Color simulationSuggestedComponentColor = defaultSuggestedComponentColor;

    static {
        properties.add(new PropertyDeclaration<>(Color.class,
                "Excited component outline",
                SimulationDecorationSettings::setExcitedComponentColor,
                SimulationDecorationSettings::getExcitedComponentColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Suggested component background",
                SimulationDecorationSettings::setSuggestedComponentColor,
                SimulationDecorationSettings::getSuggestedComponentColor));
    }

    @Override
    public List<PropertyDescriptor<?>> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setExcitedComponentColor(config.getColor(keyExcitedComponentColor, defaultExcitedComponentColor));
        setSuggestedComponentColor(config.getColor(keySuggestedComponentColor, defaultSuggestedComponentColor));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyExcitedComponentColor, getExcitedComponentColor());
        config.setColor(keySuggestedComponentColor, getSuggestedComponentColor());
    }

    @Override
    public String getName() {
        return "Simulation";
    }

    public static void setExcitedComponentColor(Color value) {
        simulationExcitedComponentColor = value;
    }

    public static Color getExcitedComponentColor() {
        return simulationExcitedComponentColor;
    }

    public static void setSuggestedComponentColor(Color value) {
        simulationSuggestedComponentColor = value;
    }

    public static Color getSuggestedComponentColor() {
        return simulationSuggestedComponentColor;
    }

}
