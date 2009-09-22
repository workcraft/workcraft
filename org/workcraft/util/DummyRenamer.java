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

package org.workcraft.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class DummyRenamer {
	public static final String dummyLinePrefix = ".dummy";
	public static final String markingLinePrefix = ".marking";

	public static void rename(File in, File out) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(in));
		PrintWriter writer = new PrintWriter(out);

		String l;
		HashSet<String> dummySet = new HashSet<String>();

		while ((l = reader.readLine()) != null) {

			if (l.startsWith(dummyLinePrefix)) {
				List<String> dummies = Arrays.asList(l.substring(
						dummyLinePrefix.length()).split(" "));

				writer.print(".dummy");

				for (String d : dummies) {
					writer.print (" " + d + "_plus");
					writer.print (" " + d + "_minus");
				}

				writer.println();

				dummySet.addAll(dummies);

			}
			else if (l.startsWith(markingLinePrefix)){
				String stripped = l.substring(markingLinePrefix.length()).replace("{", "").replace("}", "").trim();


				writer.print(".marking {");

				if(stripped.length()>0)
					for(String place : stripped.split(" "))
						writer.print(" " + renamePlace(dummySet, place));

				writer.println(" }");
			}
			else if (!l.startsWith(".")) {
				String tr[] = l.split(" ");

				boolean first = true;

				for (String t : tr) {
					if (first)
						first = false;
					else
						writer.print (" ");
					writer.print(rename(dummySet, t));
				}

				writer.println();
			}
			else
				writer.println(l);
		}

		reader.close();
		writer.close();
	}

	private static String renamePlace(HashSet<String> dummySet, String place) {
		if(place.charAt(0) != '<')
			return place;
		String [] transitions = place.substring(1, place.length()-1).split(",");
		if(transitions.length != 2)
			throw new RuntimeException(".g marking parse error");

		return "<" + rename(dummySet, transitions[0]) + "," + rename(dummySet, transitions[1]) + ">";
	}

	private static String rename(HashSet<String> dummySet, String t) {
		if (t.endsWith("-")) {
			String name = t.substring(0, t.length() - 1);
			if (dummySet.contains(name))
				return name + "_minus";
		} else if (t.endsWith("+")) {
			String name = t.substring(0, t.length() - 1);
			if (dummySet.contains(name))
				return name + "_plus";
		}

		return t;
	}
}
