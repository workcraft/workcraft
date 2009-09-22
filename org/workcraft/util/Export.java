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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;

public class Export {
	static public void exportToFile (Exporter exporter, Model model, File file) throws IOException, ModelValidationException, SerialisationException {
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);

		if (model instanceof VisualModel)
			if (!exporter.isApplicableTo(model))
				if (!exporter.isApplicableTo(((VisualModel)model).getMathModel()))
						throw new RuntimeException ("Exporter is not applicable to model.");
				else
					model = ((VisualModel)model).getMathModel();
		exporter.export(model, fos);
		fos.close();
	}

	static public void exportToFile (Exporter exporter, Model model, String fileName) throws IOException, ModelValidationException, SerialisationException {
		exportToFile(exporter, model, new File(fileName));
	}
}
