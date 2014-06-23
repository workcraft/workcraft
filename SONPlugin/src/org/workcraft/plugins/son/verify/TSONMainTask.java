package org.workcraft.plugins.son.verify;

import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.StructureVerifySettings;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;

public class TSONMainTask implements Task<VerificationResult>
{

	private SONModel net;
	protected StructureVerifySettings settings;

	private int totalErrNum = 0;
	private int totalWarningNum = 0;

	public TSONMainTask(StructureVerifySettings settings, SONModel net) {
		this.net = net;
		this.settings = settings;

	}

	@Override
	public Result<? extends VerificationResult> run (ProgressMonitor <? super VerificationResult> monitor){
		if(settings.getType() == 0){
			TSONStructureTask tsonSTask = new TSONStructureTask(net);
			tsonSTask.task(settings.getSelectedGroups());

			if(settings.getErrNodesHighlight()){
				tsonSTask.errNodesHighlight();
			}

			totalErrNum = totalErrNum + tsonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + tsonSTask.getWarningNumber();
		}

		//TSON structure tasks
		if(settings.getType() == 4){
			TSONStructureTask tsonSTask = new TSONStructureTask(net);
			tsonSTask.task(settings.getSelectedGroups());

			if(settings.getErrNodesHighlight()){
				tsonSTask.errNodesHighlight();
			}

			totalErrNum = totalErrNum + tsonSTask.getErrNumber();
			totalWarningNum = totalWarningNum + tsonSTask.getWarningNumber();
		}

		return new Result<VerificationResult>(Outcome.FINISHED);
	}

	public int getTotalErrNum(){
		return this.totalErrNum;
	}

	public int getTotalWarningNum(){
		return this.totalWarningNum;
	}

}
