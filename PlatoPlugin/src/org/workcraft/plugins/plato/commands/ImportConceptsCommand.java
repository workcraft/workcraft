package org.workcraft.plugins.plato.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.plugins.plato.tasks.PlatoResultHandler;
import org.workcraft.plugins.plato.tasks.PlatoTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.FileUtils;
import org.workcraft.util.DialogUtils;
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
                DialogUtils.showError(e1.getMessage());
                inputFile = null;
            }
        }
        return inputFile;
    }

    @Override
    public void run(WorkspaceEntry we) {
        File inputFile = getInputFile();
        if (inputFile != null) {
            String text = getFileText(inputFile);
            boolean system = false;
            if (text.contains("system =")) {
                system = true;
            }
            final Framework framework = Framework.getInstance();
            final TaskManager taskManager = framework.getTaskManager();
            PlatoTask task = new PlatoTask(inputFile);
            String inputName = FileUtils.getFileNameWithoutExtension(inputFile);
            final PlatoResultHandler result = new PlatoResultHandler(this, inputName, we, system);
            taskManager.queue(task, "Translating concepts", result);
        }
    }

    private String getFileText(File inputFile) {
        String result = "";
        try {
            Scanner k = new Scanner(inputFile);
            while (k.hasNextLine()) {
                result = result + k.nextLine();
            }
            k.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

}
