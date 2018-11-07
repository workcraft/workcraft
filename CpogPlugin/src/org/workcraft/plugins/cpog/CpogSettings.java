package org.workcraft.plugins.cpog;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.Settings;

import java.util.Collection;
import java.util.LinkedList;

public class CpogSettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CpogSettings";

    private static final String keyScencoCommand = prefix + ".scencoCommand";
    private static final String keyEspressoCommand = prefix + ".espressoCommand";
    private static final String keyAbcTool = prefix + ".abcTool";
    private static final String keyPGMinerCommand = prefix + ".PGMinerCommand";

    private static final String defaultScencoCommand = DesktopApi.getOs().isWindows() ? "tools\\ScEnco\\scenco.exe" : "tools/ScEnco/scenco";
    private static final String defaultEspressoCommand = DesktopApi.getOs().isWindows() ? "tools\\Espresso\\espresso.exe" : "tools/Espresso/espresso";
    private static final String defaultAbcTool = DesktopApi.getOs().isWindows() ? "tools\\Abc\\abc.exe" : "tools/Abc/abc";
    private static final String defaultPgminerCommand = DesktopApi.getOs().isWindows() ? "tools\\PGMiner\\pgminer.exe" : "tools/PGMiner/pgminer";

    private static String scencoCommand = defaultScencoCommand;
    private static String espressoCommand = defaultEspressoCommand;
    private static String abcTool = defaultAbcTool;
    private static String pgminerCommand = defaultPgminerCommand;

    public CpogSettings() {
        properties.add(new PropertyDeclaration<CpogSettings, String>(
                this, "Scenco command", String.class) {
            @Override
            public void setter(CpogSettings object, String value) {
                setScencoCommand(value);
            }
            @Override
            public String getter(CpogSettings object) {
                return getScencoCommand();
            }
        });

        properties.add(new PropertyDeclaration<CpogSettings, String>(
                this, "Espresso command", String.class) {
            @Override
            public void setter(CpogSettings object, String value) {
                setEspressoCommand(value);
            }
            @Override
            public String getter(CpogSettings object) {
                return getEspressoCommand();
            }
        });

        properties.add(new PropertyDeclaration<CpogSettings, String>(
                this, "Abc command", String.class) {
            @Override
            public void setter(CpogSettings object, String value) {
                setAbcTool(value);
            }
            @Override
            public String getter(CpogSettings object) {
                return getAbcTool();
            }
        });

        properties.add(new PropertyDeclaration<CpogSettings, String>(
                this, "PG miner command", String.class) {
            @Override
            public void setter(CpogSettings object, String value) {
                setPgminerCommand(value);
            }
            @Override
            public String getter(CpogSettings object) {
                return getPgminerCommand();
            }
        });
    }

    @Override
    public void load(Config config) {
        setScencoCommand(config.getString(keyScencoCommand, defaultScencoCommand));
        setEspressoCommand(config.getString(keyEspressoCommand, defaultEspressoCommand));
        setAbcTool(config.getString(keyAbcTool, defaultAbcTool));
        setPgminerCommand(config.getString(keyPGMinerCommand, defaultPgminerCommand));
    }

    @Override
    public void save(Config config) {
        config.set(keyScencoCommand, getScencoCommand());
        config.set(keyEspressoCommand, getEspressoCommand());
        config.set(keyAbcTool, getAbcTool());
        config.set(keyPGMinerCommand, getPgminerCommand());
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "SCENCO";
    }

    public static String getScencoCommand() {
        return scencoCommand;
    }

    public static void setScencoCommand(String value) {
        scencoCommand = value;
    }

    public static String getEspressoCommand() {
        return espressoCommand;
    }

    public static void setEspressoCommand(String value) {
        espressoCommand = value;
    }

    public static String getAbcTool() {
        return abcTool;
    }

    public static void setAbcTool(String value) {
        abcTool = value;
    }

    public static void setPgminerCommand(String value) {
        pgminerCommand = value;
    }

    public static String getPgminerCommand() {
        return pgminerCommand;
    }

}
