package org.workcraft.testing.dom;

import java.util.LinkedList;

import org.junit.Test;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;

public class ConnectionRemoverTest {
	@Test
	public void removeMany() throws InvalidConnectionException {
		//PropertyConfigurator.configure("config/logging.properties");

		STG stg = new STG();

		SignalTransition t1 = stg.createSignalTransition();
		Place p1 = stg.createPlace();
		SignalTransition t2 = stg.createSignalTransition();
		Place p2 = stg.createPlace();
		SignalTransition t3 = stg.createSignalTransition();

		stg.connect(t3, p2);
		stg.connect(p2, t2);
		stg.connect(t2, p1);
		stg.connect(p1, t1);

		VisualSTG vstg = new VisualSTG(stg);

		//System.out.println ("Created VSTG");

		LinkedList<Node> toDelete = new LinkedList<Node>();
		LinkedList<Node> toDeleteThen = new LinkedList<Node>();

		for (Node n : vstg.getRoot().getChildren()) {
			DependentNode dn = (DependentNode)n;
			if (!dn.getMathReferences().contains(t1))
				toDelete.add(n);
			else
				toDeleteThen.add(n);
		}

		vstg.select(toDelete);
		vstg.deleteSelection();

		//System.out.println ("O_O");

		vstg.select(toDeleteThen);
		vstg.deleteSelection();
	}
}
