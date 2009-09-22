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

package org.workcraft.plugins.interop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.parsers.lisp.LispParser;
import org.workcraft.parsers.lisp.ParseException;

public class BreezeImporter implements Importer {

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

		try {
			List<Object> list = LispParser.parse(in);
			System.out.println(list);
		} catch (ParseException e) {
			throw new DeserialisationException(e);
		}

		throw new DeserialisationException("Not implemented");
	}
}
