package org.workcraft.plugins.xbm.properties;

import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.xbm.XbmSignal;
import org.workcraft.plugins.xbm.SignalState;
import org.workcraft.plugins.xbm.XbmState;

import java.util.Map;

public class SignalModifierDescriptors {

    private static final String PROPERTY_TOGGLE = "Toggle values";
    private static final String PROPERTY_ALL_ONE = "Set all to 1";
    private static final String PROPERTY_ALL_ZERO = "Set all to 0";
    private static final String PROPERTY_INPUTS_TO_ONE = "Set all inputs to 1";
    private static final String PROPERTY_INPUTS_TO_ZERO = "Set all inputs to 0";
    private static final String PROPERTY_OUTPUTS_TO_ONE = "Set all outputs to 1";
    private static final String PROPERTY_OUTPUTS_TO_ZERO = "Set all outputs to 0";

    public static PropertyDescriptor toggleProperty(XbmState state) {
        return new PropertyDeclaration<XbmState, Boolean>(state, PROPERTY_TOGGLE, Boolean.class, true, true) {
            @Override
            public void setter(XbmState object, Boolean value) {
                if (value) {
                    for (Map.Entry<XbmSignal, SignalState> entry: object.getEncoding().entrySet()) {
                        object.addOrChangeSignalValue(entry.getKey(), entry.getValue().toggle());
                    }
                }
            }

            @Override
            public Boolean getter(XbmState object) {
                return false;
            }
        };
    }

    public static PropertyDescriptor allOneProperty(XbmState state) {
        return setToValueProperty(state, PROPERTY_ALL_ONE, SignalState.HIGH);
    }

    public static PropertyDescriptor allZeroProperty(XbmState state) {
        return setToValueProperty(state, PROPERTY_ALL_ZERO, SignalState.LOW);
    }

    public static PropertyDescriptor allInputsOneProperty(XbmState state) {
        return setToValueByTypeProperty(state, XbmSignal.Type.INPUT, PROPERTY_INPUTS_TO_ONE, SignalState.HIGH);
    }

    public static final PropertyDescriptor allInputsZeroProperty(XbmState state) {
        return setToValueByTypeProperty(state, XbmSignal.Type.INPUT, PROPERTY_INPUTS_TO_ZERO, SignalState.LOW);
    }

    public static PropertyDescriptor allOutputsOneProperty(XbmState state) {
        return setToValueByTypeProperty(state, XbmSignal.Type.OUTPUT, PROPERTY_OUTPUTS_TO_ONE, SignalState.HIGH);
    }

    public static PropertyDescriptor allOutputsZeroProperty(XbmState state) {
        return setToValueByTypeProperty(state, XbmSignal.Type.OUTPUT, PROPERTY_OUTPUTS_TO_ZERO, SignalState.LOW);
    }

    private static PropertyDescriptor setToValueProperty(XbmState state, String propertyName, SignalState targetValue) {
        return new PropertyDeclaration<XbmState, Boolean>(state, propertyName, Boolean.class, true, true) {
            @Override
            public void setter(XbmState object, Boolean value) {
                if (value) {
                    setSignalsToValue(object, targetValue);
                }
            }

            @Override
            public Boolean getter(XbmState object) {
                return false;
            }
        };
    }

    private static PropertyDescriptor setToValueByTypeProperty(XbmState state, XbmSignal.Type type, String propertyName, SignalState targetValue) {
        return new PropertyDeclaration<XbmState, Boolean>(state, propertyName, Boolean.class, true, true) {
            @Override
            public void setter(XbmState object, Boolean value) {
                if (value) {
                    setSignalsToValueByType(object, type, targetValue);
                }
            }

            @Override
            public Boolean getter(XbmState object) {
                return false;
            }
        };
    }

    private static void setSignalsToValue(XbmState state, SignalState value) {
        for (Map.Entry<XbmSignal, SignalState> entry: state.getEncoding().entrySet()) {
            if (entry.getValue() != value) {
                state.addOrChangeSignalValue(entry.getKey(), value);
            }
        }
    }

    private static void setSignalsToValueByType(XbmState state, XbmSignal.Type type, SignalState value) {
        for (Map.Entry<XbmSignal, SignalState> entry: state.getEncoding().entrySet()) {
            if (entry.getKey().getType() == type && entry.getValue() != value) {
                state.addOrChangeSignalValue(entry.getKey(), value);
            }
        }
    }
}