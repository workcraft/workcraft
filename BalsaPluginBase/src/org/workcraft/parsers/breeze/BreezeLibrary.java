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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.parsers.breeze.dom.BreezeFile;
import org.workcraft.parsers.breeze.dom.BreezePart;
import org.workcraft.parsers.breeze.dom.RawBreezePartReference;
import org.workcraft.parsers.breeze.javacc.generated.BreezeParser;
import org.workcraft.parsers.breeze.javacc.generated.ParseException;
import org.workcraft.plugins.balsa.DataPathSplitters;
import org.workcraft.plugins.balsa.io.BalsaSystem;

public class BreezeLibrary
{
	private static final String $BRZ = "$Brz";
	HashMap<String, BreezePart> breezeParts = new HashMap<String, BreezePart>();
	HashMap<String, PrimitivePart> primitiveParts = new HashMap<String, PrimitivePart>();

	FilenameFilter absFilter =
		new FilenameFilter()
		{
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".abs") && (Character.isUpperCase(name.charAt(0)) || name.equals("components.abs"));
			}
		};

	public BreezeLibrary(BalsaSystem balsa) throws IOException {
			registerPrimitives(balsa);
	}

	public void registerPrimitives(BalsaSystem balsa) throws IOException
	{
		registerPrimitives(balsa.getDefinitionsDir());
	}

	public void registerPrimitives(File dir) throws IOException
	{
		if(!dir.isDirectory())
			throw new IOException("\""+dir+"\""+" is not a directory.");

		File[] files = dir.listFiles(absFilter);
		if(files == null)
			throw new IOException("Error listing contents of \"" + dir + "\".");

		for(File file : files)
		{
			InputStream is = new FileInputStream(file);

			try
			{
				for(PrimitivePart part : BreezeParser.parsePrimitiveParts(is))
				{
					PrimitivePart primitivePart = DataPathSplitters.getControl(part);
					primitiveParts.put(primitivePart.getName(), primitivePart);
				}
			} catch (ParseException e) {
				System.err.println ("Error parsing " + file.getName() + " (" + e.getMessage() +")");
			}
			finally
			{
				is.close();
			}
		}
	}

	public BreezeDefinition get(String name)
	{
		if(name.startsWith($BRZ))
			return primitiveParts.get(name.substring($BRZ.length()));
		else
			return breezeParts.get(name);
	}

	public void registerParts(InputStream is) throws IOException, ParseException {
		BreezeFile file = BreezeParser.parseBreezeFile(is);
		//TODO
		for (BreezePart part : file.parts)
			breezeParts.put(part.getName(), part);
	}

	public Collection<PrimitivePart> getPrimitives() {
		return primitiveParts.values();
	}

	public PrimitivePart getPrimitive(String name) {
		return primitiveParts.get(name);
	}

	public Collection<BreezePart> getTopLevelParts() {
		Set<BreezePart> parts = new HashSet<BreezePart>(breezeParts.values());
		for(BreezePart part : breezeParts.values())
			for(RawBreezePartReference ref : part.getParts())
				parts.remove(get(ref.name()));
		return parts;
	}
}
