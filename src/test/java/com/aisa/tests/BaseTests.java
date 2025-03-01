package com.aisa.tests;

import com.aisa.tests.ExtentReportManager;
import org.junit.jupiter.api.*;

public class BaseTests {

    @BeforeAll
    public static void setupReport() {
        ExtentReportManager.initReport(); // ✅ Initialize Extent Reports
    }

    @BeforeEach
    public void beforeScenario(TestInfo testInfo) {
        String testName = testInfo.getDisplayName();
        ExtentReportManager.createTest(testName); // ✅ Create test for Extent Report
        ExtentReportManager.logStep("🟢 BeforeEach: Starting test - " + testName);
        System.out.println("🧵 Thread ID: " + Thread.currentThread().getId() + " - Starting " + testName);
    }

    @AfterEach
    public void afterScenario(TestInfo testInfo) {
        String testName = testInfo.getDisplayName();
        ExtentReportManager.logStep("🔴 AfterEach: Finished test - " + testName);
        ExtentReportManager.logTestStatus(true, testName); // ✅ Log test pass status
        System.out.println("🧵 Thread ID: " + Thread.currentThread().getId() + " - Finished " + testName);
    }

    @AfterAll
    public static void tearDown() {
        ExtentReportManager.flushReport(); // ✅ Flush Report
    }

    // ✅ Allow logging inside tests
    public static void logStep(String message) {
        ExtentReportManager.logStep(message);
    }
}