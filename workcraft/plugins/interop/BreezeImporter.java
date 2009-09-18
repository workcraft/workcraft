package org.workcraft.plugins.interop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.parsers.lisp.LispParser;
import org.workcraft.parsers.lisp.ParseException;

public class BreezeImporter implements Importer {

	@Override
	public boolean accept(File file) {
		return file.isDirectory() || file.getName().endsWith(".breeze");
	}

	@Override
	public String getDescription() {
		return "Breeze handshake circuit (.breeze)";
	}


	@Override
	public Model importFrom(InputStream in) throws DeserialisationException,
			IOException {

		try {
			List<Object> list = LispParser.parse(in);
			System.out.println(list);
		} catch (ParseException e) {
			throw new DeserialisationException(e);
		}

		throw new DeserialisationException("Not implemented");
	}
}
