package org.workcraft.plugins.son.verify;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;
import org.workcraft.plugins.son.gui.StructureVerifySettings;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;

public class SONMainTask implements Task<VerificationResult>{

	private SONModel net;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	private StructureVerifySettings settings;
	private int totalErrNum = 0;
	private int totalWarningNum = 0;

	public SONMainTask(StructureVerifySettings settings, SONModel net){
		this.settings = settings;
		this.net = net;
	}

	@Override
	public Result<? extends VerificationResult> run (ProgressMonitor <? super VerificationResult> monitor){
		clearConsole();
		//all tasks

		if(settings.getType() == 0){

			StructuralVerification groupSTask = new ONStructureTask(net);
			groupSTask.task(settings.getSelectedGroups());

			StructuralVerification csonSTask = new CSONStructureTask(net);
			csonSTask.task(settings.getSelectedGroups());

			StructuralVerification bsonSTask = new BSONStructureTask(net);
			bsonSTask.task(settings.getSelectedGroups());

			if(settings.getErrNodesHighlight()){
				groupSTask.errNodesHighlight();
				csonSTask.errNodesHighlight();
				bsonSTask.errNodesHighlight();
			}

			totalErrNum = groupSTask.getErrNumber();
			totalWarningNum = groupSTask.getWarningNumber();

			totalErrNum = totalErrNum + csonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + csonSTask.getWarningNumber();

			totalErrNum = totalErrNum + bsonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + bsonSTask.getWarningNumber();

			if(settings.getOuputBefore())
				outputBefore();

			//continue TSON verification

		}

		//group structure tasks
		if(settings.getType() == 1){
			ONStructureTask groupSTask = new ONStructureTask(net);
			//main group task
			groupSTask.task(settings.getSelectedGroups());

			//highlight setting
			if(settings.getErrNodesHighlight())
				groupSTask.errNodesHighlight();

			totalErrNum = groupSTask.getErrNumber();
			totalWarningNum = groupSTask.getWarningNumber();

		}

		//CSON structure tasks
		if(settings.getType() == 2){
			CSONStructureTask csonSTask = new CSONStructureTask(net);
			csonSTask.task(settings.getSelectedGroups());

			if(settings.getErrNodesHighlight()){
				csonSTask.errNodesHighlight();
			}
			totalErrNum = totalErrNum + csonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + csonSTask.getWarningNumber();

		}

		//BSON structure tasks
		if(settings.getType() == 3){
			BSONStructureTask bsonSTask = new BSONStructureTask(net);
			bsonSTask.task(settings.getSelectedGroups());

			if(settings.getErrNodesHighlight()){
				bsonSTask.errNodesHighlight();
			}

			totalErrNum = totalErrNum + bsonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + bsonSTask.getWarningNumber();

			if(settings.getOuputBefore())
				outputBefore();
		}

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

	private void outputBefore(){
		if(totalErrNum > 0){
			totalWarningNum++;
			logger.info("WARNING : Structure error exist, cannot output before(e).");
		}else{
			BSONAlg bsonAlg = new BSONAlg(net);
			logger.info("\nOutput before(e):");
			Collection<Condition[]> before = new ArrayList<Condition[]>();
			for(Event e : net.getEvents()){
				before =  bsonAlg.before(e);
				if(!before.isEmpty()){
					Collection<String> subResult = new ArrayList<String>();
					logger.info("before("+ net.getName(e)+"): ");
					for(Condition[] condition : before)
						subResult.add("("+net.getName(condition[0]) + " " + net.getName(condition[1])+ ")");
					logger.info(subResult);
				}
			}
		}
	}

	public int getTotalErrNum(){
		return this.totalErrNum;
	}

	public int getTotalWarningNum(){
		return this.totalWarningNum;
	}

}
