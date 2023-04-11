package net.goworks.todoapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class TodoE2ETest {
    static Playwright playwright;
    static Browser browser;

    // New instance for each test method.
    BrowserContext context;
    Page page;

    static ThreadLocal<Thread> threadLocal = new ThreadLocal<>();

    @BeforeAll
    static void launchBrowser() throws InterruptedException {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new LaunchOptions()
                .setHeadless(true)
                .setSlowMo(10)
        );

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
                BufferedReader in = new BufferedReader(new
                    InputStreamReader(exec.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    if (inputLine.startsWith("Serving on")) {
                        latch.countDown();
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
        threadLocal.set(thread);
        latch.await();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @AfterEach
    void tearDown() {
        Thread thread = threadLocal.get();
        if (thread != null) {
            thread.interrupt();
        }
    }

    public static String currentPath() throws IOException {
        Process exec = Runtime.getRuntime().exec("/bin/bash -c pwd");
        InputStream inputStream = exec.getInputStream();
        return readString(inputStream);
    }

    public static String readString(InputStream inputStream) throws IOException {
        StringBuilder path = new StringBuilder();
        byte[] buffer = new byte[2048];
        int read = inputStream.read(buffer);
        while (read != -1) {
            path.append(new String(buffer, 0, read, Charset.defaultCharset()));
            read = inputStream.read(buffer);
        }
        return path.toString();
    }

    @Test
    void test_add_todo_item() {
        String todoItemName = "new test todo for e2e";
        new TodoApp(browser, "http://localhost:4200/")
            .createTodo(todoItemName);
    }

    @Test
    void test_check_todo_item() {
        String todoItemName = "new test todo for e2e";
        new TodoApp(browser, "http://localhost:4200/")
            .createTodo(todoItemName)
            .checkTodo(todoItemName);
    }

    @Test
    void test_remove_todo_item() {
        String todoItemName = "new test todo for e2e";
        new TodoApp(browser, "http://localhost:4200/")
            .createTodo(todoItemName)
            .removeTodo(todoItemName);
    }

    @Test
    void test_switch_tabs() {
        String todoItemName = "switchTab";
        TodoApp todoApp = new TodoApp(browser, "http://localhost:4200/")
            .createTodo(todoItemName + 1)
            .checkTodo(todoItemName + 1)
            .createTodo(todoItemName + 2)
            .removeTodo(todoItemName + 2)
            .createTodo(todoItemName + 3);

        Locator todosLocator = todoApp.switchCompleted()
            .locator(".todo-list li");
        Assertions.assertEquals(todosLocator.count(), 1);
        Assertions.assertEquals(todosLocator
            .locator("text=" + todoItemName + 1).count(), 1);

        todosLocator = todoApp.switchActive()
            .locator(".todo-list li");
        Assertions.assertEquals(todosLocator.count(), 1);
        Assertions.assertEquals(todosLocator
            .locator("text=" + todoItemName + 3).count(), 1);
    }

    @Test
    void test_clean_completed() {
        String todoItemName = "switchTab";
        TodoApp todoApp = new TodoApp(browser, "http://localhost:4200/")
            .createTodo(todoItemName + 1)
            .checkTodo(todoItemName + 1)
            .createTodo(todoItemName + 2);

        todoApp.locator("button.clear-completed").click();
        Assertions.assertEquals(todoApp.locator("text=" + todoItemName + 1).count(), 0);
        Assertions.assertEquals(todoApp.locator("text=" + todoItemName + 2).count(), 1);
    }

    @Test
    void test_work_fine_with_location_hash() {
        String todoItemName = "switchTab";
        TodoApp todoApp = new TodoApp(browser, "http://localhost:4200/")
            .createTodo(todoItemName + 1)
            .checkTodo(todoItemName + 1)
            .createTodo(todoItemName + 2);

        todoApp.page().navigate("http://localhost:4200/#/active");

        assertThat(todoApp.locator(".filters a[href=\"#/active\"]")).hasClass("selected");
        Assertions.assertEquals(todoApp.locator("text=" + todoItemName + 1).count(), 0);
        Assertions.assertEquals(todoApp.locator("text=" + todoItemName + 2).count(), 1);
    }
}
