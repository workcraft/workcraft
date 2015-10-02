package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.workcraft.plugins.son.BlockConnector;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.StructureVerifySettings;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.util.Before;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.WorkspaceEntry;

public class SONMainTask implements Task<VerificationResult>{

	private WorkspaceEntry we;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private StructureVerifySettings settings;
	private int totalErrNum = 0;
	private int totalWarningNum = 0;

	private Collection<String> groupErrors = new HashSet<String>();
	private Collection<String> relationErrors= new HashSet<String>();
	private Collection<ArrayList<String>> cycleErrors = new ArrayList<ArrayList<String>>();

	public SONMainTask(StructureVerifySettings settings, WorkspaceEntry we){
		this.settings = settings;
		this.we = we;
	}

	@Override
	public Result<? extends VerificationResult> run (ProgressMonitor <? super VerificationResult> monitor){
		clearConsole();
		//all tasks
		SON net=(SON)we.getModelEntry().getMathModel();
		VisualSON visualNet = (VisualSON)we.getModelEntry().getVisualModel();

		if(settings.getType() == 0){

			StructuralVerification onSTask = new ONStructureTask(net);
			onSTask.task(settings.getSelectedGroups());

			StructuralVerification csonSTask = new CSONStructureTask(net);
			csonSTask.task(settings.getSelectedGroups());

			StructuralVerification bsonSTask = new BSONStructureTask(net);
			bsonSTask.task(settings.getSelectedGroups());

			groupErrors.addAll(onSTask.getGroupErrors());
			relationErrors.addAll(onSTask.getRelationErrors());
			cycleErrors.addAll(onSTask.getCycleErrors());

			groupErrors.addAll(csonSTask.getGroupErrors());
			relationErrors.addAll(csonSTask.getRelationErrors());
			cycleErrors.addAll(csonSTask.getCycleErrors());

			groupErrors.addAll(bsonSTask.getGroupErrors());
			relationErrors.addAll(bsonSTask.getRelationErrors());
			cycleErrors.addAll(bsonSTask.getCycleErrors());

			totalErrNum = totalErrNum+onSTask.getErrNumber();
			totalWarningNum = totalWarningNum+onSTask.getWarningNumber();

			totalErrNum = totalErrNum + csonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + csonSTask.getWarningNumber();

			totalErrNum = totalErrNum + bsonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + bsonSTask.getWarningNumber();

			//if(settings.getOuputBefore())
				//outputBefore(net);
		}

		//group structure tasks
		if(settings.getType() == 1){
			ONStructureTask onSTask = new ONStructureTask(net);
			//main group task
			onSTask.task(settings.getSelectedGroups());

			groupErrors.addAll(onSTask.getGroupErrors());
			relationErrors.addAll(onSTask.getRelationErrors());
			cycleErrors.addAll(onSTask.getCycleErrors());

			totalErrNum = onSTask.getErrNumber();
			totalWarningNum = onSTask.getWarningNumber();

		}

		//CSON structure tasks
		if(settings.getType() == 2){
			CSONStructureTask csonSTask = new CSONStructureTask(net);
			csonSTask.task(settings.getSelectedGroups());

			groupErrors.addAll(csonSTask.getGroupErrors());
			relationErrors.addAll(csonSTask.getRelationErrors());
			cycleErrors.addAll(csonSTask.getCycleErrors());

			totalErrNum = totalErrNum + csonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + csonSTask.getWarningNumber();

		}

		//BSON structure tasks
		if(settings.getType() == 3){
			BSONStructureTask bsonSTask = new BSONStructureTask(net);
			bsonSTask.task(settings.getSelectedGroups());

			groupErrors.addAll(bsonSTask.getGroupErrors());
			relationErrors.addAll(bsonSTask.getRelationErrors());
			cycleErrors.addAll(bsonSTask.getCycleErrors());

			totalErrNum = totalErrNum + bsonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + bsonSTask.getWarningNumber();
		}

		BlockConnector.blockInternalConnector(visualNet);

		//TSON structure tasks
		if(settings.getType() == 0){
			TSONStructureTask tsonSTask = new TSONStructureTask(net);
			tsonSTask.task(settings.getSelectedGroups());

			groupErrors.addAll(tsonSTask.getGroupErrors());
			relationErrors.addAll(tsonSTask.getRelationErrors());
			cycleErrors.addAll(tsonSTask.getCycleErrors());

			totalErrNum = totalErrNum + tsonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + tsonSTask.getWarningNumber();
		}

		if(settings.getType() == 4){
			TSONStructureTask tsonSTask = new TSONStructureTask(net);
			tsonSTask.task(settings.getSelectedGroups());

			groupErrors.addAll(tsonSTask.getGroupErrors());
			relationErrors.addAll(tsonSTask.getRelationErrors());
			cycleErrors.addAll(tsonSTask.getCycleErrors());

			totalErrNum = totalErrNum + tsonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + tsonSTask.getWarningNumber();
		}

		if(settings.getOuputBefore())
			outputBefore(net);

		int err = getTotalErrNum();
		int warning = getTotalWarningNum();

		errNodesHighlight(settings.getErrNodesHighlight(), net);

		logger.info("\n\nVerification-Result : "+ err + " Error(s), " + warning + " Warning(s).");


		return new Result<VerificationResult>(Outcome.FINISHED);
	}

	private static void clearConsole()
	{
	    try
	    {
	        String os = System.getProperty("os.name");

	        if (os.contains("Window"))
	        {
	            Runtime.getRuntime().exec("cls");
	        }
	        else
	        {
	            Runtime.getRuntime().exec("cls");
	        }
	    }
	    catch (Exception exception)
	    {
	        //  Handle exception.
	    }
	}

	private void outputBefore(SON net){
		if(totalErrNum > 0){
			totalWarningNum++;
			logger.info("WARNING : Structure error exist, cannot output before(e).");
		}else{
			BSONAlg bsonAlg = new BSONAlg(net);
			logger.info("\nOutput BSON causal dependencies:");
			Map<TransitionNode, Before> before = bsonAlg.getBeforeMap();

			for(TransitionNode e : before.keySet()){
				logger.info("before("+ net.getNodeReference(e)+"): ");
				Collection<String> relations = new ArrayList<String>();

				for(TransitionNode[] e2 : before.get(e))
					relations.add("("+net.getNodeReference(e2[0]) + ", " + net.getNodeReference(e2[1])+ ")");

				logger.info(relations);
			}
		}
	}


	private void errNodesHighlight(boolean b, SON net){
		if(b){
			for(String group : groupErrors){
				net.setFillColor(net.getNodeByReference(group), SONSettings.getRelationErrColor());
			}

			for(String node : relationErrors){
				net.setFillColor(net.getNodeByReference(node), SONSettings.getRelationErrColor());
			}

			for (ArrayList<String> list : cycleErrors)
				for (String node : list)
					net.setForegroundColor(net.getNodeByReference(node), SONSettings.getCyclePathColor());
		}
	}

	public int getTotalErrNum(){
		return this.totalErrNum;
	}

	public int getTotalWarningNum(){
		return this.totalWarningNum;

	}

	public Collection<String> getRelationErrors() {
		return this.relationErrors;
	}

	public Collection<ArrayList<String>> getCycleErrors() {
		return this.cycleErrors;
	}

	public Collection<String> getGroupErrors() {
		return this.groupErrors;
	}

}
