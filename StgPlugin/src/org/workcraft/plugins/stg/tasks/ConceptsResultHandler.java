package org.workcraft.plugins.stg.tasks;

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
import org.workcraft.util.Import;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;

public class ConceptsResultHandler extends DummyProgressMonitor<ExternalProcessResult> {

    private final String name;

    public ConceptsResultHandler(String inputName) {
        name = inputName;
    }

    public void finished(final Result<? extends ExternalProcessResult> result, String description) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    final Framework framework = Framework.getInstance();
                    MainWindow mainWindow = framework.getMainWindow();
                    try {
                        if (result.getOutcome() == Outcome.FAILED) {
                            String errors = new String(result.getReturnValue().getErrors());
                            System.out.println(LogUtils.PREFIX_STDERR + errors);
                            if (errors.contains("<no location info>")) {
                                JOptionPane.showMessageDialog(mainWindow, "Concepts code could not be found. \n"
                                        + "Download it from https://github.com/tuura/concepts. \n"
                                        + "Ensure that the preferences menu points to the correct location of the concepts folder", "Concept translation failed", JOptionPane.ERROR_MESSAGE);
                            } else if (errors.contains("Could not find module")) {
                                String pkg = "tools/concepts"; //TODO: Use setting for concepts location
                                JOptionPane.showMessageDialog(mainWindow, "Concepts could not be run. \n"
                                        + "The " + pkg + " package needs to be installed via Cabal using the command \"cabal install " + pkg + "\"\n",
                                        "Concept translation failed", JOptionPane.ERROR_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(mainWindow, "Concepts could not be translated", "Concept translation failed", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            String output = new String(result.getReturnValue().getOutput());
                            if (output.startsWith(".model out")) {
                                ModelEntry me = Import.importFromByteArray(new DotGImporter(), result.getReturnValue().getOutput());
                                String title = "Concepts - ";
                                me.getModel().setTitle(title + name);
                                boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
                                framework.getWorkspace().add(Path.<String>empty(), title + name, me, false, openInEditor);
                            } else {
                                JOptionPane.showMessageDialog(mainWindow, "Concepts could not be translated."
                                        + "\nSee console window for error information", "Concept translation failed", JOptionPane.ERROR_MESSAGE);
                                System.out.println(LogUtils.PREFIX_STDERR + output);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (DeserialisationException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        JOptionPane.showMessageDialog(mainWindow, "runghc could not run, please install Haskell", "GHC not installed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
