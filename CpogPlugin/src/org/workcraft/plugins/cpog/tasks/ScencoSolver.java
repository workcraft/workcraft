package org.workcraft.plugins.cpog.tasks;

import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.plugins.cpog.encoding.Encoding;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.cpog.*;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.utils.ExecutableUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ScencoSolver {

    public static final String ACCESS_SCENCO_ERROR = "SCENCO error";
    public static final String MSG_NOT_ENOUGH_SCENARIOS = "Not enough scenarios. Select at least two scenarios.";
    public static final String MSG_TOO_MANY_SCENARIOS = "Too many scenarios selected.";
    public static final String MSG_SELECTION_MODE_UNDEFINED = "Selection mode undefined.";
    public static final String MSG_SAT_BASED_ERROR = "SAT-Based encoding cannot handle the graphs selected";
    public static final String MSG_GATE_LIB_NOT_PRESENT = "Gate library not present. Please insert this " +
                                                          "(genlib format) inside ABC folder.";
    public static final String MSG_ABC_NOT_PRESENT = "Find out more information on " +
                                                     "\"http://www.eecs.berkeley.edu/~alanmi/abc/\" or try to " +
                                                     "set Abc path in Workcraft settings.";
    private static final String VERILOG_TMP_NAME = "micro.v";

    private final EncoderSettings settings;
    private final WorkspaceEntry we;
    private final ScencoExecutionSupport cpogBuilder;
    private VisualCpog cpog;

    // SETTING PARAMETERS FOR CALLING SCENCO
    private String scencoCommand;
    private String espressoCommand;
    private String abcTool;
    private String gatesLibrary;
    private String verbose = "";
    private String genMode = "";
    private String modBitFlag = "";
    private String modBit = "";
    private String numSol = "";
    private String customFlag = "";
    private String customPath = "";
    private String effort = "";
    private String espressoFlag = "";
    private String abcFlag = "";
    private String gateLibFlag = "";
    private String cpogSize = "";
    private String disableFunction = "";
    private String oldSynt = "";
    private String verilogFlag = "";

    private int v;
    private int a;
    private int n, m;
    private char[][][] constraints;
    private ArrayList<VisualTransformableNode> scenarios;
    private HashMap<String, Integer> events;
    private ArrayList<Point2D> positions;
    private ArrayList<Integer> count;
    private File directory;
    private File verilogFile;
    private HashMap<String, BooleanFormula> formulaeName;
    private String[] optEnc;
    private int freeVariables;
    private int pr;
    private Variable[] vars;
    private VisualVariable[] predicatives;

    public ScencoSolver(EncoderSettings settings, WorkspaceEntry we) {
        this.settings = settings;
        this.we = we;
        this.cpogBuilder = new ScencoExecutionSupport();
    }

    public ArrayList<String> getScencoArguments() {
        ArrayList<String> args = new ArrayList<>();
        ArrayList<String> check;
        HashMap<String, Integer> task;

        cpog = WorkspaceUtils.getAs(we, VisualCpog.class);
        scenarios = CpogParsingTool.getScenarios(cpog);
        we.captureMemento();

        cpogBuilder.resetVars(verbose, genMode, numSol, customFlag, customPath, effort,
                                espressoFlag, abcFlag, gateLibFlag, cpogSize, disableFunction, oldSynt);

        events = new HashMap<String, Integer>();
        positions = new ArrayList<Point2D>();
        count = new ArrayList<Integer>();

        // Scenario contains single graphs compose CPOG
        m = scenarios.size();
        // scan scenarios
        n = cpogBuilder.scanScenarios(m, scenarios, events, positions, count);

        // construct constraints
        constraints = new char[m][n][n];
        int[][] graph = new int[n][n];
        check = cpogBuilder.constructConstraints(constraints, graph, m, n,
                scenarios, events, positions, count);
        if (check.get(0).contains("ERROR")) {
            return check;
        }

        // group similar constraints
        optEnc = new String[m];
        task = new HashMap<>();
        formulaeName = new HashMap<>();
        cpogBuilder.groupConstraints(n, m, constraints, task);

        char[][] matrix = new char[m][task.size()];
        String[] instance = new String[m];
        for (String s : task.keySet()) {
            for (int i = 0; i < m; i++) matrix[i][task.get(s)] = s.charAt(i);
        }

        for (int i = 0; i < m; i++) instance[i] = new String(matrix[i]);

        // GET PREDICATES FROM WORKCRAFT ENVIRONMENT
        predicatives = new VisualVariable[n];
        pr = 0;
        for (VisualVariable variable : Hierarchy.getChildrenOfType(cpog.getRoot(), VisualVariable.class)) {
            predicatives[pr++] = variable;
        }

        if (settings.getGenMode() != GenerationMode.OLD_SYNT) {
            freeVariables = settings.getBits();
            vars = new Variable[freeVariables + pr];
            for (int i = 0; i < freeVariables; i++) vars[i] = cpog.createVisualVariable().getMathVariable();
            for (int i = 0; i < pr; i++) vars[freeVariables + i] = predicatives[i].getMathVariable();
        }

        String prefix = FileUtils.getTempPrefix(we.getTitle());
        directory = FileUtils.createTempDirectory(prefix);
        File scenarioFile = new File(directory, "scenarios.cpog");
        File encodingFile = new File(directory, "custom.enc");
        File resultDirectory = new File(directory, "result");
        verilogFile = new File(directory, VERILOG_TMP_NAME);
        resultDirectory.mkdir();
        if ((cpogBuilder.writeCpogIntoFile(m, scenarios, scenarioFile, encodingFile, settings)) != 0) {
            FileUtils.deleteOnExitRecursively(directory);
            args.add("ERROR");
            args.add("Error on writing scenario file.");
            args.add("Workcraft error");
            return args;
        }

        instantiateParameters(n, m);

        if (settings.isAbcFlag()) {
            File f = new File(abcTool);
            if (!f.exists() || f.isDirectory()) {
                FileUtils.deleteOnExitRecursively(directory);
                args.add("ERROR");
                args.add(MSG_ABC_NOT_PRESENT);
                args.add(ACCESS_SCENCO_ERROR);
                return args;
            } else {
                abcFlag = "-a";
                gateLibFlag = "-lib";
                f = new File(gatesLibrary);
                if (!f.exists() || f.isDirectory()) {
                    FileUtils.deleteOnExitRecursively(directory);
                    args.add("ERROR");
                    args.add(MSG_GATE_LIB_NOT_PRESENT);
                    args.add(ACCESS_SCENCO_ERROR);
                    return args;
                } else {
                    if (!settings.isCpogSize()) {
                        verilogFlag = "-ver";
                    }
                }
            }
        } else {
            abcFlag = "";
            abcTool = "";
            gateLibFlag = "";
            gatesLibrary = "";
        }

        // SAT-based encoding
        if (settings.getGenMode() == GenerationMode.SCENCO) {
            Encoding solution = null;

            solution = cpogBuilder.satBasedRun(pr, vars, instance, settings.getCircuitSize());
            if (solution == null) {
                FileUtils.deleteOnExitRecursively(directory);
                we.cancelMemento();
                args.add("ERROR");
                args.add(MSG_SAT_BASED_ERROR);
                args.add(ACCESS_SCENCO_ERROR);
                return args;
            }

            boolean[][] encoding = solution.getEncoding();
            PrintStream output = null;
            try {
                output = new PrintStream(encodingFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < m; i++) {
                for (int j = 0; j < settings.getBits(); j++) {
                    char c = (encoding[i][j] == true) ? '1' : '0';
                    output.print(c);
                }
                output.println();
            }
            output.println(settings.getBits());
            output.close();
        }

        // FILL IN PARAMETERS FOR CALLING PROGRAMER PROPERLY
        if (settings.isCpogSize()) cpogSize = "-cs";
        if (settings.isCostFunc()) disableFunction = "-d";
        if (settings.isVerboseMode()) verbose = "-v";
        if (settings.isEffort()) effort = "all";
        else effort = "min";
        if (settings.isCustomEncMode()) {
            customFlag = "-set";
            customPath = encodingFile.getAbsolutePath();
        }
        switch (settings.getGenMode()) {
        case OPTIMAL_ENCODING:
            genMode = "-top";
            numSol = String.valueOf(settings.getSolutionNumber());
            break;
        case RECURSIVE:
            if (settings.isCustomEncMode()) {
                modBitFlag = "-bit";
                modBit = String.valueOf(settings.getBits());
            }
            break;
        case RANDOM:
            genMode = "-r";
            if (settings.isCustomEncMode()) {
                customFlag = "-set";
                customPath = encodingFile.getAbsolutePath();
                modBitFlag = "-bit";
                modBit = String.valueOf(settings.getBits());
            }
            numSol = String.valueOf(settings.getSolutionNumber());
            break;
        case SCENCO:
            customFlag = "-set";
            genMode = "-top";
            numSol = "1";
            break;
        case OLD_SYNT:
            customFlag = "-set";
            customPath = encodingFile.getAbsolutePath();
            oldSynt = "-old";
            genMode = "-top";
            numSol = "1";
            break;
        case SEQUENTIAL:
            customFlag = "-set";
            customPath = encodingFile.getAbsolutePath();
            genMode = "-top";
            numSol = "1";
            break;
        default:
            FileUtils.deleteOnExitRecursively(directory);
            args.add("ERROR");
            args.add(MSG_SELECTION_MODE_UNDEFINED);
            args.add(ACCESS_SCENCO_ERROR);
            return args;
        }

        //Adding arguments to list
        scencoCommand = ExecutableUtils.getAbsoluteCommandPath(CpogSettings.getScencoCommand());
        if (scencoCommand != null && !scencoCommand.isEmpty()) args.add(scencoCommand);
        if (scenarioFile.getAbsolutePath() != null && !scenarioFile.getAbsolutePath().isEmpty()) args.add(scenarioFile.getAbsolutePath());
        args.add("-m");
        if (effort != null && !effort.isEmpty()) args.add(effort);
        if (genMode != null && !genMode.isEmpty()) args.add(genMode);
        if (numSol != null && !numSol.isEmpty()) args.add(numSol);
        if (customFlag != null && !customFlag.isEmpty()) args.add(customFlag);
        if (customPath != null && !customPath.isEmpty()) args.add(customPath);
        if (verbose != null && !verbose.isEmpty()) args.add(verbose);
        if (cpogSize != null && !cpogSize.isEmpty()) args.add(cpogSize);
        if (disableFunction != null && !disableFunction.isEmpty()) args.add(disableFunction);
        if (oldSynt != null && !oldSynt.isEmpty()) args.add(oldSynt);
        if (espressoFlag != null && !espressoFlag.isEmpty()) args.add(espressoFlag);
        if (espressoCommand != null && !espressoCommand.isEmpty()) args.add(espressoCommand);
        if (abcFlag != null && !abcFlag.isEmpty()) args.add(abcFlag);
        if (abcTool != null && !abcTool.isEmpty()) args.add(abcTool);
        if (gateLibFlag != null && !gateLibFlag.isEmpty()) args.add(gateLibFlag);
        if (gatesLibrary != null && !gatesLibrary.isEmpty()) args.add(gatesLibrary);
        args.add("-res");
        if ((resultDirectory.getAbsolutePath() != null) && !resultDirectory.getAbsolutePath().isEmpty()) args.add(resultDirectory.getAbsolutePath());
        if (modBitFlag != null && !modBitFlag.isEmpty()) args.add(modBitFlag);
        if (modBit != null && !modBit.isEmpty()) args.add(modBit);
        if (verilogFlag != null && !verilogFlag.isEmpty()) {
            args.add(verilogFlag);
            args.add(verilogFile.getAbsolutePath());
        }

        // final return
        return args;
    }

    public void handleResult(String[] outputLines, String resultDirectoryPath) {
        String[] optFormulaeVertices = new String[n * n];
        String[] optVertices = new String[n];
        String[] optSources = new String[n * n];
        String[] optDests = new String[n * n];
        String[] optFormulaeArcs = new String[n * n];
        String[] arcNames = new String[n * n];

        try {
            for (int i = 0; i < outputLines.length; i++) {
                if (settings.isVerboseMode()) {
                    System.out.println(outputLines[i]);
                }

                // Read Optimal Encoding
                if (outputLines[i].contains("MIN: ")) {

                    StringTokenizer string = new StringTokenizer(outputLines[i], " ");
                    int j = 0;
                    string.nextElement();
                    while (j < m) {
                        optEnc[j++] = (String) string.nextElement();
                    }
                }

                // Read Optimal Formulae
                if (outputLines[i].contains(".start_formulae")) {
                    i++;
                    v = 0;
                    a = 0;
                    while (outputLines[i].contains(".end_formulae") == false) {
                        if (settings.isVerboseMode()) {
                            System.out.println(outputLines[i]);
                        }
                        StringTokenizer st2 = new StringTokenizer(outputLines[i], ",");
                        String el = (String) st2.nextElement();
                        if (el.equals("V")) { //formula of a vertex
                            String vertexName = (String) st2.nextElement();
                            if (!vertexName.equals(settings.GO_SIGNAL) &&
                                    !vertexName.equals(settings.DONE_SIGNAL)) {
                                optVertices[v] = vertexName;
                                st2.nextElement();
                                optFormulaeVertices[v++] = (String) st2.nextElement();
                            }
                        } else {
                            optSources[a] = (String) st2.nextElement();
                            optDests[a] = (String) st2.nextElement();
                            arcNames[a] = optSources[a] + "->" + optDests[a];
                            st2.nextElement();
                            optFormulaeArcs[a++] = (String) st2.nextElement();
                        }
                        i++;
                    }

                }

                // Read statistics
                if (outputLines[i].contains(".statistics")) {
                    i++;
                    while (outputLines[i].contains(".end_statistics") == false) {
                        System.out.println(outputLines[i]);
                        i++;
                    }
                }
            }

            if (settings.getGenMode() != GenerationMode.SCENCO) {
                freeVariables = optEnc[0].length();
            } else {
                freeVariables = settings.getBits();
            }

            // Print controller
            cpogBuilder.printController(m, resultDirectoryPath, optEnc);

            Encoding solution = null;

            // READ OUTPUT OF SCENCO INSTANTIATING THE OPTIMAL ENCODING SOLUTION
            // AND CONNECTING IT TO EACH VISUAL VERTEX EXPLOITING A MAP
            System.out.println("Op-code selected for graphs:");
            for (int i = 0; i < m; i++) {
                optEnc[i] = optEnc[i].replace('-', 'X');
                String name;
                if (scenarios.get(i).getLabel().isEmpty()) {
                    name = "CPOG " + i;
                } else {
                    name = scenarios.get(i).getLabel();
                }
                System.out.println(name + ": " + optEnc[i]);
            }

            solution = new Encoding(null, null);
            BooleanFormula[] formule = new BooleanFormula[v + a];

            if (settings.getGenMode() == GenerationMode.OLD_SYNT) {
                vars = new Variable[freeVariables + pr];
                for (int i = 0; i < freeVariables; i++) vars[i] = cpog.createVisualVariable().getMathVariable();
                for (int i = 0; i < pr; i++) vars[freeVariables + i] = predicatives[i].getMathVariable();
            }

            // Set optimal formulae to graphs
            try {
                cpogBuilder.connectFormulaeToVisualVertex(v, a, vars, formulaeName, optFormulaeVertices,
                        optVertices, optFormulaeArcs, arcNames);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Set Formule
            solution.setFormule(formule);

            // Set optimal encoding to graphs
            boolean[][] optEncoding = new boolean[m][];
            for (int i = 0; i < m; i++) {
                optEncoding[i] = new boolean[freeVariables + pr];
                for (int j = 0; j < freeVariables; j++) {
                    if (optEnc[i].charAt(j) == '0' || optEnc[i].charAt(j) == '-') optEncoding[i][j] = false;
                    else    optEncoding[i][j] = true;
                }
                for (int j = freeVariables; j < freeVariables + pr; j++) {
                    optEncoding[i][j] = false;
                }

            }
            solution.setEncoding(optEncoding);

            boolean[][] encoding = solution.getEncoding();

            // CREATE RESULT PART
            VisualScenario resultCpog = cpog.createVisualScenario();
            resultCpog.setLabel("Composition");
            VisualVertex[] vertices = new VisualVertex[n];

            //INSTANTIATING THE ENCODING INTO GRAPHS IN WORKCRAFT
            cpogBuilder.instantiateEncoding(m, freeVariables, scenarios, vars, encoding, pr,
                    events, vertices, cpog, resultCpog, positions, count, formulaeName);

            // Building CPOG
            cpogBuilder.buildCpog(n, m, constraints, cpog, vertices, formulaeName);

            we.saveMemento();
        } finally {
            // clean up temporary files
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    private void instantiateParameters(int elements, int scenarios) {
        scencoCommand = ExecutableUtils.getAbsoluteCommandPath(CpogSettings.getScencoCommand());
        espressoCommand = ExecutableUtils.getAbsoluteCommandPath(CpogSettings.getEspressoCommand());
        abcTool = CpogSettings.getAbcTool();
        gatesLibrary = ExecutableUtils.getAbsoluteCommandPath(CircuitSettings.getGateLibrary());
        espressoFlag = "-e";
    }

    public File getDirectory() {
        return directory;
    }

    public Boolean isVerilog() {
        return !verilogFlag.isEmpty();
    }

    public byte[] getVerilog() {
        byte[] verilogBytes = null;
        try {
            verilogBytes = Files.readAllBytes(Paths.get(verilogFile.getAbsolutePath()));
        } catch (IOException io) {
            FileUtils.deleteOnExitRecursively(directory);
        }
        return verilogBytes;
    }

}
