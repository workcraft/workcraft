package org.workcraft.tasks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

class DefaultTaskManagerTests {

    private static final Task<Object> FAILING_TASK = monitor -> {
        throw new IllegalStateException("Task error");
    };

    @Test
    void executeThrowingTaskTest() {
        DefaultTaskManager taskManager = new DefaultTaskManager();
        BasicProgressMonitor<Object> monitor = new BasicProgressMonitor<>();
        Result<?> result = taskManager.execute(FAILING_TASK, "Failing task", monitor);
        Assertions.assertTrue(result.isFailure());
        Assertions.assertInstanceOf(IllegalStateException.class, result.getCause());
        Assertions.assertSame(result, monitor.waitResult());
    }

    @Test
    void queueThrowingTaskTest() {
        DefaultTaskManager taskManager = new DefaultTaskManager();
        BasicProgressMonitor<Object> monitor = new BasicProgressMonitor<>();
        taskManager.queue(FAILING_TASK, "Failing task", monitor);
        Result<?> result = Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), monitor::waitResult);
        Assertions.assertTrue(result.isFailure());
    }

}
