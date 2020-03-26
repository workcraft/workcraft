package org.workcraft.plugins.mpsat.commands;

import org.w3c.dom.Document;
import org.workcraft.Framework;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.*;
import org.workcraft.plugins.mpsat.gui.ReachAssertionDialog;
import org.workcraft.plugins.mpsat.tasks.VerificationChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.VerificationChainTask;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.presets.PresetManager;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class ReachAssertionVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableDataCommand<Boolean, VerificationParameters> {

    private static final String PRESET_KEY = "reach-assertions.xml";
    private static final MpsatDataSerialiser DATA_SERIALISER = new MpsatDataSerialiser();
    private static final String DEFAULT_DESCRIPTION = "Custom REACH assertion";

    private static VerificationParameters preservedData = null;

    @Override
    public String getDisplayName() {
        return "REACH assertion [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        boolean allowStgPresets = WorkspaceUtils.isApplicable(we, StgModel.class);
        MpsatPresetManager presetManager = new MpsatPresetManager(we, PRESET_KEY, DATA_SERIALISER, allowStgPresets, preservedData);
        ReachAssertionDialog dialog = new ReachAssertionDialog(mainWindow, presetManager);
        if (dialog.reveal()) {
            preservedData = dialog.getPresetData();
            VerificationChainResultHandler monitor = new VerificationChainResultHandler(we);
            run(we, preservedData, monitor);
        }
    }

    @Override
    public void run(WorkspaceEntry we, VerificationParameters data, ProgressMonitor monitor) {
        TaskManager manager = Framework.getInstance().getTaskManager();
        VerificationChainTask task = new VerificationChainTask(we, data);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        manager.queue(task, description, monitor);
    }

    @Override
    public VerificationParameters deserialiseData(String str) {
        if (str.startsWith("<" + MpsatDataSerialiser.SETTINGS_ELEMENT) && str.endsWith("</" + MpsatDataSerialiser.SETTINGS_ELEMENT + ">")) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                String xml = "<" + PresetManager.PRESET_ELEMENT_NAME + " "
                        + PresetManager.DESCRIPTION_ATTRIBUTE_NAME + "=\"" + DEFAULT_DESCRIPTION + "\">"
                        + str + "</" + PresetManager.PRESET_ELEMENT_NAME + ">";

                Document document = builder.parse(new InputSource(new StringReader(xml)));
                return DATA_SERIALISER.fromXML(document.getDocumentElement());
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }
        return new VerificationParameters(DEFAULT_DESCRIPTION,
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                str, true);
    }


    @Override
    public Boolean execute(WorkspaceEntry we, VerificationParameters data) {
        VerificationChainResultHandler monitor = new VerificationChainResultHandler(we);
        run(we, data, monitor);
        return MpsatUtils.getChainOutcome(monitor);
    }

}
