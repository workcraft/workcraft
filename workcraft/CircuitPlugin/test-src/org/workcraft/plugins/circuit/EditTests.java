package org.workcraft.plugins.circuit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Framework;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.utils.ConnectionUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class EditTests {

    @BeforeAll
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
    }

    @Test
    public void testCreateCircuit() throws InvalidConnectionException {
        VisualCircuit circuit = new VisualCircuit(new Circuit());

        VisualContact westPort = circuit.getOrCreatePort("in1", Contact.IOType.INPUT);
        westPort.setRootSpacePosition(new Point2D.Double(-5.0, 0.0));
        westPort.setDirection(VisualContact.Direction.WEST);

        VisualContact northPort = circuit.getOrCreatePort("in2", Contact.IOType.INPUT);
        northPort.setRootSpacePosition(new Point2D.Double(0.0, -5.0));
        northPort.setDirection(VisualContact.Direction.NORTH);

        VisualContact eastPort = circuit.getOrCreatePort("out1", Contact.IOType.OUTPUT);
        eastPort.setRootSpacePosition(new Point2D.Double(5.0, 0.0));
        eastPort.setDirection(VisualContact.Direction.EAST);

        VisualContact southPort = circuit.getOrCreatePort("out2", Contact.IOType.OUTPUT);
        southPort.setRootSpacePosition(new Point2D.Double(0.0, 5.0));
        southPort.setDirection(VisualContact.Direction.SOUTH);

        VisualFunctionComponent component = circuit.createFunctionComponent(null);
        component.setRootSpacePosition(new Point2D.Double(0.0, 0.0));

        VisualCircuitConnection westConnection = circuit.connect(westPort, component, null);
        VisualNode westNode = westConnection.getSecond();
        Assertions.assertTrue(westNode instanceof VisualFunctionContact);

        VisualFunctionContact westContact = (VisualFunctionContact) westNode;
        checkContact(westContact, "i0", VisualFunctionContact.Direction.WEST, new Point2D.Double(0.0, 0.0));
        ConnectionUtils.moveInternalContacts(westConnection);
        checkContact(westContact, "i0", VisualFunctionContact.Direction.WEST, new Point2D.Double(-1.0, 0.0));

        VisualCircuitConnection eastConnection = circuit.connect(component, eastPort, null);
        VisualNode eastNode = eastConnection.getFirst();
        Assertions.assertTrue(eastNode instanceof VisualFunctionContact);

        VisualFunctionContact eastContact = (VisualFunctionContact) eastNode;
        checkContact(eastContact, "o0", VisualFunctionContact.Direction.EAST, new Point2D.Double(0.0, 0.0));
        ConnectionUtils.moveInternalContacts(eastConnection);
        checkContact(eastContact, "o0", VisualFunctionContact.Direction.EAST, new Point2D.Double(1.0, 0.0));

        VisualCircuitConnection northConnection = circuit.connect(northPort, component, null);
        VisualNode northNode = northConnection.getSecond();
        Assertions.assertTrue(northNode instanceof VisualFunctionContact);

        VisualFunctionContact northContact = (VisualFunctionContact) northNode;
        checkContact(northContact, "i1", VisualFunctionContact.Direction.WEST, new Point2D.Double(0.0, 0.0));
        ConnectionUtils.moveInternalContacts(northConnection);
        checkContact(northContact, "i1", VisualFunctionContact.Direction.NORTH, new Point2D.Double(0.0, -1.0));

        VisualCircuitConnection southConnection = circuit.connect(component, southPort, null);
        VisualNode southNode = southConnection.getFirst();
        Assertions.assertTrue(southNode instanceof VisualFunctionContact);

        VisualFunctionContact southContact = (VisualFunctionContact) southNode;
        checkContact(southContact, "o1", VisualFunctionContact.Direction.EAST, new Point2D.Double(0.0, 0.0));
        ConnectionUtils.moveInternalContacts(southConnection);
        checkContact(southContact, "o1", VisualFunctionContact.Direction.SOUTH, new Point2D.Double(0.0, 1.0));

        Assertions.assertTrue(component.getVisualInputs().contains(westContact));
        Assertions.assertTrue(component.getVisualOutputs().contains(eastContact));
        Assertions.assertTrue(component.getVisualInputs().contains(northContact));
        Assertions.assertTrue(component.getVisualOutputs().contains(southContact));

        VisualFunctionComponent component1 = circuit.createFunctionComponent(null);
        component1.setRootSpacePosition(new Point2D.Double(0.0, 3.0));

        VisualCircuitConnection forkConnection = circuit.connect(westConnection, component1, null);
        VisualNode jointNode = forkConnection.getFirst();
        Assertions.assertTrue(jointNode instanceof VisualJoint);
        VisualNode forkNode = forkConnection.getSecond();
        Assertions.assertTrue(forkNode instanceof VisualFunctionContact);

        VisualFunctionContact forkContact = (VisualFunctionContact) forkNode;
        checkContact(forkContact, "i0", VisualFunctionContact.Direction.WEST, new Point2D.Double(0.0, 3.0));
        List<Point2D> controlPoints = new ArrayList<>();
        controlPoints.add(new Point2D.Double(-2.0, 2.0));
        controlPoints.add(new Point2D.Double(-0.5, 2.0));
        ConnectionHelper.addControlPoints(forkConnection, controlPoints);
        ConnectionUtils.moveInternalContacts(forkConnection);
        checkContact(forkContact, "i0", VisualFunctionContact.Direction.NORTH, new Point2D.Double(-0.5, 2.0));
    }

    private void checkContact(VisualFunctionContact contact, String name, VisualContact.Direction direction, Point2D pos) {
        Assertions.assertEquals(name, contact.getName());
        Assertions.assertEquals(direction, contact.getDirection());
        Assertions.assertEquals(pos, contact.getRootSpacePosition());
    }

}
