package org.workcraft.plugins.circuit.utils;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgSettings;

public class MutexUtils {

    private static final String SET_SUFFIX = Character.toString((char) 0x2191);
    private static final String RESET_SUFFIX = Character.toString((char) 0x2193);

    public static String getGrantSetReset(String grantName, String set, String reset) {
        return grantName + SET_SUFFIX + " = " + set + " ; " + grantName + RESET_SUFFIX + " = " + reset;
    }

    public static String getGrantSet(String reqName, String otherGrantName, String otherReqName) {
        String result = reqName + " * " + otherGrantName + "'";
        if (StgSettings.getMutexProtocol() == Mutex.Protocol.RELAXED) {
            result += " + " + reqName + " * " + otherReqName + "'";
        }
        return result;
    }

    public static String getGrantReset(String reqName) {
        return reqName + "'";
    }

    public static void setMutexFunctions(VisualCircuit circuit, VisualFunctionComponent component,
            VisualFunctionContact grantContact, String setString, String resetString) {

        setMutexFunctions(circuit.getMathModel(), component.getReferencedComponent(),
                grantContact.getReferencedComponent(), setString, resetString);
    }

    public static void setMutexFunctions(Circuit circuit, FunctionComponent component,
            FunctionContact grantContact, String setString, String resetString) {

        try {
            BooleanFormula setFormula = CircuitUtils.parsePinFuncton(circuit, component, setString);
            grantContact.setSetFunctionQuiet(setFormula);

            BooleanFormula resetFormula = CircuitUtils.parsePinFuncton(circuit, component, resetString);
            grantContact.setResetFunctionQuiet(resetFormula);
        } catch (org.workcraft.formula.jj.ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
