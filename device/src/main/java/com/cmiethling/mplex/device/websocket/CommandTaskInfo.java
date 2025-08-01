package com.cmiethling.mplex.device.websocket;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.cmiethling.mplex.device.api.DeviceCommand;
import com.cmiethling.mplex.device.message.ResultMessage;

import lombok.Getter;

public class CommandTaskInfo<T extends DeviceCommand> {
    private final CountDownLatch lock = new CountDownLatch(1);
    private final Callable<T> callable;
    private final ExecutorService executor;
    @Getter
    private Future<T> task;
    @Getter
    private ResultMessage resultMessage;

    public CommandTaskInfo(final Callable<T> callable, final ExecutorService executor) {
        this.callable = callable;
        this.executor = executor;
    }

    /**
     * Starts the {@link #task Future task}.
     */
    public void start() {
        this.task = this.executor.submit(this.callable);
    }

    public void cancel() {
        if (this.task != null)
            this.task.cancel(true);
    }

    public boolean waitForResult(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.lock.await(timeout, unit);
    }

    void setResultMessage(final ResultMessage resultMessage) {
        this.resultMessage = resultMessage;
        this.lock.countDown();
    }
}
