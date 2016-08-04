package org.workcraft.plugins.stg.tasks;

import java.awt.Container;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DockableWindowContentPanel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.DockableWindowContentPanel.ViewAction;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Import;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class ConceptsResultHandler extends DummyProgressMonitor<ExternalProcessResult> {

    private final String name;
    private final WorkspaceEntry we;

    public ConceptsResultHandler(String inputName, WorkspaceEntry we) {
        name = inputName;
        this.we = we;
    }

    public void finished(final Result<? extends ExternalProcessResult> result, String description) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    final Framework framework = Framework.getInstance();
                    MainWindow mainWindow = framework.getMainWindow();
                    GraphEditorPanel editor = mainWindow.getEditor(we);
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
                                if (isCurrentWorkEmpty(editor)) {
                                    closeEmptyWork(editor);
                                }
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

    private void closeEmptyWork(GraphEditorPanel editor) {
        Container p = editor.getParent();

        while (!(p instanceof DockableWindowContentPanel) && (p != null)) {
            p = p.getParent();
        }

        if (p instanceof DockableWindowContentPanel) {
            DockableWindowContentPanel d = (DockableWindowContentPanel) p;
            new ViewAction(d.getID(), ViewAction.CLOSE_ACTION).run();
        }
    }

    private boolean isCurrentWorkEmpty(GraphEditorPanel editor) {
        VisualStg visualStg = (VisualStg) editor.getModel();
        visualStg.selectAll();
        if (visualStg.getSelection().isEmpty()) {
            return true;
        }
        return false;
    }

}
