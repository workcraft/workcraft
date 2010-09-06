/**
 *
 */
package org.workcraft.plugins.petrify.tools;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.petrify.SynthesisResultHandler;
import org.workcraft.plugins.petrify.tasks.SynthesisTask;
import org.workcraft.plugins.stg.STG;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;

/**
 * @author Dominic Wist
 * Petrify's Complex Gate Synthesis without Technology Mapping
 */
@DisplayName("Complex Gate Synthesis")
public class ComplexGateSynthesis implements Tool {

	private final Framework framework;

	public ComplexGateSynthesis(Framework framework) {
		this.framework = framework;
	}

	/* (non-Javadoc)
	 * @see org.workcraft.Tool#getSection()
	 */
	@Override
	public String getSection() {
		return "Petrify";
	}

	/* (non-Javadoc)
	 * @see org.workcraft.Tool#isApplicableTo(org.workcraft.dom.Model)
	 */
	@Override
	public boolean isApplicableTo(Model model) {
		if (model instanceof STG)
			return true;
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see org.workcraft.Tool#run(org.workcraft.dom.Model, org.workcraft.Framework)
	 */
	@Override
	public void run(Model model) {

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
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY); // default
			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				libraryFile = fc.getSelectedFile();
		}

		// call petrify asynchronous (w/o blocking the GUI)
		try {
			framework.getTaskManager().queue(
					new SynthesisTask(getComplexGateSynParamter(), getInputSTG(framework, model),
					File.createTempFile("petrifyEquations", ".eqn"), libraryFile, null),
					"Petrify Logic Synthesis", new SynthesisResultHandler(framework));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}



	private String[] getComplexGateSynParamter() {
		String[] result = new String[1];
		result[0] = "-cg";
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

}
