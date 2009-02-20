package org.workcraft.plugins.interop;

import java.io.File;

import org.workcraft.dom.Model;
import org.workcraft.framework.Exporter;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;

public class DotGExporter implements Exporter {
	public void exportToFile(Model model, File name) {
	}

	public boolean isApplicableTo(Model model) {
		if (model.getClass().equals(STG.class))
			return true;
		if (model.getClass().equals(VisualSTG.class))
			return true;
		if (model.getClass().equals(PetriNet.class))
			return true;
		if (model.getClass().equals(VisualPetriNet.class))
			return true;

		return false;
	}

	public String getExtenstion() {
		return ".g";
	}

	public String getDescription() {
		return ".g (Petrify, PUNF)";
	}
}