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
