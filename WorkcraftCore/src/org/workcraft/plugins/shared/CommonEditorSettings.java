package org.workcraft.plugins.shared;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

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
    private static final String keyGridVisibility = prefix + ".gridVisibility";
    private static final String keyLightGrid = prefix + ".lightGrid";
    private static final String keyLightGridSize = prefix + ".lightGridSize";
    private static final String keyGridColor = prefix + ".gridColor";
    private static final String keyRulerVisibility = prefix + ".rulerVisibility";
    private static final String keyHintVisibility = prefix + ".hintVisibility";
    private static final String keyHintColor = prefix + ".hintColor";
    private static final String keyIssueVisibility = prefix + ".issueVisibility";
    private static final String keyIssueColor = prefix + ".issueColor";
    private static final String keyFlashInterval = prefix + ".flashInterval";
    private static final String keyRecentCount = prefix + ".recentCount";
    private static final String keyTitleStyle = prefix + ".titleStyle";
    private static final String keyShowAbsolutePaths = prefix + ".showAbsolutePaths";
    private static final String keyOpenNonvisual = prefix + ".openNonvisual";
    private static final String keyRedrawInterval = prefix + ".redrawInterval";

    private static final Color defaultBackgroundColor = Color.WHITE;
    private static final boolean defaultGridVisibility = true;
    private static final boolean defaultLightGrid = true;
    private static final double defaultLightGridSize = 1.0;
    private static final Color defaultGridColor = new Color(225, 225, 225);
    private static final boolean defaultRulerVisibility = false;
    private static final boolean defaultHintVisibility = true;
    private static final Color defaultHintColor = Color.BLACK;
    private static final boolean defaultIssueVisibility = true;
    private static final Color defaultIssueColor = Color.RED;
    private static final int defaultFlashInterval = 2000;
    private static final int defaultRecentCount = 10;
    private static final TitleStyle defaultTitleStyle = TitleStyle.SHORT;
    private static final boolean defaultShowAbsolutePaths = false;
    private static final boolean defaultOpenNonvisual = true;
    private static final int defaultRedrawInterval = 20;

    private static Color backgroundColor = defaultBackgroundColor;
    private static boolean gridVisibility = defaultGridVisibility;
    private static boolean lightGrid = defaultLightGrid;
    private static double lightGridSize = defaultLightGridSize;
    private static Color gridColor = defaultGridColor;
    private static boolean rulerVisibility = defaultRulerVisibility;
    private static boolean hintVisibility = defaultHintVisibility;
    private static Color hintColor = defaultHintColor;
    private static boolean issueVisibility = defaultIssueVisibility;
    private static Color issueColor = defaultIssueColor;
    private static int flashInterval = defaultFlashInterval;
    private static int recentCount = defaultRecentCount;
    private static TitleStyle titleStyle = defaultTitleStyle;
    private static boolean showAbsolutePaths = defaultShowAbsolutePaths;
    private static boolean openNonvisual = defaultOpenNonvisual;
    private static int redrawInterval = defaultRedrawInterval;

    public CommonEditorSettings() {
        properties.add(new PropertyDeclaration<CommonEditorSettings, Color>(
                this, "Background color",
                Color.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Color value) {
                setBackgroundColor(value);
            }
            @Override
            protected Color getter(CommonEditorSettings object) {
                return getBackgroundColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Show grid",
                Boolean.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Boolean value) {
                setGridVisibility(value);
            }
            @Override
            protected Boolean getter(CommonEditorSettings object) {
                return getGridVisibility();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Use light grid",
                Boolean.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Boolean value) {
                setLightGrid(value);
            }
            @Override
            protected Boolean getter(CommonEditorSettings object) {
                return getLightGrid();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Double>(
                this, "Light grid size (mm)",
                Double.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Double value) {
                setLightGridSize(value);
            }
            @Override
            protected Double getter(CommonEditorSettings object) {
                return getLightGridSize();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Color>(
                this, "Grid color",
                Color.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Color value) {
                setGridColor(value);
            }
            @Override
            protected Color getter(CommonEditorSettings object) {
                return getGridColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Show rulers",
                Boolean.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Boolean value) {
                setRulerVisibility(value);
            }
            @Override
            protected Boolean getter(CommonEditorSettings object) {
                return getRulerVisibility();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Show hints",
                Boolean.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Boolean value) {
                setHintVisibility(value);
            }
            @Override
            protected Boolean getter(CommonEditorSettings object) {
                return getHintVisibility();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Color>(
                this, "Hint color",
                Color.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Color value) {
                setHintColor(value);
            }
            @Override
            protected Color getter(CommonEditorSettings object) {
                return getHintColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Show issues",
                Boolean.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Boolean value) {
                setIssueVisibility(value);
            }
            @Override
            protected Boolean getter(CommonEditorSettings object) {
                return getIssueVisibility();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Color>(
                this, "Issue color",
                Color.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Color value) {
                setIssueColor(value);
            }
            @Override
            protected Color getter(CommonEditorSettings object) {
                return getIssueColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Integer>(
                this, "Issue visibility interval (ms)",
                Integer.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Integer value) {
                setFlashInterval(value);
            }
            @Override
            protected Integer getter(CommonEditorSettings object) {
                return getFlashInterval();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Integer>(
                this, "Number of recent files (0-99)",
                Integer.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Integer value) {
                setRecentCount(value);
            }
            @Override
            protected Integer getter(CommonEditorSettings object) {
                return getRecentCount();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, TitleStyle>(
                this, "Model title style",
                TitleStyle.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, TitleStyle value) {
                setTitleStyle(value);
            }
            @Override
            protected TitleStyle getter(CommonEditorSettings object) {
                return getTitleStyle();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Show names as absolute paths",
                Boolean.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Boolean value) {
                setShowAbsolutePaths(value);
            }
            @Override
            protected Boolean getter(CommonEditorSettings object) {
                return getShowAbsolutePaths();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
                this, "Open non-visual models (imported or transformed)",
                Boolean.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Boolean value) {
                setOpenNonvisual(value);
            }
            @Override
            protected Boolean getter(CommonEditorSettings object) {
                return getOpenNonvisual();
            }
        });

        properties.add(new PropertyDeclaration<CommonEditorSettings, Integer>(
                this, "Minimal redraw interval (ms)",
                Integer.class, true, false, false) {
            @Override
            protected void setter(CommonEditorSettings object, Integer value) {
                setRedrawInterval(value);
            }
            @Override
            protected Integer getter(CommonEditorSettings object) {
                return getRedrawInterval();
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
        setGridVisibility(config.getBoolean(keyGridVisibility, defaultGridVisibility));
        setLightGrid(config.getBoolean(keyLightGrid, defaultLightGrid));
        setLightGridSize(config.getDouble(keyLightGridSize, defaultLightGridSize));
        setGridColor(config.getColor(keyGridColor, defaultGridColor));
        setRulerVisibility(config.getBoolean(keyRulerVisibility, defaultRulerVisibility));
        setHintVisibility(config.getBoolean(keyHintVisibility, defaultHintVisibility));
        setHintColor(config.getColor(keyHintColor, defaultHintColor));
        setIssueVisibility(config.getBoolean(keyIssueVisibility, defaultIssueVisibility));
        setFlashInterval(config.getInt(keyFlashInterval, defaultFlashInterval));
        setIssueColor(config.getColor(keyIssueColor, defaultIssueColor));
        setRecentCount(config.getInt(keyRecentCount, defaultRecentCount));
        setTitleStyle(config.getEnum(keyTitleStyle, TitleStyle.class, defaultTitleStyle));
        setShowAbsolutePaths(config.getBoolean(keyShowAbsolutePaths, defaultShowAbsolutePaths));
        setOpenNonvisual(config.getBoolean(keyOpenNonvisual, defaultOpenNonvisual));
        setRedrawInterval(config.getInt(keyRedrawInterval, defaultRedrawInterval));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyBackgroundColor, getBackgroundColor());
        config.setBoolean(keyGridVisibility, getGridVisibility());
        config.setBoolean(keyLightGrid, getLightGrid());
        config.setDouble(keyLightGridSize, getLightGridSize());
        config.setColor(keyGridColor, getGridColor());
        config.setBoolean(keyRulerVisibility, getRulerVisibility());
        config.setBoolean(keyHintVisibility, getHintVisibility());
        config.setColor(keyHintColor, getHintColor());
        config.setBoolean(keyIssueVisibility, getIssueVisibility());
        config.setColor(keyIssueColor, getIssueColor());
        config.setInt(keyFlashInterval, getFlashInterval());
        config.setInt(keyRecentCount, getRecentCount());
        config.setEnum(keyTitleStyle, getTitleStyle());
        config.setBoolean(keyShowAbsolutePaths, getShowAbsolutePaths());
        config.setBoolean(keyOpenNonvisual, getOpenNonvisual());
        config.setInt(keyRedrawInterval, getRedrawInterval());
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

    public static void setGridVisibility(boolean value) {
        gridVisibility = value;
    }

    public static boolean getGridVisibility() {
        return gridVisibility;
    }

    public static void setLightGrid(boolean value) {
        lightGrid = value;
    }

    public static boolean getLightGrid() {
        return lightGrid;
    }

    public static double getLightGridSize() {
        return lightGridSize;
    }

    public static void setLightGridSize(double value) {
        lightGridSize = value;
    }

    public static Color getGridColor() {
        return gridColor;
    }

    public static void setGridColor(Color value) {
        gridColor = value;
    }

    public static void setRulerVisibility(boolean value) {
        rulerVisibility = value;
    }

    public static boolean getRulerVisibility() {
        return rulerVisibility;
    }

    public static void setHintVisibility(boolean value) {
        hintVisibility = value;
    }

    public static boolean getHintVisibility() {
        return hintVisibility;
    }

    public static Color getHintColor() {
        return hintColor;
    }

    public static void setHintColor(Color value) {
        hintColor = value;
    }

    public static void setIssueVisibility(boolean value) {
        issueVisibility = value;
    }

    public static boolean getIssueVisibility() {
        return issueVisibility;
    }

    public static Color getIssueColor() {
        return issueColor;
    }

    public static void setIssueColor(Color value) {
        issueColor = value;
    }

    public static void setFlashInterval(int value) {
        flashInterval = value;
    }

    public static int getFlashInterval() {
        return flashInterval;
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

    public static boolean getOpenNonvisual() {
        return openNonvisual;
    }

    public static void setOpenNonvisual(boolean value) {
        openNonvisual = value;
    }

    public static void setRedrawInterval(int value) {
        redrawInterval = value;
    }

    public static int getRedrawInterval() {
        return redrawInterval;
    }

}
