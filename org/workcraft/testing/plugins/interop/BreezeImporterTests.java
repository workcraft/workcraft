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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;
import org.workcraft.parsers.lisp.LispNode;
import org.workcraft.parsers.lisp.LispParser;
import org.workcraft.parsers.lisp.ParseException;

public class BreezeImporterTests {
	class BreezeComponentDeclarationParser
	{

	}

	@Test
	public void Test1() throws FileNotFoundException, ParseException
	{
		InputStream is = new FileInputStream("C:\\deleteMe\\Concur.abs");
		LispNode node = LispParser.parse(is);
		System.out.println(node.getSubNodes());
	}
}
