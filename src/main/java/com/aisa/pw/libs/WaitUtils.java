package com.aisa.pw.libs;

import com.microsoft.playwright.Page;

public class WaitUtils {

    public static void waitForElement(Page page, String selector) {
        page.locator(selector).waitFor();
    }
}
