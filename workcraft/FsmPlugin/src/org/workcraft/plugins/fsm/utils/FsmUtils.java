package org.workcraft.plugins.fsm.utils;

import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FsmUtils {

    public static Map<State, Set<Event>> calcStateOutgoingEventsMap(final Fsm fsm) {
        Map<State, Set<Event>> result = new HashMap<>();
        for (State state: fsm.getStates()) {
            Set<Event> events = new HashSet<>();
            result.put(state, events);
        }
        for (Event event: fsm.getEvents()) {
            State fromState = (State) event.getFirst();
            Set<Event> events = result.get(fromState);
            events.add(event);
        }
        return result;
    }

    public static Map<State, Set<Event>> calcStateIncommingEventsMap(final Fsm fsm) {
        Map<State, Set<Event>> result = new HashMap<>();
        for (State state: fsm.getStates()) {
            Set<Event> events = new HashSet<>();
            result.put(state, events);
        }
        for (Event event: fsm.getEvents()) {
            State toState = (State) event.getSecond();
            Set<Event> events = result.get(toState);
            events.add(event);
        }
        return result;
    }

}
