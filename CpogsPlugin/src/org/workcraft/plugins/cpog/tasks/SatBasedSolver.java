package org.workcraft.plugins.cpog.tasks;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.VisualScenario;
import org.workcraft.plugins.cpog.VisualVariable;
import org.workcraft.plugins.cpog.VisualVertex;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.CpogEncoding;
import org.workcraft.plugins.cpog.optimisation.DefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.OneHotIntBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.OneHotNumberProvider;
import org.workcraft.plugins.cpog.optimisation.Optimiser;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.ToolUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class SatBasedSolver {

    private EncoderSettings settings;
    private ScencoExecutionSupport cpogBuilder;

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

    // Allocation data structures
    private Process process;
    private String[] optEnc;
    private String[] optFormulaeVertices;
    private String[] truthTableVertices;
    private String[] optVertices;
    private String[] optSources;
    private String[] optDests;
    private String[] optFormulaeArcs;
    private String[] truthTableArcs;
    private String[] arcNames;
    private int v;
    private int a;

    public SatBasedSolver(EncoderSettings settings) {
        this.setSettings(settings);
        cpogBuilder = new ScencoExecutionSupport();
    }

    public void run(WorkspaceEntry we) {
        VisualCPOG cpog = (VisualCPOG)(we.getModelEntry().getVisualModel());
        ArrayList<VisualTransformableNode> scenarios = CpogParsingTool.getScenarios(cpog);
        ArrayList<String> check;

        we.captureMemento();

        reset_vars();

        HashMap<String, Integer> events = new HashMap<String, Integer>();
        ArrayList<Point2D> positions = new ArrayList<Point2D>();
        ArrayList<Integer> count = new ArrayList<Integer>();

        // Scenario contains single graphs compose CPOG
        int m = scenarios.size();
        // scan scenarios
        int n = cpogBuilder.scanScenarios(m, scenarios, events, positions, count);

        // construct constraints
        char[][][] constraints = new char[m][n][n];
        int[][] graph = new int[n][n];
        check = cpogBuilder.constructConstraints(constraints,graph,m,n,
                scenarios, events, positions, count);
        if(check.get(0).contains("ERROR")){
            JOptionPane.showMessageDialog(null,
                    check.get(1),
                    check.get(2),
                    JOptionPane.ERROR_MESSAGE);
            we.cancelMemento();
            return;
        }

        // Write scenarios into file.
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        File scenarioFile = new File(directory, "scenarios.cpog");
        File encodingFile = new File(directory, "custom.enc");
        File resultDirectory = new File(directory, "result");
        resultDirectory.mkdir(); // ???

        int res;
        if((res = cpogBuilder.WriteCpogIntoFile(m, scenarios, scenarioFile, encodingFile, settings)) != 0){
            FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
            if(res != -1){
                JOptionPane.showMessageDialog(null,
                        "Error on writing scenario file.",
                        "Workcraft error",
                        JOptionPane.ERROR_MESSAGE);
            }
            we.cancelMemento();
            return;
        }

        instantiateParameters(n, m);

        // CALLING SCENCO
        boolean callScenco = false;
        if(settings.isAbcFlag()){
            File f = new File(abcFolder);
            if(!f.exists() || !f.isDirectory()){
                JOptionPane.showMessageDialog(null,
                        "Find out more information on \"http://www.eecs.berkeley.edu/~alanmi/abc/\" or try to " +
                                "set path of the folder containing Abc inside Workcraft settings.",
                                "Abc tool not installed correctly",
                                JOptionPane.ERROR_MESSAGE);
            } else{
                abcFlag = "-a";
                gateLibFlag = "-lib";
                f = new File(abcFolder + gatesLibrary);
                if(!f.exists() || f.isDirectory()){
                    FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
                    JOptionPane.showMessageDialog(null,
                            "It is needed to compute area of circuit properly",
                            "Gate library not present",
                            JOptionPane.ERROR_MESSAGE);
                    we.cancelMemento();
                    return;
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

        callScenco = true;
        customFlag = "-set";
        genMode = "-top";
        numSol = "1";

        // group similar constraints
        HashMap<String, BooleanFormula> formulaeName = new HashMap<String, BooleanFormula>();
        HashMap<String, Integer> task = new HashMap<String, Integer>();
        cpogBuilder.groupConstraints(n,m,constraints,task);

        char[][] matrix = new char[m][task.size()];

        String[] instance = new String[m];
        for(String s : task.keySet())
            for(int i = 0; i < m; i++) matrix[i][task.get(s)] = s.charAt(i);

        for(int i = 0; i < m; i++) instance[i] = new String(matrix[i]);

        int freeVariables = settings.getBits();
        int derivedVariables = settings.getCircuitSize();

        Optimiser<OneHotIntBooleanFormula> oneHot = new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider());
        DefaultCpogSolver<BooleanFormula> solverCnf = new DefaultCpogSolver<BooleanFormula>(oneHot, new CleverCnfGenerator());

        // GET PREDICATES FROM WORKCRAFT ENVIRONMENT
        VisualVariable[] predicatives = new VisualVariable[n];
        int pr = 0;
        for(VisualVariable variable : Hierarchy.getChildrenOfType(cpog.getRoot(), VisualVariable.class)) {
            predicatives[pr++] = variable;
        }

        Variable[] vars = new Variable[freeVariables + pr];
        for(int i = 0; i < freeVariables; i++) vars[i] = cpog.createVisualVariable().getMathVariable();
        for(int i = 0; i< pr; i++) vars[freeVariables +i] = predicatives[i].getMathVariable();

        CpogEncoding solution = null;
        try {
            // OLD SCENCO EXECUTION
            if (callScenco) {
                if (pr > 0) {
                    we.cancelMemento();
                    FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
                    JOptionPane.showMessageDialog(null, "Exhaustive search option is not able to solve the CPOG with conditions, try other options.",
                            "Encoding result", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                solution = solverCnf.solve(instance, vars, derivedVariables);
                solverCnf.getTask(instance, vars, derivedVariables);

                if (solution == null) {
                    if (callScenco) {
                        we.cancelMemento();
                        JOptionPane.showMessageDialog(null, "SCENCO is not able to solve the CPOG, try other options.",
                                "Encoding result", JOptionPane.ERROR_MESSAGE);
                        FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
                        return;
                    }
                    System.out.println("INFORMATION: Scenco cannot solve the CPOG.");
                    System.out.println();
                }
            }
        } catch(Exception e) {
            we.cancelMemento();
            FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
            JOptionPane.showMessageDialog(null, e.getMessage(), "Encoding result", JOptionPane.ERROR_MESSAGE);
        }

        // IF SOLUTION IS NULL AN ERROR OCCURRED
        if(solution == null){
            FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
            we.cancelMemento();
            return;
        }

        try{
            boolean[][] encoding = solution.getEncoding();

            // IF OLD SCENDO MODE IS SELECTED, GET THE ENCODING SOLUTION FROM IT AND
            // SYNTHESISE IT THROUGH SCENCO IN ORDER TO OUTPUT THE MICROCONTROLLER
            // AND AREA INFORMATION

            try{
                PrintStream output = new PrintStream(encodingFile);

                for(int i=0; i<m; i++){
                    for(int j=0; j<settings.getBits(); j++){
                        if(encoding[i][j]){
                            output.print("1");
                            //System.out.print("1");
                        } else{
                            output.print("0");
                            //System.out.print("0");
                        }
                    }
                    output.println();
                    // System.out.println();
                }
                output.println(settings.getBits());
                //System.out.println(settings.getBits());
                output.close();
                customPath = encodingFile.getAbsolutePath();

                // setting all the arguments for calling Scenco for synthesys
                ArrayList<String> parameters = new ArrayList<String>();
                if(!scencoCommand.isEmpty()) parameters.add(scencoCommand);
                if(!scenarioFile.getAbsolutePath().isEmpty()) parameters.add(scenarioFile.getAbsolutePath());
                parameters.add("-m");
                if(!effort.isEmpty()) parameters.add(effort);
                if(!genMode.isEmpty()) parameters.add(genMode);
                if(!numSol.isEmpty()) parameters.add(numSol);
                if(!customFlag.isEmpty()) parameters.add(customFlag);
                if(!customPath.isEmpty()) parameters.add(customPath);
                if(!verbose.isEmpty()) parameters.add(verbose);
                if(!cpogSize.isEmpty()) parameters.add(cpogSize);
                if(!disableFunction.isEmpty()) parameters.add(disableFunction);
                if(!oldSynt.isEmpty()) parameters.add(oldSynt);
                if(!espressoFlag.isEmpty()) parameters.add(espressoFlag);
                if(!espressoCommand.isEmpty()) parameters.add(espressoCommand);
                if(!abcFlag.isEmpty()) parameters.add(abcFlag);
                if(!abcFolder.isEmpty()) parameters.add(abcFolder);
                if(!gateLibFlag.isEmpty()) parameters.add(gateLibFlag);
                if(!gatesLibrary.isEmpty()) parameters.add(gatesLibrary);
                parameters.add("-res");
                if((resultDirectory != null) && !resultDirectory.getAbsolutePath().isEmpty()) parameters.add(resultDirectory.getAbsolutePath());
                if(!modBitFlag.isEmpty()) parameters.add(modBitFlag);
                if(!modBit.isEmpty()) parameters.add(modBit);

                if(cpogBuilder.callingScenco(process,settings,parameters,Double.MAX_VALUE, we, 0,
                        false, optEnc,optFormulaeVertices,truthTableVertices,
                        optVertices, optSources, optDests, optFormulaeArcs,
                        truthTableArcs, arcNames, this) != 0){
                    FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
                    we.cancelMemento();
                    return;
                }

                // CONNECT FORMULAE INTO VISUAL ELEMENTS FOR OLD SCENCO MODE
                try {
                    cpogBuilder.connectFormulaeToVisualVertex(v, a, vars, formulaeName, optFormulaeVertices,
                            optVertices, optFormulaeArcs, arcNames);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // PRINT CONTROLLER FOR OLD SCENCO MODE
                cpogBuilder.printController(m, resultDirectory.getAbsolutePath(), optEnc);
            }catch (IOException e) {
                System.out.println("Error: " + e);
            }


            // CREATE RESULT PART
            VisualScenario resultCpog = cpog.createVisualScenario();
            resultCpog.setLabel("Composition");
            VisualVertex[] vertices = new VisualVertex[n];

            cpogBuilder.instantiateEncoding(m, freeVariables, scenarios,vars,encoding,pr,
                    events, vertices, cpog, resultCpog, positions, count, formulaeName);

            // Building CPOG
            cpogBuilder.buildCpog(n,m, constraints, cpog, vertices, formulaeName);

            we.saveMemento();
        }finally{
            FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
        }
    }

    public void setSettings(EncoderSettings settings) {
        this.settings = settings;
    }

    // RESET ALL THE PARAMETERS TO CALL SCENCO TOOL
    private void reset_vars(){
        verbose = "";
        genMode= "";
        numSol= "";
        customFlag= "";
        customPath= "";
        effort= "";
        espressoFlag= "";
        abcFlag= "";
        gateLibFlag= "";
        cpogSize= "";
        disableFunction= "";
        oldSynt= "";
        return;
    }

    private void instantiateParameters(int elements, int scenarios){
        optEnc = new String[scenarios];
        optFormulaeVertices = new String[elements*elements];
        truthTableVertices =  new String[elements*elements];
        optVertices = new String[elements];
        optSources = new String[elements*elements];
        optDests = new String[elements*elements];
        optFormulaeArcs = new String[elements*elements];
        truthTableArcs =  new String[elements*elements];
        arcNames = new String[elements*elements];
        scencoCommand = ToolUtils.getAbsoluteCommandPath(CpogSettings.getScencoCommand());
        espressoCommand = ToolUtils.getAbsoluteCommandPath(CpogSettings.getEspressoCommand());
        abcFolder = CpogSettings.getAbcFolder();
        gatesLibrary = CpogSettings.getGatesLibrary();
        espressoFlag = "-e";
        v=0;
        a=0;
    }


    public void setV(int v) {
        this.v = v;
    }

    public void setA(int a) {
        this.a = a;
    }
}
