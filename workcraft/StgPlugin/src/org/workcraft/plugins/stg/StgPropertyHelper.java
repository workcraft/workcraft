package org.workcraft.plugins.stg;

import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.references.FileReference;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.properties.TextAction;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.utils.SortUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StgPropertyHelper {

    public static PropertyDescriptor<?> getSignalNameProperty(Stg stg, SignalTransition signalTransition) {
        return new PropertyDeclaration<>(String.class, SignalTransition.PROPERTY_SIGNAL_NAME,
                value -> stg.setName(signalTransition, value),
                signalTransition::getSignalName)
                .setCombinable();
    }

    public static PropertyDescriptor<?> getSignalTypeProperty(Stg stg, SignalTransition signalTransition) {
        return new PropertyDeclaration<>(Signal.Type.class, SignalTransition.PROPERTY_SIGNAL_TYPE,
                value -> stg.setSignalType(signalTransition.getSignalName(), value),
                signalTransition::getSignalType)
                .setCombinable();
    }

    public static PropertyDescriptor<?> getDirectionProperty(Stg stg, SignalTransition signalTransition) {
        return new PropertyDeclaration<>(SignalTransition.Direction.class, SignalTransition.PROPERTY_DIRECTION,
                value -> stg.setDirection(signalTransition, value),
                signalTransition::getDirection)
                .setCombinable();
    }

    public static PropertyDescriptor<?> getInstanceProperty(Stg stg, NamedTransition namedTransition) {
        return new PropertyDeclaration<>(Integer.class, "Instance",
                value -> stg.setInstanceNumber(namedTransition, value),
                () -> stg.getInstanceNumber(namedTransition));
    }

    public static PropertyDescriptor<?> getRefinementProperty(Stg stg) {
        return new PropertyDeclaration<>(FileReference.class, "Refinement",
                stg::setRefinement, stg::getRefinement);
    }

    public static Collection<PropertyDescriptor<?>> getSignalProperties(VisualStg visualStg) {
        Collection<PropertyDescriptor<?>> result = new ArrayList<>();
        Stg stg = visualStg.getMathModel();
        Container container = NamespaceHelper.getMathContainer(visualStg, visualStg.getCurrentLevel());
        if (SignalCommonSettings.getGroupByType()) {
            for (Signal.Type type : Signal.Type.values()) {
                List<String> signalNames = SortUtils.getSortedNatural(stg.getSignalNames(type, container));
                for (final String signalName : signalNames) {
                    if (!stg.getSignalTransitions(signalName, container).isEmpty()) {
                        result.add(getSignalProperty(visualStg, signalName, container));
                    }
                }
            }
        } else {
            List<String> signalNames = SortUtils.getSortedNatural(stg.getSignalNames(container));
            for (final String signalName : signalNames) {
                if (!stg.getSignalTransitions(signalName, container).isEmpty()) {
                    result.add(getSignalProperty(visualStg, signalName, container));
                }
            }
        }
        return result;
    }

    private static PropertyDescriptor<?> getSignalProperty(VisualStg visualStg, String signal, Container container) {
        Stg mathStg = visualStg.getMathModel();
        Signal.Type signalType = mathStg.getSignalType(signal, container);
        Color color = StgUtils.getTypeColor(signalType);

        Action leftAction = new Action(PropertyHelper.BULLET_SYMBOL,
                () -> mathStg.setSignalType(signal, signalType.toggle(), container),
                "<html>Toggle type of signal <i>" + signal + "</i></html>");

        Action rightAction = new Action(PropertyHelper.SEARCH_SYMBOL,
                () -> {
                    visualStg.selectNone();
                    for (SignalTransition transition : mathStg.getSignalTransitions(signal, container)) {
                        visualStg.addToSelection(visualStg.getVisualComponent(transition, VisualSignalTransition.class));
                    }
                }, "<html>Select all events of signal <i>" + signal + "</i></html>");

        return new PropertyDeclaration<>(TextAction.class, null,
                value -> {
                    String newName = value.getText();
                    if (!signal.equals(newName)) {
                        for (SignalTransition transition : mathStg.getSignalTransitions(signal, container)) {
                            mathStg.setName(transition, newName);
                        }
                    }
                },
                () -> new TextAction(signal)
                        .setLeftAction(leftAction, true)
                        .setRightAction(rightAction, false)
                        .setForeground(color)
            ).setSpan();
    }

}
