package org.workcraft.plugins.son.test;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.workcraft.plugins.son.OutputRedirect;
import org.workcraft.plugins.son.verify.VerificationResult;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;

public class test implements Task<VerificationResult> {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public Result<? extends VerificationResult> run (ProgressMonitor <? super VerificationResult> monitor){
		try{
		output();
		}catch (Exception e){
			return new Result<VerificationResult>(Outcome.FAILED);
		}

		return new Result<VerificationResult>(Outcome.FINISHED);
	}

	public void output() throws Exception{
		OutputRedirect.Redirect();

		int i = 0;
        while (i < 10)
        {
            logger.error ("Current time: " + System.currentTimeMillis ());
            Thread.sleep (1000L);
            i++;
        }
	}

	public enum SONConnectionType
	{
		POLYLINE,
		BEZIER,
		SYNCLINE,
		ASYNLINE,
		BHVLINE;

		public String getTypetoString(SONConnectionType type){
			if (type == SONConnectionType.POLYLINE)
				return "POLY";
			if (type == SONConnectionType.SYNCLINE)
				return "SYNC";
			if (type == SONConnectionType.ASYNLINE)
				return "ASYN";
			if (type == SONConnectionType.BHVLINE)
				return "BHV";
			return "";
		}
	};
	public void print(){
		System.out.println(this.toString());
	}

	public static void main(String[] arg){
/*		test t = new test();
		t.print();*/
		ArrayList<String> test = new ArrayList<String>();
		test.add("a");
		test.add("b");
		test.add("c");
		System.out.println(test.toString());
	}


}
