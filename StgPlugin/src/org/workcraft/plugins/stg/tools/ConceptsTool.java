package org.workcraft.plugins.stg.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.tasks.ConceptsResultHandler;
import org.workcraft.plugins.stg.tasks.ConceptsTask;
import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.workspace.WorkspaceEntry;

public class ConceptsTool implements Tool {

    public String getSection() {
        return "! Concepts";
    }

    public String getDisplayName() {
        return "Import concepts...";
    }

    public boolean isApplicableTo(WorkspaceEntry we) {
        if (we.getModelEntry() == null) return false;
        if (we.getModelEntry().getVisualModel() instanceof VisualStg) return true;
        return false;
    }

    public File getInputFile() {
        File inputFile = null;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Haskell/Concept file (.hs)", "hs"));
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                if (!f.exists()) {
                    throw new FileNotFoundException();
                }

                inputFile = f;

            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                JOptionPane.showMessageDialog(null, e1.getMessage(),
                        "File not found error", JOptionPane.ERROR_MESSAGE);
                inputFile = null;
            }
        }
        return inputFile;
    }

    @Override
    public void run(WorkspaceEntry we) {
        File inputFile = getInputFile();
        if (inputFile == null) return;

        ConceptsTask task = new ConceptsTask(inputFile);

        ConceptsResultHandler result = new ConceptsResultHandler(we, inputFile.getName());

        Framework.getInstance().getTaskManager().queue(task, "PGMiner", result);
    }

}
