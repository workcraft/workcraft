package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class LogCommonSettings extends AbstractCommonSettings {

    private static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    private static final String prefix = "CommonLogSettings";

    private static final String keyTextColor = prefix + ".textColor";
    private static final String keyInfoBackground = prefix + ".infoBackground";
    private static final String keyWarningBackground = prefix + ".warningBackground";
    private static final String keyErrorBackground = prefix + ".errorBackground";
    private static final String keyStdoutBackground = prefix + ".stdoutBackground";
    private static final String keyStderrBackground = prefix + ".stderrBackground";

    private static final Color defaultTextColor = Color.BLACK;
    private static final Color defaultInfoBackground = new Color(0.7f, 1.0f, 0.7f);
    private static final Color defaultWarningBackground = new Color(1.0f, 0.8f, 0.0f);
    private static final Color defaultErrorBackground = new Color(1.0f, 0.7f, 0.7f);
    private static final Color defaultStdoutBackground = new Color(0.9f, 0.9f, 0.9f);
    private static final Color defaultStderrBackground = new Color(1.0f, 0.9f, 0.9f);

    private static Color textColor = defaultTextColor;
    private static Color infoBackground = defaultInfoBackground;
    private static Color warningBackground = defaultWarningBackground;
    private static Color errorBackground = defaultErrorBackground;
    private static Color stdoutBackground = defaultStdoutBackground;
    private static Color stderrBackground = defaultStderrBackground;

    static {
        properties.add(new PropertyDeclaration<>(Color.class,
                "Text color",
                LogCommonSettings::setTextColor,
                LogCommonSettings::getTextColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Important info background",
                LogCommonSettings::setInfoBackground,
                LogCommonSettings::getInfoBackground));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Warning background",
                LogCommonSettings::setWarningBackground,
                LogCommonSettings::getWarningBackground));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Error background",
                LogCommonSettings::setErrorBackground,
                LogCommonSettings::getErrorBackground));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Backend stdout background",
                LogCommonSettings::setStdoutBackground,
                LogCommonSettings::getStdoutBackground));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Backend stderr background",
                LogCommonSettings::setStderrBackground,
                LogCommonSettings::getStderrBackground));
    }

    @Override
    public List<PropertyDescriptor<?>> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setTextColor(config.getColor(keyTextColor, defaultTextColor));
        setInfoBackground(config.getColor(keyInfoBackground, defaultInfoBackground));
        setWarningBackground(config.getColor(keyWarningBackground, defaultWarningBackground));
        setErrorBackground(config.getColor(keyErrorBackground, defaultErrorBackground));
        setStdoutBackground(config.getColor(keyStdoutBackground, defaultStdoutBackground));
        setStderrBackground(config.getColor(keyStderrBackground, defaultStderrBackground));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyTextColor, getTextColor());
        config.setColor(keyInfoBackground, getInfoBackground());
        config.setColor(keyWarningBackground, getWarningBackground());
        config.setColor(keyErrorBackground, getErrorBackground());
        config.setColor(keyStdoutBackground, getStdoutBackground());
        config.setColor(keyStderrBackground, getStderrBackground());
    }

    @Override
    public String getName() {
        return "Log";
    }

    public static Color getTextColor() {
        return textColor;
    }

    public static void setTextColor(Color value) {
        textColor = new Color(value.getRGB(), false);
    }

    public static Color getInfoBackground() {
        return infoBackground;
    }

    public static void setInfoBackground(Color value) {
        infoBackground = new Color(value.getRGB(), false);
    }

    public static Color getWarningBackground() {
        return warningBackground;
    }

    public static void setWarningBackground(Color value) {
        warningBackground = new Color(value.getRGB(), false);
    }

    public static Color getErrorBackground() {
        return errorBackground;
    }

    public static void setErrorBackground(Color value) {
        errorBackground = new Color(value.getRGB(), false);
    }

    public static Color getStdoutBackground() {
        return stdoutBackground;
    }

    public static void setStdoutBackground(Color value) {
        stdoutBackground = new Color(value.getRGB(), false);
    }

    public static Color getStderrBackground() {
        return stderrBackground;
    }

    public static void setStderrBackground(Color value) {
        stderrBackground = new Color(value.getRGB(), false);
    }

}
