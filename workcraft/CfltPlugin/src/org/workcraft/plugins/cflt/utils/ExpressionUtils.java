package org.workcraft.plugins.cflt.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.cflt.Model;
import org.workcraft.plugins.cflt.jj.ParseException;
import org.workcraft.plugins.cflt.jj.StringParser;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.tools.NodeTraversalTool;
import org.workcraft.plugins.cflt.tools.PetriDrawingTool;
import org.workcraft.plugins.cflt.tools.StgDrawingTool;
import org.workcraft.plugins.cflt.tools.VisualModelDrawingTool;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.JavaccSyntaxUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

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
        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);

        String errorText = null;
        NodeCollection nodeCollection = null;

        StringParser parser = new StringParser(reader, model);
        try {
            nodeCollection = parser.parse();
        } catch (ParseException e) {
            errorText = e.getMessage();
        }

        return new ParseExpressionResponse(nodeCollection, errorText);
    }

    public static boolean insertInterpretedGraph(String expressionText, Mode mode, Model model, WorkspaceEntry we) {
        ParseExpressionResponse response = getParseExpressionResponse(expressionText, model);
        String errorMessage = response.errorText;
        if (errorMessage != null) {
            DialogUtils.showError(errorMessage);
            return false;
        }
        NodeCollection nodeCollection = response.nodeCollection;
        checkMode(mode);
        checkIteration(mode, nodeCollection);

        VisualModelDrawingTool drawingTool = getDrawingTool(model, nodeCollection);
        NodeTraversalTool nodeTraversalTool = new NodeTraversalTool(drawingTool, nodeCollection);

        nodeTraversalTool.drawInterpretedGraph(mode, we);
        return true;
    }

    private static VisualModelDrawingTool getDrawingTool(Model model, NodeCollection nodeCollection) {
        return switch (model) {
            case PETRI_NET -> new PetriDrawingTool(nodeCollection);
            case STG -> new StgDrawingTool(nodeCollection);
        };
    }

    private static void checkIteration(Mode mode, NodeCollection nodeCollection) {
        if ((mode != null) && nodeCollection.containsIteration()) {
            DialogUtils.showWarning("Iteration operator is experimental and may yield incorrect result.");
        }
    }

    private static void checkMode(Mode mode) {
        if (mode == Mode.SLOW_EXACT) {
            DialogUtils.showWarning("SAT-based solving may take a long time to compute,\n"
                    + "heuristics may be used instead.");
        }
    }

    private record ParseExpressionResponse(NodeCollection nodeCollection, String errorText) {
    }

}
