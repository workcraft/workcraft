package org.workcraft.plugins.shared;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class CommonSignalSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonSignalSettings";

    private static final String keyInputColor = prefix + ".inputColor";
    private static final String keyOutputColor = prefix + ".outputColor";
    private static final String keyInternalColor = prefix + ".internalColor";
    private static final String keyDummyColor = prefix + ".dummyColor";
    private static final String keyShowToggle = prefix + ".showToggle";

    private static final Color defaultInputColor = Color.RED.darker();
    private static final Color defaultOutputColor = Color.BLUE.darker();
    private static final Color defaultInternalColor = Color.GREEN.darker();
    private static final Color defaultDummyColor = Color.BLACK.darker();
    private static final boolean defaultShowToggle = false;

    private static Color inputColor = defaultInputColor;
    private static Color outputColor = defaultOutputColor;
    private static Color internalColor = defaultInternalColor;
    private static Color dummyColor = defaultDummyColor;
    private static boolean showToggle = defaultShowToggle;

    public CommonSignalSettings() {
        properties.add(new PropertyDeclaration<CommonSignalSettings, Color>(
                this, "Input signal color", Color.class, true, false, false) {
            protected void setter(CommonSignalSettings object, Color value) {
                setInputColor(value);
            }
            protected Color getter(CommonSignalSettings object) {
                return getInputColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonSignalSettings, Color>(
                this, "Output signal color", Color.class, true, false, false) {
            protected void setter(CommonSignalSettings object, Color value) {
                setOutputColor(value);
            }
            protected Color getter(CommonSignalSettings object) {
                return getOutputColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonSignalSettings, Color>(
                this, "Internal signal color", Color.class, true, false, false) {
            protected void setter(CommonSignalSettings object, Color value) {
                setInternalColor(value);
            }
            protected Color getter(CommonSignalSettings object) {
                return getInternalColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonSignalSettings, Color>(
                this, "Dummy color", Color.class, true, false, false) {
            protected void setter(CommonSignalSettings object, Color value) {
                setDummyColor(value);
            }
            protected Color getter(CommonSignalSettings object) {
                return getDummyColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonSignalSettings, Boolean>(
                this, "Show signal toggle (~)", Boolean.class, true, false, false) {
            protected void setter(CommonSignalSettings object, Boolean value) {
                setShowToggle(value);
            }
            protected Boolean getter(CommonSignalSettings object) {
                return getShowToggle();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setInputColor(config.getColor(keyInputColor, defaultInputColor));
        setOutputColor(config.getColor(keyOutputColor, defaultOutputColor));
        setInternalColor(config.getColor(keyInternalColor, defaultInternalColor));
        setDummyColor(config.getColor(keyDummyColor, defaultDummyColor));
        setShowToggle(config.getBoolean(keyShowToggle, defaultShowToggle));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyInputColor, getInputColor());
        config.setColor(keyOutputColor, getOutputColor());
        config.setColor(keyInternalColor, getInternalColor());
        config.setColor(keyDummyColor, getDummyColor());
        config.setBoolean(keyShowToggle, getShowToggle());
    }

    @Override
    public String getSection() {
        return "Common";
    }

    @Override
    public String getName() {
        return "Signal";
    }

    public static void setInputColor(Color value) {
        inputColor = value;
    }

    public static Color getInputColor() {
        return inputColor;
    }

    public static void setOutputColor(Color value) {
        outputColor = value;
    }

    public static Color getOutputColor() {
        return outputColor;
    }

    public static void setInternalColor(Color value) {
        internalColor = value;
    }

    public static Color getInternalColor() {
        return internalColor;
    }

    public static void setDummyColor(Color value) {
        dummyColor = value;
    }

    public static Color getDummyColor() {
        return dummyColor;
    }

    public static Boolean getShowToggle() {
        return showToggle;
    }

    public static void setShowToggle(Boolean value) {
        showToggle = value;
    }

}
