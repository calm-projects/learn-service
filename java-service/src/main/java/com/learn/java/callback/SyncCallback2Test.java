package com.learn.java.callback;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * 同步回调测试
 */
public class SyncCallback2Test {
    interface EventListener {
        String onTrigger();
    }

    static class EventListenerImpl implements EventListener {
        @Override
        public String onTrigger() {
            return "Synchronously running callback function";
        }
    }

    @RequiredArgsConstructor
    static class SynchronousEventConsumer {
        private final EventListener eventListener;

        public String doSynchronousOperation() {
            System.out.println("Performing callback before synchronous Task");
            return eventListener.onTrigger();
        }
    }

    @Test
    public void testSynchronousOperation() {
        EventListener listener = new EventListenerImpl();
        SynchronousEventConsumer synchronousEventConsumer = new SynchronousEventConsumer(listener);
        String result = synchronousEventConsumer.doSynchronousOperation();
        assertEquals("Synchronously running callback function", result);
    }
}
