package org.workcraft.plugins.stg;

import org.workcraft.dom.Container;
import org.workcraft.dom.references.FileReference;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.TextAction;

public class StgPropertyHelper {

    private static final String SEARCH_SYMBOL = Character.toString((char) 0x2315);

    public static PropertyDescriptor getSignalNameProperty(Stg stg, SignalTransition signalTransition) {
        return new PropertyDeclaration<>(String.class, "Signal name",
                (value) -> stg.setName(signalTransition, value),
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
                (value) -> stg.setDirection(signalTransition, value),
                () -> stg.getDirection(signalTransition))
                .setCombinable();
    }

    public static PropertyDescriptor getInstanceProperty(Stg stg, NamedTransition namedTransition) {
        return new PropertyDeclaration<>(Integer.class, "Instance",
                (value) -> stg.setInstanceNumber(namedTransition, value),
                () -> stg.getInstanceNumber(namedTransition));
    }

    public static PropertyDescriptor getRefinementProperty(Stg stg) {
        return new PropertyDeclaration<>(FileReference.class, "Refinement",
                stg::setRefinement, stg::getRefinement);
    }

    public static PropertyDescriptor getSignalNameModelProperty(VisualStg visualStg, String signal, Container container) {
        return new PropertyDeclaration<>(TextAction.class, signal + " name",
                (value) -> {
                    String newName = value.getText();
                    if (!signal.equals(newName)) {
                        Stg mathStg = visualStg.getMathModel();
                        for (SignalTransition transition : mathStg.getSignalTransitions(signal, container)) {
                            mathStg.setName(transition, newName);
                        }
                    }
                },
                () -> new TextAction(signal, new Action(SEARCH_SYMBOL,
                        () -> {
                            visualStg.selectNone();
                            Stg mathStg = visualStg.getMathModel();
                            for (SignalTransition transition : mathStg.getSignalTransitions(signal, container)) {
                                visualStg.getVisualComponent(transition, VisualSignalTransition.class);
                                visualStg.addToSelection(visualStg.getVisualComponent(transition, VisualSignalTransition.class));
                            }
                        }))
        );
    }

    public static PropertyDescriptor getSignalTypeModelProperty(Stg stg, String signal, Container container) {
        return new PropertyDeclaration<>(Signal.Type.class, signal + " type",
                (value) -> stg.setSignalType(signal, value, container),
                () -> stg.getSignalType(signal, container));
    }

}
