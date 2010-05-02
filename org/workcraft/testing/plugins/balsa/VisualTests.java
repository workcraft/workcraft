/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.testing.plugins.balsa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.connections.VisualConnectionProperties;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.parsers.breeze.BreezeLibrary;
import org.workcraft.parsers.breeze.EmptyParameterScope;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.VisualBalsaCircuit;
import org.workcraft.plugins.balsa.VisualBreezeComponent;
import org.workcraft.plugins.balsa.VisualHandshake;
import org.workcraft.plugins.balsa.components.Concur;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.io.BalsaSystem;


public class VisualTests {
	@Test
	public void whileHitTest()
	{
		BalsaCircuit circuit = new BalsaCircuit();
		BreezeComponent mathComp = new BreezeComponent();
		mathComp.setUnderlyingComponent(createWhile());
		circuit.add(mathComp);
		VisualBreezeComponent visual = new VisualBreezeComponent(mathComp);

		assertTrue(visual.hitTest(new Point2D.Double(0, 0)));
		assertTrue(visual.hitTest(new Point2D.Double(0.4, 0)));
		assertTrue(visual.hitTest(new Point2D.Double(0.3, 0.3)));
		assertFalse(visual.hitTest(new Point2D.Double(3, 0)));
	}

	private DynamicComponent createWhile() {
		return create("While");
	}

	private DynamicComponent createConcur() {
		return create("Concur");
	}

	private DynamicComponent create(String name) {
		try {
			return new DynamicComponent(new BreezeLibrary(BalsaSystem.DEFAULT()).getPrimitive(name), EmptyParameterScope.instance());
		} catch (IOException e) {
			throw new java.lang.RuntimeException(e);
		}
	}

	@Test
	public void concurBoundingBoxTest()
	{
		BalsaCircuit circuit = new BalsaCircuit();
		BreezeComponent mathComp = new BreezeComponent();
		mathComp.setUnderlyingComponent(createConcur());
		circuit.add(mathComp);
		VisualBreezeComponent visual = new VisualBreezeComponent(mathComp);

		Rectangle2D box = visual.getBoundingBoxInLocalSpace();

		assertTrue(-0.51 > box.getMinX());
		assertTrue(-0.8 < box.getMinX());

		assertTrue(0.51 < box.getMaxX());
		assertTrue(0.8 > box.getMaxX());

		assertTrue(0.49 < box.getMaxY());
		assertTrue(0.61 > box.getMaxY());

		assertTrue(-0.49 > box.getMinY());
		assertTrue(-0.61 < box.getMinY());
	}


	@Test
	public void connectionsTest() throws InvalidConnectionException, VisualModelInstantiationException
	{
		BalsaCircuit circuit = new BalsaCircuit();
		BreezeComponent mathComp1 = new BreezeComponent();
		BreezeComponent mathComp2 = new BreezeComponent();
		mathComp1.setUnderlyingComponent(createConcur());
		mathComp2.setUnderlyingComponent(createConcur());
		circuit.add(mathComp1);
		circuit.add(mathComp2);
		VisualBreezeComponent visual1 = new VisualBreezeComponent(mathComp1);
		VisualBreezeComponent visual2 = new VisualBreezeComponent(mathComp2);
		visual1.setX(10);

		VisualBalsaCircuit visualCircuit = new VisualBalsaCircuit(circuit);

		Container root = visualCircuit.getRoot();
		root.add(visual1);
		root.add(visual2);

		VisualHandshake hs1 = visual1.getHandshake("activate");
		VisualHandshake hs2 = visual2.getHandshake("activateOut1");
	//	VisualConnectionProperties connection = (VisualConnectionProperties)visualCircuit.connect(hs1, hs2);

		AffineTransform transform1 = TransformHelper.getTransform(hs1, root);
		Point2D p1 = hs1.getPosition();
		transform1.transform(p1, p1);

		AffineTransform transform2 = TransformHelper.getTransform(hs2, root);
		Point2D p2 = hs2.getPosition();
		transform2.transform(p2, p2);


		//Assert.assertEquals(p1, connection.getFirstCenter());
		//Assert.assertEquals(p2, connection.getSecondCenter());
	}

}
