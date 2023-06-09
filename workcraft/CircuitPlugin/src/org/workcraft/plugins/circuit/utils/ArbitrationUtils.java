package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.math.MathNode;
import org.workcraft.formula.And;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.Not;
import org.workcraft.formula.Or;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Wait;
import org.workcraft.types.Pair;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

public class ArbitrationUtils {

    public static void assignWaitFunctions(Wait.Type type, VisualFunctionContact sigContact,
            VisualFunctionContact ctrlContact, VisualFunctionContact sanContact) {

        assignWaitFunctions(type, sigContact.getReferencedComponent(),
                ctrlContact.getReferencedComponent(), sanContact.getReferencedComponent());
    }

    public static void assignWaitFunctions(Wait.Type type, FunctionContact sigContact,
            FunctionContact ctrlContact, FunctionContact sanContact) {

        BooleanFormula setFormula = new And(ctrlContact, type == Wait.Type.WAIT0 ? new Not(sigContact) : sigContact);
        sanContact.setSetFunctionQuiet(setFormula);

        BooleanFormula resetFormula = new Not(ctrlContact);
        sanContact.setResetFunctionQuiet(resetFormula);
    }

    public static Set<String> getWaitModuleNames() {
        Set<String> result = new HashSet<>();
        for (Wait.Type type : Wait.Type.values()) {
            Wait module = CircuitSettings.parseWaitData(type);
            if ((module != null) && (module.name != null)) {
                result.add(module.name);
            }
        }
        return result;
    }

    public static Wait getWaitModule(String moduleName) {
        for (Wait.Type type : Wait.Type.values()) {
            Wait module = CircuitSettings.parseWaitData(type);
            if ((module != null) && (module.name != null) && module.name.equals(moduleName)) {
                return module;
            }
        }
        return null;
    }

    public static void assignMutexFunctions(Mutex.Protocol protocol,
            VisualFunctionContact r1Contact, VisualFunctionContact g1Contact,
            VisualFunctionContact r2Contact, VisualFunctionContact g2Contact) {

        assignMutexFunctions(protocol,
                r1Contact.getReferencedComponent(), g1Contact.getReferencedComponent(),
                r2Contact.getReferencedComponent(), g2Contact.getReferencedComponent());
    }

    public static void assignMutexFunctions(Mutex.Protocol protocol,
            FunctionContact r1Contact, FunctionContact g1Contact,
            FunctionContact r2Contact, FunctionContact g2Contact) {

        BooleanFormula g1SetFormula = getMutexGrantSetFunction(protocol, r1Contact, g2Contact, r2Contact);
        g1Contact.setSetFunctionQuiet(g1SetFormula);

        BooleanFormula g1ResetFormula = getMutexGrantResetFunction(r1Contact);
        g1Contact.setResetFunctionQuiet(g1ResetFormula);

        BooleanFormula g2SetFormula = getMutexGrantSetFunction(protocol, r2Contact, g1Contact, r1Contact);
        g2Contact.setSetFunctionQuiet(g2SetFormula);

        BooleanFormula g2ResetFormula = getMutexGrantResetFunction(r2Contact);
        g2Contact.setResetFunctionQuiet(g2ResetFormula);
    }

    private static BooleanFormula getMutexGrantSetFunction(Mutex.Protocol mutexProtocol, BooleanFormula reqContact,
            BooleanFormula otherGrantContact, BooleanFormula otherReqContact) {

        return mutexProtocol == Mutex.Protocol.EARLY
                ? new And(reqContact, new Or(new Not(otherGrantContact), new Not(otherReqContact)))
                : new And(reqContact, new Not(otherGrantContact));
    }

    private static BooleanFormula getMutexGrantResetFunction(BooleanFormula reqContact) {
        return new Not(reqContact);
    }

    public static String appendMutexProtocolSuffix(String name, Mutex.Protocol protocol) {
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
                .map(protocol -> appendMutexProtocolSuffix(mutex.name, protocol))
                .collect(Collectors.toSet());
    }

    public static LinkedList<Pair<String, String>> getMutexGrantPersistencyExceptions(Circuit circuit) {
        LinkedList<Pair<String, String>> grantPairs = new LinkedList<>();
        Set<String> mutexModuleNames = ArbitrationUtils.getMutexModuleNames();
        Mutex mutexModule = CircuitSettings.parseMutexData();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getIsArbitrationPrimitive()) {
                String moduleName = component.getModule();
                if (mutexModuleNames.contains(moduleName)) {
                    String g1Signal = getPinSignal(circuit, component, mutexModule.g1.name);
                    String g2Signal = getPinSignal(circuit, component, mutexModule.g2.name);
                    if ((g1Signal != null) && (g2Signal != null)) {
                        grantPairs.add(Pair.of(g1Signal, g2Signal));
                        grantPairs.add(Pair.of(g2Signal, g1Signal));
                    }
                }
            }
        }
        return grantPairs;
    }

    public static LinkedList<Pair<String, String>> getWaitPersistencyExceptions(Circuit circuit,
            boolean useInternalSignal) {

        LinkedList<Pair<String, String>> grantPairs = new LinkedList<>();
        Set<String> waitModuleNames = ArbitrationUtils.getWaitModuleNames();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getIsArbitrationPrimitive()) {
                String moduleName = component.getModule();
                if (waitModuleNames.contains(moduleName)) {
                    Wait waitModule = getWaitModule(moduleName);
                    if (waitModule != null) {
                        String sanSignal = getPinSignal(circuit, component, waitModule.san.name);
                        if (useInternalSignal && (sanSignal != null)) {
                            String intRef = circuit.getNodeReference(component) + "internal";
                            grantPairs.add(Pair.of(sanSignal, intRef));
                            grantPairs.add(Pair.of(intRef, sanSignal));
                        }
                        String sigSignal = getPinSignal(circuit, component, waitModule.sig.name);
                        if ((sigSignal != null) && (sanSignal != null)) {
                            grantPairs.add(Pair.of(sigSignal, sanSignal));
                        }
                    }
                }
            }
        }
        return grantPairs;
    }

    private static String getPinSignal(Circuit circuit, CircuitComponent component, String pinName) {
        MathNode node = circuit.getNodeByReference(component, pinName);
        if (node instanceof Contact) {
            Contact signal = CircuitUtils.findSignal(circuit, (Contact) node, true);
            return CircuitUtils.getSignalReference(circuit, signal);
        }
        return null;
    }

}
