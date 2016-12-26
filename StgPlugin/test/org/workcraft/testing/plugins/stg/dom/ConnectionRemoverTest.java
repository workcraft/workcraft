package org.workcraft.testing.plugins.stg.dom;

import java.util.LinkedList;

import org.junit.Test;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Dependent;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualStg;

public class ConnectionRemoverTest {
    @Test
    public void removeMany() throws InvalidConnectionException {
        Stg stg = new Stg();

        SignalTransition t1 = stg.createSignalTransition();
        Place p1 = stg.createPlace();
        SignalTransition t2 = stg.createSignalTransition();
        Place p2 = stg.createPlace();
        SignalTransition t3 = stg.createSignalTransition();

        stg.connect(t3, p2);
        stg.connect(p2, t2);
        stg.connect(t2, p1);
        stg.connect(p1, t1);

        VisualStg vstg = new VisualStg(stg);

        LinkedList<Node> toDelete = new LinkedList<>();
        LinkedList<Node> toDeleteThen = new LinkedList<>();

        for (Node n : vstg.getRoot().getChildren()) {
            Dependent dn = (Dependent) n;
            if (!dn.getMathReferences().contains(t1)) {
                toDelete.add(n);
            } else {
                toDeleteThen.add(n);
            }
        }

        vstg.select(toDelete);
        vstg.deleteSelection();

        vstg.select(toDeleteThen);
        vstg.deleteSelection();
    }
}
