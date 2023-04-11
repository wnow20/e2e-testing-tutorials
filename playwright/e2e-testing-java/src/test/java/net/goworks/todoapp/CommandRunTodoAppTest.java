package net.goworks.todoapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import static net.goworks.todoapp.TodoE2ETest.currentPath;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandRunTodoAppTest {

    static ThreadLocal<Thread> threadLocal = new ThreadLocal<>();

    @Test
    void test_start_todo_app_by_command() throws InterruptedException, IOException {
        CountDownLatch latch = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            try {
                String path = currentPath();
                Path todoAppPath = Paths.get(path).resolve("../../todo-app").normalize();
                Process exec = new ProcessBuilder()
                    .directory(todoAppPath.toFile())
                    .command("npm", "start")
                    .start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("destroy todo app");
                    exec.destroy();
                }));
                try (BufferedReader in = new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println(inputLine);
                        if (inputLine.startsWith("Serving on")) {
                            latch.countDown();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
        threadLocal.set(thread);
        latch.await();

        URL url = new URL("http://localhost:4200/");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.connect();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            assertTrue(content.toString().contains("<title>React • TodoMVC</title>"));
        }
        con.disconnect();
    }
}
