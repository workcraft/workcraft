package org.workcraft.plugins.cflt.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.commands.Command;
import org.workcraft.commands.MenuOrdering;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.cflt.gui.ExpressionDialog;
import org.workcraft.plugins.cflt.presets.ExpressionDataSerialiser;
import org.workcraft.plugins.cflt.presets.ExpressionParameters;
import org.workcraft.plugins.cflt.utils.ExpressionUtils;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.presets.PresetManager;
import org.workcraft.utils.LayoutUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TranslateProfloExpressionCommand implements Command, MenuOrdering {

    private static final String PRESET_KEY = "proflo-expressions.xml";
    private static final ExpressionDataSerialiser DATA_SERIALISER = new ExpressionDataSerialiser();

    private static ExpressionParameters preservedData = null;

    public static Consumer<CodePanel> syntaxChecker = ExpressionUtils::checkSyntax;
    public static BiConsumer<WorkspaceEntry, String> externalTranslator = null;

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
        return "Translate ProFlo expression...";
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

        ExpressionDialog dialog = new ExpressionDialog(mainWindow, presetManager,
                syntaxChecker, externalTranslator != null);

        if (dialog.reveal()) {
            preservedData = dialog.getPresetData();
            process(we, preservedData);
        }
    }

    private static void process(WorkspaceEntry we, ExpressionParameters data) {
        ExpressionParameters.Mode mode = data.getMode();
        String expression = data.getExpression();
        if (mode != null) {
            if (mode == ExpressionParameters.Mode.EXTERNAL) {
                externalTranslator.accept(we, expression);
            } else {
                insert(we, expression, mode);
            }
        }
    }

    private static void insert(WorkspaceEntry we, String expression, ExpressionParameters.Mode mode) {
        we.captureMemento();
        // TODO: fix: we is only updated when model is inserted, but is required for pre-insertion syntax check
        ExpressionUtils.we = we;
        if (WorkspaceUtils.isApplicable(we, VisualPetri.class)) {
            if (ExpressionUtils.insertPetri(expression, mode)) {
                LayoutUtils.attemptLayout(we);
            } else {
                we.cancelMemento();
            }
        }
        if (WorkspaceUtils.isApplicable(we, VisualStg.class)) {
            if (ExpressionUtils.insertStg(expression, mode)) {
                LayoutUtils.attemptLayout(we);
            } else {
                we.cancelMemento();
            }
        }
    }

}
