package org.workcraft.testing.plugins.circuit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;

import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitComponent;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.util.Hierarchy;

public class CircuitTest {
	@Test
	public void test1() throws Throwable
	{
		Framework framework1 = new Framework();
		framework1.initPlugins();

		Circuit circuit = new Circuit();
		VisualCircuit visualCircuit = new VisualCircuit(circuit);

		CircuitComponent component1 = new CircuitComponent();
		circuit.add(component1);

		assertNotNull(circuit.getNodeReference(component1));
		System.out.println(circuit.getNodeReference(component1));

		VisualCircuitComponent visualComponent1 = new VisualCircuitComponent(component1);
		visualCircuit.add(visualComponent1);

		CircuitComponent component2 = new CircuitComponent();
		circuit.add(component2);
		VisualCircuitComponent visualComponent2 = new VisualCircuitComponent(component2);
		visualCircuit.add(visualComponent2);

		VisualContact contact1 = visualComponent1.addOutput("testOutput", Direction.WEST);
		VisualContact contact2 = visualComponent2.addInput("testInput", Direction.EAST);

		assertNotNull(circuit.getNodeReference(contact1.getReferencedComponent()));

		visualCircuit.connect(contact1, contact2);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		framework1.save(visualCircuit, outStream);

		Framework framework2 = new Framework();
		framework2.initPlugins();

		Model loaded = framework2.load(new ByteArrayInputStream(outStream.toByteArray()));

		VisualCircuit loadedCircuit = (VisualCircuit)loaded;

		Container root = loadedCircuit.getRoot();
		Collection<VisualCircuitComponent> visualComponents = Hierarchy.getChildrenOfType(root, VisualCircuitComponent.class);
		Collection<VisualConnection> visualConnections = Hierarchy.getChildrenOfType(root, VisualConnection.class);
		assertEquals(2, visualComponents.size());
		assertEquals(1, visualConnections.size());

		VisualConnection connection = visualConnections.iterator().next();
		VisualContact first = (VisualContact) connection.getFirst();
		VisualContact second = (VisualContact) connection.getSecond();

		assertEquals("testOutput", first.getName());
		assertEquals("testInput", second.getName());
	}
}
