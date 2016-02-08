/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.shared;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.dom.visual.Alignment;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class CommonCommentSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
    private static final String prefix = "CommonCommentSettings";

    private static final String keyBaseSize  = prefix + ".baseSize";
    private static final String keyStrokeWidth  = prefix + ".strokeWidth";
    private static final String keyTextAlignment = prefix + ".textAlignment";
    private static final String keyTextColor  = prefix + ".textColor";
    private static final String keyBorderColor  = prefix + ".borderColor";
    private static final String keyFillColor  = prefix + ".fillColor";

    private static final double defaultBaseSize = 1.0;
    private static final double defaultStrokeWidth = 0.02;
    private static Alignment defaultTextAlignment = Alignment.CENTER;
    private static final Color defaultTextColor = Color.BLACK;
    private static final Color defaultBorderColor = Color.GRAY;
    private static final Color defaultFillColor = new Color(255, 255, 200);

    private static double baseSize = defaultBaseSize;
    private static double strokeWidth = defaultStrokeWidth;
    private static Alignment textAlignment = defaultTextAlignment;
    private static Color textColor = defaultTextColor;
    private static Color borderColor = defaultBorderColor;
    private static Color fillColor = defaultFillColor;

    public CommonCommentSettings() {
        properties.add(new PropertyDeclaration<CommonCommentSettings, Double>(
                this, "Base size (cm)", Double.class, true, false, false) {
            protected void setter(CommonCommentSettings object, Double value) {
                setBaseSize(value);
            }
            protected Double getter(CommonCommentSettings object) {
                return getBaseSize();
            }
        });

        properties.add(new PropertyDeclaration<CommonCommentSettings, Double>(
                this, "Stroke width (cm)", Double.class, true, false, false) {
            protected void setter(CommonCommentSettings object, Double value) {
                setStrokeWidth(value);
            }
            protected Double getter(CommonCommentSettings object) {
                return getStrokeWidth();
            }
        });

        properties.add(new PropertyDeclaration<CommonCommentSettings, Alignment>(
                this, "Text alignment", Alignment.class, true, false, false) {
            protected void setter(CommonCommentSettings object, Alignment value) {
                setTextAlignment(value);
            }
            protected Alignment getter(CommonCommentSettings object) {
                return getTextAlignment();
            }
        });

        properties.add(new PropertyDeclaration<CommonCommentSettings, Color>(
                this, "Text color", Color.class, true, false, false) {
            protected void setter(CommonCommentSettings object, Color value) {
                setTextColor(value);
            }
            protected Color getter(CommonCommentSettings object) {
                return getTextColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonCommentSettings, Color>(
                this, "Border color", Color.class, true, false, false) {
            protected void setter(CommonCommentSettings object, Color value) {
                setBorderColor(value);
            }
            protected Color getter(CommonCommentSettings object) {
                return getBorderColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonCommentSettings, Color>(
                this, "Fill color", Color.class, true, false, false) {
            protected void setter(CommonCommentSettings object, Color value) {
                setFillColor(value);
            }
            protected Color getter(CommonCommentSettings object) {
                return getFillColor();
            }
        });
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
    }

    @Override
    public void save(Config config) {
        config.setDouble(keyBaseSize, getBaseSize());
        config.setDouble(keyStrokeWidth, getStrokeWidth());
        config.setEnum(keyTextAlignment, Alignment.class, getTextAlignment());
        config.setColor(keyTextColor, getTextColor());
        config.setColor(keyBorderColor, getBorderColor());
        config.setColor(keyFillColor, getFillColor());
    }

    @Override
    public String getSection() {
        return "Common";
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

}
