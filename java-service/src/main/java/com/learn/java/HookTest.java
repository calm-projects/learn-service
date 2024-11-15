package com.learn.java;

import lombok.extern.slf4j.Slf4j;

/**
 * 钩子测试
 */
@Slf4j
public class HookTest {
    public static void main(String[] args) {
        // Register a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM is shutting down...");
            // Perform cleanup tasks, like saving data or closing resources
        }));

        // Simulate a long-running application
        System.out.println("Application is running. Press Ctrl+C to exit.");
        try {
            Thread.sleep(5000); // Simulating work
        } catch (InterruptedException e) {
            log.error("error:", e);
        }
    }
}
