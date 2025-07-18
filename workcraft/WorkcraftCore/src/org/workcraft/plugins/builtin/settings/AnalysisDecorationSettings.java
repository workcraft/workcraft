package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class AnalysisDecorationSettings extends AbstractDecorationSettings {

    private static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    private static final String prefix = "AnalysisDecorationSettings";

    private static final String keyDontTouchColor = prefix + ".dontTouchColor";
    private static final String keyProblemColor = prefix + ".problematicColor";
    private static final String keyFixerColor = prefix + ".problemFixerColor";
    private static final String keyClearColor = prefix + ".problemFreeColor";

    private static final Color defaultDontTouchColor = Color.LIGHT_GRAY;
    private static final Color defaultProblemColor = new Color(1.0f, 0.4f, 1.0f);
    private static final Color defaultFixerColor = new Color(1.0f, 0.8f, 0.0f);
    private static final Color defaultClearColor = new Color(0.4f, 1.0f, 0.4f);

    private static Color dontTouchColor = defaultDontTouchColor;
    private static Color problemColor = defaultProblemColor;
    private static Color fixerColor = defaultFixerColor;
    private static Color clearColor = defaultClearColor;

    static {
        properties.add(new PropertyDeclaration<>(Color.class,
                "Don't touch component",
                AnalysisDecorationSettings::setDontTouchColor,
                AnalysisDecorationSettings::getDontTouchColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Problematic component",
                AnalysisDecorationSettings::setProblemColor,
                AnalysisDecorationSettings::getProblemColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Problem fixer component",
                AnalysisDecorationSettings::setFixerColor,
                AnalysisDecorationSettings::getFixerColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Problem-free component",
                AnalysisDecorationSettings::setClearColor,
                AnalysisDecorationSettings::getClearColor));
    }

    @Override
    public List<PropertyDescriptor<?>> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setDontTouchColor(config.getColor(keyDontTouchColor, defaultDontTouchColor));
        setProblemColor(config.getColor(keyProblemColor, defaultProblemColor));
        setFixerColor(config.getColor(keyFixerColor, defaultFixerColor));
        setClearColor(config.getColor(keyClearColor, defaultClearColor));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyDontTouchColor, getDontTouchColor());
        config.setColor(keyProblemColor, getProblemColor());
        config.setColor(keyFixerColor, getFixerColor());
        config.setColor(keyClearColor, getClearColor());
    }

    @Override
    public String getName() {
        return "Analysis";
    }

    public static void setDontTouchColor(Color value) {
        dontTouchColor = value;
    }

    public static Color getDontTouchColor() {
        return dontTouchColor;
    }

    public static void setProblemColor(Color value) {
        problemColor = value;
    }

    public static Color getProblemColor() {
        return problemColor;
    }

    public static void setClearColor(Color value) {
        clearColor = value;
    }

    public static Color getClearColor() {
        return clearColor;
    }

    public static void setFixerColor(Color value) {
        fixerColor = value;
    }

    public static Color getFixerColor() {
        return fixerColor;
    }

}
