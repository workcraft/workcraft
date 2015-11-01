package org.workcraft.plugins.cpog.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Scanner;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.gui.PGMinerImportDialog;
import org.workcraft.plugins.cpog.tasks.PGMinerResultHandler;
import org.workcraft.plugins.cpog.tasks.PGMinerTask;
import org.workcraft.workspace.WorkspaceEntry;

public class PGMinerImportTool implements Tool {

	boolean split = false;
	PGMinerImportDialog dialog;

	@Override
	public String getSection() {
		return "! Process Mining";
	}

	@Override
	public String getDisplayName() {
		return "Import an event log";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		if (we.getModelEntry() == null) return false;
		if (we.getModelEntry().getVisualModel() instanceof VisualCPOG) return true;
		return false;
	}


	public File getInputFile(WorkspaceEntry we) {
		dialog = new PGMinerImportDialog();
		dialog.setVisible(true);

		if (!dialog.getCanImport()) {
			return null;
		}

		if (!dialog.getExtractConcurrency() || dialog.getSplit()) {
			return new File(dialog.getFilePath());
		}

		try {

			File inputFile = File.createTempFile("input", ".tr");
			int c = 0;
			File originalFile = new File(dialog.getFilePath());
			Scanner k = new Scanner(originalFile);
			while(k.hasNextLine()) {
				c++;
				k.nextLine();
			}
			k.close();

			k = new Scanner(originalFile);
			String[] lines = new String[c];

			c = 0;
			while (k.hasNext()) {
				lines[c] = k.nextLine();
				c++;
			}
			k.close();
			HashSet<String> visitedEvents;
			int i = 0;
			String[] newLines = new String[lines.length];
			for (String line : lines) {
				String[] events = line.split(" ");
				line = "";
				visitedEvents = new HashSet<>();
				for (String event : events) {
					if (visitedEvents.contains(event)) {
						int d = 1;
						while (visitedEvents.contains(event + "_" + d)){
							d++;
						}
						event = event + "_" + d;
					}
					if (!line.isEmpty()) {
						line = line + " ";
					}
					line = line + event;
					visitedEvents.add(event);
				}
				newLines[i] = line;
				i++;
			}


			PrintStream expressions = new PrintStream(inputFile);

			for (String line : newLines) {
				expressions.println(line);
			}
			expressions.close();
			return inputFile;
		} catch (Exception e) {
			return null;
		}

	}


	@Override
	public void run(WorkspaceEntry we) {


			File inputFile;
			inputFile = getInputFile(we);
			try {
				if (inputFile != null) {

					if (dialog.getExtractConcurrency()) {
					PGMinerTask task = new PGMinerTask(inputFile, dialog.getSplit());

					final Framework framework = Framework.getInstance();
					PGMinerResultHandler result = new PGMinerResultHandler((VisualCPOG) we.getModelEntry().getVisualModel(), we, false);
					framework.getTaskManager().queue(task, "PGMiner", result);
					} else {

					final Framework framework = Framework.getInstance();
					final GraphEditorPanel editor = framework.getMainWindow().getCurrentEditor();
					final ToolboxPanel toolbox = editor.getToolBox();
					final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);

					Scanner k;

					k = new Scanner(inputFile);
					int i = 0;
					double yPos = tool.getLowestVertex((VisualCPOG) editor.getWorkspaceEntry().getModelEntry().getVisualModel()).getY() + 3;
					editor.getWorkspaceEntry().captureMemento();
					while (k.hasNext()) {
						String line = k.nextLine();

						tool.insertEventLog((VisualCPOG) editor.getWorkspaceEntry().getModelEntry().getVisualModel(), i++, line.split(" "), yPos);

						yPos = yPos + 5;

					}
					k.close();
					editor.getWorkspaceEntry().saveMemento();
					}
				}
			} catch (Exception e) {

			}
		}

}
