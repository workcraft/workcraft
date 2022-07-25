package org.workcraft.plugins.petri_expression.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.commands.Command;
import org.workcraft.commands.MenuOrdering;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri_expression.utils.ExpressionUtils;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.presets.PresetManager;
import org.workcraft.presets.TextDataSerialiser;
import org.workcraft.presets.TextPresetDialog;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class InsertPetriCommand implements Command, MenuOrdering {

    private static final String PRESET_KEY = "petri-expressions.xml";
    private static final TextDataSerialiser DATA_SERIALISER = new TextDataSerialiser();

    private static String preservedData = null;

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
        return Position.BOTTOM;
    }

    @Override
    public String getDisplayName() {
        return "Insert Petri expression...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, VisualPetri.class)
                || WorkspaceUtils.isApplicableExact(we, VisualStg.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        PresetManager<String> presetManager = new PresetManager<>(we, PRESET_KEY, DATA_SERIALISER, preservedData);

        // TODO: Populate expression presets
        presetManager.addExamplePreset("Mutual exclusion of transitions",
                "// Transitions a and b are mutually exclusive\n" + "a | b");

        TextPresetDialog dialog = new TextPresetDialog(mainWindow, "Petri expression", presetManager);

        // TODO: Prepare Petri expression help in workcraft.org, similar to https://workcraft.org/help/reach
        dialog.addHelpButton(new File("help/petri_expression.html"));
        dialog.addCheckerButton(event -> ExpressionUtils.checkSyntax(dialog.getCodePanel()));

        if (dialog.reveal()) {
            preservedData = dialog.getPresetData();
            we.captureMemento();
            if (WorkspaceUtils.isApplicable(we, VisualPetri.class)) {
                VisualPetri petri = WorkspaceUtils.getAs(we, VisualPetri.class);
                if (!ExpressionUtils.insert(petri, preservedData)) {
                    we.cancelMemento();
                }
            }
            if (WorkspaceUtils.isApplicable(we, VisualStg.class)) {
                VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);
                if (!ExpressionUtils.insert(stg, preservedData)) {
                    we.cancelMemento();
                }
            }
        }
    }

}
