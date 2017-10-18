package org.workcraft.tasks;

public class Result<T> {

    public enum Outcome {
        SUCCESS,
        CANCEL,
        FAILURE
    }

    private final Outcome outcome;
    private final Throwable cause;
    private final T result;

    public Result(Outcome outcome) {
        this.outcome = outcome;
        this.cause = null;
        this.result = null;
    }

    public Result(Outcome outcome, T result) {
        this.outcome = outcome;
        this.cause = null;
        this.result = result;
    }

    public Result(Throwable exception) {
        this.outcome = Outcome.FAILURE;
        this.cause = exception;
        this.result = null;
    }

    public Result(T result) {
        this.outcome = Outcome.SUCCESS;
        this.cause = null;
        this.result = result;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public Throwable getCause() {
        return cause;
    }

    public T getReturnValue() {
        return result;
    }

    public static <R> Result<R> exception(Throwable e) {
        return new Result<R>(e);
    }

    public static <R> Result<R> cancelation() {
        return new Result<R>(Outcome.CANCEL);
    }

    public static <R> Result<R> success(R res) {
        return new Result<R>(res);
    }

    public static <R> Result<R> failure(R res) {
        return new Result<R>(Outcome.FAILURE, res);
    }

}
