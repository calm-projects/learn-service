package com.learn.java.callback;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 同步回调测试
 */
@Slf4j
public class SyncCallback1Test {
    interface Callback {
        void call();
    }

    static abstract class Task {
        final void executeWith(Callback callback) {
            execute();
            Optional.ofNullable(callback).ifPresent(Callback::call);
        }

        public abstract void execute();
    }

    @Slf4j
    static final class SimpleTask extends Task {
        @Override
        public void execute() {
            log.info("Simple task executed");
        }
    }

    public static void main(String[] args) {
        SimpleTask simpleTask = new SimpleTask();
        simpleTask.executeWith(() -> log.info("I'm done now."));
    }
}