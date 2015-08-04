package org.workcraft.plugins.cpog.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.FileFilters;
import org.workcraft.workspace.WorkspaceEntry;

public class ImportAndMineEventLog extends PGMinerTool {

	public String getDisplayName() {
		return "Import event log and extract concurrency";
	}

	public File getInputFile(WorkspaceEntry we) throws OperationCancelledException {
		JFileChooser chooser = new JFileChooser();

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

	        File eventLog;

	        //chooser.showOpenDialog(null);
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

	        Scanner k;
			try {
				k = new Scanner(eventLog);
				System.out.println("\nEvent log data:");
				while (k.hasNext()) {
					String line = k.nextLine();
					System.out.println(line);
				}
				k.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			importAndExtract = true;
			return eventLog;

		} else {
			throw new OperationCancelledException("Open operation cancelled by user.");
		}



      }





}


