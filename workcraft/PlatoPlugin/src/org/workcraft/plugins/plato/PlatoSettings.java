package org.workcraft.plugins.plato;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractToolSettings;
import org.workcraft.utils.BackendUtils;

import java.util.Collection;
import java.util.LinkedList;

public class PlatoSettings extends AbstractToolSettings {

    private static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    private static final String prefix = "Tools.plato";

    private static final String keyPlatoFolderLocation = prefix + ".platoFolderLocation";
    private static final String keyPlatoIncludesList = prefix + ".platoIncludesList";

    private static final String defaultPlatoFolderLocation = BackendUtils.getToolDirectory("plato");
    private static final String defaultPlatoIncludesList = "";

    private static String conceptsFolderLocation = defaultPlatoFolderLocation;
    private static String platoIncludesList = defaultPlatoIncludesList;

    static {
        properties.add(new PropertyDeclaration<>(String.class,
                "Concepts folder location",
                PlatoSettings::setPlatoFolderLocation,
                PlatoSettings::getPlatoFolderLocation));

        properties.add(new PropertyDeclaration<>(String.class,
                "Folders to always include (separate with ';')",
                PlatoSettings::setPlatoIncludesList,
                PlatoSettings::getPlatoIncludesList));
    }

    @Override
    public Collection<PropertyDescriptor<?>> getDescriptors() {
        return properties;
    }

    @Override
    public void save(Config config) {
        config.set(keyPlatoFolderLocation, getPlatoFolderLocation());
        config.set(keyPlatoIncludesList, getPlatoIncludesList());
    }

    @Override
    public void load(Config config) {
        setPlatoFolderLocation(config.getString(keyPlatoFolderLocation, defaultPlatoFolderLocation));
        setPlatoIncludesList(config.getString(keyPlatoIncludesList, defaultPlatoIncludesList));
    }

    @Override
    public String getName() {
        return "Plato";
    }

    public static String getPlatoFolderLocation() {
        return conceptsFolderLocation;
    }

    public static void setPlatoFolderLocation(String value) {
        conceptsFolderLocation = value;
    }

    public static String getPlatoIncludesList() {
        return platoIncludesList;
    }

    public static void setPlatoIncludesList(String value) {
        platoIncludesList = value;
    }

}
