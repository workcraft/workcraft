/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.shared;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class CommonSimulationSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonSimulationSettings";

    private static final String keyExcitedComponentColor = prefix + ".excitedComponentColor";
    private static final String keySuggestedComponentColor = prefix + ".suggestedComponetColor";

    private static final Color deafultExcitedComponentColor = new Color(1.0f, 0.5f, 0.0f);
    private static final Color deafultSuggestedComponentColor = new Color(1.0f, 0.0f, 1.0f);

    private static Color excitedComponentColor = deafultExcitedComponentColor;
    private static Color suggestedComponentColor = deafultSuggestedComponentColor;

    public CommonSimulationSettings() {
        properties.add(new PropertyDeclaration<CommonSimulationSettings, Color>(
                this, "Excited component color", Color.class, true, false, false) {
            protected void setter(CommonSimulationSettings object, Color value) {
                setExcitedComponentColor(value);
            }
            protected Color getter(CommonSimulationSettings object) {
                return getExcitedComponentColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonSimulationSettings, Color>(
                this, "Suggested component background", Color.class, true, false, false) {
            protected void setter(CommonSimulationSettings object, Color value) {
                setSuggestedComponentColor(value);
            }
            protected Color getter(CommonSimulationSettings object) {
                return getSuggestedComponentColor();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setExcitedComponentColor(config.getColor(keyExcitedComponentColor, deafultExcitedComponentColor));
        setSuggestedComponentColor(config.getColor(keySuggestedComponentColor, deafultSuggestedComponentColor));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyExcitedComponentColor, getExcitedComponentColor());
        config.setColor(keySuggestedComponentColor, getSuggestedComponentColor());
    }

    @Override
    public String getSection() {
        return "Common";
    }

    @Override
    public String getName() {
        return "Simulation";
    }

    public static void setExcitedComponentColor(Color value) {
        excitedComponentColor = value;
    }

    public static Color getExcitedComponentColor() {
        return excitedComponentColor;
    }

    public static void setSuggestedComponentColor(Color value) {
        suggestedComponentColor = value;
    }

    public static Color getSuggestedComponentColor() {
        return suggestedComponentColor;
    }

}
