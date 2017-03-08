package org.workcraft.plugins.plato.commands;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.plugins.plato.tasks.ConceptsTask;
import org.workcraft.plugins.plato.tasks.PlatoResultHandler;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ImportConceptsCommand implements Command {

    public String getSection() {
        return "! Concepts";
    }

    public String getDisplayName() {
        return "Import concepts...";
    }

    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
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
        if (inputFile != null) {
            final Framework framework = Framework.getInstance();
            final TaskManager taskManager = framework.getTaskManager();
            ConceptsTask task = new ConceptsTask(inputFile);
            String inputName = FileUtils.getFileNameWithoutExtension(inputFile);
            final PlatoResultHandler result = new PlatoResultHandler(this, inputName, we);
            taskManager.queue(task, "Translating concepts", result);
        }
    }

}
