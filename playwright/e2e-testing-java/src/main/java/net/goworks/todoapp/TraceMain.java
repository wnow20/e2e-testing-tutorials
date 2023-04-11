package net.goworks.todoapp;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;

public class TraceMain {
    public static void main(String[] args) {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(
            new LaunchOptions()
                .setHeadless(true)
                .setSlowMo(10)
        );
        BrowserContext context = browser.newContext();

        // Start tracing before creating / navigating a page.
        context.tracing().start(new Tracing.StartOptions()
            .setScreenshots(true)
            .setSnapshots(true)
            .setSources(true));

        Page page = context.newPage();
        page.navigate("http://localhost:4200/#/");

        // Stop tracing and export it into a zip archive.
        Path path = Paths.get("trace.zip");
        System.out.println(path.toAbsolutePath());
        context.tracing().stop(new Tracing.StopOptions()
            .setPath(path));

        // mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="show-trace trace.zip"
    }
}
