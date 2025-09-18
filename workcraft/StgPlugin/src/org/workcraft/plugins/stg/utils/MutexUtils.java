package org.workcraft.plugins.stg.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.stg.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MutexUtils {

    private static final String RIGHT_ARROW_SYMBOL = Character.toString((char) 0x2192);

    public static LinkedList<Pair<String, String>> getMutexGrantPersistencyExceptions(Stg stg) {
        return getMutexGrantPersistencyExceptions(getMutexes(stg));
    }

    public static LinkedList<Pair<String, String>> getMutexGrantPersistencyExceptions(List<Mutex> mutexes) {
        LinkedList<Pair<String, String>> result = new LinkedList<>();
        for (Mutex mutex : mutexes) {
            result.add(Pair.of(mutex.g1.name, mutex.g2.name));
            result.add(Pair.of(mutex.g2.name, mutex.g1.name));
        }
        return result;
    }

    public static List<Mutex> getMutexes(Stg stg) {
        List<Mutex> result = new LinkedList<>();
        for (StgPlace place: stg.getMutexPlaces()) {
            Mutex mutex = getMutex(stg, place);
            if (mutex != null) {
                result.add(mutex);
            }
        }
        return result;
    }

    public static Mutex getMutex(Stg stg, StgPlace mutexPlace) {
        Set<MathNode> postset = stg.getPostset(mutexPlace);
        if (!postset.stream().allMatch(t -> t instanceof SignalTransition)) {
            return null;
        }

        Set<SignalTransition> grantTransitions = postset.stream()
                .map(node -> (SignalTransition) node)
                .collect(Collectors.toSet());

        if (!grantTransitions.stream().allMatch(t -> isGoodMutexGrant(stg, t))) {
            return null;
        }

        Set<Signal> grantSignals = grantTransitions.stream()
                .map(t -> new Signal(stg.getSignalReference(t), t.getSignalType()))
                .collect(Collectors.toSet());

        if (grantSignals.size() != 2) {
            return null;
        }

        Iterator<Signal> grantSignalIterator = grantSignals.iterator();
        Signal g1Signal = grantSignalIterator.next();
        Signal g2Signal = grantSignalIterator.next();
        Set<SignalTransition> g1Transitions = getTransitionsOfSignal(stg, grantTransitions, g1Signal.name);
        Set<SignalTransition> g2Transitions = getTransitionsOfSignal(stg, grantTransitions, g2Signal.name);

        Signal r1Signal = getRequestSignal(stg, g1Transitions, grantSignals, mutexPlace);
        Signal r2Signal = getRequestSignal(stg, g2Transitions, grantSignals, mutexPlace);

        if ((r1Signal == null) || (r2Signal == null)) {
            return null;
        }

        String name = stg.getNodeReference(mutexPlace);
        Mutex.Protocol protocol = mutexPlace.getMutexProtocol();
        return new Mutex(name, r1Signal, g1Signal, r2Signal, g2Signal, protocol);
    }

    private static Set<SignalTransition> getTransitionsOfSignal(Stg stg,
            Collection<SignalTransition> transitions, String signal) {

        return transitions.stream()
                .filter(t -> signal.equals(stg.getSignalReference(t)))
                .collect(Collectors.toSet());
    }

    private static Signal getRequestSignal(Stg stg, Collection<SignalTransition> grantTransitions,
            Set<Signal> skipSignals, StgPlace mutexPlace) {

        Signal result = null;
        for (SignalTransition grantTransition : grantTransitions) {
            for (MathNode predPlace : stg.getPreset(grantTransition)) {
                // Skip triggers via mutex place
                if (predPlace == mutexPlace) {
                    continue;
                }
                for (MathNode predTransition : stg.getPreset(predPlace)) {
                    // Skip read-arc transitions
                    if (stg.getPreset(predTransition).contains(predPlace)) {
                        continue;
                    }
                    // Determine syntactic trigger transition and its signal
                    SignalTransition requestTransition = (SignalTransition) predTransition;
                    Signal requestSignal = new Signal(stg.getSignalReference(requestTransition),
                            requestTransition.getSignalType());

                    // Skip transitions of grant signals
                    if (skipSignals.contains(requestSignal)) {
                        continue;
                    }

                    // Request must be rising edge
                    if (!isGoodMutexRequest(stg, requestTransition)) {
                        return null;
                    }

                    // All triggers must be of the same request signal
                    if (result == null) {
                        result = requestSignal;
                    } else if (!result.equals(requestSignal)) {
                        return null;
                    }
                }
            }
        }
        return result;
    }

    private static boolean isGoodMutexRequest(Stg stg, SignalTransition signalTransition) {
        return stg.getDirection(signalTransition) == SignalTransition.Direction.PLUS;
    }

    private static boolean isGoodMutexGrant(Stg stg, SignalTransition signalTransition) {
        return (stg.getDirection(signalTransition) == SignalTransition.Direction.PLUS)
                && (signalTransition.getSignalType() != Signal.Type.INPUT);
    }

    public static void factoroutMutexes(StgModel model, Collection<Mutex> mutexes) {
        if ((model instanceof Stg stg) && (mutexes != null)) {
            for (Mutex mutex: mutexes) {
                LogUtils.logInfo("Factoring out " + mutex);
                factoroutMutexRequest(stg, mutex.r1);
                factoroutMutexGrant(stg, mutex.g1);
                factoroutMutexRequest(stg, mutex.r2);
                factoroutMutexGrant(stg, mutex.g2);
            }
        }
    }

    private static void factoroutMutexRequest(Stg stg, Signal signal) {
        if (signal.type == Signal.Type.INTERNAL) {
            stg.setSignalType(signal.name, Signal.Type.OUTPUT);
        }
    }

    private static void factoroutMutexGrant(Stg stg, Signal signal) {
        stg.setSignalType(signal.name, Signal.Type.INPUT);
    }

    public static void restoreMutexSignals(StgModel model, Collection<Mutex> mutexes) {
        if ((model instanceof Stg stg) && (mutexes != null)) {
            for (Mutex mutex : mutexes) {
                stg.setSignalType(mutex.r1.name, mutex.r1.type);
                stg.setSignalType(mutex.g1.name, mutex.g1.type);
                stg.setSignalType(mutex.r2.name, mutex.r2.type);
                stg.setSignalType(mutex.g2.name, mutex.g2.type);
            }
        }
    }

    public static void restoreMutexPlacesByName(StgModel model, Collection<Mutex> mutexes) {
        if ((model != null) && (mutexes != null)) {
            for (Mutex mutex : mutexes) {
                Node node = model.getNodeByReference(mutex.name);
                if (node instanceof StgPlace place) {
                    place.setMutex(true);
                }
            }
        }
    }

    public static void restoreMutexPlacesByContext(StgModel model, Collection<Mutex> mutexes) {
        if ((model instanceof Stg stg) && (mutexes != null)) {
            for (Mutex mutex : mutexes) {
                restoreMutexPlaceByContext(stg, mutex);
            }
        }
    }

    private static void restoreMutexPlaceByContext(Stg stg, Mutex mutex) {
        if ((stg == null) || (mutex == null)) {
            return;
        }
        for (StgPlace place: stg.getPlaces()) {
            Mutex newMutex = MutexUtils.getMutex(stg, place);
            if (newMutex == null) {
                continue;
            }
            boolean r1r1 = mutex.r1.equals(newMutex.r1);
            boolean g1g1 = mutex.g1.equals(newMutex.g1);
            boolean r2r2 = mutex.r2.equals(newMutex.r2);
            boolean g2g2 = mutex.g2.equals(newMutex.g2);
            boolean r1r2 = mutex.r1.equals(newMutex.r2);
            boolean g1g2 = mutex.g1.equals(newMutex.g2);
            boolean r2r1 = mutex.r2.equals(newMutex.r1);
            boolean g2g1 = mutex.g2.equals(newMutex.g1);
            boolean match11 = r1r1 && g1g1 && r2r2 && g2g2;
            boolean match21 = r1r2 && g1g2 && r2r1 && g2g1;
            if (match11 || match21) {
                place.setMutex(true);
            }
        }
    }

    public static void logInfoPossiblyImplementableMutex(Collection<Mutex> mutexes) {
        if ((mutexes != null) && !mutexes.isEmpty()) {
            List<String> mutexPlaceNames = mutexes.stream()
                    .map(mutex -> mutex.name)
                    .sorted(SortUtils::compareNatural)
                    .collect(Collectors.toList());

            LogUtils.logInfo(TextUtils.wrapMessageWithItems(
                    "Possibly implementable (structurally detected) mutex place", mutexPlaceNames));
        }
    }

    public static Set<String> getMutexPlaceReferences(Stg stg) {
        HashSet<String> result = new HashSet<>();
        for (StgPlace place: stg.getMutexPlaces()) {
            result.add(stg.getNodeReference(place));
        }
        return result;
    }

    public static Collection<Mutex> getMutexesWithoutMatch(Collection<Mutex> mutexes, Collection<Mutex> otherMutexes) {
        Collection<Mutex> mismatchMutexes = new ArrayList<>();
        for (Mutex mutex : mutexes) {
            Mutex matchMutex = getMutexMatch(mutex, otherMutexes);
            if (matchMutex == null) {
                mismatchMutexes.add(mutex);
            }
        }
        return mismatchMutexes;
    }

    private static Mutex getMutexMatch(Mutex mutex, Collection<Mutex> otherMutexes) {
        if ((mutex != null) && (mutex.g1 != null) && (mutex.r1 != null) && (mutex.g2 != null) && (mutex.r2 != null)) {
            for (Mutex otherMutex : otherMutexes) {
                if (isMutexMatch(mutex, otherMutex)) {
                    return otherMutex;
                }
            }
        }
        return null;
    }

    private static boolean isMutexMatch(Mutex mutex, Mutex otherMutex) {
        return (mutex.g1.name.equals(otherMutex.g1.name) && mutex.r1.name.equals(otherMutex.r1.name) &&
                mutex.r2.name.equals(otherMutex.r2.name) && mutex.g2.name.equals(otherMutex.g2.name))
                ||
                (mutex.g1.name.equals(otherMutex.g2.name) && mutex.r1.name.equals(otherMutex.r2.name) &&
                 mutex.g2.name.equals(otherMutex.g1.name) && mutex.r2.name.equals(otherMutex.r1.name));
    }

    public static String getMutexPlaceExtendedTitle(Mutex mutex) {
        return "'" + mutex.name + "' ("
                + mutex.r1.name + RIGHT_ARROW_SYMBOL + mutex.g1.name + ", "
                + mutex.r2.name + RIGHT_ARROW_SYMBOL + mutex.g2.name + ")";
    }

    public static String getMutexPlaceExtendedTitles(Collection<Mutex> mutexes) {
        StringBuilder result = new StringBuilder();
        for (Mutex mutex : mutexes) {
            result.append(TextUtils.getBulletpoint(getMutexPlaceExtendedTitle(mutex)));
        }
        return result.toString();
    }

    public static boolean mutexStructuralCheck(Stg stg, boolean allowEmptyMutexPlaces) {
        Collection<StgPlace> mutexPlaces = stg.getMutexPlaces();
        if (!allowEmptyMutexPlaces && mutexPlaces.isEmpty()) {
            DialogUtils.showWarning("No mutex places found to check protocol.");
            return false;
        }
        final ArrayList<StgPlace> problematicPlaces = new ArrayList<>();
        for (StgPlace place: mutexPlaces) {
            Mutex mutex = MutexUtils.getMutex(stg, place);
            if (mutex == null) {
                problematicPlaces.add(place);
            }
        }
        if (!problematicPlaces.isEmpty()) {
            Collection<String> problematicPlacesRefs = ReferenceHelper.getReferenceList(stg, problematicPlaces);
            String msg = TextUtils.wrapMessageWithItems("Failed to determine requests or grants for mutex place", problematicPlacesRefs)
                    + TextUtils.getBulletpoint("postset of mutex place must comprise rising transitions of 2 distinct output or internal signals (grants)")
                    + TextUtils.getBulletpoint("rising transitions of each grant must be triggered by rising transitions of the same signal (request)");

            DialogUtils.showError(msg, "Model validation");
            return false;
        }
        return true;
    }

}
