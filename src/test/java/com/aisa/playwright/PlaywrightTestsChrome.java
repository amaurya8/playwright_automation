package com.aisa.playwright;

import com.microsoft.playwright.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PlaywrightTestsChrome {
    public static void main(String[] args) {
        // Initialize Playwright
        try (Playwright playwright = Playwright.create()) {

          String chromePath =  "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";

          // Launch browser using the installed Chrome
            Browser browserChrome = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setChannel("chrome") // Optional: Use Chrome instead of bundled Chromium
                    .setExecutablePath(Paths.get(chromePath))
                    .setHeadless(false)); // Set headless to false to observe actions

            // Create a new browser context and page
            BrowserContext context = browserChrome.newContext();
            Page page = context.newPage();

            page.navigate("https://calculator.net");

            page.pause();
            // Print the page title
            System.out.println("Page Title: " + page.title());
            Locator element = page.locator("text=Financial Calculators");

            element.click(); // Click an element
            page.fill("//input[@id='calcSearchTerm']","search calculator");
            page.click("//span[@id='bluebtn']");
            page.route("**/api/data", route -> route.fulfill(
                    new Route.FulfillOptions().setBody("{\"message\": \"mocked response\"}")
            ));

            // Take a screenshot
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(".screenshot.png")));

            // Close the browser
            browserChrome.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
