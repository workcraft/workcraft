package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.dom.visual.Alignment;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VisualCommonSettings extends AbstractCommonSettings {

    private static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    private static final String prefix = "CommonVisualSettings";

    /*
     * Keys
     */
    // Node
    private static final String keyNodeSize = prefix + ".nodeSize";
    private static final String keyStrokeWidth = prefix + ".strokeWidth";
    private static final String keyBorderColor = prefix + ".borderColor";
    private static final String keyFillColor = prefix + ".fillColor";
    // Label
    private static final String keyLineSpacing = prefix + ".lineSpacing";
    private static final String keyLabelVisibility = prefix + ".labelVisibility";
    private static final String keyLabelPositioning = prefix + ".labelPositioning";
    private static final String keyLabelColor = prefix + ".labelColor";
    private static final String keyLabelFontSize = prefix + ".labelFontSize";
    // Name
    private static final String keyNameVisibility = prefix + ".nameVisibility";
    private static final String keyNamePositioning = prefix + ".namePositioning";
    private static final String keyNameColor = prefix + ".nameColor";
    private static final String keyNameFontSize = prefix + ".nameFontSize";
    private static final String keyShowAbsolutePaths = prefix + ".showAbsolutePaths";
    // Connection
    private static final String keyConnectionLineWidth = prefix + ".connectionLineWidth";
    private static final String keyConnectionArrowWidth = prefix + ".connectionArrowWidth";
    private static final String keyConnectionArrowLength = prefix + ".connectionArrowLength";
    private static final String keyConnectionBubbleSize = prefix + ".connectionBubbleSize";
    private static final String keyConnectionColor = prefix + ".connectionColor";
    // Comment
    private static final String keyCommentBaseSize = prefix + ".commentBaseSize";
    private static final String keyCommentStrokeWidth = prefix + ".commentStrokeWidth";
    private static final String keyCommentTextAlignment = prefix + ".commentTextAlignment";
    private static final String keyCommentTextColor = prefix + ".commentTextColor";
    private static final String keyCommentBorderColor = prefix + ".commentBorderColor";
    private static final String keyCommentFillColor = prefix + ".commentFillColor";
    private static final String keyCommentFontSize = prefix + ".commentFontSize";
    // Pivot
    private static final String keyPivotSize = prefix + ".pivotSize";
    private static final String keyPivotWidth = prefix + ".pivotWidth";
    // Expression
    private static final String keyUseSubscript = prefix + ".useSubscript";

    /*
     * Defaults
     */
    // Node
    private static final double defaultNodeSize = 1.0;
    private static final double defaultStrokeWidth = 0.1;
    private static final Color defaultBorderColor = Color.BLACK;
    private static final Color defaultFillColor = Color.WHITE;
    // Label
    private static final boolean defaultLabelVisibility = true;
    private static final Positioning defaultLabelPositioning = Positioning.TOP;
    private static final Color defaultLabelColor = Color.BLACK;
    private static final double defaultLabelFontSize = 0.5f;
    private static final double defaultLineSpacing = 0.3;
    // Name
    private static final boolean defaultNameVisibility = true;
    private static final Positioning defaultNamePositioning = Positioning.BOTTOM;
    private static final Color defaultNameColor = Color.GRAY.darker();
    private static final double defaultNameFontSize = 0.5f;
    private static final boolean defaultShowAbsolutePaths = false;
    // Connection
    private static final double defaultConnectionLineWidth = 0.02;
    private static final double defaultConnectionArrowWidth = 0.15;
    private static final double defaultConnectionArrowLength = 0.4;
    private static final double defaultConnectionBubbleSize = 0.2;
    private static final Color defaultConnectionColor = Color.BLACK;
    // Comment
    private static final double defaultCommentBaseSize = 1.0;
    private static final double defaultCommentStrokeWidth = 0.02;
    private static final Alignment defaultCommentTextAlignment = Alignment.CENTER;
    private static final Color defaultCommentTextColor = Color.BLACK;
    private static final Color defaultCommentBorderColor = Color.GRAY;
    private static final Color defaultCommentFillColor = new Color(255, 255, 200);
    private static final double defaultCommentFontSize = 0.5f;
    // Pivot
    private static final double defaultPivotSize = 0.2;
    private static final double defaultPivotWidth = 0.02;
    // Expression
    private static final boolean defaultUseSubscript = false;

    /*
     * Variables
     */
    // Node
    private static double nodeSize = defaultNodeSize;
    private static double strokeWidth = defaultStrokeWidth;
    private static Color borderColor = defaultBorderColor;
    private static Color fillColor = defaultFillColor;
    // Label
    private static boolean labelVisibility = defaultLabelVisibility;
    private static Positioning labelPositioning = defaultLabelPositioning;
    private static Color labelColor = defaultLabelColor;
    private static double labelFontSize = defaultLabelFontSize;
    private static double lineSpacing = defaultLineSpacing;
    // Name
    private static boolean nameVisibility = defaultNameVisibility;
    private static Positioning namePositioning = defaultNamePositioning;
    private static Color nameColor = defaultNameColor;
    private static double nameFontSize = defaultNameFontSize;
    private static boolean showAbsolutePaths = defaultShowAbsolutePaths;
    // Connection
    private static double connectionLineWidth = defaultConnectionLineWidth;
    private static double connectionArrowWidth = defaultConnectionArrowWidth;
    private static double connectionArrowLength = defaultConnectionArrowLength;
    private static double connectionBubbleSize = defaultConnectionBubbleSize;
    private static Color connectionColor = defaultConnectionColor;
    // Comment
    private static double commentBaseSize = defaultCommentBaseSize;
    private static double commentStrokeWidth = defaultCommentStrokeWidth;
    private static Alignment commentTextAlignment = defaultCommentTextAlignment;
    private static Color commentTextColor = defaultCommentTextColor;
    private static Color commentBorderColor = defaultCommentBorderColor;
    private static Color commentFillColor = defaultCommentFillColor;
    private static double commentFontSize = defaultCommentFontSize;
    // Pivot
    private static double pivotSize = defaultPivotSize;
    private static double pivotWidth = defaultPivotWidth;
    // Expression
    private static boolean useSubscript = defaultUseSubscript;

    static {
        properties.add(PropertyHelper.createSeparatorProperty("Node"));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Size (cm)"),
                VisualCommonSettings::setNodeSize,
                VisualCommonSettings::getNodeSize));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Stroke width (cm)"),
                VisualCommonSettings::setStrokeWidth,
                VisualCommonSettings::getStrokeWidth));

        properties.add(new PropertyDeclaration<>(Color.class,
                PropertyHelper.indentWithBullet("Border color"),
                VisualCommonSettings::setBorderColor,
                VisualCommonSettings::getBorderColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                PropertyHelper.indentWithBullet("Fill color"),
                VisualCommonSettings::setFillColor,
                VisualCommonSettings::getFillColor));

        properties.add(PropertyHelper.createSeparatorProperty("Label"));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.indentWithBullet("Visible"),
                VisualCommonSettings::setLabelVisibility,
                VisualCommonSettings::getLabelVisibility));

        properties.add(new PropertyDeclaration<>(Positioning.class,
                PropertyHelper.indentWithBullet("Positioning"),
                VisualCommonSettings::setLabelPositioning,
                VisualCommonSettings::getLabelPositioning));

        properties.add(new PropertyDeclaration<>(Color.class,
                PropertyHelper.indentWithBullet("Color"),
                VisualCommonSettings::setLabelColor,
                VisualCommonSettings::getLabelColor));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Font size (cm)"),
                VisualCommonSettings::setLabelFontSize,
                VisualCommonSettings::getLabelFontSize));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Line spacing in multi-line text (ratio)"),
                VisualCommonSettings::setLineSpacing,
                VisualCommonSettings::getLineSpacing));

        properties.add(PropertyHelper.createSeparatorProperty("Name"));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.indentWithBullet("Visible"),
                VisualCommonSettings::setNameVisibility,
                VisualCommonSettings::getNameVisibility));

        properties.add(new PropertyDeclaration<>(Positioning.class,
                PropertyHelper.indentWithBullet("Positioning"),
                VisualCommonSettings::setNamePositioning,
                VisualCommonSettings::getNamePositioning));

        properties.add(new PropertyDeclaration<>(Color.class,
                PropertyHelper.indentWithBullet("Color"),
                VisualCommonSettings::setNameColor,
                VisualCommonSettings::getNameColor));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Font size (cm)"),
                VisualCommonSettings::setNameFontSize,
                VisualCommonSettings::getNameFontSize));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.indentWithBullet("Show hierarchy as absolute paths"),
                VisualCommonSettings::setShowAbsolutePaths,
                VisualCommonSettings::getShowAbsolutePaths));

        properties.add(PropertyHelper.createSeparatorProperty("Connection"));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Line width (cm)"),
                VisualCommonSettings::setConnectionLineWidth,
                VisualCommonSettings::getConnectionLineWidth));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Arrow width (cm)"),
                VisualCommonSettings::setConnectionArrowWidth,
                VisualCommonSettings::getConnectionArrowWidth));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Arrow length (cm)"),
                VisualCommonSettings::setConnectionArrowLength,
                VisualCommonSettings::getConnectionArrowLength) {
            @Override
            public Map<Double, String> getChoice() {
                return VisualConnection.PREDEFINED_ARROW_LENGTHS;
            }
        });

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Bubble size (cm)"),
                VisualCommonSettings::setConnectionBubbleSize,
                VisualCommonSettings::getConnectionBubbleSize));

        properties.add(new PropertyDeclaration<>(Color.class,
                PropertyHelper.indentWithBullet("Color"),
                VisualCommonSettings::setConnectionColor,
                VisualCommonSettings::getConnectionColor));

        properties.add(PropertyHelper.createSeparatorProperty("Comment"));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Base size (cm)"),
                VisualCommonSettings::setCommentBaseSize,
                VisualCommonSettings::getCommentBaseSize));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Stroke width (cm)"),
                VisualCommonSettings::setCommentStrokeWidth,
                VisualCommonSettings::getCommentStrokeWidth));

        properties.add(new PropertyDeclaration<>(Alignment.class,
                PropertyHelper.indentWithBullet("Text alignment"),
                VisualCommonSettings::setCommentTextAlignment,
                VisualCommonSettings::getCommentTextAlignment));

        properties.add(new PropertyDeclaration<>(Color.class,
                PropertyHelper.indentWithBullet("Text color"),
                VisualCommonSettings::setCommentTextColor,
                VisualCommonSettings::getCommentTextColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                PropertyHelper.indentWithBullet("Border color"),
                VisualCommonSettings::setCommentBorderColor,
                VisualCommonSettings::getCommentBorderColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                PropertyHelper.indentWithBullet("Fill color"),
                VisualCommonSettings::setCommentFillColor,
                VisualCommonSettings::getCommentFillColor));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Font size (cm)"),
                VisualCommonSettings::setCommentFontSize,
                VisualCommonSettings::getCommentFontSize));

        properties.add(PropertyHelper.createSeparatorProperty("Pivot"));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Size (cm)"),
                VisualCommonSettings::setPivotSize,
                VisualCommonSettings::getPivotSize));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.indentWithBullet("Stroke width (cm)"),
                VisualCommonSettings::setPivotWidth,
                VisualCommonSettings::getPivotWidth));

        properties.add(PropertyHelper.createSeparatorProperty("Expression"));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.indentWithBullet("Render text after '_' as subscript"),
                VisualCommonSettings::setUseSubscript,
                VisualCommonSettings::getUseSubscript));
    }

    @Override
    public List<PropertyDescriptor<?>> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        // Node
        setNodeSize(config.getDouble(keyNodeSize, defaultNodeSize));
        setStrokeWidth(config.getDouble(keyStrokeWidth, defaultStrokeWidth));
        setBorderColor(config.getColor(keyBorderColor, defaultBorderColor));
        setFillColor(config.getColor(keyFillColor, defaultFillColor));
        // Label
        setLabelVisibility(config.getBoolean(keyLabelVisibility, defaultLabelVisibility));
        setLabelPositioning(config.getEnum(keyLabelPositioning, Positioning.class, defaultLabelPositioning));
        setLabelColor(config.getColor(keyLabelColor, defaultLabelColor));
        setLabelFontSize(config.getDouble(keyLabelFontSize, defaultLabelFontSize));
        setLineSpacing(config.getDouble(keyLineSpacing, defaultLineSpacing));
        // Name
        setNameVisibility(config.getBoolean(keyNameVisibility, defaultNameVisibility));
        setNamePositioning(config.getEnum(keyNamePositioning, Positioning.class, defaultNamePositioning));
        setNameColor(config.getColor(keyNameColor, defaultNameColor));
        setNameFontSize(config.getDouble(keyNameFontSize, defaultNameFontSize));
        setShowAbsolutePaths(config.getBoolean(keyShowAbsolutePaths, defaultShowAbsolutePaths));
        // Connection
        setConnectionLineWidth(config.getDouble(keyConnectionLineWidth, defaultConnectionLineWidth));
        setConnectionArrowWidth(config.getDouble(keyConnectionArrowWidth, defaultConnectionArrowWidth));
        setConnectionArrowLength(config.getDouble(keyConnectionArrowLength, defaultConnectionArrowLength));
        setConnectionBubbleSize(config.getDouble(keyConnectionBubbleSize, defaultConnectionBubbleSize));
        setConnectionColor(config.getColor(keyConnectionColor, defaultConnectionColor));
        // Comment
        setCommentBaseSize(config.getDouble(keyCommentBaseSize, defaultCommentBaseSize));
        setCommentStrokeWidth(config.getDouble(keyCommentStrokeWidth, defaultCommentStrokeWidth));
        setCommentTextAlignment(config.getEnum(keyCommentTextAlignment, Alignment.class, defaultCommentTextAlignment));
        setCommentTextColor(config.getColor(keyCommentTextColor, defaultCommentTextColor));
        setCommentBorderColor(config.getColor(keyCommentBorderColor, defaultCommentBorderColor));
        setCommentFillColor(config.getColor(keyCommentFillColor, defaultCommentFillColor));
        setCommentFontSize(config.getDouble(keyCommentFontSize, defaultCommentFontSize));
        // Pivot
        setPivotSize(config.getDouble(keyPivotSize, defaultPivotSize));
        setPivotWidth(config.getDouble(keyPivotWidth, defaultPivotWidth));
        // Expression
        setUseSubscript(config.getBoolean(keyUseSubscript, defaultUseSubscript));
    }

    @Override
    public void save(Config config) {
        // Node
        config.setDouble(keyNodeSize, getNodeSize());
        config.setDouble(keyStrokeWidth, getStrokeWidth());
        config.setColor(keyBorderColor, getBorderColor());
        config.setColor(keyFillColor, getFillColor());
        // Label
        config.setBoolean(keyLabelVisibility, getLabelVisibility());
        config.setEnum(keyLabelPositioning, getLabelPositioning());
        config.setColor(keyLabelColor, getLabelColor());
        config.setDouble(keyLabelFontSize, getLabelFontSize());
        config.setDouble(keyLineSpacing, getLineSpacing());
        // Name
        config.setBoolean(keyNameVisibility, getNameVisibility());
        config.setEnum(keyNamePositioning, getNamePositioning());
        config.setColor(keyNameColor, getNameColor());
        config.setDouble(keyNameFontSize, getNameFontSize());
        config.setBoolean(keyShowAbsolutePaths, getShowAbsolutePaths());
        // Connection
        config.setDouble(keyConnectionLineWidth, getConnectionLineWidth());
        config.setDouble(keyConnectionArrowWidth, getConnectionArrowWidth());
        config.setDouble(keyConnectionArrowLength, getConnectionArrowLength());
        config.setDouble(keyConnectionBubbleSize, getConnectionBubbleSize());
        config.setColor(keyConnectionColor, getConnectionColor());
        // Comment
        config.setDouble(keyCommentBaseSize, getCommentBaseSize());
        config.setDouble(keyCommentStrokeWidth, getCommentStrokeWidth());
        config.setEnum(keyCommentTextAlignment, getCommentTextAlignment());
        config.setColor(keyCommentTextColor, getCommentTextColor());
        config.setColor(keyCommentBorderColor, getCommentBorderColor());
        config.setColor(keyCommentFillColor, getCommentFillColor());
        config.setDouble(keyCommentFontSize, getCommentFontSize());
        // Pivot
        config.setDouble(keyPivotSize, getPivotSize());
        config.setDouble(keyPivotWidth, getPivotWidth());
        // Expression
        config.setBoolean(keyUseSubscript, getUseSubscript());
    }

    @Override
    public String getName() {
        return "Visual";
    }

    public static double getNodeSize() {
        return nodeSize;
    }

    public static void setNodeSize(double value) {
        nodeSize = value;
    }

    public static double getStrokeWidth() {
        return strokeWidth;
    }

    public static void setStrokeWidth(double value) {
        strokeWidth = value;
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

    public static double getLineSpacing() {
        return lineSpacing;
    }

    public static void setLineSpacing(double value) {
        lineSpacing = value;
    }

    public static boolean getLabelVisibility() {
        return labelVisibility;
    }

    public static void setLabelVisibility(boolean value) {
        labelVisibility = value;
    }

    public static Positioning getLabelPositioning() {
        return labelPositioning;
    }

    public static void setLabelPositioning(Positioning value) {
        labelPositioning = value;
    }

    public static Color getLabelColor() {
        return labelColor;
    }

    public static void setLabelColor(Color value) {
        labelColor = value;
    }

    public static double getLabelFontSize() {
        return labelFontSize;
    }

    public static void setLabelFontSize(double value) {
        labelFontSize = value;
    }

    public static boolean getNameVisibility() {
        return nameVisibility;
    }

    public static void setNameVisibility(boolean value) {
        nameVisibility = value;
    }

    public static Positioning getNamePositioning() {
        return namePositioning;
    }

    public static void setNamePositioning(Positioning value) {
        namePositioning = value;
    }

    public static Color getNameColor() {
        return nameColor;
    }

    public static void setNameColor(Color value) {
        nameColor = value;
    }

    public static double getNameFontSize() {
        return nameFontSize;
    }

    public static void setShowAbsolutePaths(boolean value) {
        showAbsolutePaths = value;
    }

    public static boolean getShowAbsolutePaths() {
        return showAbsolutePaths;
    }

    public static void setNameFontSize(double value) {
        nameFontSize = value;
    }

    public static double getConnectionLineWidth() {
        return connectionLineWidth;
    }

    public static void setConnectionLineWidth(double value) {
        connectionLineWidth = value;
    }

    public static double getConnectionArrowWidth() {
        return connectionArrowWidth;
    }

    public static void setConnectionArrowWidth(double value) {
        connectionArrowWidth = value;
    }

    public static double getConnectionArrowLength() {
        return connectionArrowLength;
    }

    public static void setConnectionArrowLength(double value) {
        connectionArrowLength = value;
    }

    public static double getConnectionBubbleSize() {
        return connectionBubbleSize;
    }

    public static void setConnectionBubbleSize(double value) {
        connectionBubbleSize = value;
    }

    public static Color getConnectionColor() {
        return connectionColor;
    }

    public static void setConnectionColor(Color value) {
        connectionColor = value;
    }

    public static double getCommentBaseSize() {
        return commentBaseSize;
    }

    public static void setCommentBaseSize(double value) {
        commentBaseSize = value;
    }

    public static double getCommentStrokeWidth() {
        return commentStrokeWidth;
    }

    public static void setCommentStrokeWidth(double value) {
        commentStrokeWidth = value;
    }

    public static Alignment getCommentTextAlignment() {
        return commentTextAlignment;
    }

    public static void setCommentTextAlignment(Alignment value) {
        commentTextAlignment = value;
    }

    public static Color getCommentTextColor() {
        return commentTextColor;
    }

    public static void setCommentTextColor(Color value) {
        commentTextColor = value;
    }

    public static Color getCommentBorderColor() {
        return commentBorderColor;
    }

    public static void setCommentBorderColor(Color value) {
        commentBorderColor = value;
    }

    public static Color getCommentFillColor() {
        return commentFillColor;
    }

    public static void setCommentFillColor(Color value) {
        commentFillColor = value;
    }

    public static double getCommentFontSize() {
        return commentFontSize;
    }

    public static void setCommentFontSize(double value) {
        commentFontSize = value;
    }

    public static double getPivotSize() {
        return pivotSize;
    }

    public static void setPivotSize(double value) {
        pivotSize = value;
    }

    public static double getPivotWidth() {
        return pivotWidth;
    }

    public static void setPivotWidth(double value) {
        pivotWidth = value;
    }

    public static boolean getUseSubscript() {
        return useSubscript;
    }

    public static void setUseSubscript(boolean value) {
        useSubscript = value;
    }

}
