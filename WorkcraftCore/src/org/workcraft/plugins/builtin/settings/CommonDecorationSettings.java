package org.workcraft.plugins.builtin.settings;

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

    private static final String keyHighlightingColor = prefix + ".highlightingColor";
    private static final String keySelectionColor = prefix + ".selectionColor";
    private static final String keyShadingColor = prefix + ".shadingColor";
    private static final String keySimulationExcitedComponentColor = prefix + ".simulationExcitedComponentColor";
    private static final String keySimulationSuggestedComponentColor = prefix + ".simulationSuggestedComponentColor";
    private static final String keyAnalysisProblematicComponentColor = prefix + ".analysisProblematicComponentColor";
    private static final String keyAnalysisFixerComponentColor = prefix + ".analysisFixerComponentColor";
    private static final String keyAnalysisImmaculateComponentColor = prefix + ".analysisImmaculateComponentColor";

    private static final Color defaultHighlightingColor = new Color(1.0f, 0.5f, 0.0f).brighter();
    private static final Color defaultSelectionColor = new Color(99, 130, 191).brighter();
    private static final Color defaultShadingColor = Color.LIGHT_GRAY;
    private static final Color defaultSimulationExcitedComponentColor = new Color(1.0f, 0.5f, 0.0f);
    private static final Color defaultSimulationSuggestedComponentColor = new Color(0.0f, 1.0f, 0.0f);
    private static final Color defaultAnalysisProblematicComponentColor = new Color(1.0f, 0.4f, 1.0f);
    private static final Color defaultAnalysisFixerComponentColor = new Color(1.0f, 0.8f, 0.0f);
    private static final Color defaultAnalysisImmaculateComponentColor = new Color(0.4f, 1.0f, 0.4f);

    private static Color highlightingColor = defaultHighlightingColor;
    private static Color selectionColor = defaultSelectionColor;
    private static Color shadingColor = defaultShadingColor;
    private static Color simulationExcitedComponentColor = defaultSimulationExcitedComponentColor;
    private static Color simulationSuggestedComponentColor = defaultSimulationSuggestedComponentColor;
    private static Color analysisProblematicComponentColor = defaultAnalysisProblematicComponentColor;
    private static Color analysisFixerComponentColor = defaultAnalysisFixerComponentColor;
    private static Color analysisImmaculateComponentColor = defaultAnalysisImmaculateComponentColor;

    public CommonDecorationSettings() {
        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Highlighting color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setHighlightingColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getHighlightingColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Selection color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setSelectionColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getSelectionColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Shading color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setShadingColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getShadingColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Simulation: Excited component outline color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setSimulationExcitedComponentColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getSimulationExcitedComponentColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Simulation: Suggested component background color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setSimulationSuggestedComponentColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getSimulationSuggestedComponentColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Analysis: Problematic component color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setAnalysisProblematicComponentColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getAnalysisProblematicComponentColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Analysis: Problem-fixing component color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setAnalysisFixerComponentColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getAnalysisFixerComponentColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonDecorationSettings, Color>(
                this, "Analysis: Immaculate component color", Color.class) {
            @Override
            public void setter(CommonDecorationSettings object, Color value) {
                setAnalysisImmaculateComponentColor(value);
            }
            @Override
            public Color getter(CommonDecorationSettings object) {
                return getAnalysisImmaculateComponentColor();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setHighlightingColor(config.getColor(keyHighlightingColor, defaultHighlightingColor));
        setSelectionColor(config.getColor(keySelectionColor, defaultSelectionColor));
        setShadingColor(config.getColor(keyShadingColor, defaultShadingColor));
        setSimulationExcitedComponentColor(config.getColor(keySimulationExcitedComponentColor, defaultSimulationExcitedComponentColor));
        setSimulationSuggestedComponentColor(config.getColor(keySimulationSuggestedComponentColor, defaultSimulationSuggestedComponentColor));
        setAnalysisProblematicComponentColor(config.getColor(keyAnalysisProblematicComponentColor, defaultAnalysisProblematicComponentColor));
        setAnalysisFixerComponentColor(config.getColor(keyAnalysisFixerComponentColor, defaultAnalysisFixerComponentColor));
        setAnalysisImmaculateComponentColor(config.getColor(keyAnalysisImmaculateComponentColor, defaultAnalysisImmaculateComponentColor));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyHighlightingColor, getHighlightingColor());
        config.setColor(keySelectionColor, getSelectionColor());
        config.setColor(keyShadingColor, getShadingColor());
        config.setColor(keySimulationExcitedComponentColor, getSimulationExcitedComponentColor());
        config.setColor(keySimulationSuggestedComponentColor, getSimulationSuggestedComponentColor());
        config.setColor(keyAnalysisProblematicComponentColor, getAnalysisProblematicComponentColor());
        config.setColor(keyAnalysisFixerComponentColor, getAnalysisFixerComponentColor());
        config.setColor(keyAnalysisImmaculateComponentColor, getAnalysisImmaculateComponentColor());
    }

    @Override
    public String getSection() {
        return "Common";
    }

    @Override
    public String getName() {
        return "Decoration";
    }

    public static void setHighlightingColor(Color value) {
        highlightingColor = value;
    }

    public static Color getHighlightingColor() {
        return highlightingColor;
    }

    public static void setSelectionColor(Color value) {
        selectionColor = value;
    }

    public static Color getSelectionColor() {
        return selectionColor;
    }

    public static void setShadingColor(Color value) {
        shadingColor = value;
    }

    public static Color getShadingColor() {
        return shadingColor;
    }

    public static void setSimulationExcitedComponentColor(Color value) {
        simulationExcitedComponentColor = value;
    }

    public static Color getSimulationExcitedComponentColor() {
        return simulationExcitedComponentColor;
    }

    public static void setSimulationSuggestedComponentColor(Color value) {
        simulationSuggestedComponentColor = value;
    }

    public static Color getSimulationSuggestedComponentColor() {
        return simulationSuggestedComponentColor;
    }

    public static void setAnalysisProblematicComponentColor(Color value) {
        analysisProblematicComponentColor = value;
    }

    public static Color getAnalysisProblematicComponentColor() {
        return analysisProblematicComponentColor;
    }

    public static void setAnalysisFixerComponentColor(Color value) {
        analysisFixerComponentColor = value;
    }

    public static Color getAnalysisFixerComponentColor() {
        return analysisFixerComponentColor;
    }

    public static void setAnalysisImmaculateComponentColor(Color value) {
        analysisImmaculateComponentColor = value;
    }

    public static Color getAnalysisImmaculateComponentColor() {
        return analysisImmaculateComponentColor;
    }

}
