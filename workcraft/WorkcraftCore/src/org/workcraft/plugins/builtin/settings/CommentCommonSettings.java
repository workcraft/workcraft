package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.dom.visual.Alignment;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class CommentCommonSettings extends AbstractCommonSettings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonCommentSettings";

    private static final String keyBaseSize = prefix + ".baseSize";
    private static final String keyStrokeWidth = prefix + ".strokeWidth";
    private static final String keyTextAlignment = prefix + ".textAlignment";
    private static final String keyTextColor = prefix + ".textColor";
    private static final String keyBorderColor = prefix + ".borderColor";
    private static final String keyFillColor = prefix + ".fillColor";
    private static final String keyFontSize = prefix + ".fontSize";

    private static final double defaultBaseSize = 1.0;
    private static final double defaultStrokeWidth = 0.02;
    private static Alignment defaultTextAlignment = Alignment.CENTER;
    private static final Color defaultTextColor = Color.BLACK;
    private static final Color defaultBorderColor = Color.GRAY;
    private static final Color defaultFillColor = new Color(255, 255, 200);
    private static final double defaultFontSize = 0.5f;

    private static double baseSize = defaultBaseSize;
    private static double strokeWidth = defaultStrokeWidth;
    private static Alignment textAlignment = defaultTextAlignment;
    private static Color textColor = defaultTextColor;
    private static Color borderColor = defaultBorderColor;
    private static Color fillColor = defaultFillColor;
    private static double fontSize = defaultFontSize;

    static {
        properties.add(new PropertyDeclaration<>(Double.class,
                "Base size (cm)",
                CommentCommonSettings::setBaseSize,
                CommentCommonSettings::getBaseSize));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Stroke width (cm)",
                CommentCommonSettings::setStrokeWidth,
                CommentCommonSettings::getStrokeWidth));

        properties.add(new PropertyDeclaration<>(Alignment.class,
                "Text alignment",
                CommentCommonSettings::setTextAlignment,
                CommentCommonSettings::getTextAlignment));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Text color",
                CommentCommonSettings::setTextColor,
                CommentCommonSettings::getTextColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Border color",
                CommentCommonSettings::setBorderColor,
                CommentCommonSettings::getBorderColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Fill color",
                CommentCommonSettings::setFillColor,
                CommentCommonSettings::getFillColor));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Font size (cm)",
                CommentCommonSettings::setFontSize,
                CommentCommonSettings::getFontSize));
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setBaseSize(config.getDouble(keyBaseSize, defaultBaseSize));
        setStrokeWidth(config.getDouble(keyStrokeWidth, defaultStrokeWidth));
        setTextAlignment(config.getEnum(keyTextAlignment, Alignment.class, defaultTextAlignment));
        setTextColor(config.getColor(keyTextColor, defaultTextColor));
        setBorderColor(config.getColor(keyBorderColor, defaultBorderColor));
        setFillColor(config.getColor(keyFillColor, defaultFillColor));
        setFontSize(config.getDouble(keyFontSize, defaultFontSize));
    }

    @Override
    public void save(Config config) {
        config.setDouble(keyBaseSize, getBaseSize());
        config.setDouble(keyStrokeWidth, getStrokeWidth());
        config.setEnum(keyTextAlignment, getTextAlignment());
        config.setColor(keyTextColor, getTextColor());
        config.setColor(keyBorderColor, getBorderColor());
        config.setColor(keyFillColor, getFillColor());
        config.setDouble(keyFontSize, getFontSize());
    }

    @Override
    public String getName() {
        return "Comment";
    }

    public static double getBaseSize() {
        return baseSize;
    }

    public static void setBaseSize(double value) {
        baseSize = value;
    }

    public static double getStrokeWidth() {
        return strokeWidth;
    }

    public static void setStrokeWidth(double value) {
        strokeWidth = value;
    }

    public static Alignment getTextAlignment() {
        return textAlignment;
    }

    public static void setTextAlignment(Alignment value) {
        textAlignment = value;
    }

    public static Color getTextColor() {
        return textColor;
    }

    public static void setTextColor(Color value) {
        textColor = value;
    }

    public static Color getBorderColor() {
        return borderColor;
    }

    public static void setBorderColor(Color value) {
        borderColor = value;
    }

    public static Color getFillColor() {
        return fillColor;
    }

    public static void setFillColor(Color value) {
        fillColor = value;
    }

    public static double getFontSize() {
        return fontSize;
    }

    public static void setFontSize(double value) {
        fontSize = value;
    }

}
