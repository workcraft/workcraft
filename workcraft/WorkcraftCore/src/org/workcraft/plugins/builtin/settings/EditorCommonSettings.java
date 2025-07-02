package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.utils.TextUtils;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EditorCommonSettings extends AbstractCommonSettings {

    public static final Map<Double, String> PREDEFINED_SCREEN_DPI = new LinkedHashMap<>();

    static {
        PREDEFINED_SCREEN_DPI.put(81.6, "27\" 1920 x 1080 (or 3840 x 2160 @ 200%)");
        PREDEFINED_SCREEN_DPI.put(91.8, "24\" 1920 x 1080");
        PREDEFINED_SCREEN_DPI.put(94.3, "24\" 1920 x 1200");
        PREDEFINED_SCREEN_DPI.put(108.8, "27\" 2560 x 1440 (or 3840 x 2160 @ 150%)");
        PREDEFINED_SCREEN_DPI.put(141.2, "15.6\" 1920 x 1080 (or 3840 x 2160 @ 200%)");
        PREDEFINED_SCREEN_DPI.put(157.4, "14\" 1920 x 1080");
        PREDEFINED_SCREEN_DPI.put(163.2, "27\" 3840 x 2160");
        PREDEFINED_SCREEN_DPI.put(188.3, "15.6\" 2560 x 1440 (or 3840 x 2160 @ 150%)");
        PREDEFINED_SCREEN_DPI.put(282.4, "15.6\" 3840 x 2160");
    }

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

    public enum DialogStyle {
        CROSS_PLATFORM("cross-platform"),
        NATIVE("native");

        public final String name;

        DialogStyle(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public boolean isDefaultLafStyle() {
            return this == CROSS_PLATFORM;
        }
    }

    public enum TabStyle {
        SCROLL("scroll"),
        WRAP("wrap");

        public final String name;

        TabStyle(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum ExportHeaderStyle {
        NONE("none"),
        BRIEF("brief"),
        DETAILED("detailed");

        public final String name;

        ExportHeaderStyle(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonEditorSettings";

    /*
     * Keys
     */
    // GUI
    private static final String keyScreenDpi = prefix + ".screenDpi";
    private static final String keyFontSize = prefix + ".fontSize";
    private static final String keyTitleStyle = prefix + ".titleStyle";
    private static final String keyTabStyle = prefix + ".tabStyle";
    private static final String keyDialogStyle = prefix + ".dialogStyle";
    private static final String keyRecentCount = prefix + ".recentCount";
    // Canvas
    private static final String keyRedrawInterval = prefix + ".redrawInterval";
    private static final String keyBackgroundColor = prefix + ".backgroundColor";
    private static final String keyPngBackgroundColor = prefix + ".pngBackgroundColor";
    // Grid
    private static final String keyGridVisibility = prefix + ".gridVisibility";
    private static final String keyLightGrid = prefix + ".lightGrid";
    private static final String keyGridColor = prefix + ".gridColor";
    private static final String keyRulerVisibility = prefix + ".rulerVisibility";
    // Hints
    private static final String keyHintVisibility = prefix + ".hintVisibility";
    private static final String keyHintColor = prefix + ".hintColor";
    private static final String keyIssueVisibility = prefix + ".issueVisibility";
    private static final String keyIssueColor = prefix + ".issueColor";
    private static final String keyFlashInterval = prefix + ".flashInterval";
    // Layout
    private static final String keyOpenNonvisual = prefix + ".openNonvisual";
    private static final String keyLargeModelSize = prefix + ".largeModelSize";
    // Export
    private static final String keyFlatnameSeparator = prefix + ".flatnameSeparator";
    private static final String keyExportHeaderStyle = prefix + ".exportHeaderStyle";

    /*
     * Defaults
     */
    // GUI
    private static final double defaultScreenDpi = 96.0;
    private static final double defaultFontSize = 10.0;
    private static final TitleStyle defaultTitleStyle = TitleStyle.SHORT;
    private static final TabStyle defaultTabStyle = TabStyle.SCROLL;
    private static final DialogStyle defaultDialogStyle = DialogStyle.CROSS_PLATFORM;
    private static final int defaultRecentCount = 10;
    // Canvas
    private static final int defaultRedrawInterval = 20;
    private static final Color defaultBackgroundColor = Color.WHITE;
    private static final Color defaultPngBackgroundColor = new Color(0, 0, 0, 0);
    // Grid
    private static final boolean defaultGridVisibility = true;
    private static final boolean defaultLightGrid = true;
    private static final Color defaultGridColor = new Color(225, 225, 225);
    private static final boolean defaultRulerVisibility = false;
    // Hints
    private static final boolean defaultHintVisibility = true;
    private static final Color defaultHintColor = Color.BLACK;
    private static final boolean defaultIssueVisibility = true;
    private static final Color defaultIssueColor = Color.RED;
    private static final int defaultFlashInterval = 2000;
    // Layout
    private static final boolean defaultOpenNonvisual = true;
    private static final int defaultLargeModelSize = 500;
    // Export
    private static final String defaultFlatnameSeparator = "_";
    private static final ExportHeaderStyle defaultExportHeaderStyle = ExportHeaderStyle.DETAILED;

    /*
     * Variables
     */
    // GUI
    private static double screenDpi = defaultScreenDpi;
    private static double fontSize = defaultFontSize;
    private static TitleStyle titleStyle = defaultTitleStyle;
    private static TabStyle tabStyle = defaultTabStyle;
    private static DialogStyle dialogStyle = defaultDialogStyle;
    private static int recentCount = defaultRecentCount;
    // Canvas
    private static int redrawInterval = defaultRedrawInterval;
    private static Color backgroundColor = defaultBackgroundColor;
    private static Color usePngBackgroundColor = defaultPngBackgroundColor;
    // Grid
    private static boolean gridVisibility = defaultGridVisibility;
    private static boolean lightGrid = defaultLightGrid;
    private static Color gridColor = defaultGridColor;
    private static boolean rulerVisibility = defaultRulerVisibility;
    // Hints
    private static boolean hintVisibility = defaultHintVisibility;
    private static Color hintColor = defaultHintColor;
    private static boolean issueVisibility = defaultIssueVisibility;
    private static Color issueColor = defaultIssueColor;
    private static int flashInterval = defaultFlashInterval;
    // Layout
    private static boolean openNonvisual = defaultOpenNonvisual;
    private static int largeModelSize = defaultLargeModelSize;
    // Export
    private static String flatnameSeparator = defaultFlatnameSeparator;
    private static ExportHeaderStyle exportHeaderStyle = defaultExportHeaderStyle;

    static {
        properties.add(PropertyHelper.createSeparatorProperty("GUI decoration"));

        properties.add(new PropertyDeclaration<>(Double.class,
                TextUtils.getBullet("Screen pixel density (DPI) - requires restart"),
                EditorCommonSettings::setScreenDpi,
                EditorCommonSettings::getScreenDpi) {
            @Override
            public Map<Double, String> getChoice() {
                return PREDEFINED_SCREEN_DPI;
            }
        });

        properties.add(new PropertyDeclaration<>(Double.class,
                TextUtils.getBullet("Base font size (point) - requires restart"),
                EditorCommonSettings::setFontSize,
                EditorCommonSettings::getFontSize));

        properties.add(new PropertyDeclaration<>(Integer.class,
                TextUtils.getBullet("Number of recent files (0-99)"),
                EditorCommonSettings::setRecentCount,
                EditorCommonSettings::getRecentCount));

        properties.add(new PropertyDeclaration<>(TitleStyle.class,
                TextUtils.getBullet("Tab title style"),
                EditorCommonSettings::setTitleStyle,
                EditorCommonSettings::getTitleStyle));

        properties.add(new PropertyDeclaration<>(TabStyle.class,
                TextUtils.getBullet("Tab layout - requires restart"),
                EditorCommonSettings::setTabStyle,
                EditorCommonSettings::getTabStyle));

        properties.add(new PropertyDeclaration<>(DialogStyle.class,
                TextUtils.getBullet("Dialog style"),
                EditorCommonSettings::setDialogStyle,
                EditorCommonSettings::getDialogStyle));

        properties.add(PropertyHelper.createSeparatorProperty("Canvas"));

        properties.add(new PropertyDeclaration<>(Integer.class,
                TextUtils.getBullet("Minimal redraw interval (ms)"),
                EditorCommonSettings::setRedrawInterval,
                EditorCommonSettings::getRedrawInterval));

        properties.add(new PropertyDeclaration<>(Color.class,
                TextUtils.getBullet("Background color"),
                EditorCommonSettings::setBackgroundColor,
                EditorCommonSettings::getBackgroundColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                TextUtils.getBullet("Background color for PNG export"),
                EditorCommonSettings::setPngBackgroundColor,
                EditorCommonSettings::getPngBackgroundColor));

        properties.add(PropertyHelper.createSeparatorProperty("Grid and rulers"));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                TextUtils.getBullet("Show grid"),
                EditorCommonSettings::setGridVisibility,
                EditorCommonSettings::getGridVisibility));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                TextUtils.getBullet("Use light grid"),
                EditorCommonSettings::setLightGrid,
                EditorCommonSettings::getLightGrid));

        properties.add(new PropertyDeclaration<>(Color.class,
                TextUtils.getBullet("Grid color"),
                EditorCommonSettings::setGridColor,
                EditorCommonSettings::getGridColor));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                TextUtils.getBullet("Show rulers"),
                EditorCommonSettings::setRulerVisibility,
                EditorCommonSettings::getRulerVisibility));

        properties.add(PropertyHelper.createSeparatorProperty("Hints and issues"));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                TextUtils.getBullet("Show hints"),
                EditorCommonSettings::setHintVisibility,
                EditorCommonSettings::getHintVisibility));

        properties.add(new PropertyDeclaration<>(Color.class,
                TextUtils.getBullet("Hint color"),
                EditorCommonSettings::setHintColor,
                EditorCommonSettings::getHintColor));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                TextUtils.getBullet("Show issues"),
                EditorCommonSettings::setIssueVisibility,
                EditorCommonSettings::getIssueVisibility));

        properties.add(new PropertyDeclaration<>(Color.class,
                TextUtils.getBullet("Issue color"),
                EditorCommonSettings::setIssueColor,
                EditorCommonSettings::getIssueColor));

        properties.add(new PropertyDeclaration<>(Integer.class,
                TextUtils.getBullet("Issue visibility interval (ms)"),
                EditorCommonSettings::setFlashInterval,
                EditorCommonSettings::getFlashInterval));

        properties.add(PropertyHelper.createSeparatorProperty("Model visualisation"));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                TextUtils.getBullet("Open non-visual models (imported or converted)"),
                EditorCommonSettings::setOpenNonvisual,
                EditorCommonSettings::getOpenNonvisual));

        properties.add(new PropertyDeclaration<>(Integer.class,
                TextUtils.getBullet("Model size for layout warning (0-9999 elements)"),
                EditorCommonSettings::setLargeModelSize,
                EditorCommonSettings::getLargeModelSize));

        properties.add(PropertyHelper.createSeparatorProperty("Model export"));

        properties.add(new PropertyDeclaration<>(String.class,
                TextUtils.getBullet("Separator for converting page references into flat name"),
                EditorCommonSettings::setFlatnameSeparator,
                EditorCommonSettings::getFlatnameSeparator));

        properties.add(new PropertyDeclaration<>(ExportHeaderStyle.class,
                TextUtils.getBullet("File header style"),
                EditorCommonSettings::setExportHeaderStyle,
                EditorCommonSettings::getExportHeaderStyle));
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        // GUI
        setScreenDpi(config.getDouble(keyScreenDpi, defaultScreenDpi));
        setFontSize(config.getDouble(keyFontSize, defaultFontSize));
        setTitleStyle(config.getEnum(keyTitleStyle, TitleStyle.class, defaultTitleStyle));
        setTabStyle(config.getEnum(keyTabStyle, TabStyle.class, defaultTabStyle));
        setDialogStyle(config.getEnum(keyDialogStyle, DialogStyle.class, defaultDialogStyle));
        setRecentCount(config.getInt(keyRecentCount, defaultRecentCount));
        // Canvas
        setRedrawInterval(config.getInt(keyRedrawInterval, defaultRedrawInterval));
        setBackgroundColor(config.getColor(keyBackgroundColor, defaultBackgroundColor));
        setPngBackgroundColor(config.getColor(keyPngBackgroundColor, defaultPngBackgroundColor));
        // Grid
        setGridVisibility(config.getBoolean(keyGridVisibility, defaultGridVisibility));
        setLightGrid(config.getBoolean(keyLightGrid, defaultLightGrid));
        setGridColor(config.getColor(keyGridColor, defaultGridColor));
        setRulerVisibility(config.getBoolean(keyRulerVisibility, defaultRulerVisibility));
        // Hints
        setHintVisibility(config.getBoolean(keyHintVisibility, defaultHintVisibility));
        setHintColor(config.getColor(keyHintColor, defaultHintColor));
        setIssueVisibility(config.getBoolean(keyIssueVisibility, defaultIssueVisibility));
        setFlashInterval(config.getInt(keyFlashInterval, defaultFlashInterval));
        setIssueColor(config.getColor(keyIssueColor, defaultIssueColor));
        // Layout
        setOpenNonvisual(config.getBoolean(keyOpenNonvisual, defaultOpenNonvisual));
        setLargeModelSize(config.getInt(keyLargeModelSize, defaultLargeModelSize));
        // Export
        setFlatnameSeparator(config.getString(keyFlatnameSeparator, defaultFlatnameSeparator));
        setExportHeaderStyle(config.getEnum(keyExportHeaderStyle, ExportHeaderStyle.class, defaultExportHeaderStyle));
    }

    @Override
    public void save(Config config) {
        // GUI
        config.setDouble(keyScreenDpi, getScreenDpi());
        config.setDouble(keyFontSize, getFontSize());
        config.setEnum(keyTitleStyle, getTitleStyle());
        config.setEnum(keyTabStyle, getTabStyle());
        config.setEnum(keyDialogStyle, getDialogStyle());
        config.setInt(keyRecentCount, getRecentCount());
        // Canvas
        config.setInt(keyRedrawInterval, getRedrawInterval());
        config.setColor(keyBackgroundColor, getBackgroundColor());
        config.setColor(keyPngBackgroundColor, getPngBackgroundColor());
        // Grid
        config.setBoolean(keyGridVisibility, getGridVisibility());
        config.setBoolean(keyLightGrid, getLightGrid());
        config.setColor(keyGridColor, getGridColor());
        config.setBoolean(keyRulerVisibility, getRulerVisibility());
        // Hints
        config.setBoolean(keyHintVisibility, getHintVisibility());
        config.setColor(keyHintColor, getHintColor());
        config.setBoolean(keyIssueVisibility, getIssueVisibility());
        config.setColor(keyIssueColor, getIssueColor());
        config.setInt(keyFlashInterval, getFlashInterval());
        // Layout
        config.setBoolean(keyOpenNonvisual, getOpenNonvisual());
        config.setInt(keyLargeModelSize, getLargeModelSize());
        // Export
        config.set(keyFlatnameSeparator, getFlatnameSeparator());
        config.setEnum(keyExportHeaderStyle, getExportHeaderStyle());
    }

    @Override
    public String getName() {
        return "Editor";
    }

    public static double getScreenDpi() {
        return screenDpi;
    }

    public static void setScreenDpi(double value) {
        screenDpi = value;
    }

    public static double getFontSize() {
        return fontSize;
    }

    public static void setFontSize(double value) {
        fontSize = value;
    }

    public static TitleStyle getTitleStyle() {
        return titleStyle;
    }

    public static void setTitleStyle(TitleStyle value) {
        titleStyle = value;
    }

    public static TabStyle getTabStyle() {
        return tabStyle;
    }

    public static void setTabStyle(TabStyle value) {
        tabStyle = value;
    }

    public static DialogStyle getDialogStyle() {
        return dialogStyle;
    }

    public static void setDialogStyle(DialogStyle value) {
        dialogStyle = value;
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

    public static void setRedrawInterval(int value) {
        redrawInterval = value;
    }

    public static int getRedrawInterval() {
        return redrawInterval;
    }

    public static Color getBackgroundColor() {
        return backgroundColor;
    }

    public static void setBackgroundColor(Color value) {
        backgroundColor = new Color(value.getRGB(), false);
    }

    public static Color getPngBackgroundColor() {
        return usePngBackgroundColor;
    }

    public static void setPngBackgroundColor(Color value) {
        usePngBackgroundColor = value;
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

    public static String getFlatnameSeparator() {
        return flatnameSeparator;
    }

    public static void setFlatnameSeparator(String value) {
        flatnameSeparator = value;
    }

    public static ExportHeaderStyle getExportHeaderStyle() {
        return exportHeaderStyle;
    }

    public static void setExportHeaderStyle(ExportHeaderStyle value) {
        exportHeaderStyle = value;
    }

}
