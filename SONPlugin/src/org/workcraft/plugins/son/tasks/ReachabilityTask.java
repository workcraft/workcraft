package org.workcraft.plugins.son.tasks;

import java.util.Collection;

import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;

public class ReachabilityTask implements Task<VerificationResult>{

	@Override
	public Result<? extends VerificationResult> run(
			ProgressMonitor<? super VerificationResult> monitor) {
		return null;
	}

	private boolean BSONReachable(Collection<PlaceNode> marking){
		boolean result = true;


		return result;
	}

	private boolean CSONReachable(Collection<PlaceNode> marking){
		boolean result = true;


		return result;
	}
}
