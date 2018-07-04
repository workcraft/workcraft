package org.workcraft.plugins.dfs;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class DfsSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "DfsSettings";

    private static final String keyComputedLogicColor = prefix + ".computedLogicColor";
    private static final String keySynchronisationRegisterColor = prefix + ".synchronisationRegisterColor";
    private static final String keyTokenPalette = prefix + ".tokenPalette";

    private static final Color defaultComputedLogicColor = new Color(153, 153, 153);
    private static final Color defaultSynchronisationRegisterColor = new Color(153, 153, 153);
    private static final Palette defaultTokenPalette = Palette.RGB;

    private static Color computedLogicColor = defaultComputedLogicColor;
    private static Color synchronisationRegisterColor = defaultSynchronisationRegisterColor;
    private static Palette tokenPalette = defaultTokenPalette;

    public enum Palette {
        RGBYMC("6-color palette (RGBYMC)", new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN}),
        RGB("3-color palette (RGB)", new Color[]{Color.RED, Color.GREEN, Color.BLUE}),
        NONE("Empty palette", new Color[]{Color.BLACK}),
        GENERATED("Generated palette", null);

        private final String name;
        private final Color[] colors;

        Palette(String name, Color[] colors) {
            this.name = name;
            this.colors = colors;
        }

        @Override
        public String toString() {
            return name;
        }

        public Color[] getColors() {
            return colors;
        }
    }

    public DfsSettings() {
        properties.add(new PropertyDeclaration<DfsSettings, Color>(
                this, "Computed logic color", Color.class, true, false, false) {
            protected void setter(DfsSettings object, Color value) {
                setComputedLogicColor(value);
            }
            protected Color getter(DfsSettings object) {
                return getComputedLogicColor();
            }
        });

        properties.add(new PropertyDeclaration<DfsSettings, Color>(
                this, "Register synchronisation color", Color.class, true, false, false) {
            protected void setter(DfsSettings object, Color value) {
                setSynchronisationRegisterColor(value);
            }
            protected Color getter(DfsSettings object) {
                return getSynchronisationRegisterColor();
            }
        });

        properties.add(new PropertyDeclaration<DfsSettings, Palette>(
                this, "Token palette", Palette.class, true, false, false) {
            protected void setter(DfsSettings object, Palette value) {
                setTokenPalette(value);
            }
            protected Palette getter(DfsSettings object) {
                return getTokenPalette();
            }
        });

    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setComputedLogicColor(config.getColor(keyComputedLogicColor, defaultComputedLogicColor));
        setSynchronisationRegisterColor(config.getColor(keySynchronisationRegisterColor, defaultSynchronisationRegisterColor));
        setTokenPalette(config.getEnum(keyTokenPalette, Palette.class, defaultTokenPalette));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyComputedLogicColor, getComputedLogicColor());
        config.setColor(keySynchronisationRegisterColor, getSynchronisationRegisterColor());
        config.setEnum(keyTokenPalette, Palette.class, getTokenPalette());
    }

    @Override
    public String getSection() {
        return "Models";
    }

    @Override
    public String getName() {
        return "Dataflow Structure";
    }

    public static Color getComputedLogicColor() {
        return computedLogicColor;
    }

    public static void setComputedLogicColor(Color value) {
        computedLogicColor = value;
    }

    public static Color getSynchronisationRegisterColor() {
        return synchronisationRegisterColor;
    }

    public static void setSynchronisationRegisterColor(Color value) {
        synchronisationRegisterColor = value;
    }

    public static Palette getTokenPalette() {
        return tokenPalette;
    }

    public static void setTokenPalette(Palette value) {
        tokenPalette = value;
    }

}
