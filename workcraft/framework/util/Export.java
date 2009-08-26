package org.workcraft.framework.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.serialisation.Exporter;

public class Export {
	static public void exportToFile (Exporter exporter, Model model, File file) throws IOException, ModelValidationException, SerialisationException {
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		exporter.export(model, fos);
		fos.close();
	}

	static public void exportToFile (Exporter exporter, Model model, String fileName) throws IOException, ModelValidationException, SerialisationException {
		File f = new File(fileName); f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		exporter.export(model, fos);
		fos.close();
	}
}
