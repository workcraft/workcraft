package org.workcraft.plugins.cpog.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
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


	        File inputFile = new File("");
	        File eventLog = new File("");

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

	        System.out.println("Start import and mine: " + System.nanoTime() / 1000000);

	        Scanner k;
			try {
				inputFile = File.createTempFile("input", ".tr");
				int c = 0;
				k = new Scanner(eventLog);
				while(k.hasNextLine()) {
					c++;
					k.nextLine();
				}
				k = new Scanner(eventLog);
				String[] lines = new String[c];

//				System.out.println("\nEvent log data:");
				c = 0;
				while (k.hasNext()) {
					lines[c] = k.nextLine();
//					System.out.println(lines[c]);
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

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			importAndExtract = true;
			return inputFile;

		} else {
			throw new OperationCancelledException("Open operation cancelled by user.");
		}



      }





}


