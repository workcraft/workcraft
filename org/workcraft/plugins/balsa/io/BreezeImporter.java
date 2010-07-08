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
import java.util.Collection;

import javax.swing.JOptionPane;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.interop.Importer;
import org.workcraft.parsers.breeze.BreezeDefinition;
import org.workcraft.parsers.breeze.BreezeLibrary;
import org.workcraft.parsers.breeze.DefaultBreezeFactory;
import org.workcraft.parsers.breeze.EmptyValueList;
import org.workcraft.parsers.breeze.dom.BreezePart;
import org.workcraft.plugins.balsa.BalsaCircuit;

public class BreezeImporter implements Importer
{
	private BalsaSystem balsa = null;

	public BreezeImporter()
	{

	}

	public BreezeImporter(BalsaSystem balsa) {
		this.balsa = balsa;
	}

	@Override
	public boolean accept(File file) {
		return file.isDirectory() || file.getName().endsWith(".breeze");
	}

	@Override
	public String getDescription() {
		return "Breeze handshake circuit (.breeze)";
	}

	public BalsaCircuit importFromBreeze(InputStream in, String breezeName) throws DeserialisationException, IOException, OperationCancelledException
	{
		if (balsa == null)
			balsa = BalsaSystem.DEFAULT();

		BreezeLibrary lib = new BreezeLibrary(balsa);

		try {
			lib.registerParts(in);
		} catch (org.workcraft.parsers.breeze.javacc.ParseException e) {
			throw new DeserialisationException(e);
		}

		BalsaCircuit circuit = new BalsaCircuit();
		DefaultBreezeFactory factory = new DefaultBreezeFactory(circuit);

		BreezeDefinition choice;
		if(breezeName!=null)
			choice = lib.get(breezeName);
		else
		{
			Collection<BreezePart> parts = lib.getTopLevelParts();

			Object[] possibilities = parts.toArray();
			choice = (BreezePart)JOptionPane.showInputDialog(
			                    null,
			                    "Select the part to instantiate:",
			                    "Breeze part selection",
			                    JOptionPane.QUESTION_MESSAGE,
			                    null,
			                    possibilities,
			                    possibilities[0]);
			if(choice == null)
				throw new OperationCancelledException();
		}

		choice.instantiate(lib, factory, EmptyValueList.instance());
	    return circuit;
	}

	@Override
	public BalsaCircuit importFrom(InputStream in) throws DeserialisationException, IOException
	{
		try {
			return importFromBreeze(in, null);
		} catch (OperationCancelledException e) {
			throw new java.lang.RuntimeException(e);
		}
	}
}
