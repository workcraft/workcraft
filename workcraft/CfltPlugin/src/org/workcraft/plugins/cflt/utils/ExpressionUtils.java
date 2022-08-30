package org.workcraft.plugins.cflt.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.controls.CodePanel;
import org.workcraft.plugins.cflt.javaccPetri.ParseException;
import org.workcraft.plugins.cflt.javaccPetri.PetriStringParser;
import org.workcraft.plugins.cflt.javaccStg.StgStringParser;
import org.workcraft.plugins.cflt.presets.ExpressionParameters;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.tools.CotreeTool;
import org.workcraft.plugins.cflt.tools.CotreeTool.Model;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ExpressionUtils {

    private static final Pattern SYNTAX_ERROR_PATTERN = Pattern.compile(
            "parse error:\\R>>> (.+)\\R    (.*)(\\^+)\\Rsyntax error, (.+)\\R",
            Pattern.UNIX_LINES);

    private static final int POSITION_GROUP = 2;
    private static final int LENGTH_GROUP = 3;
    private static final int MESSAGE_GROUP = 4;

    public static final char CHOICE = '#';
    public static final char CONCURRENCY = '|';
    public static final char SEQUENCE = ';';
    public static final char OPEN_BRACKET = '(';
    public static final char CLOSED_BRACKET = ')';
    public static final char OPEN_CURLY_BRACKET = '{';
    public static final char CLOSED_CURLY_BRACKET = '}';

    public static final char PLUS_DIR = '+';
    public static final char MINUS_DIR = '-';
    public static final char TOGGLE_DIR = '~';

    public static WorkspaceEntry we;
    public static HashMap<String, String> labelNameMap = new HashMap<>();
    public static HashMap<String, Character> nameDirectionMap = new HashMap<>();

    public static void checkSyntax(CodePanel codePanel) {

        Model model = Model.DEFAULT;
        if (WorkspaceUtils.isApplicable(we, VisualPetri.class)) {
            model = Model.PETRI_NET;
        } else if (WorkspaceUtils.isApplicable(we, VisualStg.class)) {
            model = Model.STG;
        }

        String data = codePanel.getText();
        String errorText = null;
        if (model == Model.PETRI_NET) {
            errorText = parseData(data, Model.PETRI_NET);
        } else if (model == Model.STG) {
            errorText = parseData(data, Model.STG);
        }
        if (errorText == null) {
            String message = "Property is syntactically correct";
            codePanel.showInfoStatus(message);
            LogUtils.logInfo(message);
        } else {
            Matcher matcher = SYNTAX_ERROR_PATTERN.matcher(errorText);
            if (matcher.find()) {
                String message = "Syntax error: " + matcher.group(MESSAGE_GROUP);
                LogUtils.logError(message);
                int pos = matcher.group(POSITION_GROUP).length();
                int len = matcher.group(LENGTH_GROUP).length();
                int fromPos = getCodePosition(codePanel.getText(), pos);
                int toPos = getCodePosition(codePanel.getText(), pos + len);
                codePanel.highlightError(fromPos, toPos, message);
            } else {
                String message = "Syntax check failed";
                LogUtils.logError(message);
                codePanel.showErrorStatus(message);
            }
        }
    }

    private static String parseData(String data, Model model) {

        try {
            validateExpression(data, model);
            //checkNestedIteration(data);
            checkIteration(model);
        } catch (ParseException | org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            data = null;
            e.printStackTrace();
        }

        return data == null ? "Bad control flow logic expression" : null;
    }

    public static int getCodePosition(String text, int pos) {
        for (int i = 0; i < text.length(); i++) {
            if (i > pos) {
                break;
            }
            if (text.charAt(i) == '\n') {
                pos++;
            }
        }
        return pos;
    }
    public static boolean insert(VisualPetri petri, String expressionText, ExpressionParameters.Mode mode) throws InvalidConnectionException {

        checkMode(mode);

        try {
            validateExpression(expressionText, Model.PETRI_NET);
            //checkNestedIteration(expressionText);
        } catch (ParseException e1) {
            e1.printStackTrace();
            return false;
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
            e.printStackTrace();
            return false;
        }
        checkIteration(Model.PETRI_NET);
        CotreeTool ctr = new CotreeTool();
        //if the expression is merely a single transition
        if (CotreeTool.nodes.size() == 0 && CotreeTool.singleTransition != null) {
            ctr.drawSingleTransition(Model.PETRI_NET);
        }
        labelNameMap = new HashMap<>();
        expressionText = makeTransitionsUnique(expressionText);

        try {
            validateExpression(expressionText, Model.PETRI_NET);
        } catch (ParseException | org.workcraft.plugins.cflt.javaccStg.ParseException e1) {
            e1.printStackTrace();
            return false;
        }
        ctr.drawInterpretedGraph(mode, Model.PETRI_NET);

        AbstractLayoutCommand alc = petri.getBestLayouter();
        alc.layout(petri);

        return (petri != null) && (expressionText != null) && (mode != null);
    }

    public static boolean insert(VisualStg stg, String expressionText, ExpressionParameters.Mode mode) {
        checkMode(mode);

        try {
            validateExpression(expressionText, Model.STG);
            //checkNestedIteration(expressionText);
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException | ParseException e) {
            e.printStackTrace();
            return false;
        }
        checkIteration(Model.STG);
        CotreeTool ctr = new CotreeTool();
        //if the expression is merely a single transition
        if (CotreeTool.nodes.size() == 0 && CotreeTool.singleTransition != null) {
            ctr.drawSingleTransition(Model.STG);
        }
        nameDirectionMap = new HashMap<>();
        labelNameMap = new HashMap<>();
        expressionText = makeTransitionsUnique(expressionText);

        try {
            validateExpression(expressionText, Model.STG);
        } catch (org.workcraft.plugins.cflt.javaccStg.ParseException | ParseException e) {
            e.printStackTrace();
            return false;
        }
        ctr.drawInterpretedGraph(mode, Model.STG);

        AbstractLayoutCommand alc = stg.getBestLayouter();
        alc.layout(stg);

        return (stg != null) && (expressionText != null) && (mode != null);
    }

    /**
     * Transitions in the expression text are made unique and their original name is stored, later to be used as a label
     * @param expressionText
     * @return expressionText with all transitions being unique
     */
    private static String makeTransitionsUnique(String expressionText) {
        String str = expressionText;
        int i = 0;
        int repNo = 0;
        while (i < expressionText.length()) {

            String transition = "";
            while (expressionText.charAt(i) != CONCURRENCY
                    && expressionText.charAt(i) != SEQUENCE
                    && expressionText.charAt(i) != CHOICE
                    && expressionText.charAt(i) != OPEN_BRACKET
                    && expressionText.charAt(i) != CLOSED_BRACKET
                    && expressionText.charAt(i) != OPEN_CURLY_BRACKET
                    && expressionText.charAt(i) != CLOSED_CURLY_BRACKET
                    && expressionText.charAt(i) != '\t'
                    && expressionText.charAt(i) != '\n'
                    && expressionText.charAt(i) != ' '
                    && expressionText.charAt(i) != '/') {
                transition += expressionText.charAt(i);
                i++;
                if (i == expressionText.length()) {
                    break;
                }
            }
            if (!transition.contains("//") && !transition.equals("\n") && !transition.equals("")) {
                String uniqueT = "t" + repNo;

                char lastC = expressionText.charAt(i - 1);
                int wasAltered = 0;
                nameDirectionMap.put(uniqueT, lastC);
                if (lastC == PLUS_DIR || lastC == MINUS_DIR || lastC == TOGGLE_DIR) {
                    transition = transition.substring(0, transition.length() - 1);
                    wasAltered = 1;
                }
                labelNameMap.put(uniqueT, transition);

                str = str.substring(0, i - transition.length() - wasAltered) + uniqueT + str.substring(i);
                i -= transition.length();
                i += uniqueT.length() - 1;
                repNo++;
                expressionText = str;
            }
            i++;
        }
        return str;
    }

    public static void validateExpression(String expressionText, Model model) throws ParseException,
                            org.workcraft.plugins.cflt.javaccStg.ParseException {

        InputStream is = new ByteArrayInputStream(expressionText.getBytes(StandardCharsets.UTF_8));
        if (model == Model.PETRI_NET) {

            PetriStringParser parser = new PetriStringParser(is);

            try {
                parser.parse(expressionText);
            } catch (ParseException e) {
                DialogUtils.showError(e.getMessage(), "Parse Exception");
                e.printStackTrace();
                throw e;

            } catch (Error e) {
                DialogUtils.showError(e.getMessage(), "Error");
                e.printStackTrace();
                throw e;
            }
        } else if (model == Model.STG) {

            StgStringParser parser = new StgStringParser(is);

            try {
                parser.parse(expressionText);
            } catch (Error e) {
                DialogUtils.showError(e.getMessage(), "Error");
                e.printStackTrace();
                throw e;
            } catch (org.workcraft.plugins.cflt.javaccStg.ParseException e) {
                DialogUtils.showError(e.getMessage(), "Parse Exception");
                e.printStackTrace();
                throw e;
            }
        }
    }
    private static void checkIteration(Model model) {
        if (CotreeTool.containsIteration) {
            DialogUtils.showWarning(model.toString() + " may yield unexpected/ incorrect result.", "Iteration Detected");
        }
    }
    private static void checkMode(Mode mode) {
        if (mode == Mode.SLOW_EXACT) {
            DialogUtils.showWarning("The exhaustive search algorithm may take a long time to compute," + "\n" +
                    "heuristics may be used instead.", "Slow Exact Algorithm Used");
        }
    }
}
