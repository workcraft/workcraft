package org.workcraft.plugins.stg.properties;

import org.workcraft.dom.Container;
import org.workcraft.dom.references.FileReference;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.stg.NamedTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;

public class PropertyDescriptors {

    public static final String PROPERTY_REFINEMENT = "Refinement";

    public static PropertyDescriptor getSignalTypeProperty(Stg stg, String signal, Container container) {
        return new PropertyDeclaration<Stg, Signal.Type>(
                stg, signal + " type", Signal.Type.class, false, false) {
            @Override
            public Signal.Type getter(Stg object) {
                return stg.getSignalType(signal, container);
            }
            @Override
            public void setter(Stg object, Signal.Type value) {
                stg.setSignalType(signal, value, container);
            }
        };
    }

    public static PropertyDescriptor getSignalNameProperty(Stg stg, SignalTransition signalTransition) {
        return new PropertyDeclaration<SignalTransition, String>(
                signalTransition, "Signal name", String.class, true, false) {
            @Override
            public String getter(SignalTransition object) {
                return object.getSignalName();
            }

            @Override
            public void setter(SignalTransition object, String value) {
                stg.setName(object, value);
            }
        };
    }

    public static PropertyDescriptor getSignalTypeProperty(Stg stg, SignalTransition signalTransition) {
        return new PropertyDeclaration<SignalTransition, Signal.Type>(
                signalTransition, "Signal type", Signal.Type.class, true, false) {
            @Override
            public Signal.Type getter(SignalTransition object) {
                return object.getSignalType();
            }

            @Override
            public void setter(SignalTransition object, Signal.Type value) {
                object.setSignalType(value);
            }
        };
    }

    public static PropertyDescriptor getDirectionProperty(Stg stg, SignalTransition signalTransition) {
        return new PropertyDeclaration<SignalTransition, SignalTransition.Direction>(
                signalTransition, SignalTransition.PROPERTY_DIRECTION, SignalTransition.Direction.class, true, false) {
            @Override
            public SignalTransition.Direction getter(SignalTransition object) {
                return stg.getDirection(object);
            }

            @Override
            public void setter(SignalTransition object, SignalTransition.Direction value) {
                stg.setDirection(object, value);
            }
        };
    }

    public static PropertyDescriptor getInstanceProperty(Stg stg, NamedTransition namedTransition) {
        return new PropertyDeclaration<NamedTransition, Integer>(
                namedTransition, "Instance", Integer.class) {
            @Override
            public Integer getter(NamedTransition object) {
                return stg.getInstanceNumber(object);
            }

            @Override
            public void setter(NamedTransition object, Integer value) {
                stg.setInstanceNumber(object, value);
            }
        };
    }

    public static PropertyDescriptor getRefinementProperty(Stg stg) {
        return new PropertyDeclaration<Stg, FileReference>(
                stg, PROPERTY_REFINEMENT, FileReference.class) {
            @Override
            public void setter(Stg object, FileReference value) {
                object.setRefinement(value);
            }
            @Override
            public FileReference getter(Stg object) {
                return object.getRefinement();
            }
        };
    }

}
