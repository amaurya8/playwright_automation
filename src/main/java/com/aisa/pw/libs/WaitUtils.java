package com.aisa.pw.libs;

import com.microsoft.playwright.Page;

public class WaitUtils {

    public static void waitForElement(Page page, String selector) {
        page.locator(selector).waitFor();
    }

    public static void waitForText(Page page, String selector, String text) {
        page.waitForCondition(() -> page.locator(selector).textContent().contains(text),
                new Page.WaitForConditionOptions().setTimeout(5000));
    }

    public static void waitForURL(Page page, String expectedURL) {
        page.waitForCondition(() -> page.url().equals(expectedURL),
                new Page.WaitForConditionOptions().setTimeout(5000));
    }

    public static void waitForAttribute(Page page, String selector, String attribute, String expectedValue) {
        page.waitForCondition(() -> page.locator(selector).getAttribute(attribute).equals(expectedValue),
                new Page.WaitForConditionOptions().setTimeout(5000));
    }

    public static void waitForTimeout(Page page, int milliseconds) {
        page.waitForTimeout(milliseconds);
    }
}