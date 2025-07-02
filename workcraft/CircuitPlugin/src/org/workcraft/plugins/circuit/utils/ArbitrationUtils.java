package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.formula.*;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.Wait;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;

import java.util.*;

public class ArbitrationUtils {

    public record WaitData(
            Wait.Type waitType,
            Contact sigInputPin,
            Contact ctrlInputPin,
            Contact sanOutputPin) { }

    public record MutexData(
            Mutex.Protocol mutexProtocol,
            Contact r1InputPin,
            Contact g1OutputPin,
            Contact r2InputPin,
            Contact g2OutputPin) { }


    public static WaitData getWaitData(Circuit circuit, FunctionComponent component,
            String errorPrefixOrNullToSilence) {

        Collection<FunctionContact> outputPins = component.getFunctionOutputs();
        if (outputPins.size() != 1) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : expected 1 output pin");
            }
            return null;
        }

        FunctionContact sanPin = outputPins.iterator().next();
        if (sanPin == null) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : problem with san output pin");
            }
            return null;
        }

        BooleanFormula setFunction = sanPin.getSetFunction();
        BooleanFormula resetFunction = sanPin.getResetFunction();
        if ((setFunction == null) || (resetFunction == null)) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : missing set / reset function on san output pin");
            }
            return null;
        }

        FreeVariable ctrlVar = new FreeVariable("ctrl");
        BooleanFormula sanWaitResetFunction = new Not(ctrlVar);
        Map<BooleanVariable, String> resetVarMapping
                = GenlibUtils.getVariableMappingIfEquivalentOrNull(sanWaitResetFunction, resetFunction);

        if (resetVarMapping == null) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : incorrect reset function on san output pin");
            }
            return null;
        }

        String ctrlPinName = resetVarMapping.get(ctrlVar);
        Contact ctrlPin = ctrlPinName == null ? null : circuit.getPin(component, ctrlPinName);
        if (ctrlPin == null) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : problem with ctrl input pin");
            }
            return null;
        }

        FreeVariable sigVar = new FreeVariable("sig");
        BooleanFormula sanWait1SetFunction = new And(ctrlVar, sigVar);
        BooleanFormula sanWait0SetFunction = new And(ctrlVar, new Not(sigVar));
        Map<BooleanVariable, String> waitSetVarMapping
                = GenlibUtils.getVariableMappingIfEquivalentOrNull(sanWait1SetFunction, setFunction);

        Wait.Type waitType = null;
        if (waitSetVarMapping != null) {
            waitType = Wait.Type.WAIT1;
        } else {
            waitSetVarMapping = GenlibUtils.getVariableMappingIfEquivalentOrNull(sanWait0SetFunction, setFunction);
            if (waitSetVarMapping != null) {
                waitType = Wait.Type.WAIT0;
            }
        }

        if (waitType == null) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : incorrect set function on san output pin");
            }
            return null;
        }

        String sigPinName = waitSetVarMapping.values().stream()
                .filter(pinName -> !ctrlPinName.equals(pinName))
                .findFirst().orElse(null);

        Contact sigPin = sigPinName == null ? null : circuit.getPin(component, sigPinName);
        if (sigPin == null) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : problem with sig input pin");
            }
            return null;
        }

        return new WaitData(waitType, sigPin, ctrlPin, sanPin);
    }

    public static MutexData getMutexData(Circuit circuit, FunctionComponent component,
            String errorPrefixOrNullToSilence) {

        Collection<FunctionContact> outputPins = component.getFunctionOutputs();
        List<FunctionContact> orderedOutputPins = SortUtils.getSortedNatural(outputPins, Contact::getName);
        if (orderedOutputPins.size() != 2) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : expected 2 output pins");
            }
            return null;
        }

        Iterator<FunctionContact> outputPinIterator = orderedOutputPins.iterator();
        FunctionContact g1Pin = outputPinIterator.next();
        FunctionContact g2Pin = outputPinIterator.next();
        if ((g1Pin == null) || (g2Pin == null)) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : problem with g1 / g2 output pins");
            }
            return null;
        }

        BooleanFormula g1SetFunction = g1Pin.getSetFunction();
        BooleanFormula g1ResetFunction = g1Pin.getResetFunction();
        BooleanFormula g2SetFunction = g2Pin.getSetFunction();
        BooleanFormula g2ResetFunction = g2Pin.getResetFunction();
        if ((g1SetFunction == null) || (g1ResetFunction == null) || (g2SetFunction == null) || (g2ResetFunction == null)) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : missing set / reset functions on g1 / g2 output pins");
            }
            return null;
        }

        FreeVariable r1Var = new FreeVariable("r1");
        BooleanFormula g1MutexResetFunction = new Not(r1Var);
        Map<BooleanVariable, String> g1ResetVarMapping
                = GenlibUtils.getVariableMappingIfEquivalentOrNull(g1MutexResetFunction, g1ResetFunction);

        FreeVariable r2Var = new FreeVariable("r2");
        BooleanFormula g2MutexResetFunction = new Not(r2Var);
        Map<BooleanVariable, String> g2ResetVarMapping
                = GenlibUtils.getVariableMappingIfEquivalentOrNull(g2MutexResetFunction, g2ResetFunction);

        if ((g1ResetVarMapping == null) || (g2ResetVarMapping == null)) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : incorrect reset function on g1 / g2 output pins");
            }
            return null;
        }

        String r1PinName = g1ResetVarMapping.get(r1Var);
        Contact r1Pin = r1PinName == null ? null : circuit.getPin(component, r1PinName);
        String r2PinName = g2ResetVarMapping.get(r2Var);
        Contact r2Pin = r2PinName == null ? null : circuit.getPin(component, r2PinName);
        if ((r1Pin == null) || (r2Pin == null)) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : problem with r1 / r2 input pins");
            }
            return null;
        }

        FreeVariable g2Var = new FreeVariable("g2");
        BooleanFormula g1MutexLateSetFunction = new And(r1Var, new Not(g2Var));
        Map<BooleanVariable, String> g1SetVarMapping
                = GenlibUtils.getVariableMappingIfEquivalentOrNull(g1MutexLateSetFunction, g1SetFunction);

        FreeVariable g1Var = new FreeVariable("g1");
        BooleanFormula g2MutexLateSetFunction = new And(r2Var, new Not(g1Var));
        Map<BooleanVariable, String> g2SetVarMapping
                = GenlibUtils.getVariableMappingIfEquivalentOrNull(g2MutexLateSetFunction, g2SetFunction);

        Mutex.Protocol mutexProtocol = null;
        if ((g1SetVarMapping != null) && (g2SetVarMapping != null)) {
            mutexProtocol = Mutex.Protocol.LATE;
        } else {
            BooleanFormula g1MutexEarlySetFunction = new And(r1Var, new Not(new And(g2Var, r2Var)));
            g1SetVarMapping = GenlibUtils.getVariableMappingIfEquivalentOrNull(g1MutexEarlySetFunction, g1SetFunction);

            BooleanFormula g2MutexEarlySetFunction = new And(r2Var, new Not(new And(g1Var, r1Var)));
            g2SetVarMapping = GenlibUtils.getVariableMappingIfEquivalentOrNull(g2MutexEarlySetFunction, g2SetFunction);
            if ((g1SetVarMapping != null) && (g2SetVarMapping != null)) {
                mutexProtocol = Mutex.Protocol.EARLY;
            }
        }

        if (mutexProtocol == null) {
            if (errorPrefixOrNullToSilence != null) {
                LogUtils.logWarning(errorPrefixOrNullToSilence + " : incorrect set functions on g1 / g2 output pins");
            }
            return null;
        }
        return new MutexData(mutexProtocol, r1Pin, g1Pin, r2Pin, g2Pin);
    }

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

    private static Map<String, Wait> getModuleNameToWaitMap() {
        Map<String, Wait> result = new HashMap<>();
        for (Wait.Type type : Wait.Type.values()) {
            Wait module = CircuitSettings.parseWaitData(type);
            if ((module != null) && (module.name != null)) {
                result.put(module.name, module);
            }
        }
        return result;
    }

    public static Set<String> getWaitModuleNames() {
        return getModuleNameToWaitMap().keySet();
    }

    public static Wait getWaitModule(String moduleName) {
        return getModuleNameToWaitMap().get(moduleName);
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

    public static String getMissingWaitMessage(Wait.Type type) {
        return type + " definition is missing in Digital Circuit settings";
    }

    public static void setMutexFunctionsQuiet(Mutex.Protocol protocol,
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

    public static void setMutexFunctions(Mutex.Protocol protocol,
            FunctionContact r1Contact, FunctionContact g1Contact,
            FunctionContact r2Contact, FunctionContact g2Contact) {

        BooleanFormula g1SetFormula = getMutexGrantSetFunction(protocol, r1Contact, g2Contact, r2Contact);
        BooleanFormula g1ResetFormula = getMutexGrantResetFunction(r1Contact);
        g1Contact.setBothFunctions(g1SetFormula, g1ResetFormula);

        BooleanFormula g2SetFormula = getMutexGrantSetFunction(protocol, r2Contact, g1Contact, r1Contact);
        BooleanFormula g2ResetFormula = getMutexGrantResetFunction(r2Contact);
        g2Contact.setBothFunctions(g2SetFormula, g2ResetFormula);
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

    private static Map<String, Mutex> getModuleNameToMutexMap() {
        Map<String, Mutex> result = new HashMap<>();
        for (Mutex.Protocol protocol : Mutex.Protocol.values()) {
            Mutex module = CircuitSettings.parseMutexData(protocol);
            if ((module != null) && (module.name != null)) {
                result.put(module.name, module);
            }
        }
        return result;
    }

    public static Set<String> getMutexModuleNames() {
        return getModuleNameToMutexMap().keySet();
    }

    public static Mutex getMutexModule(String moduleName) {
        return getModuleNameToMutexMap().get(moduleName);
    }

    public static boolean hasMutexInterface(VisualFunctionComponent component) {
        return (component.getVisualInputs().size() == 2) && (component.getVisualOutputs().size() == 2);
    }

    public static LinkedList<Pair<String, String>> getMutexGrantPersistencyExceptions(Circuit circuit) {
        LinkedList<Pair<String, String>> grantPairs = new LinkedList<>();
        Map<String, Mutex> moduleNameToMutexMap = getModuleNameToMutexMap();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getIsArbitrationPrimitive()) {
                Mutex mutexModule = moduleNameToMutexMap.get(component.getModule());
                if (mutexModule != null) {
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

    public static List<Mutex> getImplementableMutexesOrNullForError(Stg stg) {
        List<Mutex> result = new LinkedList<>();
        List<StgPlace> missingEarlyMutexPlaces = new ArrayList<>();
        List<StgPlace> missingLateMutexPlaces = new ArrayList<>();
        List<StgPlace> unimplementableMutexPlaces = new ArrayList<>();
        for (StgPlace place : stg.getMutexPlaces()) {
            if (CircuitSettings.parseMutexData(place.getMutexProtocol()) == null) {
                List<StgPlace> missingMutexPlaces = (place.getMutexProtocol() == Mutex.Protocol.LATE) ?
                        missingLateMutexPlaces : missingEarlyMutexPlaces;

                missingMutexPlaces.add(place);
            }
            Mutex mutex = MutexUtils.getMutex(stg, place);
            if (mutex == null) {
                unimplementableMutexPlaces.add(place);
            } else {
                result.add(mutex);
            }
        }

        String messageDetails = "";
        boolean addSettingsHint = false;
        if (!unimplementableMutexPlaces.isEmpty()) {
            messageDetails += TextUtils.getBulletpoint("Not implementable by MUTEX: "
                    + ReferenceHelper.getNodesAsString(stg, unimplementableMutexPlaces));
        }
        if (!missingEarlyMutexPlaces.isEmpty()) {
            addSettingsHint = true;
            messageDetails += TextUtils.getBulletpoint("Missing definition of Early protocol MUTEX: "
                    + ReferenceHelper.getNodesAsString(stg, missingEarlyMutexPlaces));

        }
        if (!missingLateMutexPlaces.isEmpty()) {
            addSettingsHint = true;
            messageDetails += TextUtils.getBulletpoint("Missing definition of Late protocol MUTEX: "
                    + ReferenceHelper.getNodesAsString(stg, missingLateMutexPlaces));
        }
        if (!messageDetails.isEmpty()) {
            String messagePrefix = "Synthesis cannot be performed due to problems with Mutex places.";
            String messageSuffix = addSettingsHint ? "\n\nSee Digital Circuit settings for MUTEX definitions." : "";
            DialogUtils.showError(messagePrefix + messageDetails + messageSuffix);
            return null;
        }
        return result;
    }

    public static String getMissingMutexMessage(Mutex.Protocol protocol) {
        return protocol + " MUTEX definition is missing in Digital Circuit settings";
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
