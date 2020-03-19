package org.workcraft.observation;

import java.util.HashSet;

public class ObservableHierarchyImpl implements ObservableHierarchy {

    private final HashSet<HierarchyObserver> observers = new HashSet<>();

    @Override
    public void addObserver(HierarchyObserver obs) {
        observers.add(obs);
    }

    @Override
    public void removeObserver(HierarchyObserver obs) {
        observers.remove(obs);
    }

    @Override
    public void removeAllObservers() {
        observers.clear();
    }

    public void sendNotification(HierarchyEvent e) {
        for (HierarchyObserver obs : observers) {
            obs.notify(e);
        }
    }

}
