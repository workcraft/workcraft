package org.workcraft.plugins.son.verify;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.StructureVerifySettings;
import org.workcraft.plugins.son.algorithm.BSONAlg;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;
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

	private Collection<ONGroup> groupErrors = new HashSet<ONGroup>();
	private Collection<Node> relationErrors= new HashSet<Node>();
	private Collection<ArrayList<Node>> cycleErrors = new ArrayList<ArrayList<Node>>();

	public SONMainTask(StructureVerifySettings settings, SONModel net){
		this.settings = settings;
		this.net = net;
	}

	@Override
	public Result<? extends VerificationResult> run (ProgressMonitor <? super VerificationResult> monitor){
		clearConsole();
		//all tasks

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

			totalErrNum = onSTask.getErrNumber();
			totalWarningNum = onSTask.getWarningNumber();

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

	public Collection<Node> getRelationErrors() {
		return this.relationErrors;
	}

	public Collection<ArrayList<Node>> getCycleErrors() {
		return this.cycleErrors;
	}

	public Collection<ONGroup> getGroupErrors() {
		return this.groupErrors;
	}

}
