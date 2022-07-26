package org.workcraft.plugins.petri_expression.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.commands.Command;
import org.workcraft.commands.MenuOrdering;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri_expression.gui.ExpressionDialog;
import org.workcraft.plugins.petri_expression.presets.ExpressionDataSerialiser;
import org.workcraft.plugins.petri_expression.presets.ExpressionParameters;
import org.workcraft.plugins.petri_expression.utils.ExpressionUtils;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.presets.PresetManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class InsertPetriCommand implements Command, MenuOrdering {

    private static final String PRESET_KEY = "petri-expressions.xml";
    private static final ExpressionDataSerialiser DATA_SERIALISER = new ExpressionDataSerialiser();

    private static ExpressionParameters preservedData = null;

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
        PresetManager<ExpressionParameters> presetManager
                = new PresetManager<>(we, PRESET_KEY, DATA_SERIALISER, preservedData);

        ExpressionDialog dialog = new ExpressionDialog(mainWindow, presetManager);

        if (dialog.reveal()) {
            preservedData = dialog.getPresetData();
            String expression = preservedData.getExpression();
            ExpressionParameters.Mode mode = preservedData.getMode();
            we.captureMemento();
            if (WorkspaceUtils.isApplicable(we, VisualPetri.class)) {
                VisualPetri petri = WorkspaceUtils.getAs(we, VisualPetri.class);
                if (!ExpressionUtils.insert(petri, expression, mode)) {
                    we.cancelMemento();
                }
            }
            if (WorkspaceUtils.isApplicable(we, VisualStg.class)) {
                VisualStg stg = WorkspaceUtils.getAs(we, VisualStg.class);
                if (!ExpressionUtils.insert(stg, expression, mode)) {
                    we.cancelMemento();
                }
            }
        }
    }

}
