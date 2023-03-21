package org.workcraft.plugins.circuit;

import org.workcraft.dom.Model;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.*;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.AnalysisDecorationSettings;
import org.workcraft.plugins.circuit.refinement.ComponentInterface;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.utils.*;
import org.workcraft.workspace.ModelEntry;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

public class CircuitPropertyHelper {

    private static final String PROPERTY_ENVIRONMENT = "Environment";

    public static Collection<PropertyDescriptor> getComponentProperties(VisualCircuit circuit) {
        List<VisualFunctionComponent> components = new ArrayList<>(Hierarchy.getChildrenOfType(
                circuit.getCurrentLevel(), VisualFunctionComponent.class));

        components.sort((component1, component2) -> SortUtils.compareNatural(
                circuit.getMathName(component1), circuit.getMathName(component2)));

        Collection<PropertyDescriptor> result = new ArrayList<>();
        if (!components.isEmpty()) {
            result.add(PropertyHelper.createSeparatorProperty("Components with color-coded refinement type"));
            result.add(getRefinementLegendProperty());
        }
        for (VisualFunctionComponent component : components) {
            result.add(getComponentProperty(circuit, component));
        }
        return result;
    }

    private static PropertyDescriptor getRefinementLegendProperty() {
        Color cellColor = GuiUtils.getTableCellBackgroundColor();

        return new LegendListDeclaration()
                .addLegend("none", ColorUtils.colorise(cellColor, Color.WHITE))
                .addLegend("STG", ColorUtils.colorise(cellColor, AnalysisDecorationSettings.getClearColor()))
                .addLegend("circuit", ColorUtils.colorise(cellColor, AnalysisDecorationSettings.getFixerColor()))
                .addLegend("error", ColorUtils.colorise(cellColor, AnalysisDecorationSettings.getProblemColor()))
                .setReadonly();
    }

    private static PropertyDescriptor getComponentProperty(VisualCircuit circuit, VisualFunctionComponent component) {
        String name = Identifier.truncateNamespaceSeparator(circuit.getMathName(component));

        Action rightAction = new Action(PropertyHelper.SEARCH_SYMBOL,
                () -> circuit.select(component),
                "Select component '" + name + "'");

        Color backgroundColor = getComponentPropertyBackgroundColor(component);

        return new PropertyDeclaration<>(TextAction.class, null,
                value -> {
                    String newName = value.getText();
                    if (!name.equals(newName)) {
                        circuit.setMathName(component, newName);
                        component.sendNotification(new PropertyChangedEvent(component, Model.PROPERTY_NAME));
                    }
                },
                () -> new TextAction(name).setRightAction(rightAction).setBackground(backgroundColor)
            ).setSpan();
    }

    private static Color getComponentPropertyBackgroundColor(VisualFunctionComponent component) {
        Color color = null;
        File refinementFile = component == null ? null : component.getReferencedComponent().getRefinementFile();
        if (refinementFile != null) {
            ModelDescriptor modelDescriptor = null;
            if (FileUtils.isReadableFile(refinementFile)) {
                try {
                    modelDescriptor = WorkUtils.extractModelDescriptor(refinementFile);
                } catch (DeserialisationException ignored) {
                }
            }
            if (modelDescriptor instanceof StgDescriptor) {
                color = AnalysisDecorationSettings.getClearColor();
            } else if (modelDescriptor instanceof CircuitDescriptor) {
                color = AnalysisDecorationSettings.getFixerColor();
            } else {
                color = AnalysisDecorationSettings.getProblemColor();
            }
        }
        return ColorUtils.colorise(GuiUtils.getTableCellBackgroundColor(), color);
    }

    public static PropertyDescriptor getEnvironmentProperty(VisualCircuit circuit) {
        return new PropertyDeclaration<>(FileReference.class, PROPERTY_ENVIRONMENT,
                circuit.getMathModel()::setEnvironment, circuit.getMathModel()::getEnvironment);
    }

    public static PropertyDescriptor getRefinementProperty(VisualCircuit circuit, VisualFunctionComponent component) {
        return new PropertyDeclaration<>(FileReference.class, CircuitComponent.PROPERTY_REFINEMENT,
                value -> setRefinementIfCompatible(circuit, component, value),
                () -> component.getReferencedComponent().getRefinement());
    }

    public static void setRefinementIfCompatible(VisualCircuit circuit, VisualFunctionComponent component, FileReference value) {
        if (value == null) {
            component.getReferencedComponent().setRefinement(null);
            return;
        }

        String title = "Refinement model for component "
                + Identifier.truncateNamespaceSeparator(circuit.getMathReference(component));

        MathModel refinementModel;
        File file = value.getFile();
        try {
            ModelEntry me = WorkUtils.loadModel(file);
            if (me == null) {
                throw new DeserialisationException();
            }
            refinementModel = me.getMathModel();
        } catch (DeserialisationException e) {
            String path = FileUtils.getFullPath(file);
            String details = e.getMessage();
            String msg = "Cannot read refinement model from '" + path + "'"
                    + ((details == null) || details.isEmpty() ? "" : (":\n " + details));

            DialogUtils.showError(msg, title);
            return;
        }

        if (!(refinementModel instanceof Stg) && !(refinementModel instanceof Circuit)) {
            DialogUtils.showError("Incompatible refinement model type: " + refinementModel.getDisplayName(),
                    title);
            return;
        }

        String refinementTitle = refinementModel.getTitle();
        String componentLabel = component.getLabel();
        if (RefinementUtils.isInconsistentModelTitle(componentLabel, refinementTitle)) {
            int answer = DialogUtils.showYesNoCancel(
                    "Refinement title does not match component label."
                            + "\n" + getBulletPair("Refinement title", refinementTitle)
                            + "\n" + getBulletPair("Component label", componentLabel)
                            + "\n\nUpdate component label to match refinement title?",
                    title, 0);

            if (answer == 2) {
                return;
            }
            if (answer == 0) {
                component.getReferencedComponent().setModule(refinementTitle);
            }
        }

        ComponentInterface componentInterface = RefinementUtils.getComponentInterface(component.getReferencedComponent());
        ComponentInterface refinementInterface = RefinementUtils.getModelInterface(refinementModel);
        if (RefinementUtils.isInconsistentSignalNames(componentInterface, refinementInterface)) {
            Set<String> missingPins = componentInterface.getMissingSignals(refinementInterface);
            Set<String> extraPins = componentInterface.getExtraSignals(refinementInterface);
            Set<String> mismatchPins = componentInterface.getMismatchSignals(refinementInterface);
            int answer = DialogUtils.showYesNoCancel("Refinement interface signals do not match component pins."
                            + getBulletPair("Missing component pin", missingPins)
                            + getBulletPair("Unexpected component pin", extraPins)
                            + getBulletPair("Incorrect I/O type pin", mismatchPins)
                            + "\n\nUpdate component pins to match refinement model signals?",
                    title, 0);

            if (answer == 2) {
                return;
            }
            if (answer == 0) {
                RefinementUtils.updateInterface(circuit, component, refinementInterface);
            }
        }

        ComponentInterface updatedComponentInterface = RefinementUtils.getComponentInterface(
                component.getReferencedComponent());

        Set<String> matchingOutputSignals = updatedComponentInterface.getMatchingOutputSignals(refinementInterface);

        Map<String, Boolean> componentOutputInitialState = RefinementUtils.getComponentSignalsInitialState(
                circuit.getMathModel(), component.getReferencedComponent(), matchingOutputSignals);

        Map<String, Boolean> refinementOutputInitialState = RefinementUtils.getSignalsInitialState(
                refinementModel, matchingOutputSignals);

        Set<String> inconsistentInitialStateOutputs = RefinementUtils.getSignalsWithInconsistentStates(
                componentOutputInitialState, refinementOutputInitialState);

        if (!inconsistentInitialStateOutputs.isEmpty()) {
            int answer = DialogUtils.showYesNoCancel("Initial state of output signals in the refinement model differs\n"
                            + "from the initial state of the component output pins."
                            + getBulletPair("Inconsistent initial state pin", inconsistentInitialStateOutputs)
                            + "\n\nUpdate initial state of the component output pins?",
                    title, 0);

            if (answer == 2) {
                return;
            }
            if (answer == 0) {
                RefinementUtils.updateInitialState(component.getReferencedComponent(), refinementOutputInitialState);
            }

        }

        Set<String> constrainedPins = RefinementUtils.getConstrainedPins(component);
        if (!constrainedPins.isEmpty()) {
            int answer = DialogUtils.showYesNoCancel("Component has constrained pins that may conflict with the refinement model."
                            + "\n\nRemove set/reset functions for the component pins?",
                    title, 0);

            if (answer == 2) {
                return;
            }
            if (answer == 0) {
                RefinementUtils.removeComponentFunctions(component);
            }
        }

        component.getReferencedComponent().setRefinement(value);
    }

    private static String getBulletPair(String key, String value) {
        return PropertyHelper.BULLET_PREFIX + key + ((value == null) || value.isEmpty() ? " is empty" : (": " + value));
    }

    private static String getBulletPair(String key, Set<String> value) {
        if (value.isEmpty()) {
            return "";
        }
        return "\n" + PropertyHelper.BULLET_PREFIX + key + (value.size() > 1 ? "s: " : ": ") + TextUtils.wrapItems(value);
    }

    public static PropertyDescriptor getSetFunctionProperty(VisualCircuit circuit, VisualFunctionContact contact) {
        return new PropertyDeclaration<>(String.class, FunctionContact.PROPERTY_SET_FUNCTION,
                value -> {
                    try {
                        contact.setSetFunction(CircuitUtils.parseContactFunction(circuit, contact, value));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> StringGenerator.toString(contact.getSetFunction())) {
            @Override
            public boolean isVisible() {
                return contact.isDriver();
            }
        }.setCombinable();
    }

    public static PropertyDescriptor getResetFunctionProperty(VisualCircuit circuit, VisualFunctionContact contact) {
        return new PropertyDeclaration<>(String.class, FunctionContact.PROPERTY_RESET_FUNCTION,
                value -> {
                    try {
                        contact.setResetFunction(CircuitUtils.parseContactFunction(circuit, contact, value));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> StringGenerator.toString(contact.getResetFunction())) {
            @Override
            public boolean isVisible() {
                return contact.isDriver();
            }
        }.setCombinable();
    }

}
