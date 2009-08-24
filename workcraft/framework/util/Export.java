package org.workcraft.framework.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;

import org.workcraft.dom.Model;
import org.workcraft.framework.Exporter;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.exceptions.ModelValidationException;

public class Export {
	static public void exportToFile (Exporter exporter, Model model, File file) throws IOException, ModelValidationException, ExportException {
		file.createNewFile();
		WritableByteChannel ch = new FileOutputStream(file).getChannel();
		exporter.export(model, ch);
		ch.close();
	}

	static public void exportToFile (Exporter exporter, Model model, String fileName) throws IOException, ModelValidationException, ExportException {
		File f = new File(fileName); f.createNewFile();
		WritableByteChannel ch = new FileOutputStream(f).getChannel();
		exporter.export(model, ch);
		ch.close();
	}
}
