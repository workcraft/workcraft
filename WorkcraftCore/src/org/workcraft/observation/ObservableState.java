package org.workcraft.observation;

public interface ObservableState {
    void addObserver(StateObserver obs);
    void removeObserver(StateObserver obs);
    void sendNotification(StateEvent e);
}
