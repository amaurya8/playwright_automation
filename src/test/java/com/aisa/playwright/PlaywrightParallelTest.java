package com.aisa.playwright;

import com.microsoft.playwright.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlaywrightParallelTest {

    @Test
    public void testCalculator() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate("https://www.calculator.net/");
            page.pause();
            assertTrue(page.title().contains("Example Domain"));
            browser.close();
        }
    }

    @Test
    public void testCalendar() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate("https://fullcalendar.io/");
            page.pause();
            assertTrue(page.title().contains("FullCalendar"));
            browser.close();
        }
    }
}
