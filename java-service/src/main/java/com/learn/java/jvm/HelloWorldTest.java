package com.learn.java.jvm;

import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Data
public class HelloWorldTest {
    private static final String name1 = "JACK";
    private static final String name2 = "JACK";
    private final int age = 10;
    private float weight;

    @Test
    public void testAdd() {
        int i = 0;
        i = i++;
        Assertions.assertEquals(i, 1);
    }
}
