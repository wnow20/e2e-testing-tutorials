package net.goworks.todoapp;

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

@TestInstance(Lifecycle.PER_CLASS)
public class TodoE2ETest {
    static Playwright playwright;
    static Browser browser;

    // New instance for each test method.
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new LaunchOptions()
                .setHeadless(true)
                .setSlowMo(10)
        );
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


    @Test
    void test_add_todo_item() {
        // 测试新增TODO事项可用
    }

    @Test
    void test_check_todo_item() {
        // 测试完成事项可用
    }

    @Test
    void test_remove_todo_item() {
        // 测试删除TODO事项可用
    }

    @Test
    void test_switch_tabs() {
        // 测试状态选项卡可用
    }

    @Test
    void test_clean_completed() {
        // 清理已完成的TODO事项
    }

    @Test
    void test_work_fine_with_location_hash() {
        // 测试url控制状态选项卡可用
    }
}
