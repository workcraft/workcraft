package org.workcraft.plugins.stg.tasks;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Import;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class ConceptsResultHandler extends DummyProgressMonitor<ExternalProcessResult> {

    private final WorkspaceEntry we;
    private final String name;

    public ConceptsResultHandler(WorkspaceEntry we, String inputName) {
        this.we = we;
        name = inputName;
    }

    public void finished(final Result<? extends ExternalProcessResult> result, String description) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    final Framework framework = Framework.getInstance();
                    MainWindow mainWindow = framework.getMainWindow();

                    
                    if (result.getOutcome() == Outcome.FAILED) {
                        if (result.getReturnValue().getReturnCode() == 2) {
                            JOptionPane.showMessageDialog(mainWindow, "runghc could not run, please install the haskell compiler", "GHC not installed", JOptionPane.ERROR_MESSAGE);
                        }

                        JOptionPane.showMessageDialog(mainWindow, "Concepts could not be translated", "Concept translation failed", JOptionPane.ERROR_MESSAGE);
                    } else {
                        try{
                            String output = new String(result.getReturnValue().getOutput());
                            if (output.startsWith(".model out")) {
                                ModelEntry me = Import.importFromByteArray(new DotGImporter(), result.getReturnValue().getOutput());
                                String title = "Concepts - ";
                                me.getModel().setTitle(title);
                                boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
                                framework.getWorkspace().add(Path.<String>empty(), name, me, false, openInEditor);
                            } else {
                                JOptionPane.showMessageDialog(mainWindow, "Concepts could not be translated.\nSee console window for error information", "Concept translation failed", JOptionPane.ERROR_MESSAGE);
                                System.out.println(output);
                            }
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (DeserialisationException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                       }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
