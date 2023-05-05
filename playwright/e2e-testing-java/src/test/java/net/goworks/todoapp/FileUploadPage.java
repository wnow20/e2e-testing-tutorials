package net.goworks.todoapp;

import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.options.AriaRole;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileUploadPage {
    private final Page page;
    private final String homePage;

    public FileUploadPage(Browser browser, String url) {
        this.page = browser.newPage();
        this.page.navigate(url);
        this.homePage = url;
    }

    public FileUploadPage uploadFile(String filepath) {
        FileChooser fileChooser = page.waitForFileChooser(() -> {
            page.locator("#file").click();
        });
        fileChooser.setFiles(Paths.get(filepath));

        page.getByRole(AriaRole.BUTTON, new GetByRoleOptions().setName("提交")).click();
        System.out.println(page.content());

        assertTrue(page.content().contains("\"uploads/bagel.jpg\""));

        return this;
    }

    public FileUploadPage openFile(String s) {
        String url = homePage + "files/" + s;
        page.navigate(url);

        assertTrue(page.content().contains(url));
        return this;
    }
}
