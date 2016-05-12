package org.workcraft.plugins.cpog.tasks;

import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;

public class PetriToCpogHandler extends DummyProgressMonitor<PetriToCpogResult>  {

    public PetriToCpogHandler(PetriToCpogTask task) {
        super();
    }

    @Override
    public void finished(Result<? extends PetriToCpogResult> result, String description) {

        if (result.getOutcome() == Outcome.FINISHED) {
            System.out.println(result.getReturnValue().getStdout());

        } else if (result.getOutcome() == Outcome.FAILED) {
            System.out.println(result.getReturnValue().getStdout());
        }
    }

}
