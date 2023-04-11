package net.goworks.todoapp;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.GetByRoleOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.LocatorOptions;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Assertions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class TodoApp {
    private final Page page;

    public TodoApp(Browser browser, String url) {
        this.page = browser.newPage();
        this.page.navigate(url);
    }

    public TodoApp createTodo(String todoTitle) {
        ElementHandle newTodoInput = page.querySelector(".new-todo");
        newTodoInput.fill(todoTitle);
        newTodoInput.press("Enter");
        Locator locator = page.locator("text=" + todoTitle);
        assertThat(locator).not().isEmpty();
        return this;
    }

    public TodoApp checkTodo(String todoItemName) {
        Locator textLocator = page.locator("text=" + todoItemName);
        Locator todoLocator = page.locator(".todo-list li", new LocatorOptions().setHas(textLocator));
        Locator checkboxLocator = todoLocator.getByRole(AriaRole.CHECKBOX, new GetByRoleOptions().setChecked(false));
        checkboxLocator.check();
        assertThat(todoLocator).hasClass("completed");
        return this;
    }

    public TodoApp removeTodo(String todoItemName) {
        Locator textLocator = page.locator("text=" + todoItemName);
        Locator todoLocator = page.locator(".todo-list li", new LocatorOptions().setHas(textLocator));
        Assertions.assertEquals(todoLocator.count(), 1);

        Locator destroyLocator = todoLocator.locator("button.destroy");
        assertThat(destroyLocator).isHidden();
        todoLocator.hover();
        assertThat(destroyLocator).isVisible();
        destroyLocator.click();
        todoLocator = page.locator(".todo-list li", new LocatorOptions().setHas(textLocator));

        Assertions.assertEquals(todoLocator.count(), 0);
        return this;
    }

    public TodoApp switchCompleted() {
        Locator completeLocator = page.locator(".filters a[href='#/completed']");
        Assertions.assertEquals(completeLocator.count(), 1);
        completeLocator.click();
        return this;
    }

    public Locator locator(String s) {
        return page.locator(s);
    }

    public TodoApp switchActive() {
        Locator completeLocator = page.locator(".filters a[href='#/active']");
        Assertions.assertEquals(completeLocator.count(), 1);
        completeLocator.click();
        return this;
    }

    public Page page() {
        return page;
    }
}
