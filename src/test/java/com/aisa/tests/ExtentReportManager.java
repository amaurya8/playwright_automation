package com.aisa.tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReportManager {
    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // Initialize Extent Report (Singleton)
    public static synchronized void initReport() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");
            extent = new ExtentReports();
            extent.attachReporter(spark);
        }
    }

    // Create test instance per thread
    public static synchronized void createTest(String testName) {
        ExtentTest test = extent.createTest(testName);
        extentTest.set(test);
    }

    // Get current test instance
    public static synchronized ExtentTest getTest() {
        return extentTest.get();
    }

    // Log steps
    public static synchronized void logStep(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.info(message);
        } else {
            System.err.println("❌ ExtentTest is NULL! Logging Failed: " + message);
        }
    }

    // Log pass/fail status
    public static synchronized void logTestStatus(boolean isPassed, String testName) {
        ExtentTest test = getTest();
        if (test != null) {
            if (isPassed) {
                test.pass(testName + " passed.");
            } else {
                test.fail(testName + " failed.");
            }
        }
    }

    // Flush reports
    public static synchronized void flushReport() {
        if (extent != null) {
            extent.flush();
        }
    }
}