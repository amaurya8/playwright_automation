package com.aisa.playwright;

import com.microsoft.playwright.*;

import java.nio.file.Paths;

public class PlaywrightTestsGeneric {
    public static void main(String[] args) {
        // Initialize Playwright
        try (Playwright playwright = Playwright.create()) {
            // Launch Chromium browser
            Browser browserChromium = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

            // Create a new browser context and page
            BrowserContext context = browserChromium.newContext();
            Page page = context.newPage();

            page.navigate("https://calculator.net");


/*            To debug tracing - networks calls, css/svg/screenshots details

        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));

            // Navigate to a webpage
            page.navigate("https://calculator.net");

            context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("trace.zip")));
            */

            // Print the page title
            System.out.println("Page Title: " + page.title());
            Locator element = page.locator("text=Financial Calculators");

            element.click(); // Click an element
            page.fill("//input[@id='calcSearchTerm']","search calculator");
            page.click("//span[@id='bluebtn']");
            page.route("**/api/data", route -> route.fulfill(
                    new Route.FulfillOptions().setBody("{\"message\": \"mocked response\"}")
            ));

/*            // To pause the execution, in order to debug something on the page
            page.pause();*/

            // Take a screenshot
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(".screenshot.png")));

            // Close the browser
            browserChromium.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
