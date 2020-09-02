package org.workcraft.plugins.mpsat_verification.commands;

import org.w3c.dom.Element;
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
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandshakeVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableDataCommand<Boolean, HandshakeParameters> {

    private static final Pattern DATA_PATTERN = Pattern.compile(
            "\\s*\\{\\s*([\\w.]+\\s*)+}\\s*\\{\\s*([\\w.]+\\s*)+}\\s*");

    private static final String PRESET_KEY = "handshake-wizard.xml";
    private static final HandshakeDataSerialiser DATA_SERIALISER = new HandshakeDataSerialiser();

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
    public HandshakeParameters deserialiseData(String data) {
        if (TextUtils.isXmlElement(data)) {
            Element presetElement  = PresetManager.createPresetElement("Handshake wizard assertion", data);
            return DATA_SERIALISER.fromXML(presetElement, new HandshakeParameters());
        }
        Matcher matcher = DATA_PATTERN.matcher(data);
        if (matcher.matches()) {
            String[] split = data.split("}\\s*\\{");
            if (split.length == 2) {
                String reqsStr = split[0].replace("{", "").trim();
                String acksStr = split[1].replace("}", "").trim();
                return new HandshakeParameters(getSignals(reqsStr), getSignals(acksStr));
            }
        }
        return null;
    }

    private Collection<String> getSignals(String str) {
        return TextUtils.splitWords(str);
    }

    @Override
    public Boolean execute(WorkspaceEntry we, HandshakeParameters data) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, false);
        run(we, data, monitor);
        return monitor.waitForHandledResult();
    }

}
