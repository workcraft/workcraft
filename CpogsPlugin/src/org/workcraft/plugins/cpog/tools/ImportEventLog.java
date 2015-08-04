package org.workcraft.plugins.cpog.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.workspace.WorkspaceEntry;

public class ImportEventLog extends PGMinerTool {

	@Override
	public String getDisplayName() {
		return "Import event log";
	}

	@Override
	public File getInputFile(WorkspaceEntry we) {
		JFileChooser chooser = new JFileChooser();
        File eventLog;

        chooser.showOpenDialog(null);
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
        return eventLog;
	}

	public void run(WorkspaceEntry we) {
		File eventLog = getInputFile(we);
		VisualCPOG visualCpog = (VisualCPOG) we.getModelEntry().getVisualModel();

		 Scanner k;
			try {
				k = new Scanner(eventLog);
				System.out.println("Event log input");
				int i = 0;
				while (k.hasNext()) {
					String line = k.nextLine();
					while (line.endsWith(" \n")) {
						line = line.replace(" \n", "\n");
					}
					while (line.endsWith(" ")) {
						line = line.substring(0, line.length() - 1);
					}
					line = line.replace(" ", " -> ");
					line = "t" + (i++) + " = " + line;
					System.out.println(line);
					CpogSelectionTool selectionTool = visualCpog.getSelectionTool();
					System.out.println(visualCpog + "   " + selectionTool);
					selectionTool.insertExpression(line, false, false, true, true);
				}
				k.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
