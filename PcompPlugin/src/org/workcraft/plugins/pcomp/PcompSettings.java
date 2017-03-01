package org.workcraft.plugins.pcomp;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class PcompSettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.pcomp";

    private static final String ketCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";

    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\UnfoldingTools\\pcomp.exe" : "tools/UnfoldingTools/pcomp";
    private static final String defaultArgs = "";

    private static String command = defaultCommand;
    private static String args = defaultArgs;

    public PcompSettings() {
        properties.add(new PropertyDeclaration<PcompSettings, String>(
                this, "PComp command", String.class, true, false, false) {
            protected void setter(PcompSettings object, String value) {
                setCommand(value);
            }
            protected String getter(PcompSettings object) {
                return getCommand();
            }
        });

        properties.add(new PropertyDeclaration<PcompSettings, String>(
                this, "Additional parameters", String.class, true, false, false) {
            protected void setter(PcompSettings object, String value) {
                setArgs(value);
            }
            protected String getter(PcompSettings object) {
                return getArgs();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setCommand(config.getString(ketCommand, defaultCommand));
        setArgs(config.getString(keyArgs, defaultArgs));
    }

    @Override
    public void save(Config config) {
        config.set(ketCommand, getCommand());
        config.set(keyArgs, getArgs());
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "PComp";
    }

    public static String getCommand() {
        return command;
    }

    public static void setCommand(String value) {
        command = value;
    }

    public static String getArgs() {
        return args;
    }

    public static void setArgs(String value) {
        args = value;
    }

}
