package org.workcraft.plugins.circuit.utils;

import org.workcraft.formula.And;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.Not;
import org.workcraft.formula.Or;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.stg.Mutex;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class MutexUtils {

    public static BooleanFormula getGrantSet(Mutex.Protocol mutexProtocol, VisualFunctionContact reqContact,
            VisualFunctionContact otherGrantContact, VisualFunctionContact otherReqContact) {

        return getGrantSet(mutexProtocol, reqContact.getReferencedComponent(),
                otherGrantContact.getReferencedComponent(), otherReqContact.getReferencedComponent());
    }

    public static BooleanFormula getGrantSet(Mutex.Protocol mutexProtocol, BooleanFormula reqContact,
            BooleanFormula otherGrantContact, BooleanFormula otherReqContact) {

        BooleanFormula result = new And(reqContact, new Not(otherGrantContact));
        if (mutexProtocol == Mutex.Protocol.EARLY) {
            result = new Or(result, new And(reqContact, new Not(otherReqContact)));
        }
        return result;
    }

    public static BooleanFormula getGrantReset(VisualFunctionContact reqContact) {
        return getGrantReset(reqContact.getReferencedComponent());
    }

    public static BooleanFormula getGrantReset(BooleanFormula reqContact) {
        return new Not(reqContact);
    }

    public static String appendProtocolSuffix(String name, Mutex.Protocol protocol) {
        String result = name == null ? "" : name;
        if (protocol == Mutex.Protocol.LATE) {
            result += CircuitSettings.getMutexLateSuffix();
        }
        if (protocol == Mutex.Protocol.EARLY) {
            result += CircuitSettings.getMutexEarlySuffix();
        }
        return result;
    }

    public static Set<String> getMutexModuleNames() {
        Mutex mutex = CircuitSettings.parseMutexData();
        return Arrays.stream(Mutex.Protocol.values())
                .map(protocol -> appendProtocolSuffix(mutex.name, protocol))
                .collect(Collectors.toSet());
    }

}
