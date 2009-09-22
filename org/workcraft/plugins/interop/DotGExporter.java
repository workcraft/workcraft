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

import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.Plugin;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.serialisation.dotg.DotGSerialiser;

public class DotGExporter implements Exporter, Plugin {
	DotGSerialiser serialiser = new DotGSerialiser();

	public void export(Model model, OutputStream out)
			throws IOException, ModelValidationException, SerialisationException {
		serialiser.export(model, out, null);
	}

	public String getDescription() {
		return serialiser.getExtension() + " (" + serialiser.getDescription()+")";
	}

	public String getExtenstion() {
		return serialiser.getExtension();
	}

	public boolean isApplicableTo(Model model) {
		return serialiser.isApplicableTo(model);
	}
}
