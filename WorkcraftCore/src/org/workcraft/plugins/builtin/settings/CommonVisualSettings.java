package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class CommonVisualSettings extends AbstractCommonSettings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonVisualSettings";

    private static final String keyFontSize = prefix + ".fontSize";
    private static final String keyNodeSize = prefix + ".nodeSize";
    private static final String keyStrokeWidth = prefix + ".strokeWidth";
    private static final String keyBorderColor = prefix + ".borderColor";
    private static final String keyFillColor = prefix + ".fillColor";
    private static final String keyPivotSize = prefix + ".pivotSize";
    private static final String keyPivotWidth = prefix + ".pivotWidth";
    private static final String keyLineSpacing = prefix + ".lineSpacing";
    private static final String keyLabelVisibility = prefix + ".labelVisibility";
    private static final String keyLabelPositioning = prefix + ".labelPositioning";
    private static final String keyLabelColor = prefix + ".labelColor";
    private static final String keyNameVisibility = prefix + ".nameVisibility";
    private static final String keyNamePositioning = prefix + ".namePositioning";
    private static final String keyNameColor = prefix + ".nameColor";
    private static final String keyConnectionLineWidth = prefix + ".connectionLineWidth";
    private static final String keyConnectionArrowWidth = prefix + ".connectionArrowWidth";
    private static final String keyConnectionArrowLength = prefix + ".connectionArrowLength";
    private static final String keyConnectionBubbleSize = prefix + ".connectionBubbleSize";
    private static final String keyConnectionColor = prefix + ".connectionColor";
    private static final String keyUseSubscript = prefix + ".useSubscript";

    private static final double defaultFontSize = 10.0;
    private static final double defaultNodeSize = 1.0;
    private static final double defaultStrokeWidth = 0.1;
    private static final Color defaultBorderColor = Color.BLACK;
    private static final Color defaultFillColor = Color.WHITE;
    private static final double defaultPivotSize = 0.2;
    private static final double defaultPivotWidth = 0.02;
    private static final double defaultLineSpacing = 0.3;
    private static final boolean defaultLabelVisibility = true;
    private static final Positioning defaultLabelPositioning = Positioning.TOP;
    private static final Color defaultLabelColor = Color.BLACK;
    private static final boolean defaultNameVisibility = true;
    private static final Positioning defaultNamePositioning = Positioning.BOTTOM;
    private static final Color defaultNameColor = Color.GRAY.darker();
    private static final double defaultConnectionLineWidth = 0.02;
    private static final double defaultConnectionArrowWidth = 0.15;
    private static final double defaultConnectionArrowLength = 0.4;
    private static final double defaultConnectionBubbleSize = 0.2;
    private static final Color defaultConnectionColor = Color.BLACK;
    private static final boolean defaultUseSubscript = false;

    private static double fontSize = defaultFontSize;
    private static double nodeSize = defaultNodeSize;
    private static double strokeWidth = defaultStrokeWidth;
    private static Color borderColor = defaultBorderColor;
    private static Color fillColor = defaultFillColor;
    private static double pivotSize = defaultPivotSize;
    private static double pivotWidth = defaultPivotWidth;
    private static double lineSpacing = defaultLineSpacing;
    private static boolean labelVisibility = defaultLabelVisibility;
    private static Positioning labelPositioning = defaultLabelPositioning;
    private static Color labelColor = defaultLabelColor;
    private static boolean nameVisibility = defaultNameVisibility;
    private static Positioning namePositioning = defaultNamePositioning;
    private static Color nameColor = defaultNameColor;
    private static double connectionLineWidth = defaultConnectionLineWidth;
    private static double connectionArrowWidth = defaultConnectionArrowWidth;
    private static double connectionArrowLength = defaultConnectionArrowLength;
    private static double connectionBubbleSize = defaultConnectionBubbleSize;
    private static Color connectionColor = defaultConnectionColor;
    private static boolean useSubscript = defaultUseSubscript;

    public CommonVisualSettings() {
        addPropertyFontSize();
        addNodeSize();
        addStrokeWidth();
        addBorderColor();
        addFillColor();
        addPivotSize();
        addPivotWidth();
        addLineSpacing();
        addLabelVisibility();
        addLabelPositioning();
        addLabelColor();
        addNameVisibility();
        addNamePositioning();
        addNameColor();
        addConnectionLineWidth();
        addConnectionArrowWidth();
        addConnectionArrowLength();
        addConnectionBubbleSize();
        addConnectionColor();
        addUseSubscript();
    }

    private void addPropertyFontSize() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
                this, "Base font size (point) - requires restart", Double.class) {
            @Override
            public void setter(CommonVisualSettings object, Double value) {
                setFontSize(value);
            }
            @Override
            public Double getter(CommonVisualSettings object) {
                return getFontSize();
            }
        });
    }

    private void addNodeSize() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
                this, "Node size (cm)", Double.class) {
            @Override
            public void setter(CommonVisualSettings object, Double value) {
                setNodeSize(value);
            }
            @Override
            public Double getter(CommonVisualSettings object) {
                return getNodeSize();
            }
        });
    }

    private void addStrokeWidth() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
                this, "Stroke width (cm)", Double.class) {
            @Override
            public void setter(CommonVisualSettings object, Double value) {
                setStrokeWidth(value);
            }
            @Override
            public Double getter(CommonVisualSettings object) {
                return getStrokeWidth();
            }
        });
    }

    private void addBorderColor() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Color>(
                this, "Border color", Color.class) {
            @Override
            public void setter(CommonVisualSettings object, Color value) {
                setBorderColor(value);
            }
            @Override
            public Color getter(CommonVisualSettings object) {
                return getBorderColor();
            }
        });
    }

    private void addFillColor() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Color>(
                this, "Fill color", Color.class) {
            @Override
            public void setter(CommonVisualSettings object, Color value) {
                setFillColor(value);
            }
            @Override
            public Color getter(CommonVisualSettings object) {
                return getFillColor();
            }
        });
    }

    private void addPivotSize() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
                this, "Pivot size (cm)", Double.class) {
            @Override
            public void setter(CommonVisualSettings object, Double value) {
                setPivotSize(value);
            }
            @Override
            public Double getter(CommonVisualSettings object) {
                return getPivotSize();
            }
        });
    }

    private void addPivotWidth() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
                this, "Pivot stroke width (cm)", Double.class) {
            @Override
            public void setter(CommonVisualSettings object, Double value) {
                setPivotWidth(value);
            }
            @Override
            public Double getter(CommonVisualSettings object) {
                return getPivotWidth();
            }
        });
    }

    private void addLineSpacing() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
                this, "Line spacing in multi-line text (ratio)", Double.class) {
            @Override
            public void setter(CommonVisualSettings object, Double value) {
                setLineSpacing(value);
            }
            @Override
            public Double getter(CommonVisualSettings object) {
                return getLineSpacing();
            }
        });
    }

    private void addLabelVisibility() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Boolean>(
                this, "Show labels", Boolean.class) {
            @Override
            public void setter(CommonVisualSettings object, Boolean value) {
                setLabelVisibility(value);
            }
            @Override
            public Boolean getter(CommonVisualSettings object) {
                return getLabelVisibility();
            }
        });
    }

    private void addLabelPositioning() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Positioning>(
                this, "Label positioning", Positioning.class) {
            @Override
            public void setter(CommonVisualSettings object, Positioning value) {
                setLabelPositioning(value);
            }
            @Override
            public Positioning getter(CommonVisualSettings object) {
                return getLabelPositioning();
            }
        });
    }

    private void addLabelColor() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Color>(
                this, "Label color", Color.class) {
            @Override
            public void setter(CommonVisualSettings object, Color value) {
                setLabelColor(value);
            }
            @Override
            public Color getter(CommonVisualSettings object) {
                return getLabelColor();
            }
        });
    }

    private void addNameVisibility() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Boolean>(
                this, "Show names", Boolean.class) {
            @Override
            public void setter(CommonVisualSettings object, Boolean value) {
                setNameVisibility(value);
            }
            @Override
            public Boolean getter(CommonVisualSettings object) {
                return getNameVisibility();
            }
        });
    }

    private void addNamePositioning() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Positioning>(
                this, "Name positioning", Positioning.class) {
            @Override
            public void setter(CommonVisualSettings object, Positioning value) {
                setNamePositioning(value);
            }
            @Override
            public Positioning getter(CommonVisualSettings object) {
                return getNamePositioning();
            }
        });
    }

    private void addNameColor() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Color>(
                this, "Name color", Color.class) {
            @Override
            public void setter(CommonVisualSettings object, Color value) {
                setNameColor(value);
            }
            @Override
            public Color getter(CommonVisualSettings object) {
                return getNameColor();
            }
        });
    }

    private void addConnectionLineWidth() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
                this, "Connection line width (cm)", Double.class) {
            @Override
            public void setter(CommonVisualSettings object, Double value) {
                setConnectionLineWidth(value);
            }
            @Override
            public Double getter(CommonVisualSettings object) {
                return getConnectionLineWidth();
            }
        });
    }

    private void addConnectionArrowWidth() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
                this, "Connection arrow width (cm)", Double.class) {
            @Override
            public void setter(CommonVisualSettings object, Double value) {
                setConnectionArrowWidth(value);
            }
            @Override
            public Double getter(CommonVisualSettings object) {
                return getConnectionArrowWidth();
            }
        });
    }

    private void addConnectionArrowLength() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
                this, "Connection arrow length (cm)", Double.class) {
            @Override
            public void setter(CommonVisualSettings object, Double value) {
                setConnectionArrowLength(value);
            }
            @Override
            public Double getter(CommonVisualSettings object) {
                return getConnectionArrowLength();
            }
        });
    }

    private void addConnectionBubbleSize() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
                this, "Connection bubble size (cm)", Double.class) {
            @Override
            public void setter(CommonVisualSettings object, Double value) {
                setConnectionBubbleSize(value);
            }
            @Override
            public Double getter(CommonVisualSettings object) {
                return getConnectionBubbleSize();
            }
        });
    }

    private void addConnectionColor() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Color>(
                this, "Connection color", Color.class) {
            @Override
            public void setter(CommonVisualSettings object, Color value) {
                setConnectionColor(value);
            }
            @Override
            public Color getter(CommonVisualSettings object) {
                return getConnectionColor();
            }
        });
    }

    private void addUseSubscript() {
        properties.add(new PropertyDeclaration<CommonVisualSettings, Boolean>(
                this, "In Boolean expressions render text after \'_\' as subscript", Boolean.class) {
            @Override
            public void setter(CommonVisualSettings object, Boolean value) {
                setUseSubscript(value);
            }
            @Override
            public Boolean getter(CommonVisualSettings object) {
                return getUseSubscript();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setFontSize(config.getDouble(keyFontSize, defaultFontSize));
        setNodeSize(config.getDouble(keyNodeSize, defaultNodeSize));
        setStrokeWidth(config.getDouble(keyStrokeWidth, defaultStrokeWidth));
        setBorderColor(config.getColor(keyBorderColor, defaultBorderColor));
        setFillColor(config.getColor(keyFillColor, defaultFillColor));
        setPivotSize(config.getDouble(keyPivotSize, defaultPivotSize));
        setPivotWidth(config.getDouble(keyPivotWidth, defaultPivotWidth));
        setLineSpacing(config.getDouble(keyLineSpacing, defaultLineSpacing));
        setLabelVisibility(config.getBoolean(keyLabelVisibility, defaultLabelVisibility));
        setLabelPositioning(config.getEnum(keyLabelPositioning, Positioning.class, defaultLabelPositioning));
        setLabelColor(config.getColor(keyLabelColor, defaultLabelColor));
        setNameVisibility(config.getBoolean(keyNameVisibility, defaultNameVisibility));
        setNamePositioning(config.getEnum(keyNamePositioning, Positioning.class, defaultNamePositioning));
        setNameColor(config.getColor(keyNameColor, defaultNameColor));
        setConnectionLineWidth(config.getDouble(keyConnectionLineWidth, defaultConnectionLineWidth));
        setConnectionArrowWidth(config.getDouble(keyConnectionArrowWidth, defaultConnectionArrowWidth));
        setConnectionArrowLength(config.getDouble(keyConnectionArrowLength, defaultConnectionArrowLength));
        setConnectionBubbleSize(config.getDouble(keyConnectionBubbleSize, defaultConnectionBubbleSize));
        setConnectionColor(config.getColor(keyConnectionColor, defaultConnectionColor));
        setUseSubscript(config.getBoolean(keyUseSubscript, defaultUseSubscript));
    }

    @Override
    public void save(Config config) {
        config.setDouble(keyFontSize, getFontSize());
        config.setDouble(keyNodeSize, getNodeSize());
        config.setDouble(keyStrokeWidth, getStrokeWidth());
        config.setColor(keyBorderColor, getBorderColor());
        config.setColor(keyFillColor, getFillColor());
        config.setDouble(keyPivotSize, getPivotSize());
        config.setDouble(keyPivotWidth, getPivotWidth());
        config.setDouble(keyLineSpacing, getLineSpacing());
        config.setBoolean(keyLabelVisibility, getLabelVisibility());
        config.setEnum(keyLabelPositioning, getLabelPositioning());
        config.setColor(keyLabelColor, getLabelColor());
        config.setBoolean(keyNameVisibility, getNameVisibility());
        config.setColor(keyNameColor, getNameColor());
        config.setEnum(keyNamePositioning, getNamePositioning());
        config.setDouble(keyConnectionLineWidth, getConnectionLineWidth());
        config.setDouble(keyConnectionArrowWidth, getConnectionArrowWidth());
        config.setDouble(keyConnectionArrowLength, getConnectionArrowLength());
        config.setDouble(keyConnectionBubbleSize, getConnectionBubbleSize());
        config.setColor(keyConnectionColor, getConnectionColor());
        config.setBoolean(keyUseSubscript, getUseSubscript());
    }

    @Override
    public String getName() {
        return "Visual";
    }

    public static double getFontSize() {
        return fontSize;
    }

    public static void setFontSize(double value) {
        fontSize = value;
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

    public static boolean getUseSubscript() {
        return useSubscript;
    }

    public static void setUseSubscript(boolean value) {
        useSubscript = value;
    }

}
