package org.workcraft.plugins.fst.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.workcraft.Framework;
import org.workcraft.MenuOrdering;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.ProcessWindowsSettings;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.interop.DotGExporter;
import org.workcraft.plugins.fst.task.LtscatResultHandler;
import org.workcraft.plugins.fst.task.LtscatTask;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.FileUtils;
import org.workcraft.util.ToolUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ExtractWindowsCommand implements Command, MenuOrdering  {

    @Override
    public final String getSection() {
        return "!    Conversion"; // 4 spaces - positions 1st
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
        String sgFileName = we.getTitle() + ProcessWindowsSettings.getExportedFstExtension();
        String scriptFileName = ProcessWindowsSettings.getScriptName();
        DotGExporter exporter = new DotGExporter();

        // temporary directory
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        dir = FileUtils.createTempDirectory(prefix);

        // get model name
        Fst fst = WorkspaceUtils.getAs(we, Fst.class);
        Framework framework = Framework.getInstance();

        sgFile = new File(dir, sgFileName);

        // exporting the file
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(sgFile);
            exporter.export(fst, fos);
        } catch (Exception e) {
            e.printStackTrace();
            FileUtils.deleteOnExitRecursively(dir);
        }
        // writing the script for ltscat
        scriptFile = writeScript(dir, scriptFileName, sgFile.getAbsolutePath(), we.getTitle());

        // calling ltscat
        final LtscatTask ltscatTask = new LtscatTask(we, dir, scriptFile);
        final LtscatResultHandler ltscatResult = new LtscatResultHandler(ltscatTask, dir);
        final TaskManager taskManager = framework.getTaskManager();
        taskManager.queue(ltscatTask, "Ltscat - process windows", ltscatResult);

    }

    private File writeScript(File dir, String scriptName, String sgName, String title) {
        File script = new File(dir, scriptName);
        File ltscatPath = new File(ToolUtils.getAbsoluteCommandPath(ProcessWindowsSettings.getLtscatFolder()));
        String ltscatModule = ProcessWindowsSettings.getLtscatModuleName();

        try {
            PrintWriter writer = new PrintWriter(script);
            writer.println("import sys");
            writer.println("sys.path.append('" + ltscatPath.getAbsolutePath() + "')");
            writer.println("from " + ltscatModule + " import *");
            writer.println("from " + ltscatModule + " import LtsCat as lts");
            writer.println("from " + ltscatModule + " import LtsCat_Windows as win");
            writer.println("l = lts('" + sgName + "')");
            writer.println("l.extractWindows(prefix='"
                    + dir.getAbsolutePath()
                    + (DesktopApi.getOs().isWindows() ? "\\" : "/")
                    + title
                    + "')");
            writer.println("exit");
            writer.close();
        } catch (IOException e) {
            FileUtils.deleteOnExitRecursively(dir);
            e.printStackTrace();
        }

        return script;

    }

}
