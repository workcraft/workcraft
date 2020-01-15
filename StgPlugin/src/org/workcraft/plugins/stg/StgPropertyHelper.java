package org.workcraft.plugins.stg;

import org.workcraft.dom.Container;
import org.workcraft.dom.references.FileReference;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.properties.TextAction;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.utils.ColorUtils;

public class StgPropertyHelper {

    public static PropertyDescriptor getSignalNameProperty(Stg stg, SignalTransition signalTransition) {
        return new PropertyDeclaration<>(String.class, "Signal name",
                value -> stg.setName(signalTransition, value),
                signalTransition::getSignalName)
                .setCombinable();
    }

    public static PropertyDescriptor getSignalTypeProperty(Stg stg, SignalTransition signalTransition) {
        return new PropertyDeclaration<>(Signal.Type.class, "Signal type",
                signalTransition::setSignalType, signalTransition::getSignalType)
                .setCombinable();
    }

    public static PropertyDescriptor getDirectionProperty(Stg stg, SignalTransition signalTransition) {
        return new PropertyDeclaration<>(SignalTransition.Direction.class, SignalTransition.PROPERTY_DIRECTION,
                value -> stg.setDirection(signalTransition, value),
                () -> stg.getDirection(signalTransition))
                .setCombinable();
    }

    public static PropertyDescriptor getInstanceProperty(Stg stg, NamedTransition namedTransition) {
        return new PropertyDeclaration<>(Integer.class, "Instance",
                value -> stg.setInstanceNumber(namedTransition, value),
                () -> stg.getInstanceNumber(namedTransition));
    }

    public static PropertyDescriptor getRefinementProperty(Stg stg) {
        return new PropertyDeclaration<>(FileReference.class, "Refinement",
                stg::setRefinement, stg::getRefinement);
    }

    public static PropertyDescriptor getSignalNameModelProperty(VisualStg visualStg, String signal, Container container) {
        Stg mathStg = visualStg.getMathModel();
        Signal.Type signalType = mathStg.getSignalType(signal, container);
        String colorCode = ColorUtils.getHexRGB(StgUtils.getTypeColor(signalType));
        return new PropertyDeclaration<>(TextAction.class,
                "<html>Signal <span style='color: " + colorCode + "'>"  + signal + "</span></html>",
                value -> {
                    String newName = value.getText();
                    if (!signal.equals(newName)) {
                        for (SignalTransition transition : mathStg.getSignalTransitions(signal, container)) {
                            mathStg.setName(transition, newName);
                        }
                    }
                },
                () -> new TextAction(signal, new Action(PropertyHelper.SEARCH_SYMBOL,
                        () -> {
                            visualStg.selectNone();
                            for (SignalTransition transition : mathStg.getSignalTransitions(signal, container)) {
                                visualStg.addToSelection(visualStg.getVisualComponent(transition, VisualSignalTransition.class));
                            }
                        }, "Select all events of signal '" + signal + "'")
                ));
    }

}
