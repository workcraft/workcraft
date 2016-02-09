package org.workcraft.plugins.cpog.tasks;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.VisualScenario;
import org.workcraft.plugins.cpog.VisualVariable;
import org.workcraft.plugins.cpog.VisualVertex;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CpogEncoding;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.ToolUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ScencoSolver {

    public static final String ACCESS_SCENCO_ERROR = "SCENCO error";
    public static final String MSG_NOT_ENOUGH_SCENARIOS = "Not enough scenarios. Select at least two scenarios.";
    public static final String MSG_TOO_MANY_SCENARIOS = "Too many scenarios selected.";
    public static final String MSG_SELECTION_MODE_UNDEFINED = "Selection mode undefined.";
    public static final String MSG_GATE_LIB_NOT_PRESENT = "Gate library not present. Please insert this " +
                                                          "(genlib format) inside ABC folder.";
    public static final String MSG_ABC_NOT_PRESENT = "Find out more information on " +
                                                     "\"http://www.eecs.berkeley.edu/~alanmi/abc/\" or try to " +
                                                     "set path of the folder containing Abc inside Workcraft settings.";

    private EncoderSettings settings;
    private WorkspaceEntry we;
    private ScencoExecutionSupport cpogBuilder;
    private VisualCPOG cpog;

    // SETTING PARAMETERS FOR CALLING SCENCO
    private String scencoCommand;
    private String espressoCommand;
    private String abcFolder;
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

    private String[] opt_enc;
    private String[] opt_formulaeVertices;
    private String[] truthTableVertices;
    private String[] opt_vertices;
    private String[] opt_sources;
    private String[] opt_dests;
    private String[] opt_formulaeArcs;
    private String[] truthTableArcs;
    private String[] arcNames;
    private int v;
    private int a;
    private int n,m;
    private char[][][] constraints;
    private int[][] graph;
    private ArrayList<VisualTransformableNode> scenarios;
    private HashMap<String, Integer> events;
    private ArrayList<Point2D> positions;
    private ArrayList<Integer> count;
    private File directory;

    public ScencoSolver(EncoderSettings settings, WorkspaceEntry we){
        this.settings = settings;
        this.we = we;
        this.cpogBuilder = new ScencoExecutionSupport();
    }

    public ArrayList<String> getScencoArguments(){
        ArrayList<String> args = new ArrayList<String>();
        ArrayList<String> check;

        cpog = (VisualCPOG)(we.getModelEntry().getVisualModel());
        scenarios = CpogParsingTool.getScenarios(cpog);
        we.captureMemento();

        cpogBuilder.reset_vars(    verbose, genMode, numSol, customFlag, customPath, effort,
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
        graph = new int[n][n];
        check = cpogBuilder.constructConstraints(constraints,graph,m,n,
                scenarios, events, positions, count);
        if(check.get(0).contains("ERROR")){
            return check;
        }

        String prefix = FileUtils.getTempPrefix(we.getTitle());
        directory = FileUtils.createTempDirectory(prefix);
        File scenarioFile = new File(directory , "scenarios.cpog");
        File encodingFile = new File(directory, "custom.enc");
        File resultDirectory = new File(directory, "result");
        resultDirectory.mkdir();
        if((cpogBuilder.WriteCpogIntoFile(m, scenarios, scenarioFile, encodingFile, settings)) != 0){
            FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
            args.add("ERROR");
            args.add("Error on writing scenario file.");
            args.add("Workcraft error");
            return args;
        }

        instantiateParameters(n, m);

        if(settings.isAbcFlag()){
            File f = new File(abcFolder);
            if(!f.exists() || !f.isDirectory()){
                FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
                args.add("ERROR");
                args.add(MSG_ABC_NOT_PRESENT);
                args.add(ACCESS_SCENCO_ERROR);
                return args;
            }
            else{
                abcFlag = "-a";
                gateLibFlag = "-lib";
                f = new File(abcFolder + gatesLibrary);
                if(!f.exists() || f.isDirectory()){
                    FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
                    args.add("ERROR");
                    args.add(MSG_GATE_LIB_NOT_PRESENT);
                    args.add(ACCESS_SCENCO_ERROR);
                    return args;
                }
            }
        }else{
            abcFlag = "";
            abcFolder = "";
            gateLibFlag = "";
            gatesLibrary = "";
        }

        // FILL IN PARAMETERS FOR CALLING PROGRAMER PROPERLY
        if(settings.isCpogSize()) cpogSize = "-cs";
        if(settings.isCostFunc()) disableFunction = "-d";
        if(settings.isVerboseMode()) verbose = "-v";
        if(settings.isEffort()) effort = "all";
        else effort = "min";
        if(settings.isCustomEncMode()){
            customFlag = "-set";
            customPath = encodingFile.getAbsolutePath();
        }
        switch(settings.getGenMode()){
            case OPTIMAL_ENCODING:
                genMode = "-top";
                numSol = String.valueOf(settings.getSolutionNumber());
                break;
            case RECURSIVE:
                if(settings.isCustomEncMode()){
                    modBitFlag = "-bit";
                    modBit = String.valueOf(settings.getBits());
                }
                break;
            case RANDOM:
                genMode = "-r";
                if(settings.isCustomEncMode()){
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
                FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
                args.add("ERROR");
                args.add(MSG_SELECTION_MODE_UNDEFINED);
                args.add(ACCESS_SCENCO_ERROR);
                return args;
        }

        //Adding arguments to list
        scencoCommand = ToolUtils.getAbsoluteCommandPath(CpogSettings.getScencoCommand());
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
        if (abcFolder != null && !abcFolder.isEmpty()) args.add(abcFolder);
        if (gateLibFlag != null && !gateLibFlag.isEmpty()) args.add(gateLibFlag);
        if (gatesLibrary != null && !gatesLibrary.isEmpty()) args.add(gatesLibrary);
        args.add("-res");
        if ((resultDirectory.getAbsolutePath() != null) && !resultDirectory.getAbsolutePath().isEmpty()) args.add(resultDirectory.getAbsolutePath());
        if (modBitFlag != null && !modBitFlag.isEmpty()) args.add(modBitFlag);
        if (modBit != null && !modBit.isEmpty()) args.add(modBit);

        // final return
        return args;
    }

    public void handleResult(String[] outputLines, String resultDirectoryPath){
        opt_enc = new String[m];
        opt_formulaeVertices = new String[n*n];
        truthTableVertices =  new String[n*n];
        opt_vertices = new String[n];
        opt_sources = new String[n*n];
        opt_dests = new String[n*n];
        opt_formulaeArcs = new String[n*n];
        truthTableArcs =  new String[n*n];
        arcNames = new String[n*n];

        try{
            for (int i=0; i <outputLines.length; i++){
                if(settings.isVerboseMode())
                    System.out.println(outputLines[i]);

                // Read Optimal Encoding
                if(outputLines[i].contains("MIN: ")){

                    StringTokenizer string = new StringTokenizer(outputLines[i], " ");
                    int j = 0;
                    string.nextElement();
                    while (j < m) {
                        opt_enc[j++] = (String) string.nextElement();
                    }
                }

                // Read Optimal Formulae
                if(outputLines[i].contains(".start_formulae")){
                    i++;
                    v = 0; a = 0;
                    while(outputLines[i].contains(".end_formulae") == false){
                        if(settings.isVerboseMode())
                            System.out.println(outputLines[i]);
                        StringTokenizer st2 = new StringTokenizer(outputLines[i], ",");
                        String el = (String)st2.nextElement();
                        if(el.equals("V")){ //formula of a vertex
                            opt_vertices[v] = (String) st2.nextElement();
                            truthTableVertices[v] = (String) st2.nextElement();
                            opt_formulaeVertices[v++] = (String) st2.nextElement();
                        }else{
                            opt_sources[a] = (String) st2.nextElement();
                            opt_dests[a] = (String) st2.nextElement();
                            arcNames[a] = opt_sources[a] + "->" + opt_dests[a];
                            truthTableArcs[a] = (String) st2.nextElement();
                            opt_formulaeArcs[a++] = (String) st2.nextElement();
                        }
                        i++;
                    }

                }

                // Read statistics
                if(outputLines[i].contains(".statistics")){
                    i++;
                    while(outputLines[i].contains(".end_statistics") == false){
                        System.out.println(outputLines[i]);
                        i++;
                    }
                }
            }

            // Print controller
            cpogBuilder.printController(m, resultDirectoryPath, opt_enc);

            // group similar constraints
            HashMap<String, BooleanFormula> formulaeName = new HashMap<String, BooleanFormula>();
            HashMap<String, Integer> task = new HashMap<String, Integer>();

            cpogBuilder.groupConstraints(n,m,constraints,task);

            char[][] matrix = new char[m][task.size()];
            String[] instance = new String[m];
            for(String s : task.keySet())
                for(int i = 0; i < m; i++) matrix[i][task.get(s)] = s.charAt(i);

            for(int i = 0; i < m; i++) instance[i] = new String(matrix[i]);

            int freeVariables;
            if(settings.getGenMode() != GenerationMode.SCENCO)
                freeVariables = opt_enc[0].length();
            else{
                freeVariables = settings.getBits();
            }
            settings.getCircuitSize();

            // GET PREDICATES FROM WORKCRAFT ENVIRONMENT
            VisualVariable predicatives[] = new VisualVariable[n];
            int pr = 0;
            for(VisualVariable variable : Hierarchy.getChildrenOfType(cpog.getRoot(), VisualVariable.class)) {
                predicatives[pr++] = variable;
            }

            Variable[] vars = new Variable[freeVariables + pr];
            for(int i = 0; i < freeVariables; i++) vars[i] = cpog.createVisualVariable().getMathVariable();
            for(int i = 0; i< pr; i++) vars[freeVariables +i] = predicatives[i].getMathVariable();

            CpogEncoding solution = null;

            // READ OUTPUT OF SCENCO INSTANTIATING THE OPTIMAL ENCODING SOLUTION
            // AND CONNECTING IT TO EACH VISUAL VERTEX EXPLOITING A MAP
            System.out.println("Op-code selected for graphs:");
            for(int i=0; i<m; i++){
                opt_enc[i] = opt_enc[i].replace('-', 'X');
                String name;
                if (scenarios.get(i).getLabel().isEmpty()) {
                    name = "CPOG " + i;
                } else {
                    name = scenarios.get(i).getLabel();
                }
                System.out.println(name + ": " + opt_enc[i]);
            }

            solution = new CpogEncoding(null, null);
            //BooleanFormula[][] encodingVars = opt_task.getEncodingVars();
            BooleanFormula[] formule = new BooleanFormula[v + a];

            // Set optimal formulae to graphs
            try {
                cpogBuilder.connectFormulaeToVisualVertex(v, a, vars, formulaeName, opt_formulaeVertices,
                        opt_vertices, opt_formulaeArcs, arcNames);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Set Formule
            solution.setFormule(formule);

            // Set optimal encoding to graphs
            boolean[][] opt_encoding = new boolean[m][];
            for(int i=0;i<m;i++)
            {
                opt_encoding[i] = new boolean[freeVariables + pr];
                for(int j=0;j<freeVariables;j++){
                    if(opt_enc[i].charAt(j) == '0' || opt_enc[i].charAt(j) == '-') opt_encoding[i][j] = false;
                    else    opt_encoding[i][j] = true;
                }
                for(int j=freeVariables;j<freeVariables + pr;j++){
                    opt_encoding[i][j] = false;
                }

            }
            solution.setEncoding(opt_encoding);

            boolean[][] encoding = solution.getEncoding();

            // CREATE RESULT PART
            VisualScenario resultCpog = cpog.createVisualScenario();
            resultCpog.setLabel("Composition");
            VisualVertex[] vertices = new VisualVertex[n];

            //INSTANTIATING THE ENCODING INTO GRAPHS IN WORKCRAFT
            cpogBuilder.instantiateEncoding(m, freeVariables, scenarios,vars,encoding,pr,
                    events, vertices, cpog, resultCpog, positions, count, formulaeName);

            // Building CPOG
            cpogBuilder.buildCpog(n,m, constraints, cpog, vertices, formulaeName);

            we.saveMemento();
        }finally{
            // clean up temporary files
            FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
        }
    }

    private void instantiateParameters(int elements, int scenarios){
        scencoCommand = ToolUtils.getAbsoluteCommandPath(CpogSettings.getScencoCommand());
        espressoCommand = ToolUtils.getAbsoluteCommandPath(CpogSettings.getEspressoCommand());
        abcFolder = CpogSettings.getAbcFolder();
        gatesLibrary = CpogSettings.getGatesLibrary();
        espressoFlag = "-e";
    }

    public File getDirectory() {
        return directory;
    }

}
