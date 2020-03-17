package org.workcraft.observation;

import java.util.HashSet;

public class ObservableStateImpl implements ObservableState {

    private final HashSet<StateObserver> observers = new HashSet<>();

    @Override
    public void addObserver(StateObserver obs) {
        observers.add(obs);
    }

    @Override
    public void removeObserver(StateObserver obs) {
        observers.remove(obs);
    }

    @Override
    public void sendNotification(StateEvent e) {
        for (StateObserver o : observers) {
            o.notify(e);
        }
    }

}
