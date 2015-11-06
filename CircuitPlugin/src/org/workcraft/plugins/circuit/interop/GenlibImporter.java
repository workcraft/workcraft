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

package org.workcraft.plugins.circuit.interop;

import java.io.File;
import java.io.InputStream;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.javacc.GenlibParser;
import org.workcraft.plugins.circuit.javacc.ParseException;
import org.workcraft.workspace.ModelEntry;

public class GenlibImporter implements Importer {

	@Override
	public boolean accept(File file) {
		return file.getName().endsWith(".lib");
	}

	@Override
	public String getDescription() {
		return "Genlib (.lib)";
	}

	@Override
	public ModelEntry importFrom(InputStream in) throws DeserialisationException {
		return new ModelEntry(new CircuitDescriptor(), importGenlib(in));
	}

	public Circuit importGenlib(InputStream in) throws DeserialisationException {
		final Circuit circuit = new Circuit();
		GenlibParser parser = new GenlibParser(in);
		parser.disable_tracing();
		try {
			Library library = parser.parseGenlib();
			for (final String name: library.getNames()) {
				final Gate gate = library.get(name);
				GenlibUtils.instantiateGate(gate, null, circuit);
			}
		} catch (ParseException e) {
			throw new DeserialisationException(e);
		}
		return circuit;
	}

}
