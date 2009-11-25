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

package org.workcraft.parsers.breeze;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;

import org.junit.Test;
import org.workcraft.parsers.breeze.javacc.BreezeParser;
import org.workcraft.parsers.lisp.ParseException;
import org.workcraft.plugins.balsa.BalsaCircuit;


public class TESTS {
	private static final class EmptyParameterScope implements ParameterScope {
		@Override
		public Object get(String key) {
			throw new RuntimeException ("Not implemented");
		}
	}

	@Test
	public void parseAbs() throws FileNotFoundException, ParseException, org.workcraft.parsers.breeze.javacc.ParseException
	{
		InputStream is = new FileInputStream("C:\\deleteMe\\Variable.abs");
		PrimitivePart part = BreezeParser.parsePrimitivePart(is);

		System.out.println(part);
	}

	@Test
	public void viterbiToGates() throws Exception
	{
		File bzrFileName = new File("C:\\deleteMe\\viterbi\\BMU.breeze");
		File definitionsFolder = new File("C:\\deleteMe");

		BreezeLibrary lib = new BreezeLibrary();
		registerPrimitives(definitionsFolder, lib);

		registerParts(bzrFileName, lib);

		BalsaCircuit circuit = new BalsaCircuit();

		DefaultBreezeFactory factory = new DefaultBreezeFactory(circuit);

		lib.get("BMU").instantiate(factory, new EmptyParameterScope());
	}

	private void registerParts(File file, BreezeLibrary lib) throws Exception {
		InputStream is = new FileInputStream(file);
		try
		{
			BreezeParser.registerBreezeParts(is, lib);
		}
		finally
		{
			is.close();
		}
	}

	private void registerPrimitives(File dir, BreezeLibrary lib) throws Exception {
		FilenameFilter absFilter = new FilenameFilter(){
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".abs");
			}};

		for(File file : dir.listFiles(absFilter))
		{
			InputStream is = new FileInputStream(file);
			try
			{
				System.out.println("parsing "+file.getAbsolutePath() + "...");
				PrimitivePart primitivePart = BreezeParser.parsePrimitivePart(is);
				lib.put("$Brz"+primitivePart.name, primitivePart);
			}
			finally
			{
				is.close();
			}
		}
	}
}
