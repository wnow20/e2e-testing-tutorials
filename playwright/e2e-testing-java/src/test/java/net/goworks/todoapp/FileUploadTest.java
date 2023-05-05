package net.goworks.todoapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static net.goworks.todoapp.TodoE2ETest.currentPath;

@TestInstance(Lifecycle.PER_CLASS)
public class FileUploadTest {
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
                .setHeadless(false)
                .setTimeout(5000)
                .setSlowMo(10)
        );

        CountDownLatch latch = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            try {
                String path = currentPath();
                Path todoAppPath = Paths.get(path).resolve("../../file-app").normalize();
                Process exec = new ProcessBuilder()
                    .directory(todoAppPath.toFile())
                    .command("node", "server.js")
                    .start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("destroy file app");
                    exec.destroy();
                }));
                BufferedReader in = new BufferedReader(new
                    InputStreamReader(exec.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    if (inputLine.startsWith("Example app listening on")) {
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

    @Test
    void fileUpload() {
        new FileUploadPage(browser, "http://localhost:3000/")
            .uploadFile("bagel.jpg")
            .openFile("bagel.jpg");
    }
}
