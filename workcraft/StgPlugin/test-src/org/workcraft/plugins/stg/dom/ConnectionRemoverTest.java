package org.workcraft.plugins.stg.dom;

import org.junit.jupiter.api.Test;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Dependent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualStg;

import java.util.LinkedList;

class ConnectionRemoverTest {

    @Test
    void removeMany() throws InvalidConnectionException {
        Stg stg = new Stg();

        SignalTransition t1 = stg.createSignalTransition(null, null);
        Place p1 = stg.createPlace();
        SignalTransition t2 = stg.createSignalTransition(null, null);
        Place p2 = stg.createPlace();
        SignalTransition t3 = stg.createSignalTransition(null, null);

        stg.connect(t3, p2);
        stg.connect(p2, t2);
        stg.connect(t2, p1);
        stg.connect(p1, t1);

        VisualStg vstg = new VisualStg(stg);

        LinkedList<VisualNode> toDelete = new LinkedList<>();
        LinkedList<VisualNode> toDeleteThen = new LinkedList<>();

        for (Node n : vstg.getRoot().getChildren()) {
            if ((n instanceof Dependent) && (n instanceof VisualNode)) {
                Dependent dn = (Dependent) n;
                if (!dn.getMathReferences().contains(t1)) {
                    toDelete.add((VisualNode) n);
                } else {
                    toDeleteThen.add((VisualNode) n);
                }
            }
        }

        vstg.select(toDelete);
        vstg.deleteSelection();

        vstg.select(toDeleteThen);
        vstg.deleteSelection();
    }

}
