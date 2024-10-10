package org.workcraft.plugins.cflt.utils;

import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.cflt.jj.petri.PetriStringParser;
import org.workcraft.plugins.cflt.jj.stg.StgStringParser;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.cflt.presets.ExpressionParameters;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.tools.NodeTraversalTool;
import org.workcraft.plugins.cflt.Model;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.JavaccSyntaxUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ExpressionUtils {

    private static final NodeCollection nodeCollection = NodeCollection.getInstance();

    public static final char PLUS_DIR = '+';
    public static final char MINUS_DIR = '-';
    public static final char TOGGLE_DIR = '~';

    public static WorkspaceEntry we;

    private ExpressionUtils() {
    }

    public static void checkSyntax(CodePanel codePanel) {
        Model model;
        if (WorkspaceUtils.isApplicable(we, VisualPetri.class)) {
            model = Model.PETRI_NET;
        } else if (WorkspaceUtils.isApplicable(we, VisualStg.class)) {
            model = Model.STG;
        } else {
            String message = "Couldn't determine which model to check";
            codePanel.showWarningStatus(message);
            return;
        }

        String data = codePanel.getText();
        String errorText = getErrorText(data, model);

        if (errorText == null) {
            String message = "Property is syntactically correct";
            codePanel.showInfoStatus(message);
            LogUtils.logInfo(message);
        } else {
            JavaccSyntaxUtils.processSyntaxError(errorText, codePanel);
        }
    }

    private static String getErrorText(String data, Model model) {
        InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        String errorText = null;

        if (model == Model.PETRI_NET) {
            PetriStringParser parser = new PetriStringParser(is);
            try {
                parser.parse(data);
            } catch (org.workcraft.plugins.cflt.jj.petri.TokenMgrError | org.workcraft.plugins.cflt.jj.petri.ParseException e) {
                errorText = e.getMessage();
            }
        } else if (model == Model.STG) {
            StgStringParser parser = new StgStringParser(is);
            try {
                parser.parse(data);
            } catch (org.workcraft.plugins.cflt.jj.stg.TokenMgrError | org.workcraft.plugins.cflt.jj.stg.ParseException e) {
                errorText = e.getMessage();
            }
        }
        return errorText;
    }

    public static boolean insertPetri(String expressionText, ExpressionParameters.Mode mode) {
        if (!isExpressionValid(expressionText, Model.PETRI_NET)) {
            return false;
        }
        checkMode(mode);
        checkIteration(mode);
        NodeTraversalTool nodeTraversalTool = new NodeTraversalTool();

        // If the expression is merely a single transition
        if (nodeCollection.isEmpty() && nodeCollection.getSingleTransition() != null) {
            nodeTraversalTool.drawSingleTransition(Model.PETRI_NET);
        }

        if (!isExpressionValid(expressionText, Model.PETRI_NET)) {
            return false;
        }
        nodeTraversalTool.drawInterpretedGraph(mode, Model.PETRI_NET);
        return true;
    }

    public static boolean insertStg(String expressionText, ExpressionParameters.Mode mode) {
        if (!isExpressionValid(expressionText, Model.STG)) {
            return false;
        }
        checkMode(mode);
        checkIteration(mode);
        NodeTraversalTool nodeTraversalTool = new NodeTraversalTool();

        // If the expression is merely a single transition
        if (nodeCollection.isEmpty() && nodeCollection.getSingleTransition() != null) {
            nodeTraversalTool.drawSingleTransition(Model.STG);
        }

        if (!isExpressionValid(expressionText, Model.STG)) {
            return false;
        }

        nodeTraversalTool.drawInterpretedGraph(mode, Model.STG);
        return true;
    }

    public static boolean isExpressionValid(String expressionText, Model model) {
        InputStream is = new ByteArrayInputStream(expressionText.getBytes(StandardCharsets.UTF_8));
        if (model == Model.PETRI_NET) {
            PetriStringParser parser = new PetriStringParser(is);
            try {
                parser.parse(expressionText);
            } catch (org.workcraft.plugins.cflt.jj.petri.ParseException | org.workcraft.plugins.cflt.jj.petri.TokenMgrError e) {
                DialogUtils.showError(e.getMessage());
                return false;
            }
            return true;
        } else if (model == Model.STG) {
            StgStringParser parser = new StgStringParser(is);
            try {
                parser.parse(expressionText);
            } catch (org.workcraft.plugins.cflt.jj.stg.ParseException | org.workcraft.plugins.cflt.jj.stg.TokenMgrError e) {
                DialogUtils.showError(e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    private static void checkIteration(Mode mode) {
        if ((mode != null) && nodeCollection.containsIteration()) {
            DialogUtils.showWarning("Iteration operator is experimental and may yield incorrect result.");
        }
    }

    private static void checkMode(Mode mode) {
        if (mode == Mode.SLOW_EXACT) {
            DialogUtils.showWarning("The exhaustive search algorithm may take a long time to compute,\n"
                    + "heuristics may be used instead.");
        }
    }

}
