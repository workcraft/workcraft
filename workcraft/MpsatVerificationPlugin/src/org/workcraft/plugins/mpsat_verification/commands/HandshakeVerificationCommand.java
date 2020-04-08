package org.workcraft.plugins.mpsat_verification.commands;

import org.w3c.dom.Document;
import org.workcraft.Framework;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat_verification.gui.HandshakeWizardDialog;
import org.workcraft.plugins.mpsat_verification.presets.HandshakeDataSerialiser;
import org.workcraft.plugins.mpsat_verification.presets.HandshakeParameters;
import org.workcraft.plugins.mpsat_verification.presets.HandshakePresetManager;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainTask;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.presets.PresetManager;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandshakeVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableDataCommand<Boolean, HandshakeParameters> {

    private static final Pattern DATA_PATTERN = Pattern.compile(
            "\\s*\\{\\s*([\\w.]+\\s*)+}\\s*\\{\\s*([\\w.]+\\s*)+}\\s*");

    private static final String PRESET_KEY = "handshake-wizard.xml";
    private static final HandshakeDataSerialiser DATA_SERIALISER = new HandshakeDataSerialiser();
    private static final String DEFAULT_DESCRIPTION = "Custom REACH assertion";

    private static HandshakeParameters preservedData = null;

    @Override
    public String getDisplayName() {
        return "Handshake wizard [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        HandshakePresetManager presetManager = new HandshakePresetManager(we, PRESET_KEY, DATA_SERIALISER, preservedData);
        HandshakeWizardDialog dialog = new HandshakeWizardDialog(mainWindow, presetManager);
        if (dialog.reveal()) {
            preservedData = dialog.getPresetData();
            VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, true);
            run(we, preservedData, monitor);
        }
    }

    @Override
    public void run(WorkspaceEntry we, HandshakeParameters data, ProgressMonitor monitor) {
        TaskManager manager = Framework.getInstance().getTaskManager();
        VerificationChainTask task = new VerificationChainTask(we,
                data == null ? null : data.getVerificationParameters());

        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        manager.queue(task, description, monitor);
    }

    @Override
    public HandshakeParameters deserialiseData(String str) {
        if (str.startsWith("<") && str.endsWith(">")) {
            Document document = PresetManager.buildPresetDocumentFromSettings(DEFAULT_DESCRIPTION, str);
            return DATA_SERIALISER.fromXML(document.getDocumentElement());
        }
        Matcher matcher = DATA_PATTERN.matcher(str);
        if (matcher.matches()) {
            String[] split = str.split("}\\s*\\{");
            if (split.length == 2) {
                String reqsStr = split[0].replaceAll("\\{", "").trim();
                System.out.println(reqsStr);
                String acksStr = split[1].replaceAll("}", "").trim();
                System.out.println(acksStr);
                return new HandshakeParameters(getSignals(reqsStr), getSignals(acksStr));
            }
        }
        return null;
    }

    private Collection<String> getSignals(String str) {
        Collection<String> result = new ArrayList<>();
        for (String s : str.trim().split("\\s")) {
            if (!s.isEmpty()) {
                result.add(s);
            }
        }
        return result;
    }

    @Override
    public Boolean execute(WorkspaceEntry we, HandshakeParameters data) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, false);
        run(we, data, monitor);
        return monitor.waitForHandledResult();
    }

}
