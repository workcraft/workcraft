package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CombinedChainTask implements Task<CombinedChainOutput> {

    private static final String STG_FILE_EXTENSION = StgFormat.getInstance().getExtension();
    private static final String NET_FILE_NAME = StgUtils.SPEC_FILE_PREFIX + STG_FILE_EXTENSION;

    private final WorkspaceEntry we;
    private final List<VerificationParameters> verificationParametersList;
    private final Task<VerificationChainOutput> extraTask;

    public CombinedChainTask(WorkspaceEntry we, List<VerificationParameters> verificationParametersList) {
        this(we, verificationParametersList, null);
    }

    public CombinedChainTask(WorkspaceEntry we, List<VerificationParameters> verificationParametersList,
            Task<VerificationChainOutput> extraTask) {

        this.we = we;
        this.verificationParametersList = verificationParametersList;
        this.extraTask = extraTask;
    }

    @Override
    public Result<? extends CombinedChainOutput> run(ProgressMonitor<? super CombinedChainOutput> monitor) {
        Result<? extends CombinedChainOutput> result = checkTrivialCases();
        if (result == null) {
            File directory = FileUtils.createTempDirectory(FileUtils.getTempPrefix(we.getTitle()));
            Chain<CombinedChainOutput> chain = new Chain<>(this::init, monitor);
            chain.andOnSuccess(payload -> exportNet(payload, monitor, directory), 0.1);
            chain.andOnSuccess(payload -> unfoldNet(payload, monitor, directory), 0.5);
            chain.andOnSuccess(payload -> verifyPropertyList(payload, monitor, directory), 9.0);
            chain.andOnSuccess(payload -> runExtraTask(payload, monitor), 1.0);
            chain.andThen(() -> FileUtils.deleteOnExitRecursively(directory));
            result = chain.process();
        }
        return result;
    }

    public Result<? extends CombinedChainOutput> checkTrivialCases() {
        // The model should be a Petri net (not necessarily an STG)
        if (!WorkspaceUtils.isApplicable(we, PetriModel.class)) {
            return Result.exception("Incorrect model type.");
        }
        if (verificationParametersList == null) {
            return Result.exception("Verification parameters undefined.");
        }
        return null;
    }

    private Result<? extends CombinedChainOutput> init() {
        return Result.success(new CombinedChainOutput().applyVerificationParametersList(verificationParametersList));
    }

    private Result<? extends CombinedChainOutput> exportNet(CombinedChainOutput payload,
            ProgressMonitor<? super CombinedChainOutput> monitor, File directory) {

        // The model should be a Petri net (not necessarily an STG)
        PetriModel net = WorkspaceUtils.getAs(we, PetriModel.class);
        File netFile = new File(directory, NET_FILE_NAME);
        Result<? extends ExportOutput> exportResult = StgUtils.exportStg(net, netFile, monitor);
        return new Result<>(exportResult.getOutcome(), payload.applyExportResult(exportResult));
    }

    private Result<? extends CombinedChainOutput> unfoldNet(CombinedChainOutput payload,
            ProgressMonitor<? super CombinedChainOutput> monitor, File directory) {

        File netFile = new File(directory, NET_FILE_NAME);
        MpsatUnfoldingTask mpsatUnfoldingTask = new MpsatUnfoldingTask(netFile, directory);
        Result<? extends MpsatOutput> mpsatUnfoldingResult = Framework.getInstance().getTaskManager().execute(
                mpsatUnfoldingTask, "Unfolding .g", new SubtaskMonitor<>(monitor));

        return new Result<>(mpsatUnfoldingResult.getOutcome(), payload.applyMpsatResult(mpsatUnfoldingResult));
    }

    private Result<? extends CombinedChainOutput> verifyPropertyList(CombinedChainOutput payload,
            ProgressMonitor<? super CombinedChainOutput> monitor, File directory) {

        File unfoldingFile = payload.getMpsatResult().getPayload().getUnfoldingFile();
        File netFile = new File(directory, NET_FILE_NAME);
        ArrayList<Result<? extends MpsatOutput>> mpsatResultList = new ArrayList<>(verificationParametersList.size());
        for (VerificationParameters verificationParameters : verificationParametersList) {
            MpsatTask mpsatTask = new MpsatTask(unfoldingFile, netFile, verificationParameters, directory);
            Result<? extends MpsatOutput>  mpsatResult = Framework.getInstance().getTaskManager().execute(
                    mpsatTask, "Running verification [MPSat]", new SubtaskMonitor<>(monitor));

            mpsatResultList.add(mpsatResult);

            // Return results at the first failure or property violation
            boolean inversePredicate = verificationParameters.isInversePredicate();
            if (!mpsatResult.isSuccess() || (mpsatResult.getPayload().hasSolutions() == inversePredicate)) {
                return new Result<>(mpsatResult.getOutcome(), payload.applyMpsatResultList(mpsatResultList));
            }
        }
        return Result.success(payload.applyMpsatResultList(mpsatResultList));
    }

    private Result<? extends CombinedChainOutput> runExtraTask(CombinedChainOutput payload,
            ProgressMonitor<? super CombinedChainOutput> monitor) {

        // Only proceed with the extra task if the main tasks have no solutions
        if ((extraTask != null) && (CombinedChainResultHandlingMonitor.getViolationMpsatOutput(payload) == null)) {
            Result<? extends VerificationChainOutput> taskResult = Framework.getInstance().getTaskManager().execute(
                    extraTask, MpsatUtils.getToolchainDescription(we.getTitle()), new SubtaskMonitor<Object>(monitor));

            VerificationChainOutput extraPayload = taskResult.getPayload();
            payload.getMpsatResultList().add(extraPayload.getMpsatResult());
            payload.getVerificationParametersList().add(extraPayload.getVerificationParameters());
            return new Result<>(taskResult.getOutcome(), payload);
        }
        return Result.success(payload);
    }

}
