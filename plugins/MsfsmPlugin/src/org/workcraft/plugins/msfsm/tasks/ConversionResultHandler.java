package org.workcraft.plugins.msfsm.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.FstDescriptor;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.converters.FstToFsmConverter;
import org.workcraft.plugins.fst.interop.SgImporter;
import org.workcraft.tasks.AbstractExtendedResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class ConversionResultHandler extends AbstractExtendedResultHandler<ConversionOutput, Collection<WorkspaceEntry>> {

    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final WorkspaceEntry we;
    private final boolean convertToFsm;

    public ConversionResultHandler(WorkspaceEntry we, boolean convertToFsm) {
        this.we = we;
        this.convertToFsm = convertToFsm;
    }

    @Override
    public Collection<WorkspaceEntry> handleResult(Result<? extends ConversionOutput> result) {
        Collection<WorkspaceEntry> wes = null;
        ConversionOutput msfsmResult = result.getPayload();
        if (result.getOutcome() == Outcome.SUCCESS) {
            wes = handleSuccess(msfsmResult);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            handleFailure(msfsmResult);
        }
        return wes;
    }

    private Collection<WorkspaceEntry> handleSuccess(ConversionOutput msfsmOutput) {
        Collection<WorkspaceEntry> wes = new ArrayList<>();
        Path path = we.getWorkspacePath();
        Framework framework = Framework.getInstance();
        for (File file : msfsmOutput.getFiles()) {
            try {
                InputStream in = new FileInputStream(file);
                Fst fst = new SgImporter().importSG(in);
                Fsm model = convertToFsm ? convertFstToFsm(fst) : fst;
                ModelDescriptor modelDescriptor = convertToFsm ? new FsmDescriptor() : new FstDescriptor();
                ModelEntry me = new ModelEntry(modelDescriptor, model);
                WorkspaceEntry we = framework.createWork(me, path);
                wes.add(we);
            } catch (FileNotFoundException | DeserialisationException e) {
                throw new RuntimeException("Cannot import file " + file.getAbsolutePath());
            }
        }
        return wes;
    }

    private Fsm convertFstToFsm(Fst srcModel) {
        VisualFst fst = new VisualFst(srcModel);
        VisualFsm fsm = new VisualFsm(new Fsm());
        FstToFsmConverter converter = new FstToFsmConverter(fst, fsm);
        return converter.getDstModel().getMathModel();
    }

    private void handleFailure(ConversionOutput msfsmOutput) {
        String errorMessage = "Error: MSFMS conversion failed.\n"
                + "Please refer to the log in Output window for details";
        if (msfsmOutput != null) {
            errorMessage += ERROR_CAUSE_PREFIX + msfsmOutput.getStderrString();
        }
        DialogUtils.showError(errorMessage);
    }

}
