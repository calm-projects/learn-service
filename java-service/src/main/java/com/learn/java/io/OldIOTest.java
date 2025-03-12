package com.learn.java.io;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.stream.Collectors;

public class OldIOTest {

    final String FILENAME = "src/main/java/com/learn/java/nio/IOTest.java";

    public static String read(String filename) {
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            return in.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void test_from_file() {
        System.out.print(read(FILENAME));
    }


    @Test
    public void test_from_Memory() throws IOException {
        StringReader sr = new StringReader(read(FILENAME));
        int c;
        while ((c = sr.read()) != -1)
            System.out.print((char) c);
    }

    @Test
    public void test_output_file() {
        String tmpFileName = "IOTest.txt";
        try (
                BufferedReader in = new BufferedReader(new StringReader(read(FILENAME)));
                // PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tmpFileName)))
                // 快捷方式
                PrintWriter out = new PrintWriter(tmpFileName)
        ) {
            in.lines().forEach(out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Show the stored file:
        System.out.println(read(tmpFileName));
    }
}
