package com.learn.java.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

/**
 * 异步回调测试
 */
@Slf4j
public class AsyncCallbackTest {
    interface EventListener {
        String onTrigger();
    }

    static class AsynchronousEventListenerImpl implements EventListener {
        @Override
        public String onTrigger() {
            return "Asynchronously running callback function";
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    static class AsynchronousEventConsumer {
        private final EventListener eventListener;
        private final ExecutorService executorService = Executors.newSingleThreadExecutor();

        // 不推荐
        public String asyncTask01() {
            System.out.println("Performing callback before asyncTask01 Task");
            new Thread(eventListener::onTrigger).start();
            return "Performing asyncTask01";
        }

        // 不推荐
        public String asyncTask02() throws ExecutionException, InterruptedException {
            System.out.println("Performing callback before asyncTask02 Task");
            Future<?> future = executorService.submit(eventListener::onTrigger);
            // get会阻塞当前线程，相当于变为了同步，这里仅仅是观察下结果
            log.info("asyncTask02执行结果:{}", future.get());
            return "Performing asyncTask02";
        }

        // 推荐
        public String asyncTask03() {
            System.out.println("Performing callback before asyncTask03 Task");
            CompletableFuture.supplyAsync(() -> "Task completed")
                    .thenAccept(result -> {
                        log.info(result);
                        String res = eventListener.onTrigger();
                        log.info("asyncTask03执行结果:{}", res);
                    });
            return "Performing asyncTask03";
        }
    }

    @Test
    public void testSynchronousOperation() throws Exception {
        EventListener listener = new AsynchronousEventListenerImpl();
        AsynchronousEventConsumer asynchronousEventConsumer = new AsynchronousEventConsumer(listener);
        String result1 = asynchronousEventConsumer.asyncTask01();
        String result2 = asynchronousEventConsumer.asyncTask02();
        String result3 = asynchronousEventConsumer.asyncTask03();
        log.info("res1:{}; res2:{}, res3:{}", result1, result2, result3);
        TimeUnit.SECONDS.sleep(2);
    }
}
