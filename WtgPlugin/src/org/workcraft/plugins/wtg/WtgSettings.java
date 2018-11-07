package org.workcraft.plugins.wtg;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.Settings;
import org.workcraft.util.DialogUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class WtgSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "WtgSettings";

    private static final String keyLowStateSuffix = prefix + ".lowStateSuffix";
    private static final String keyHighStateSuffix = prefix + ".highStateSuffix";
    private static final String keyStableStateSuffix = prefix + ".stableStateSuffix";
    private static final String keyUnstableStateSuffix = prefix + ".unstableStateSuffix";
    private static final String keyStabiliseEventSuffix = prefix + ".stabiliseEventSuffix";
    private static final String keyDestabiliseEventSuffix = prefix + ".destabiliseEventSuffix";
    private static final String keyEntryEventSuffix = prefix + ".entryEventSuffix";
    private static final String keyExitEventSuffix = prefix + ".exitEventSuffix";

    private static final String defaultLowStateSuffix = "_LOW";
    private static final String defaultHighStateSuffix = "_HIGH";
    private static final String defaultStableStateSuffix = "_STABLE";
    private static final String defaultUnstableStateSuffix = "_UNSTABLE";
    private static final String defaultStabiliseEventSuffix = "_STABILISE";
    private static final String defaultDestabiliseEventSuffix = "_DESTABILISE";
    private static final String defaultEntryEventSuffix = "_ENTRY";
    private static final String defaultExitEventSuffix = "_EXIT";

    private static String lowStateSuffix = defaultLowStateSuffix;
    private static String highStateSuffix = defaultHighStateSuffix;
    private static String stableStateSuffix = defaultStableStateSuffix;
    private static String unstableStateSuffix = defaultUnstableStateSuffix;
    private static String stabiliseEventSuffix = defaultStabiliseEventSuffix;
    private static String destabiliseEventSuffix = defaultDestabiliseEventSuffix;
    private static String entryEventSuffix = defaultEntryEventSuffix;
    private static String exitEventSuffix = defaultExitEventSuffix;

    public WtgSettings() {
        properties.add(new PropertyDeclaration<WtgSettings, String>(
                this, "Suffix for signal low state (STG conversion)", String.class) {
            @Override
            public void setter(WtgSettings object, String value) {
                setSuffixIfValid(value, WtgSettings::setLowStateSuffix);
            }
            @Override
            public String getter(WtgSettings object) {
                return getLowStateSuffix();
            }
        });

        properties.add(new PropertyDeclaration<WtgSettings, String>(
                this, "Suffix for signal high state (STG conversion)", String.class) {
            @Override
            public void setter(WtgSettings object, String value) {
                setSuffixIfValid(value, WtgSettings::setHighStateSuffix);
            }
            @Override
            public String getter(WtgSettings object) {
                return getHighStateSuffix();
            }
        });

        properties.add(new PropertyDeclaration<WtgSettings, String>(
                this, "STG conversion suffix for signal stable state", String.class) {
            @Override
            public void setter(WtgSettings object, String value) {
                setSuffixIfValid(value, WtgSettings::setStableStateSuffix);
            }
            @Override
            public String getter(WtgSettings object) {
                return getStableStateSuffix();
            }
        });

        properties.add(new PropertyDeclaration<WtgSettings, String>(
                this, "STG conversion suffix for signal unstable state", String.class) {
            @Override
            public void setter(WtgSettings object, String value) {
                setSuffixIfValid(value, WtgSettings::setUnstableStateSuffix);
            }
            @Override
            public String getter(WtgSettings object) {
                return getUnstableStateSuffix();
            }
        });

        properties.add(new PropertyDeclaration<WtgSettings, String>(
                this, "STG conversion suffix for signal stabilise event", String.class) {
            @Override
            public void setter(WtgSettings object, String value) {
                setSuffixIfValid(value, WtgSettings::setStabiliseEventSuffix);
            }
            @Override
            public String getter(WtgSettings object) {
                return getStabiliseEventSuffix();
            }
        });

        properties.add(new PropertyDeclaration<WtgSettings, String>(
                this, "STG conversion suffix for signal destabilise event", String.class) {
            @Override
            public void setter(WtgSettings object, String value) {
                setSuffixIfValid(value, WtgSettings::setDestabiliseEventSuffix);
            }
            @Override
            public String getter(WtgSettings object) {
                return getDestabiliseEventSuffix();
            }
        });

        properties.add(new PropertyDeclaration<WtgSettings, String>(
                this, "STG conversion suffix for waveform entry event", String.class) {
            @Override
            public void setter(WtgSettings object, String value) {
                setSuffixIfValid(value, WtgSettings::setEntryEventSuffix);
            }
            @Override
            public String getter(WtgSettings object) {
                return getEntryEventSuffix();
            }
        });

        properties.add(new PropertyDeclaration<WtgSettings, String>(
                this, "STG conversion suffix for waveform exit event", String.class) {
            @Override
            public void setter(WtgSettings object, String value) {
                setSuffixIfValid(value, WtgSettings::setExitEventSuffix);
            }
            @Override
            public String getter(WtgSettings object) {
                return getExitEventSuffix();
            }
        });
    }

    private void setSuffixIfValid(String value, Consumer<String> setter) {
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (!Character.isDigit(c) && !Character.isLetter(c) && (c != '_')) {
                DialogUtils.showError("Suffix must only consist of letters, numbers and underscores.");
            }
        }
        if (value.length() < 2) {
            DialogUtils.showWarning("Short suffix increases the risk of name clashing.\n"
                    + "Consider making it at least two characters long.");
        }
        setter.accept(value);
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setLowStateSuffix(config.getString(keyLowStateSuffix, defaultLowStateSuffix));
        setHighStateSuffix(config.getString(keyHighStateSuffix, defaultHighStateSuffix));
        setStableStateSuffix(config.getString(keyStableStateSuffix, defaultStableStateSuffix));
        setUnstableStateSuffix(config.getString(keyUnstableStateSuffix, defaultUnstableStateSuffix));
        setStabiliseEventSuffix(config.getString(keyStabiliseEventSuffix, defaultStabiliseEventSuffix));
        setDestabiliseEventSuffix(config.getString(keyDestabiliseEventSuffix, defaultDestabiliseEventSuffix));
        setEntryEventSuffix(config.getString(keyEntryEventSuffix, defaultEntryEventSuffix));
        setExitEventSuffix(config.getString(keyExitEventSuffix, defaultExitEventSuffix));
    }

    @Override
    public void save(Config config) {
        config.set(keyLowStateSuffix, getLowStateSuffix());
        config.set(keyHighStateSuffix, getHighStateSuffix());
        config.set(keyStableStateSuffix, getStableStateSuffix());
        config.set(keyUnstableStateSuffix, getUnstableStateSuffix());
        config.set(keyStabiliseEventSuffix, getStabiliseEventSuffix());
        config.set(keyDestabiliseEventSuffix, getDestabiliseEventSuffix());
        config.set(keyEntryEventSuffix, getEntryEventSuffix());
        config.set(keyExitEventSuffix, getExitEventSuffix());
    }

    @Override
    public String getSection() {
        return "Models";
    }

    @Override
    public String getName() {
        return "Waveform Transition Graph";
    }

    public static String getLowStateSuffix() {
        return lowStateSuffix;
    }

    public static void setLowStateSuffix(String value) {
        if (value.length() > 0) {
            lowStateSuffix = value;
        }
    }

    public static String getHighStateSuffix() {
        return highStateSuffix;
    }

    public static void setHighStateSuffix(String value) {
        if (value.length() > 0) {
            highStateSuffix = value;
        }
    }

    public static String getStableStateSuffix() {
        return stableStateSuffix;
    }

    public static void setStableStateSuffix(String value) {
        if (value.length() > 0) {
            stableStateSuffix = value;
        }
    }

    public static String getUnstableStateSuffix() {
        return unstableStateSuffix;
    }

    public static void setUnstableStateSuffix(String value) {
        if (value.length() > 0) {
            unstableStateSuffix = value;
        }
    }

    public static String getStabiliseEventSuffix() {
        return stabiliseEventSuffix;
    }

    public static void setStabiliseEventSuffix(String value) {
        if (value.length() > 0) {
            stabiliseEventSuffix = value;
        }
    }

    public static String getDestabiliseEventSuffix() {
        return destabiliseEventSuffix;
    }

    public static void setDestabiliseEventSuffix(String value) {
        if (value.length() > 0) {
            destabiliseEventSuffix = value;
        }
    }

    public static String getEntryEventSuffix() {
        return entryEventSuffix;
    }

    public static void setEntryEventSuffix(String value) {
        if (value.length() > 0) {
            entryEventSuffix = value;
        }
    }

    public static String getExitEventSuffix() {
        return exitEventSuffix;
    }

    public static void setExitEventSuffix(String value) {
        if (value.length() > 0) {
            exitEventSuffix = value;
        }
    }

}
