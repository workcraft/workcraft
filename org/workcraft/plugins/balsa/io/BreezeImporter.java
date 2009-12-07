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

package org.workcraft.plugins.balsa.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.parsers.breeze.BreezeLibrary;
import org.workcraft.parsers.breeze.DefaultBreezeFactory;
import org.workcraft.parsers.breeze.EmptyValueList;
import org.workcraft.parsers.breeze.javacc.BreezeParser;
import org.workcraft.parsers.lisp.LispNode;
import org.workcraft.parsers.lisp.LispParser;
import org.workcraft.parsers.lisp.ParseException;
import org.workcraft.plugins.balsa.BalsaCircuit;

public class BreezeImporter implements Importer {
	private static final String ABSPATH = "/share/tech/common/components";

	@Override
	public boolean accept(File file) {
		return file.isDirectory() || file.getName().endsWith(".breeze");
	}

	@Override
	public String getDescription() {
		return "Breeze handshake circuit (.breeze)";
	}


	@Override
	public Model importFrom(InputStream in) throws DeserialisationException,
			IOException {
		BreezeLibrary lib = new BreezeLibrary();
		String balsaHome = System.getenv("BALSA_HOME");
		if (balsaHome == null || balsaHome.isEmpty())
			throw new DeserialisationException("BALSA_HOME environment variable not set -- cannot load primitive parts definitions.");
		lib.registerPrimitives(new File(balsaHome + ABSPATH));

		try {
			lib.registerParts(in);
		} catch (org.workcraft.parsers.breeze.javacc.ParseException e) {
			throw new DeserialisationException(e);
		}

		BalsaCircuit circuit = new BalsaCircuit();
		DefaultBreezeFactory factory = new DefaultBreezeFactory(circuit);

		lib.get("BMU").instantiate(lib, factory, EmptyValueList.instance());

		return circuit;
	}
}
