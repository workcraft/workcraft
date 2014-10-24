package org.workcraft.plugins.petrify.tools;

import java.io.File;
import java.io.IOException;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Model;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.petrify.SynthesisResultHandler;
import org.workcraft.plugins.petrify.tasks.SynthesisTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

abstract public class PetrifySynthesis implements Tool {
	protected final Framework framework;

	public PetrifySynthesis(Framework framework) {
		this.framework = framework;
	}

	@Override
	public String getSection() {
		return "Synthesis";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public void run(WorkspaceEntry we) {
		// call petrify asynchronous (w/o blocking the GUI)
		try {
			framework.getTaskManager().queue(
				new SynthesisTask(getSynthesisParameter(),
						getInputSTG(framework, WorkspaceUtils.getAs(we, STGModel.class)),
						File.createTempFile("petrifyEquations", ".eqn"), null),
				"Petrify Logic Synthesis", new SynthesisResultHandler(framework));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected File getInputSTG(Framework framework, Model model) {
		Exporter stgExporter = Export.chooseBestExporter(framework.getPluginManager(), model, Format.STG);
		if (stgExporter == null) {
			throw new RuntimeException ("Exporter not available: model class " + model.getClass().getName() + " to format STG.");
		}

		File stgFile;
		try {
			stgFile = File.createTempFile("STG", stgExporter.getExtenstion());
			ExportTask exportTask = new ExportTask(stgExporter, model, stgFile.getCanonicalPath());
			framework.getTaskManager().execute(exportTask, "Exporting .g");

			return stgFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	abstract public String[] getSynthesisParameter();
}
