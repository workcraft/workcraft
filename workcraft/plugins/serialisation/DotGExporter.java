package org.workcraft.plugins.serialisation;

import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.framework.serialisation.Exporter;

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
