package org.workcraft.plugins.plato;

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class PlatoSettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.plato";

    private static final String keyConceptsFolderLocation = prefix + ".conceptsFolderLocation";

    private static final String defaultConceptsFolderLocation = DesktopApi.getOs().isWindows() ? "tools\\plato\\" : "tools/plato/";

    private static String conceptsFolderLocation = defaultConceptsFolderLocation;

    public PlatoSettings() {
        properties.add(new PropertyDeclaration<PlatoSettings, String>(
                this, "Concepts folder location", String.class, true, false, false) {
            protected void setter(PlatoSettings object, String value) {
                setConceptsFolderLocation(value);
            }
            protected String getter(PlatoSettings object) {
                return getConceptsFolderLocation();
            }
        });
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void save(Config config) {
        config.set(keyConceptsFolderLocation, getConceptsFolderLocation());
    }

    @Override
    public void load(Config config) {
        setConceptsFolderLocation(config.getString(keyConceptsFolderLocation, defaultConceptsFolderLocation));
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "Plato";
    }

    public static String getConceptsFolderLocation() {
        return conceptsFolderLocation;
    }

    public static void setConceptsFolderLocation(String value) {
        conceptsFolderLocation = value;
    }

}
