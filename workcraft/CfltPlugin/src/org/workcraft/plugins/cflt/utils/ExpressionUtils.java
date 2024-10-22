package org.workcraft.plugins.cflt.utils;

import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.cflt.jj.petri.ParseException;
import org.workcraft.plugins.cflt.jj.petri.PetriStringParser;
import org.workcraft.plugins.cflt.jj.petri.TokenMgrError;
import org.workcraft.plugins.cflt.jj.stg.StgStringParser;
import org.workcraft.plugins.cflt.node.NodeCollection;
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
    public static final char PLUS_DIR = '+';
    public static final char MINUS_DIR = '-';
    public static final char TOGGLE_DIR = '~';

    private ExpressionUtils() {
    }

    public static void checkSyntax(WorkspaceEntry we, CodePanel codePanel) {
        Model model = null;
        if (WorkspaceUtils.isApplicable(we, VisualPetri.class)) {
            model = Model.PETRI_NET;
        } else if (WorkspaceUtils.isApplicable(we, VisualStg.class)) {
            model = Model.STG;
        }

        String data = codePanel.getText();
        String errorText = getParseExpressionResponse(data, model).errorText;

        if (errorText == null) {
            String message = "Property is syntactically correct";
            codePanel.showInfoStatus(message);
            LogUtils.logInfo(message);
        } else {
            JavaccSyntaxUtils.processSyntaxError(errorText, codePanel);
        }
    }

    private static ParseExpressionResponse getParseExpressionResponse(String data, Model model) {
        InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        String errorText = null;
        NodeCollection nodeCollection = null;

        if (model == Model.PETRI_NET) {
            PetriStringParser parser = new PetriStringParser(is);
            try {
                nodeCollection = parser.parse();
            } catch (TokenMgrError | ParseException e) {
                errorText = e.getMessage();
            }
        } else if (model == Model.STG) {
            StgStringParser parser = new StgStringParser(is);
            try {
                nodeCollection = parser.parse();
            } catch (org.workcraft.plugins.cflt.jj.stg.TokenMgrError | org.workcraft.plugins.cflt.jj.stg.ParseException e) {
                errorText = e.getMessage();
            }
        }
        return new ParseExpressionResponse(nodeCollection, errorText);
    }

    public static boolean insertInterpretedGraph(String expressionText, Mode mode, Model model, WorkspaceEntry we) {
        var response = getParseExpressionResponse(expressionText, model);
        var errorMessage = response.errorText;
        if (errorMessage != null) {
            DialogUtils.showError(errorMessage);
            return false;
        }
        var nodeCollection = response.nodeCollection;
        checkMode(mode);
        checkIteration(mode, nodeCollection);
        NodeTraversalTool nodeTraversalTool = new NodeTraversalTool(nodeCollection, model);
        nodeTraversalTool.drawInterpretedGraph(mode, model, we);
        return true;
    }

    private static void checkIteration(Mode mode, NodeCollection nodeCollection) {
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

    private record ParseExpressionResponse(NodeCollection nodeCollection, String errorText) {
    }

}
