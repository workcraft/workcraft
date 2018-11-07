package org.workcraft.plugins.shared;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.Settings;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class CommonDecorationSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonDecorationSettings";

    private static final String keyHighlightedComponentColor = prefix + ".highlightedComponentColor";
    private static final String keySelectedComponentColor = prefix + ".selectedComponentColor";
    private static final String keyShadedComponentColor = prefix + ".shadedComponentColor";
    private static final String keyExcitedComponentColor = prefix + ".excitedComponentColor";
    private static final String keySuggestedComponentColor = prefix + ".suggestedComponetColor";

    private static final Color deafultHighlightedComponentColor = new Color(1.0f, 0.5f, 0.0f).brighter();
    private static final Color deafultSelectedComponentColor = new Color(99, 130, 191).brighter();
    private static final Color deafultShadedComponentColor = Color.LIGHT_GRAY;
    private static final Color deafultExcitedComponentColor = new Color(1.0f, 0.5f, 0.0f);
    private static final Color deafultSuggestedComponentColor = new Color(0.0f, 1.0f, 0.0f);

    private static Color highlightedComponentColor = deafultHighlightedComponentColor;
    private static Color selectedComponentColor = deafultSelectedComponentColor;
    private static Color shadedComponentColor = deafultShadedComponentColor;
    private static Color excitedComponentColor = deafultExcitedComponentColor;
    private static Color suggestedComponentColor = deafultSuggestedComponentColor;

    public CommonDecorationSettings() {
        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Highlighted component color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setHighlightedComponentColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getHighlightedComponentColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Selected component color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setSelectedComponentColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getSelectedComponentColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Shaded component color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setShadedComponentColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getShadedComponentColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Excited component color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setExcitedComponentColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getExcitedComponentColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Suggested component background", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setSuggestedComponentColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
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
        setHighlightedComponentColor(config.getColor(keyHighlightedComponentColor, deafultHighlightedComponentColor));
        setSelectedComponentColor(config.getColor(keySelectedComponentColor, deafultSelectedComponentColor));
        setShadedComponentColor(config.getColor(keyShadedComponentColor, deafultShadedComponentColor));
        setExcitedComponentColor(config.getColor(keyExcitedComponentColor, deafultExcitedComponentColor));
        setSuggestedComponentColor(config.getColor(keySuggestedComponentColor, deafultSuggestedComponentColor));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyHighlightedComponentColor, getHighlightedComponentColor());
        config.setColor(keySelectedComponentColor, getSelectedComponentColor());
        config.setColor(keyShadedComponentColor, getShadedComponentColor());
        config.setColor(keyExcitedComponentColor, getExcitedComponentColor());
        config.setColor(keySuggestedComponentColor, getSuggestedComponentColor());
    }

    @Override
    public String getSection() {
        return "Common";
    }

    @Override
    public String getName() {
        return "Decoration";
    }

    public static void setHighlightedComponentColor(Color value) {
        highlightedComponentColor = value;
    }

    public static Color getHighlightedComponentColor() {
        return highlightedComponentColor;
    }

    public static void setSelectedComponentColor(Color value) {
        selectedComponentColor = value;
    }

    public static Color getSelectedComponentColor() {
        return selectedComponentColor;
    }

    public static void setShadedComponentColor(Color value) {
        shadedComponentColor = value;
    }

    public static Color getShadedComponentColor() {
        return shadedComponentColor;
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
