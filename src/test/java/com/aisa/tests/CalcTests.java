package com.aisa.tests;

import com.aisa.pw.libs.BasePage;
import com.aisa.pw.libs.ExtentReportManager;
import com.aisa.pw.libs.PlaywrightManager;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CalcTests extends BaseTests{

//    @Test
//    public void testCalculator1() {
//        System.out.println("Thread ID: " + Thread.currentThread().getId() + " - Running testCalculator1");
//
//// Initialize Extent Report
//        ExtentReportManager.createTest("Calculator Test");
//
//        // Initialize Playwright
//        PlaywrightManager.initialize(PlaywrightManager.BrowserEnum.CHROME, false);
//        ExtentReportManager.logInfo("Initialized Playwright and launched Chrome.");
//
//        // Get the base page
//        BasePage basePage = new BasePage();
//        basePage.launchUrl("https://www.calculator.net/");
//        ExtentReportManager.logInfo("Navigated to Calculator.net");
//        basePage.pausePageForDebug();
//
//        // Validate title
//        String title = basePage.getPageTitle();
//        if (title.contains("Calculator.net")) {
//            ExtentReportManager.logPass("Page title is correct: " + title);
//        } else {
//            ExtentReportManager.logFail("Incorrect page title: " + title);
//        }
//        assertTrue(title.contains("Calculator.net"));
//
//        // Close browser
//        PlaywrightManager.close();
//        ExtentReportManager.logInfo("Closed the browser.");
//    }

    @Test
    public void testGoogle() {
        System.out.println("Thread ID: " + Thread.currentThread().getId() + " - Running testGoogle");
//        PlaywrightManager.initialize(PlaywrightManager.BrowserEnum.EDGE, true); // for edge
        PlaywrightManager.initialize(PlaywrightManager.BrowserEnum.SAFARI, true); // for edge
        Page page = PlaywrightManager.getPage();
        page.navigate("https://www.google.com");
        assertTrue(page.title().equalsIgnoreCase("google"));
        PlaywrightManager.close();
    }

//    @Test
//    public void testCalculator2() {
//        System.out.println("Thread ID: " + Thread.currentThread().getId() + " - Running testCalculator2");
//        PlaywrightManager.initialize(PlaywrightManager.BrowserEnum.CHROME, false);
//        Page page = PlaywrightManager.getPage();
//        page.navigate("https://www.calculator.net/");
//        assertTrue(page.title().contains("Calculator.net"));
//        PlaywrightManager.close();
//    }

    @AfterAll
    public static void tearDown() {
        ExtentReportManager.flush();
    }

}
