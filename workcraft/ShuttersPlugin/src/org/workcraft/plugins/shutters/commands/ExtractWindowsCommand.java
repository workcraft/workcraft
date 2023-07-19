package org.workcraft.plugins.shutters.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.commands.Command;
import org.workcraft.commands.MenuOrdering;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.interop.SgExporter;
import org.workcraft.plugins.shutters.ShuttersSettings;
import org.workcraft.plugins.shutters.tasks.LtscatResultHandler;
import org.workcraft.plugins.shutters.tasks.LtscatTask;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ExtractWindowsCommand implements Command, MenuOrdering {

    @Override
    public final String getSection() {
        return AbstractConversionCommand.SECTION_TITLE;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public String getDisplayName() {
        return "Extract windows";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, VisualFst.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        File dir;
        File sgFile;
        File scriptFile;
        String sgFileName = we.getTitle() + ShuttersSettings.getExportedFstExtension();
        String scriptFileName = ShuttersSettings.getScriptName();
        SgExporter exporter = new SgExporter();

        // temporary directory
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        dir = FileUtils.createTempDirectory(prefix);

        // get model name
        Fst fst = WorkspaceUtils.getAs(we, Fst.class);
        Framework framework = Framework.getInstance();

        sgFile = new File(dir, sgFileName);
        // exporting the file
        try {
            exporter.exportToFile(fst, sgFile);
        } catch (SerialisationException e) {
            e.printStackTrace();
            FileUtils.deleteOnExitRecursively(dir);
        }
        // writing the script for ltscat
        scriptFile = writeScript(dir, scriptFileName, sgFile.getAbsolutePath(), we.getTitle());

        // calling ltscat
        final LtscatTask ltscatTask = new LtscatTask(we, dir, scriptFile);
        final LtscatResultHandler ltscatResult = new LtscatResultHandler(we, dir);
        final TaskManager taskManager = framework.getTaskManager();
        taskManager.queue(ltscatTask, "Ltscat - process windows", ltscatResult);

    }

    private File writeScript(File dir, String scriptName, String sgName, String title) {
        File script = new File(dir, scriptName);
        File ltscatPath = new File(ExecutableUtils.getAbsoluteCommandPath(ShuttersSettings.getLtscatFolder()));
        String ltscatModule = ShuttersSettings.getLtscatModuleName();

        try {
            PrintWriter writer = new PrintWriter(script);
            writer.println("import sys");
            writer.println("sys.path.append('" + ltscatPath.getAbsolutePath() + "')");
            writer.println("from " + ltscatModule + " import *");
            writer.println("from " + ltscatModule + " import LtsCat as lts");
            writer.println("from " + ltscatModule + " import LtsCat_Windows as win");
            writer.println("l = lts('" + sgName + "')");
            writer.println("l.extractWindows(prefix='" + dir.getAbsolutePath() + File.separator + title + "')");
            writer.println("exit");
            writer.close();
        } catch (IOException e) {
            FileUtils.deleteOnExitRecursively(dir);
            e.printStackTrace();
        }

        return script;

    }

}
