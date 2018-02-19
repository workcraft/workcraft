package org.workcraft.tasks;

public class Result<T> {

    public enum Outcome {
        SUCCESS,
        CANCEL,
        FAILURE
    }

    private final Outcome outcome;
    private final Throwable cause;
    private final T payload;

    public Result(Outcome outcome) {
        this.outcome = outcome;
        this.cause = null;
        this.payload = null;
    }

    public Result(Outcome outcome, T payload) {
        this.outcome = outcome;
        this.cause = null;
        this.payload = payload;
    }

    public Result(Throwable exception) {
        this.outcome = Outcome.FAILURE;
        this.cause = exception;
        this.payload = null;
    }

    public Result(T payload) {
        this.outcome = Outcome.SUCCESS;
        this.cause = null;
        this.payload = payload;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public Throwable getCause() {
        return cause;
    }

    public T getPayload() {
        return payload;
    }

    public static <T> Result<T> exception(Throwable e) {
        return new Result<T>(e);
    }

    public static <T> Result<T> cancelation() {
        return new Result<T>(Outcome.CANCEL);
    }

    public static <T> Result<T> success(T payload) {
        return new Result<T>(Outcome.SUCCESS, payload);
    }

    public static <T> Result<T> failure() {
        return new Result<T>(Outcome.FAILURE);
    }

    public static <T> Result<T> failure(T payload) {
        return new Result<T>(Outcome.FAILURE, payload);
    }

}
