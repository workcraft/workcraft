package org.workcraft.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;

public class Import {
	static public Model importFromFile (Importer importer, File file) throws IOException, DeserialisationException {
		FileInputStream fileInputStream = new FileInputStream(file);
		Model model = importer.importFrom(fileInputStream);
		fileInputStream.close();
		return model;
	}

	static public Model importFromFile (Importer importer, String fileName) throws IOException, DeserialisationException {
		FileInputStream fileInputStream = new FileInputStream(new File(fileName));
		Model model = importer.importFrom(fileInputStream);
		fileInputStream.close();
		return model;
	}
}
