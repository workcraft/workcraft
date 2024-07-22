package org.workcraft.plugins.cflt.utils;

import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.cflt.jj.petri.PetriStringParser;
import org.workcraft.plugins.cflt.jj.stg.StgStringParser;
import org.workcraft.plugins.cflt.presets.ExpressionParameters;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.tools.CotreeTool;
import org.workcraft.plugins.cflt.tools.CotreeTool.Model;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.JavaccSyntaxUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public final class ExpressionUtils {

    private static final char CHOICE = '#';
    private static final char CONCURRENCY = '|';
    private static final char SEQUENCE = ';';
    private static final char OPEN_BRACKET = '(';
    private static final char CLOSED_BRACKET = ')';
    private static final char OPEN_CURLY_BRACKET = '{';
    private static final char CLOSED_CURLY_BRACKET = '}';

    public static final char PLUS_DIR = '+';
    public static final char MINUS_DIR = '-';
    public static final char TOGGLE_DIR = '~';

    public static WorkspaceEntry we;
    public static HashMap<String, String> labelToName = new HashMap<>();
    public static HashMap<String, SignalTransition.Direction> nameToDirection = new HashMap<>();

    private ExpressionUtils() {
    }

    public static void checkSyntax(CodePanel codePanel) {
        Model model = Model.DEFAULT;
        if (WorkspaceUtils.isApplicable(we, VisualPetri.class)) {
            model = Model.PETRI_NET;
        } else if (WorkspaceUtils.isApplicable(we, VisualStg.class)) {
            model = Model.STG;
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
        if (!validateExpression(expressionText, Model.PETRI_NET)) {
            return false;
        }
        checkMode(mode);
        checkIteration(mode);
        CotreeTool contreeTool = new CotreeTool();
        // If the expression is merely a single transition
        if (CotreeTool.nodes.isEmpty() && CotreeTool.singleTransition != null) {
            contreeTool.drawSingleTransition(Model.PETRI_NET);
        }
        labelToName = new HashMap<>();
        expressionText = makeTransitionsUnique(expressionText);
        if (!validateExpression(expressionText, Model.PETRI_NET)) {
            return false;
        }
        contreeTool.drawInterpretedGraph(mode, Model.PETRI_NET);
        return true;
    }

    public static boolean insertStg(String expressionText, ExpressionParameters.Mode mode) {
        if (!validateExpression(expressionText, Model.STG)) {
            return false;
        }
        checkMode(mode);
        checkIteration(mode);
        CotreeTool ctr = new CotreeTool();
        // If the expression is merely a single transition
        if (CotreeTool.nodes.isEmpty() && CotreeTool.singleTransition != null) {
            ctr.drawSingleTransition(Model.STG);
        }
        nameToDirection = new HashMap<>();
        labelToName = new HashMap<>();
        expressionText = makeTransitionsUnique(expressionText);
        if (!validateExpression(expressionText, Model.STG)) {
            return false;
        }

        ctr.drawInterpretedGraph(mode, Model.STG);
        return true;
    }

    /**
     * Transitions in the expression text are made unique and their original name is stored, later to be used as a label
     * @param expressionText original expression, possibly with reused transition names
     * @return expression with all transitions being unique
     */
    private static String makeTransitionsUnique(String expressionText) {
        String str = expressionText;
        int i = 0;
        int repNo = 0;
        while (i < expressionText.length()) {

            StringBuilder transitionNameBuilder = new StringBuilder();
            while ((expressionText.charAt(i) != CONCURRENCY)
                    && (expressionText.charAt(i) != SEQUENCE)
                    && (expressionText.charAt(i) != CHOICE)
                    && (expressionText.charAt(i) != OPEN_BRACKET)
                    && (expressionText.charAt(i) != CLOSED_BRACKET)
                    && (expressionText.charAt(i) != OPEN_CURLY_BRACKET)
                    && (expressionText.charAt(i) != CLOSED_CURLY_BRACKET)
                    && (expressionText.charAt(i) != '\t')
                    && (expressionText.charAt(i) != '\n')
                    && (expressionText.charAt(i) != ' ')
                    && (expressionText.charAt(i) != '/')) {

                transitionNameBuilder.append(expressionText.charAt(i));
                i++;
                if (i == expressionText.length()) {
                    break;
                }
            }
            final String s = transitionNameBuilder.toString();
            if (!s.isEmpty() && !s.contains("//") && !s.equals("\n")) {
                String uniqueT = "t" + repNo;

                char lastC = expressionText.charAt(i - 1);
                int wasAltered = 0;
                nameToDirection.put(uniqueT, getDirection(lastC));
                if (lastC == PLUS_DIR || lastC == MINUS_DIR || lastC == TOGGLE_DIR) {
                    transitionNameBuilder = new StringBuilder(transitionNameBuilder.substring(0, transitionNameBuilder.length() - 1));
                    wasAltered = 1;
                }
                labelToName.put(uniqueT, transitionNameBuilder.toString());

                str = str.substring(0, i - transitionNameBuilder.length() - wasAltered) + uniqueT + str.substring(i);
                i -= transitionNameBuilder.length();
                i += uniqueT.length() - 1;
                repNo++;
                expressionText = str;
            }
            i++;
        }
        return str;
    }

    public static boolean validateExpression(String expressionText, Model model) {
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

    private static SignalTransition.Direction getDirection(char dir) {
        switch (dir) {
        case PLUS_DIR:
            return SignalTransition.Direction.PLUS;
        case MINUS_DIR:
            return SignalTransition.Direction.MINUS;
        default:
            return SignalTransition.Direction.TOGGLE;
        }
    }
    private static void checkIteration(Mode mode) {
        if ((mode != null) && CotreeTool.containsIteration) {
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
