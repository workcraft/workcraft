package org.workcraft.plugins.petrify.tools;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

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

public class PetrifyGeneralizedCelementSynthesis implements Tool {

	private final Framework framework;

	public PetrifyGeneralizedCelementSynthesis(Framework framework) {
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

		//Custom button text
		Object[] options = {"Yes, please",
		                    "No, thanks",
		                    "Cancel Logic Synthesis"};
		int option = JOptionPane.showOptionDialog(framework.getMainWindow(),
		    "Would you like to do technology mapping as well?",
		    "Technology mapping",
		    JOptionPane.YES_NO_CANCEL_OPTION,
		    JOptionPane.QUESTION_MESSAGE,
		    null, // no icon
		    options,
		    options[2]); // initial Value: Cancel

		if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION)
			return;

		File libraryFile = null;

		if (option == JOptionPane.YES_OPTION) {
			// choose library File
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Choose a library file");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY); // default
			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				libraryFile = fc.getSelectedFile();
		}

		// call petrify asynchronous (w/o blocking the GUI)
		try {
			framework.getTaskManager().queue(
					new SynthesisTask(getSynthesisParameter(), getInputSTG(framework, WorkspaceUtils.getAs(we, STGModel.class)),
					File.createTempFile("petrifyEquations", ".eqn"), libraryFile, null),
					"Petrify Logic Synthesis", new SynthesisResultHandler(framework));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private String[] getSynthesisParameter() {
		String[] result = new String[1];
		result[0] = "-gc";
		return result;
	}

	private File getInputSTG(Framework framework, Model model) {
		Exporter stgExporter = Export.chooseBestExporter(framework.getPluginManager(), model, Format.STG);

		if (stgExporter == null)
			throw new RuntimeException ("Exporter not available: model class " + model.getClass().getName() + " to format STG.");

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

	@Override
	public String getDisplayName() {
		return "Generalized C-element synthesis (Petrify)";
	}

}
