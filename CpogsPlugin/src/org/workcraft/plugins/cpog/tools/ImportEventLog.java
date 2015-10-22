package org.workcraft.plugins.cpog.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.workspace.WorkspaceEntry;

public class ImportEventLog extends PGMinerTool {

	@Override
	public String getDisplayName() {
		return "Import event log";
	}

	@Override
	public File getInputFile(WorkspaceEntry we) throws OperationCancelledException {

		File eventLog;
		JFileChooser chooser = new JFileChooser();
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			 eventLog = chooser.getSelectedFile();
		        try {
		        	if (!eventLog.exists()) {
		        		throw new FileNotFoundException();
		        	}

		        } catch (FileNotFoundException e1) {
		            // TODO Auto-generated catch block
		            JOptionPane.showMessageDialog(null, e1.getMessage(),
		                    "File not found error", JOptionPane.ERROR_MESSAGE);
		        } catch (NullPointerException e2) {

		        }

		} else {
			throw new OperationCancelledException("Open operation cancelled by user.");
		}





        return eventLog;
	}

	public void run(WorkspaceEntry we) {

			double start = 0;

			try {
				File eventLog = getInputFile(we);
				start = System.nanoTime();

				final Framework framework = Framework.getInstance();
				final GraphEditorPanel editor = framework.getMainWindow().getCurrentEditor();
				final ToolboxPanel toolbox = editor.getToolBox();
				final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);

				Scanner k;

				k = new Scanner(eventLog);
//				System.out.println("Event log input");
				int i = 0;
				double yPos = 0;
				while (k.hasNext()) {
					String line = k.nextLine();
//					System.out.println("t" + i + " = " + line);

					tool.insertEventLog((VisualCPOG) editor.getWorkspaceEntry().getModelEntry().getVisualModel(), i++, line.split(" "), yPos);

					yPos = yPos + 5;

				}
				k.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OperationCancelledException e) {

			}

			System.out.println("import only: " + (System.nanoTime() - start) / 1000000);

	}




}
