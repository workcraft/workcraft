package org.workcraft.plugins.fsm.tools;

import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;

public class FsmUtils {

    static public HashMap<State, HashSet<Event>> calcStateOutgoingEventsMap(final Fsm fsm) {
        HashMap<State, HashSet<Event>> stateOutgoingEvents = new HashMap<>();
        for (State state: fsm.getStates()) {
            HashSet<Event> events = new HashSet<>();
            stateOutgoingEvents.put(state, events);
        }
        for (Event event: fsm.getEvents()) {
            State fromState = (State) event.getFirst();
            HashSet<Event> events = stateOutgoingEvents.get(fromState);
            events.add(event);
        }
        return stateOutgoingEvents;
    }

    static public HashMap<State, HashSet<Event>> calcStateIncommingEventsMap(final Fsm fsm) {
        HashMap<State, HashSet<Event>> stateIncommingEvents = new HashMap<>();
        for (State state: fsm.getStates()) {
            HashSet<Event> events = new HashSet<>();
            stateIncommingEvents.put(state, events);
        }
        for (Event event: fsm.getEvents()) {
            State toState = (State) event.getSecond();
            HashSet<Event> events = stateIncommingEvents.get(toState);
            events.add(event);
        }
        return stateIncommingEvents;
    }

}
