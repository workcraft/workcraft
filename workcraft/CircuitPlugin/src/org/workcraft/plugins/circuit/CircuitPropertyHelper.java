package org.workcraft.plugins.circuit;

import org.workcraft.dom.Model;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.properties.TextAction;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.circuit.refinement.ComponentInterface;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.*;
import org.workcraft.workspace.ModelEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CircuitPropertyHelper {

    private static final String PROPERTY_ENVIRONMENT = "Environment";
    private static final String REFINEMENT_MODEL_TITLE = "Refinement model";

    public static Collection<PropertyDescriptor> getComponentProperties(VisualCircuit circuit) {
        List<VisualFunctionComponent> components = new ArrayList<>(Hierarchy.getChildrenOfType(
                circuit.getCurrentLevel(), VisualFunctionComponent.class));

        components.sort((component1, component2) -> SortUtils.compareNatural(
                circuit.getMathName(component1), circuit.getMathName(component2)));

        Collection<PropertyDescriptor> result = new ArrayList<>();
        for (VisualFunctionComponent component : components) {
            result.add(getComponentProperty(circuit, component));
        }
        return result;
    }

    private static PropertyDescriptor getComponentProperty(VisualCircuit circuit, VisualFunctionComponent component) {
        String name = Identifier.truncateNamespaceSeparator(circuit.getMathName(component));

        Action rightAction = new Action(PropertyHelper.SEARCH_SYMBOL,
                () -> circuit.select(component),
                "Select component '" + name + "'");

        return new PropertyDeclaration<>(TextAction.class, null,
                value -> {
                    String newName = value.getText();
                    if (!name.equals(newName)) {
                        circuit.setMathName(component, newName);
                        component.sendNotification(new PropertyChangedEvent(component, Model.PROPERTY_NAME));
                    }
                },
                () -> new TextAction(name).setRightAction(rightAction)
            ).setSpan();
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

    private static void setRefinementIfCompatible(VisualCircuit circuit, VisualFunctionComponent component, FileReference value) {
        if (value == null) {
            component.getReferencedComponent().setRefinement(null);
            return;
        }

        MathModel refinementModel;
        File file = value.getFile();
        try {
            ModelEntry me = WorkUtils.loadModel(file);
            refinementModel = me.getMathModel();
        } catch (DeserialisationException e) {
            String path = FileUtils.getFullPath(file);
            DialogUtils.showError("Cannot read refinement model from '" + path + "':\n " + e.getMessage(),
                    REFINEMENT_MODEL_TITLE);
            return;
        }

        if (!(refinementModel instanceof Stg) && !(refinementModel instanceof Circuit)) {
            DialogUtils.showError("Incompatible refinement model type: " + refinementModel.getDisplayName(),
                    REFINEMENT_MODEL_TITLE);
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
                    REFINEMENT_MODEL_TITLE, 0);

            if (answer == 2) {
                return;
            }
            if (answer == 0) {
                component.getReferencedComponent().setModule(refinementTitle);
            }
        }

        ComponentInterface componentInterface = RefinementUtils.getComponentInterface(component.getReferencedComponent());
        ComponentInterface refinementInterface = RefinementUtils.getModelInterface(refinementModel);
        if (!RefinementUtils.isInconsistentSignalNames(componentInterface, refinementInterface)) {
            Set<String> missingPins = componentInterface.getMissingSignals(refinementInterface);
            Set<String> extraPins = componentInterface.getExtraSignals(refinementInterface);
            Set<String> mismatchPins = componentInterface.getMismatchSignals(refinementInterface);
            int answer = DialogUtils.showYesNoCancel("Refinement interface signals do not match component pins."
                            + getBulletPair("Missing component pin", missingPins)
                            + getBulletPair("Unexpected component pin", extraPins)
                            + getBulletPair("Incorrect I/O type pin", mismatchPins)
                            + "\n\nUpdate component pins to match refinement model signals?",
                    REFINEMENT_MODEL_TITLE, 0);

            if (answer == 2) {
                return;
            }
            if (answer == 0) {
                RefinementUtils.updateInterface(circuit, component, refinementInterface);
            }
        }

        Set<String> constrainedPins = RefinementUtils.getConstrainedPins(component);
        if (!constrainedPins.isEmpty()) {
            int answer = DialogUtils.showYesNoCancel("Component has constrained pins that may conflict with the refinement model."
                            + "\n\nRemove set/reset functions for the component pins?",
                    REFINEMENT_MODEL_TITLE, 0);

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
        return new PropertyDeclaration<String>(String.class, FunctionContact.PROPERTY_SET_FUNCTION,
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
        return new PropertyDeclaration<String>(String.class, FunctionContact.PROPERTY_RESET_FUNCTION,
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
