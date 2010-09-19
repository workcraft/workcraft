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

package org.workcraft.testing.plugins.interop;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.parsers.breeze.BreezeLibrary;
import org.workcraft.parsers.breeze.EmptyParameterScope;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.io.BalsaSystem;

public class BalsaToGatesExporterTests {
	@Test
	public void test() throws IOException, ModelValidationException, SerialisationException
	{
		BalsaCircuit circuit = new BalsaCircuit();
		BreezeComponent component = new BreezeComponent();
		component.setUnderlyingComponent(new DynamicComponent(new BreezeLibrary(BalsaSystem.DEFAULT()).getPrimitive("While"), EmptyParameterScope.instance()));
		circuit.add(component);
		Framework f = new Framework();
		f.initPlugins();
		Assert.fail();//TODO
		//Export.exportToFile(new SynthesisWithMpsat(f), circuit, "while.eqn");
	}
}
