package org.workcraft.plugins.punf.tasks;

import org.workcraft.tasks.AbstractResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.IOException;

public class Ltl2tgbaResultHandler extends AbstractResultHandler<Ltl2tgbaOutput> {

    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    @Override
    public void handleResult(final Result<? extends Ltl2tgbaOutput> result) {
        if (result.getOutcome() == Outcome.SUCCESS) {
            handleSuccess(result.getPayload());
        } else if (result.getOutcome() == Outcome.FAILURE) {
            handleFailure(result.getPayload());
        }
    }

    private void handleSuccess(final Ltl2tgbaOutput output) {
        LogUtils.logInfo("Conversion result in HOA format:");
        File resultFile = (output == null) ? null : output.getOutputFile();
        try {
            String resultText = FileUtils.readAllText(resultFile);
            LogUtils.logMessage(resultText);
        } catch (IOException e) {
        }
    }

    private void handleFailure(Ltl2tgbaOutput output) {
        String errorMessage = "Error: conversion failed.";
        if (output != null) {
            errorMessage += ERROR_CAUSE_PREFIX + output.getStderrString();
        }
        DialogUtils.showError(errorMessage);
    }

}
