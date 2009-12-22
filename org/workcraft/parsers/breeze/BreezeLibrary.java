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
import java.util.HashMap;
import java.util.List;

import org.workcraft.parsers.breeze.javacc.BreezeParser;
import org.workcraft.parsers.breeze.javacc.ParseException;

@SuppressWarnings("serial")
public class BreezeLibrary extends HashMap<String, BreezeDefinition> {
	public void registerPrimitives(File dir) throws IOException {
		FilenameFilter absFilter = new FilenameFilter(){
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".abs") && Character.isUpperCase(name.charAt(0));
			}};

			for(File file : dir.listFiles(absFilter))
			{
				InputStream is = new FileInputStream(file);
				try
				{
					PrimitivePart primitivePart = BreezeParser.parsePrimitivePart(is);
					put("$Brz"+primitivePart.getName(), primitivePart);
				} catch (ParseException e) {
					System.err.println ("Error parsing " + file.getName() + " (" + e.getMessage() +")");
				}
				finally
				{
					is.close();
				}
			}
	}

	public void registerParts(InputStream is) throws IOException, ParseException {
		List<BreezePart> parts = BreezeParser.parseBreezeParts(is);
		for (BreezePart part : parts)
			put (part.getName(), part);
	}
}
