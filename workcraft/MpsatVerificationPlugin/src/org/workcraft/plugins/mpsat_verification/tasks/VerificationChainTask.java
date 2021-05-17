package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class VerificationChainTask implements Task<VerificationChainOutput> {

    private static final String STG_FILE_EXTENSION = StgFormat.getInstance().getExtension();
    private static final String NET_FILE_NAME = StgUtils.SPEC_FILE_PREFIX + STG_FILE_EXTENSION;

    private final WorkspaceEntry we;
    private final VerificationParameters verificationParameters;

    public VerificationChainTask(WorkspaceEntry we, VerificationParameters verificationParameters) {
        this.we = we;
        this.verificationParameters = verificationParameters;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Result<? extends VerificationChainOutput> result = checkTrivialCases();
        if (result == null) {
            File directory = FileUtils.createTempDirectory(FileUtils.getTempPrefix(we.getTitle()));
            Chain<VerificationChainOutput> chain = new Chain<>(this::init, monitor);
            chain.andOnSuccess(payload -> exportNet(payload, monitor, directory), 0.1);
            chain.andOnSuccess(payload -> verifyProperty(payload, monitor, directory), 1.0);
            chain.andThen(() -> FileUtils.deleteOnExitRecursively(directory));
            result = chain.process();
        }
        return result;
    }

    public Result<? extends VerificationChainOutput> checkTrivialCases() {
        // The model should be a Petri net (not necessarily an STG)
        if (!WorkspaceUtils.isApplicable(we, PetriModel.class)) {
            return Result.exception("Incorrect model type");
        }
        if (verificationParameters == null) {
            return Result.exception("Verification parameters undefined");
        }
        return null;
    }

    private Result<? extends VerificationChainOutput> init() {
        VerificationParameters verificationParameters = ReachUtils.getToolchainPreparationParameters();
        return Result.success(new VerificationChainOutput().applyVerificationParameters(verificationParameters));
    }

    private Result<? extends VerificationChainOutput> exportNet(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        // The model should be a Petri net (not necessarily an STG)
        PetriModel net = WorkspaceUtils.getAs(we, PetriModel.class);
        File netFile = new File(directory, NET_FILE_NAME);
        Result<? extends ExportOutput> exportResult = StgUtils.exportStg(net, netFile, monitor);
        return new Result<>(exportResult.getOutcome(), payload.applyExportResult(exportResult));
    }

    private Result<? extends VerificationChainOutput> verifyProperty(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        File netFile = new File(directory, NET_FILE_NAME);
        MpsatTask mpsatTask = new MpsatTask(netFile, verificationParameters, directory);
        Result<? extends MpsatOutput>  mpsatResult = Framework.getInstance().getTaskManager().execute(
                mpsatTask, "Running verification [MPSat]", new SubtaskMonitor<>(monitor));

        return new Result<>(mpsatResult.getOutcome(), payload.applyMpsatResult(mpsatResult)
                .applyVerificationParameters(verificationParameters));
    }

}
