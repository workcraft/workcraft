package org.workcraft.plugins.cpog.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.workcraft.Tool;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class PGMinerTool implements Tool {

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		if (we.getModelEntry() == null) return false;
		if (we.getModelEntry().getVisualModel() instanceof VisualCPOG) return true;
		return false;
	}

	@Override
	public String getSection() {
		return "!Process Mining";
	}

	abstract public File getInputFile(WorkspaceEntry we);

	abstract public File getOutputFile(File inputFile);

//	public File getInputFile() throws IOException {
//		File inputFile = File.createTempFile("input", ".tr");
//		return inputFile;
//	}

	@Override
	public void run(WorkspaceEntry we) {

		try {

			File inputFile = getInputFile(we);
			if (inputFile.exists()) {

				Process process = new ProcessBuilder(CpogSettings.getPGMinerCommand(), inputFile.getAbsolutePath()).start();
				try {
					process.waitFor();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				File outputFile = getOutputFile(inputFile);

				VisualCPOG visualCpog = (VisualCPOG) we.getModelEntry().getVisualModel();

				CpogSelectionTool selectionTool = (CpogSelectionTool) visualCpog.getSelectionTool();

				boolean open = false;

				while (!open) {
					try {
						Scanner k = new Scanner(outputFile);
						open = true;
						while (k.hasNext()) {
							String line = k.nextLine();
							System.out.println(line);
							selectionTool.insertExpression(line, false, false, true);
						}
						k.close();
					} catch (IOException ex) {
						open = false;
					}
				}
			}

		} catch (IOException exception) {
			exception.printStackTrace();
		} catch (NullPointerException e2) {

		}
	}


}
