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

import javax.swing.JOptionPane;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class CommonEditorSettings implements Settings {

    public enum TitleStyle {
        MINIMAL("minimal: Title"),
        SHORT("short: Title [MN]"),
        LONG("long: Title - Model Name");

        public final String name;

        TitleStyle(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonEditorSettings";

    private static final String keyBackgroundColor = prefix + ".backgroundColor";
    private static final String keyShowGrid = prefix + ".showGrid";
    private static final String keyLightGrid = prefix + ".lightGrid";
    private static final String keyShowRulers = prefix + ".showRulers";
    private static final String keyShowHints = prefix + ".showHints";
    private static final String keyRecentCount = prefix + ".recentCount";
    private static final String keyIconSize = prefix + ".iconSize";
    private static final String keyTitleStyle = prefix + ".titleStyle";
    private static final String keyShowAbsolutePaths = prefix + ".showAbsolutePaths";
    private static final String keyOpenNonvisual = prefix + ".openNonvisual";
    private static final String keyFlatNameSeparator = prefix + ".flatNameSeparator";

    private static final Color defaultBackgroundColor = Color.WHITE;
    private static final boolean defaultShowGrid = true;
    private static final boolean defaultLightGrid = true;
    private static final boolean defaultShowRulers = true;
    private static final boolean defaultShowHints = true;
    private static final int defaultIconSize = 24;
    private static final int defaultRecentCount = 10;
    private static final TitleStyle defaultTitleStyle = TitleStyle.SHORT;
    private static final boolean defaultShowAbsolutePaths = false;
    private static final boolean defaultOpenNonvisual = true;
    private static String defaultFlatNameSeparator = "__";

    private static Color backgroundColor = defaultBackgroundColor;
    private static boolean showGrid = defaultShowGrid;
    private static boolean lightGrid = defaultLightGrid;
    private static boolean showRulers = defaultShowRulers;
    private static boolean showHints = defaultShowHints;
    private static int iconSize = defaultIconSize;
    private static int recentCount = defaultRecentCount;
    private static TitleStyle titleStyle = defaultTitleStyle;
    private static boolean showAbsolutePaths = defaultShowAbsolutePaths;
    private static boolean openNonvisual = defaultOpenNonvisual;
    private static String flatNameSeparator = defaultFlatNameSeparator;

    public CommonEditorSettings() {
        properties.add(new PropertyDeclaration<CommonEditorSettings, Color>(
                this, "Background color", Color.class, true, false, false) {
            protected void setter(CommonEditorSettings object, Color value) {
                setBackgroundColor(value);
            }
            protected Color getter(CommonEditorSettings object) {
                return getBackgroundColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Show grid", Boolean.class, true, false, false) {
            protected void setter(CommonEditorSettings object, Boolean value) {
                setShowGrid(value);
            }
            protected Boolean getter(CommonEditorSettings object) {
                return getShowGrid();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Use light grid", Boolean.class, true, false, false) {
            protected void setter(CommonEditorSettings object, Boolean value) {
                setLightGrid(value);
            }
            protected Boolean getter(CommonEditorSettings object) {
                return getLightGrid();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Show rulers", Boolean.class, true, false, false) {
            protected void setter(CommonEditorSettings object, Boolean value) {
                setShowRulers(value);
            }
            protected Boolean getter(CommonEditorSettings object) {
                return getShowRulers();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Show hints", Boolean.class, true, false, false) {
            protected void setter(CommonEditorSettings object, Boolean value) {
                setShowHints(value);
            }
            protected Boolean getter(CommonEditorSettings object) {
                return getShowHints();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Integer>(
                this, "Icon width (pixels, 8-256)", Integer.class, true, false, false) {
            protected void setter(CommonEditorSettings object, Integer value) {
                setIconSize(value);
            }
            protected Integer getter(CommonEditorSettings object) {
                return getIconSize();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Integer>(
                this, "Number of recent files (0-99)", Integer.class, true, false, false) {
            protected void setter(CommonEditorSettings object, Integer value) {
                setRecentCount(value);
            }
            protected Integer getter(CommonEditorSettings object) {
                return getRecentCount();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, TitleStyle>(
                this, "Model title style", TitleStyle.class, true, false, false) {
            protected void setter(CommonEditorSettings object, TitleStyle value) {
                setTitleStyle(value);
            }
            protected TitleStyle getter(CommonEditorSettings object) {
                return getTitleStyle();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Names shown with absolute paths", Boolean.class, true, false, false) {
            protected void setter(CommonEditorSettings object, Boolean value) {
                setShowAbsolutePaths(value);
            }
            protected Boolean getter(CommonEditorSettings object) {
                return getShowAbsolutePaths();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Open non-visual models (imported or transformed)", Boolean.class, true, false, false) {
            protected void setter(CommonEditorSettings object, Boolean value) {
                setOpenNonvisual(value);
            }
            protected Boolean getter(CommonEditorSettings object) {
                return getOpenNonvisual();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, String>(
                this, "Flat name separator", String.class, true, false, false) {
            protected void setter(CommonEditorSettings object, String value) {
                if (value.length() < 2) {
                    JOptionPane.showMessageDialog(null,
                            "Short flat name separator increases the risk of name clashing.\n"
                             + "Consider making it at least two characters long.",
                            "Common editor settings", JOptionPane.WARNING_MESSAGE);
                }
                boolean badValue = false;
                for (int i = 0; i < value.length(); ++i) {
                    char c = value.charAt(i);
                    if (!Character.isDigit(c) && !Character.isLetter(c) && (c != '_')) {
                        badValue = true;
                        break;
                    }
                }
                if (badValue) {
                    JOptionPane.showMessageDialog(null,
                            "Flat name separator must only consist of letters, numbers and underscores.",
                            "Common editor settings", JOptionPane.ERROR_MESSAGE);

                } else {
                    setFlatNameSeparator(value);
                }
            }
            protected String getter(CommonEditorSettings object) {
                return getFlatNameSeparator();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setBackgroundColor(config.getColor(keyBackgroundColor, defaultBackgroundColor));
        setShowGrid(config.getBoolean(keyShowGrid, defaultShowGrid));
        setLightGrid(config.getBoolean(keyLightGrid, defaultLightGrid));
        setShowRulers(config.getBoolean(keyShowRulers, defaultShowRulers));
        setShowHints(config.getBoolean(keyShowHints, defaultShowHints));
        setIconSize(config.getInt(keyIconSize, defaultIconSize));
        setRecentCount(config.getInt(keyRecentCount, defaultRecentCount));
        setTitleStyle(config.getEnum(keyTitleStyle, TitleStyle.class, defaultTitleStyle));
        setShowAbsolutePaths(config.getBoolean(keyShowAbsolutePaths, defaultShowAbsolutePaths));
        setOpenNonvisual(config.getBoolean(keyOpenNonvisual, defaultOpenNonvisual));
        setFlatNameSeparator(config.getString(keyFlatNameSeparator, defaultFlatNameSeparator));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyBackgroundColor, getBackgroundColor());
        config.setBoolean(keyShowGrid, getShowGrid());
        config.setBoolean(keyLightGrid, getLightGrid());
        config.setBoolean(keyShowRulers, getShowRulers());
        config.setBoolean(keyShowHints, getShowHints());
        config.setInt(keyIconSize, getIconSize());
        config.setInt(keyRecentCount, getRecentCount());
        config.setEnum(keyTitleStyle, TitleStyle.class, getTitleStyle());
        config.setBoolean(keyShowAbsolutePaths, getShowAbsolutePaths());
        config.setBoolean(keyOpenNonvisual, getOpenNonvisual());
        config.set(keyFlatNameSeparator, getFlatNameSeparator());
    }

    @Override
    public String getSection() {
        return "Common";
    }

    @Override
    public String getName() {
        return "Editor";
    }

    public static Color getBackgroundColor() {
        return backgroundColor;
    }

    public static void setBackgroundColor(Color value) {
        backgroundColor = value;
    }

    public static void setShowGrid(Boolean value) {
        showGrid = value;
    }

    public static Boolean getShowGrid() {
        return showGrid;
    }

    public static void setLightGrid(Boolean value) {
        lightGrid = value;
    }

    public static Boolean getLightGrid() {
        return lightGrid;
    }

    public static void setShowRulers(Boolean value) {
        showRulers = value;
    }

    public static Boolean getShowRulers() {
        return showRulers;
    }

    public static void setShowHints(Boolean value) {
        showHints = value;
    }

    public static Boolean getShowHints() {
        return showHints;
    }

    public static int getIconSize() {
        return iconSize;
    }

    public static void setIconSize(int value) {
        if (value < 8) {
            value = 8;
        }
        if (value > 256) {
            value = 256;
        }
        iconSize = value;
    }

    public static int getRecentCount() {
        return recentCount;
    }

    public static void setRecentCount(int value) {
        if (value < 0) {
            value = 0;
        }
        if (value > 99) {
            value = 99;
        }
        recentCount = value;
    }

    public static TitleStyle getTitleStyle() {
        return titleStyle;
    }

    public static void setTitleStyle(TitleStyle value) {
        titleStyle = value;
    }

    public static void setShowAbsolutePaths(boolean value) {
        showAbsolutePaths = value;
    }

    public static boolean getShowAbsolutePaths() {
        return showAbsolutePaths;
    }

    public static Boolean getOpenNonvisual() {
        return openNonvisual;
    }

    public static void setOpenNonvisual(Boolean value) {
        openNonvisual = value;
    }

    public static String getFlatNameSeparator() {
        return flatNameSeparator;
    }

    public static void setFlatNameSeparator(String value) {
        if (value.length() > 0) {
            flatNameSeparator = value;
        }
    }

}
