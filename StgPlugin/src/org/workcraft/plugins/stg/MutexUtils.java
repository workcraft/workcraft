package org.workcraft.plugins.stg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.util.Pair;

public class MutexUtils {

    public static LinkedList<Pair<String, String>> getMutexGrantPairs(Stg stg) {
        LinkedList<Pair<String, String>> exceptions = new LinkedList<>();
        for (Mutex mutex: getMutexes(stg)) {
            Pair<String, String> exception = Pair.of(mutex.g1.name, mutex.g2.name);
            exceptions.add(exception);
        }
        return exceptions;
    }

    public static LinkedList<Mutex> getMutexes(Stg stg) {
        LinkedList<Mutex> result = new LinkedList<>();
        for (StgPlace place: stg.getMutexPlaces()) {
            Mutex mutex = getMutex(stg, place);
            if (mutex != null) {
                result.add(mutex);
            }
        }
        return result;
    }

    public static Mutex getMutex(Stg stg, StgPlace place) {
        if (!place.isMutex()) {
            return null;
        }
        String name = stg.getNodeReference(place);
        Signal r1 = null;
        Signal g1 = null;
        Signal r2 = null;
        Signal g2 = null;
        Set<Node> preset = stg.getPreset(place);
        Set<Node> postset = stg.getPostset(place);
        if ((preset.size() != 2) || (postset.size() != 2)) {
            return null;
        }
        Iterator<Node> postsetIterator = postset.iterator();
        Node succ1 = postsetIterator.next();
        Node succ2 = postsetIterator.next();
        if ((succ1 instanceof SignalTransition) && (succ2 instanceof SignalTransition)) {
            SignalTransition tSucc1 = (SignalTransition) succ1;
            SignalTransition tSucc2 = (SignalTransition) succ2;
            if ((tSucc1.getSignalType() != Type.OUTPUT) || (tSucc2.getSignalType() != Type.OUTPUT)) {
                return null;
            }
            g1 = new Signal(tSucc1.getSignalName(), tSucc1.getSignalType());
            g2 = new Signal(tSucc2.getSignalName(), tSucc2.getSignalType());
            Set<SignalTransition> triggers1 = getTriggers(stg, tSucc1, place);
            Set<SignalTransition> triggers2 = getTriggers(stg, tSucc2, place);
            if ((triggers1.size() != 1) || (triggers2.size() != 1)) {
                return null;
            }
            SignalTransition trigger1 = triggers1.iterator().next();
            SignalTransition trigger2 = triggers2.iterator().next();
            r1 = new Signal(trigger1.getSignalName(), trigger1.getSignalType());
            r2 = new Signal(trigger2.getSignalName(), trigger2.getSignalType());
        }
        return new Mutex(name, r1, g1, r2, g2);
    }

    private static Set<SignalTransition> getTriggers(Stg stg, SignalTransition transition, StgPlace skipPlace) {
        HashSet<SignalTransition> result = new HashSet<>();
        for (Node predPlace: stg.getPreset(transition)) {
            if (!(predPlace instanceof StgPlace) || (predPlace == skipPlace)) {
                continue;
            }
            for (Node predTransition: stg.getPreset(predPlace)) {
                if (!(predTransition instanceof SignalTransition) || stg.getPreset(predTransition).contains(predPlace)) {
                    continue;
                }
                result.add((SignalTransition) predTransition);
            }
        }
        return result;
    }

}
