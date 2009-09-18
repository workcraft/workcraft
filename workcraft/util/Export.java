package org.workcraft.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.Exporter;

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
