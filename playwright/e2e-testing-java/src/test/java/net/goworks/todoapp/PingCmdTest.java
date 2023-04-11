package net.goworks.todoapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

public class PingCmdTest {
    // TODO blog test command
    @Test
    void test_ping_command() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(10);
        Thread thread = new Thread(() -> {
            try {
                Process exec = Runtime.getRuntime().exec("ping 127.0.0.1");
                Runtime.getRuntime().addShutdownHook(new Thread(exec::destroy));

                BufferedReader in = new BufferedReader(new
                    InputStreamReader(exec.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    latch.countDown();
                    System.out.println(inputLine);
                }
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        thread.start();
        latch.await();
        thread.interrupt();
    }
}
