package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class EditorCommonSettings extends AbstractCommonSettings {

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
    private static final String keyLargeModelSize = prefix + ".largeModelSize";
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
    private static final int defaultLargeModelSize = 500;
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
    private static int largeModelSize = defaultLargeModelSize;
    private static int redrawInterval = defaultRedrawInterval;

    static {
        properties.add(new PropertyDeclaration<>(Color.class,
                "Background color",
                EditorCommonSettings::setBackgroundColor,
                EditorCommonSettings::getBackgroundColor));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Show grid",
                EditorCommonSettings::setGridVisibility,
                EditorCommonSettings::getGridVisibility));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Use light grid",
                EditorCommonSettings::setLightGrid,
                EditorCommonSettings::getLightGrid));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Light grid cross size (mm)",
                EditorCommonSettings::setLightGridSize,
                EditorCommonSettings::getLightGridSize));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Grid color",
                EditorCommonSettings::setGridColor,
                EditorCommonSettings::getGridColor));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Show rulers",
                EditorCommonSettings::setRulerVisibility,
                EditorCommonSettings::getRulerVisibility));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Show hints",
                EditorCommonSettings::setHintVisibility,
                EditorCommonSettings::getHintVisibility));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Hint color",
                EditorCommonSettings::setHintColor,
                EditorCommonSettings::getHintColor));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Show issues",
                EditorCommonSettings::setIssueVisibility,
                EditorCommonSettings::getIssueVisibility));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Issue color",
                EditorCommonSettings::setIssueColor,
                EditorCommonSettings::getIssueColor));

        properties.add(new PropertyDeclaration<>(Integer.class,
                "Issue visibility interval (ms)",
                EditorCommonSettings::setFlashInterval,
                EditorCommonSettings::getFlashInterval));

        properties.add(new PropertyDeclaration<>(Integer.class,
                "Number of recent files (0-99)",
                EditorCommonSettings::setRecentCount,
                EditorCommonSettings::getRecentCount));

        properties.add(new PropertyDeclaration<>(TitleStyle.class,
                "Model title style",
                EditorCommonSettings::setTitleStyle,
                EditorCommonSettings::getTitleStyle));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Show names as absolute paths",
                EditorCommonSettings::setShowAbsolutePaths,
                EditorCommonSettings::getShowAbsolutePaths));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Open non-visual models (imported or converted)",
                EditorCommonSettings::setOpenNonvisual,
                EditorCommonSettings::getOpenNonvisual));

        properties.add(new PropertyDeclaration<>(Integer.class,
                "Model size for layout warning (0-9999 elements)",
                EditorCommonSettings::setLargeModelSize,
                EditorCommonSettings::getLargeModelSize));

        properties.add(new PropertyDeclaration<>(Integer.class,
                "Minimal redraw interval (ms)",
                EditorCommonSettings::setRedrawInterval,
                EditorCommonSettings::getRedrawInterval));
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
        setLargeModelSize(config.getInt(keyLargeModelSize, defaultLargeModelSize));
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
        config.setInt(keyLargeModelSize, getLargeModelSize());
        config.setInt(keyRedrawInterval, getRedrawInterval());
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

    public static int getLargeModelSize() {
        return largeModelSize;
    }

    public static void setLargeModelSize(int value) {
        if (value < 0) {
            value = 0;
        }
        if (value > 9999) {
            value = 9999;
        }
        largeModelSize = value;
    }

    public static void setRedrawInterval(int value) {
        redrawInterval = value;
    }

    public static int getRedrawInterval() {
        return redrawInterval;
    }

}
