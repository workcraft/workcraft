package org.workcraft.plugins.cpog.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.workcraft.workspace.WorkspaceEntry;

public class ImportAndMineEventLog extends PGMinerTool {

	public String getDisplayName() {
		return "Mine event log and import";
	}

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


        return eventLog;

      }





}


